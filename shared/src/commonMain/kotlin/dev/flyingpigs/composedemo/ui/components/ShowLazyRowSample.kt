package dev.flyingpigs.composedemo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ShowLazyRowSample() {
    val list = (1..50).map { it.toString() }
    Column(Modifier.padding(20.dp)) {
        LazyRow {
            items(list) { item ->
                SampleLazyRowItem(item)
            }
        }
    }
}

@Composable
fun SampleLazyRowItem(item: String) {
    Card(
        elevation = CardDefaults.cardElevation(), modifier = Modifier
            .padding(5.dp)
            .wrapContentWidth()
            .wrapContentHeight()
    ) {
        Text("List Item #$item", modifier = Modifier.padding(10.dp), maxLines = 1)
    }
}