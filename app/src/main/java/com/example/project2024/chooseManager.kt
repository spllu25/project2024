package com.example.project2024
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object chooseManager {

    private val chosenCards = mutableListOf<Card>()
    private var currentUserId: String? = null // Изменено на String, если id хранится в Firestore как строка
    private lateinit var userRepository: UserRepository // Репозиторий для работы с Firestore

    fun initialize(context: Context, userId: String, repository: UserRepository) {
        userRepository = repository
        currentUserId = userId
        CoroutineScope(Dispatchers.IO).launch {
            loadCards(userId)
        }
    }

    suspend fun loadCards(userId: String) {
        withContext(Dispatchers.IO) {
           // val cards = userRepository.getCardsByUserId(userId) // Предполагается, что у вас есть метод в UserRepository для получения карточек
            chosenCards.clear()
            //chosenCards.addAll(cards)
        }
    }

    suspend fun saveCards() {
        currentUserId?.let { userId ->
            withContext(Dispatchers.IO) {
               // userRepository.deleteCardsByUserId(userId) // Очищаем старые записи
                for (card in chosenCards) {
              //      userRepository.insertCard(card.copy(userId = userId)) // Сохраняем обновлённые карты
                }
            }
        }
    }

    fun getChosenCards(): List<Card> {
        return chosenCards
    }
}

