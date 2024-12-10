package com.example.project2024

data class Order(
    val clientName: String? = null,
    val address: String? = null,
    val date: String? = null,
    val cost: String? = null,
    val cards: List<Card> = emptyList()
)

