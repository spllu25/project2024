package com.example.project2024

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

data class Order(
    val id: String = "",
    val userId: String,
    val orderDetails: String,
    val address: String,
    val date : String,
    val cost: String
)

/*class OrderRepository {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val ordersCollection = firestore.collection("orders")

    suspend fun insertOrder(order: Order) {
        ordersCollection.document(order.id).set(order).await()
    }

    suspend fun getOrdersByUserId(userId: String): List<Order> {
        return try {
            val querySnapshot = ordersCollection.whereEqualTo("userId", userId).get().await()
            querySnapshot.toObjects(Order::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteOrder(order: Order) {
        ordersCollection.document(order.id).delete().await()
    }
}*/
