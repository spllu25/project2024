package com.example.project2024
import android.content.Intent
import android.net.ParseException
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileFragment : Fragment() {
    private lateinit var ordersList: RecyclerView
    private lateinit var db: FirebaseFirestore
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val buttonToSettings: Button = view.findViewById(R.id.buttonProfileSettings)

        ordersList = view.findViewById(R.id.newOrdersList)
        ordersList.layoutManager = LinearLayoutManager(requireContext())
        ordersList.adapter = cardAdapter(mutableListOf(), requireContext(), false, true)

        db = FirebaseFirestore.getInstance()

        buttonToSettings.setOnClickListener {
            val intent = Intent(context, ProfilActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    private fun loadOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            currentUserId?.let { userId ->
                try {
                    val ordersSnapshot = db.collection("users").document(userId).collection("orders").get().await()
                    val allCardsWithStatus = mutableListOf<Pair<Card, Boolean>>()

                    val today = Calendar.getInstance().time
                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    for (orderDoc in ordersSnapshot.documents) {
                        val orderDateStr = orderDoc.getString("date")
                        val isOrderArchived = try {
                            val orderDate = formatter.parse(orderDateStr ?: "")
                            orderDate != null && orderDate.before(today)
                        } catch (e: Exception) {
                            false
                        }

                        val cardsSnapshot = orderDoc.reference.collection("cards").get().await()
                        val cards = cardsSnapshot.documents.mapNotNull { cardDoc ->
                            cardDoc.toObject(Card::class.java)?.let { card ->
                                Pair(card, isOrderArchived)
                            }
                        }

                        allCardsWithStatus.addAll(cards)
                    }

                    updateUI(allCardsWithStatus)
                } catch (e: Exception) {
                    Log.e("ProfileFragment", "Error loading orders", e)
                }
            }
        }
    }

    private fun updateUI(cardsWithStatus: List<Pair<Card, Boolean>>) {
        val adapter = ordersList.adapter as cardAdapter
        val cards = cardsWithStatus.map { it.first }
        adapter.updateCards(cards)

        ordersList.post {
            for ((index, pair) in cardsWithStatus.withIndex()) {
                val (_, isArchived) = pair
                val viewHolder = ordersList.findViewHolderForAdapterPosition(index) as? cardAdapter.MyViewHolder
                if (viewHolder != null) {
                    val backgroundColor = if (isArchived) {
                        ContextCompat.getColor(requireContext(), R.color.md_theme_outline_mediumContrast)
                    } else {
                        ContextCompat.getColor(requireContext(), R.color.md_theme_background)
                    }
                    viewHolder.container.setBackgroundColor(backgroundColor)
                }
            }
        }
    }
}

