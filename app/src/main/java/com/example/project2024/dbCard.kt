package com.example.project2024

data class Card(
    val id: Int = 0,
    val title: String = "",
    val txt: String = "",
    val img: String = "",
    var isFav: Boolean = false,
    var isPurch: Boolean = false,
    var quantityPurch: Int = 0,
    val price: Int =0
)

