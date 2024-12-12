package com.example.project2024

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
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

class FavFragment : Fragment() {
    private lateinit var favCardsList: RecyclerView
    private lateinit var adapter: cardAdapter
    private var cartUpdatedReceiver: BroadcastReceiver? = null
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_fav, container, false)
        favCardsList = view.findViewById(R.id.favcards)

        adapter = cardAdapter(mutableListOf(), requireContext(), isCartFragment = false)
        favCardsList.layoutManager = LinearLayoutManager(requireContext())
        favCardsList.adapter = adapter



        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        loadFavoriteCards()

        cartUpdatedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val updatedCardId = intent?.getIntExtra("cardId", -1) ?: -1
                val isPurchased = intent?.getBooleanExtra("isPurch", false) ?: false
                val quantity = intent?.getIntExtra("quantity", 0) ?: 0

                Log.d("FavFragment", "Broadcast received: cardId=$updatedCardId, isPurchased=$isPurchased, quantity=$quantity")

                if (updatedCardId != -1) {
                    adapter.updateCardStatus(updatedCardId, isPurchased, quantity)
                }
            }
        }

        val intentFilter = IntentFilter("ACTION_CART_UPDATED")
        requireContext().registerReceiver(
            cartUpdatedReceiver,
            intentFilter,
            Context.RECEIVER_NOT_EXPORTED
        )
    }


    override fun onStop() {
        super.onStop()
        cartUpdatedReceiver?.let {
            requireContext().unregisterReceiver(it)
        }
        cartUpdatedReceiver = null
    }

    private fun loadFavoriteCards() {
        viewLifecycleOwner.lifecycleScope.launch {
            currentUserId?.let { userId ->
                val favCards = withContext(Dispatchers.IO) {
                    db.collection("users")
                        .document(userId)
                        .collection("favorites")
                        .get()
                        .await()
                        .documents.mapNotNull { it.toObject(Card::class.java) }
                }
                adapter.updateCards(favCards)
            } ?: run {
                adapter.updateCards(emptyList())
            }
        }
    }

}
