# "Trust anchor for certification path not found" (corporate TLS proxy)

> **Hit on: 2026-06-22** — login API call failed on desktop/JVM with
> `java.security.cert.CertPathValidatorException: Trust anchor for certification
> path not found`.

## What it means

The JVM couldn't verify the server's TLS certificate. The corporate network
(`crocobet`) runs an **SSL-inspection proxy**: it intercepts HTTPS and re-signs
every site's certificate with an internal root CA (`CN=cpssl.crocobet.local`).

```
issuer  = CN=cpssl.crocobet.local     ← corporate proxy, NOT a public CA
subject = CN=dummyjson.com
```

macOS already trusts that CA (IT installed it in the keychain), so `curl`, the
browser, and Xcode work fine. **But the JVM has its own separate trust store**
(`<jdk>/lib/security/cacerts`) that doesn't know the corporate CA → it rejects
the handshake. This is an environment issue, not an app bug.

> Flutter note: you'd see the same thing — Dart/Android also keep their own
> trust stores separate from the OS on some platforms.

## How we diagnosed it

```bash
# Who signed the cert presented to YOU? (public CA = fine; *.local = MITM proxy)
echo | openssl s_client -connect dummyjson.com:443 -servername dummyjson.com 2>/dev/null \
  | openssl x509 -noout -issuer -subject

# OS trusts it (curl works) but the JVM doesn't → it's a JVM trust-store gap
curl -sS -o /dev/null -w "HTTP %{http_code}\n" https://dummyjson.com/test
```

## The fix (desktop / JVM) — import the corporate CA into the JDK trust store

1. **Export the corporate CA** from the macOS keychain to a PEM:
   ```bash
   security find-certificate -a -c "cpssl.crocobet.local" -p \
     /Library/Keychains/System.keychain > /tmp/crocobet-ca.pem
   ```
2. **Import it into the cacerts of the JDK that runs the app.** This project has
   no `jvmToolchain` set, so the desktop `:run` task uses the **Gradle daemon JVM
   = Amazon Corretto 21** (provisioned under `~/.gradle/jdks/...`). We imported
   into that one *and* the likely IDE JDK (OpenJDK 25):
   ```bash
   keytool -importcert -noprompt -trustcacerts -alias crocobet-cpssl \
     -file /tmp/crocobet-ca.pem -storepass changeit \
     -keystore "<JDK_HOME>/lib/security/cacerts"
   ```
   (Default cacerts password is `changeit`. These JDKs live under the user home,
   so no `sudo` needed.)
3. **Verify** the handshake from that exact JVM:
   ```bash
   echo 'var c=(java.net.HttpURLConnection)java.net.URI.create("https://dummyjson.com/test").toURL().openConnection(); c.connect(); System.out.println("HTTP "+c.getResponseCode());' \
     | "<JDK_HOME>/bin/jshell" -q -
   # → HTTP 200
   ```
4. Restart the app so it reloads the trust store.

**Undo:** `keytool -delete -alias crocobet-cpssl -keystore <cacerts> -storepass changeit`

## Gotchas / when it comes back

- **Per-JDK, per-machine.** This edits a specific JDK's `cacerts` — it is NOT in
  the repo and NOT shared with teammates. Switch JDKs, reinstall, or let Gradle
  re-provision the toolchain → re-import.
- **Which JVM actually runs the app?** THIS is the trap — there are many JVMs on
  the machine, each with its OWN `cacerts`, and the fix only helps the one that
  actually runs your code:
  - `./gradlew :desktopApp:run` → the **Gradle daemon JVM** (here: Corretto 21).
  - **IntelliJ / Android Studio Run button** (direct app run config) → the IDE's
    bundled **JetBrains Runtime** at `/Applications/<IDE>.app/Contents/jbr/...`.
  - The **packaged/installed app** (`/Applications/<app>.app`) → its **bundled
    runtime** at `Contents/runtime/Contents/Home/...` — and this one is
    regenerated every time you repackage, so the import must be redone.
  Find every trust store and check each:
  ```bash
  find /Applications ~/Library ~/.gradle /Library/Java -name cacerts -path "*/security/*" 2>/dev/null \
    | while read -r ks; do keytool -list -keystore "$ks" -storepass changeit -alias crocobet-cpssl >/dev/null 2>&1 \
        && echo "HAS  $ks" || echo "MISS $ks"; done
  ```
- **Durable alternative for desktop** (survives repackaging): add a JVM arg in
  `desktopApp/build.gradle.kts` under `compose.desktop.application` so the app
  trusts the macOS keychain instead of its bundled cacerts —
  `jvmArgs += listOf("-Djavax.net.ssl.trustStoreType=KeychainStore")`. Applies to
  both `:run` and the packaged app; macOS-only.
- **iOS:** unaffected — it uses the macOS/iOS keychain, which already trusts the
  CA.
- **Android:** different model. Apps don't trust user/corporate CAs by default on
  API 24+. If the call fails there, add a `network_security_config.xml` that
  trusts user certs (debug builds only) — do NOT ship that to production.
- **Never** "fix" this by disabling certificate validation in the Ktor client.
  That turns off security for real users, not just your corporate proxy.

## Alternative (in-repo, macOS only)

Instead of patching cacerts, point the run JVM at the macOS keychain:
`-Djavax.net.ssl.trustStoreType=KeychainStore` as a JVM arg on the desktop run
task. Cleaner (survives JDK changes) but only affects `:desktopApp:run`, not IDE
runs, and is macOS-specific.
