package com.example.project2024
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CartFragment : Fragment() {
    private lateinit var purchCardsList: RecyclerView
    private lateinit var adapter: cardAdapter
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)
        purchCardsList = view.findViewById(R.id.addedCards)

        adapter = cardAdapter(mutableListOf(), requireContext(), true)
        purchCardsList.layoutManager = LinearLayoutManager(requireContext())
        purchCardsList.adapter = adapter

        view.findViewById<Button>(R.id.buttonBuy).setOnClickListener {
            startOformlActivity()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        loadPurchaseCards()
    }

    private fun loadPurchaseCards() {
        viewLifecycleOwner.lifecycleScope.launch {
            currentUserId?.let { userId ->
                val cartCards = withContext(Dispatchers.IO) {
                    db.collection("users").document(userId).collection("cart").get().await()
                        .documents.mapNotNull { it.toObject(Card::class.java) }
                }
                adapter.updateCards(cartCards)
            }
        }
    }

    private fun startOformlActivity() {
        if (adapter.getCards().isEmpty()) {
            Toast.makeText(context, "Ваша корзина пуста!", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(context, OformlActivity::class.java)
            intent.putExtra("cost", calculateTotalCost())
            startActivity(intent)
        }
    }

    private fun calculateTotalCost(): Int {
        return adapter.getCards().sumOf { it.price }
    }
}

