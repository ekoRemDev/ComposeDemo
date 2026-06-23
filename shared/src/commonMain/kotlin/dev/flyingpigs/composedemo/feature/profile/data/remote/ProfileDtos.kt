package dev.flyingpigs.composedemo.feature.profile.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto (val firstName:String, val email:String){
}
