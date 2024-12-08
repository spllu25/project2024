package com.example.project2024
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class Card(
    val id: Int = 0,
    val userId: String = "",
    val title: String = "",
    val txt: String = "",
    val img: String = "",
    var isFav: Boolean = false,
    var isPurch: Boolean = false,
    var quantityPurch: Int = 0
) {}


/*class CardRepository(private val db: FirebaseFirestore) {

    // Сохранение карточки
    suspend fun saveCard(card: Card) = withContext(Dispatchers.IO) {
        db.collection("cards").document(card.id.toString()).set(card).await()
    }

    // Сохранение заказа
    suspend fun saveOrder(
        clientName: String,
        address: String,
        date: String,
        totalCost: String,
        cards: List<Card>
    ) = withContext(Dispatchers.IO) {
        cards.forEach { card ->
            val orderCard = card.copy(
                id = generateCardId(card.userId.toInt(), card.id),
                isFav = false,
                isPurch = false,
                date = date
            )
            db.collection("cards").document(orderCard.id.toString()).set(orderCard).await()
        }
    }

    // Генерация ID карточки
    private fun generateCardId(userId: Int, localId: Int): Int {
        return userId+localId
    }

    // Загрузка карточек
    suspend fun loadCards(userId: String): List<Card> = withContext(Dispatchers.IO) {
        val querySnapshot = db.collection("cards").whereEqualTo("userId", userId).get().await()
        return@withContext querySnapshot.documents.map { doc ->
            doc.toObject(Card::class.java)?.copy(id = doc.id.toInt()) ?: throw Exception("Error parsing card")
        }
    }

    // Очистка карточек пользователя
    suspend fun clearCards(userId: String) = withContext(Dispatchers.IO) {
        val querySnapshot = db.collection("cards").whereEqualTo("userId", userId).get().await()
        querySnapshot.documents.forEach { doc ->
            db.collection("cards").document(doc.id).delete().await()
        }
    }
    suspend fun updateIsFav(cardId: String, isFav: Boolean) = withContext(Dispatchers.IO) {
        val values = mapOf("isFav" to isFav)
        db.collection("cards").document(cardId).update(values).await()
    }

    // Обновление поля isPurch
    suspend fun updateIsPurch(cardId: String, isPurch: Boolean) = withContext(Dispatchers.IO) {
        val values = mapOf("isPurch" to isPurch)
        db.collection("cards").document(cardId).update(values).await()
    }

    // Обновление количества
    suspend fun updateQuantity(cardId: String, quantity: Int) = withContext(Dispatchers.IO) {
        val values = mapOf("quantityPurch" to quantity)
        db.collection("cards").document(cardId).update(values).await()
    }

    // Удаление карточки
    suspend fun deleteCard(cardId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            db.collection("cards").document(cardId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}*/
