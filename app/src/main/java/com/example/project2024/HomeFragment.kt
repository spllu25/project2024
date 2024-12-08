package com.example.project2024
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private lateinit var cardsList: RecyclerView
    private lateinit var adapter: cardAdapter
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        cardsList = view.findViewById(R.id.cards)

        adapter = cardAdapter(mutableListOf(), requireContext(), false)
        cardsList.layoutManager = LinearLayoutManager(requireContext())
        cardsList.adapter = adapter

        loadCards()
        return view
    }

    private fun loadCards() {
        viewLifecycleOwner.lifecycleScope.launch {
            currentUserId?.let { userId ->
                val userCards = withContext(Dispatchers.IO) {
                    getCardsFromFirestore(userId)
                }.toMutableList()

                if (userCards.isEmpty()) {
                    val staticCards = listOf(
                        Card(1, userId, getString(R.string.title1), getString(R.string.txt1), "image1", false, false, 0),
                        Card(2, userId, getString(R.string.title2), getString(R.string.txt1), "image1", false, false, 0),
                        Card(3, userId, getString(R.string.title1), getString(R.string.txt2), "image1", false, false, 0),
                        Card(4, userId, getString(R.string.title2), getString(R.string.txt2), "image1", false, false, 0),
                        Card(5, userId, getString(R.string.title1), getString(R.string.txt1), "image1", false, false, 0)
                    )
                    saveStaticCardsToFirestore(staticCards, userId)
                    adapter.updateCards(staticCards.toMutableList())
                } else {
                    adapter.updateCards(userCards)
                }
            }
        }
    }

    private suspend fun getCardsFromFirestore(userId: String): List<Card> {
        return withContext(Dispatchers.IO) {
            val cards = mutableListOf<Card>()
            val snapshot = db.collection("users").document(userId).collection("cards").get().await()

            for (document in snapshot.documents) {
                val card = document.toObject(Card::class.java)
                if (card != null) {
                    cards.add(card)
                }
            }
            cards
        }
    }

    private suspend fun saveStaticCardsToFirestore(staticCards: List<Card>, userId: String) {
        withContext(Dispatchers.IO) {
            for (card in staticCards) {
                db.collection("users").document(userId).collection("cards")
                    .document(card.id.toString()).set(card).await()
            }
        }
    }
}


