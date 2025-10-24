package com.example.chatter.data.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val senderImage: String? = null,
    val imageUrl: String? = null
){
   constructor(): this("", "", "")
}
