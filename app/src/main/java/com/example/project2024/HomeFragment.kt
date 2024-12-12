package com.example.project2024
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
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
    private lateinit var cartUpdatedReceiver: BroadcastReceiver
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        cartUpdatedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                loadCards()
            }
        }

        requireContext().registerReceiver(
            cartUpdatedReceiver,
            IntentFilter("ACTION_CART_UPDATED"),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(cartUpdatedReceiver)
    }

    private fun loadCards() {
        viewLifecycleOwner.lifecycleScope.launch {
            currentUserId?.let { userId ->
                val userCards = withContext(Dispatchers.IO) {
                    db.collection("users").document(userId).collection("cards").get().await()
                        .documents.mapNotNull { it.toObject(Card::class.java) }
                }
                if (userCards.isEmpty()) {

                    val staticCards =load()

                    saveStaticCardsToFirestore(staticCards, userId)
                    adapter.updateCards(staticCards)
                } else {
                    adapter.updateCards(userCards)
                }
            }
        }
    }

    private suspend fun saveStaticCardsToFirestore(staticCards: List<Card>, userId: String) {
        withContext(Dispatchers.IO) {
            staticCards.forEach { card ->
                db.collection("users").document(userId).collection("cards")
                    .document(card.id.toString()).set(card).await()
            }
        }
    }

    private fun load(): List<Card> {
        val bases = resources.getStringArray(R.array.bases).toList()
        val fillings = resources.getStringArray(R.array.fillings).toList()
        val creams = resources.getStringArray(R.array.creams).toList()
        val colors = resources.getStringArray(R.array.colors).toList()
        val staticCards = listOf(
            Card(
                1,
                getString(R.string.title1),
                """
            Состав:
            • Основа: ${bases[0]}
            • Начинка: ${fillings[0]}
            • Крем: ${creams[0]}
            • Цвет: ${colors[0]}
        """.trimIndent(),
                "def",
                false,
                false,
                0,
                700
            ),
            Card(
                2,
                getString(R.string.title2),
                """
            Состав:
            • Основа: ${bases[1]}
            • Начинка: ${fillings[1]}
            • Крем: ${creams[1]}
            • Цвет: ${colors[1]}
        """.trimIndent(),
                "def",
                false,
                false,
                0,
                1000
            ),
            Card(
                3,
                getString(R.string.title3),
                """
            Состав:
            • Основа: ${bases[2]}
            • Начинка: ${fillings[2]}
            • Крем: ${creams[2]}
            • Цвет: ${colors[2]}
        """.trimIndent(),
                "def",
                false,
                false,
                0,
                700
            ),
            Card(
                4,
                getString(R.string.title4),
                """
            Состав:
            • Основа: ${bases[3]}
            • Начинка: ${fillings[3]}
            • Крем: ${creams[3]}
            • Цвет: ${colors[3]}
        """.trimIndent(),
                "def",
                false,
                false,
                0,
                1500
            )
        )
        return staticCards
    }
}


