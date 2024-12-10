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

class FavFragment : Fragment() {
    private lateinit var favCardsList: RecyclerView
    private lateinit var adapter: cardAdapter
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_fav, container, false)
        favCardsList = view.findViewById(R.id.favcards)

        adapter = cardAdapter(mutableListOf(), requireContext(), false)
        favCardsList.layoutManager = LinearLayoutManager(requireContext())
        favCardsList.adapter = adapter

        return view
    }

    override fun onResume() {
        super.onResume()
        loadFavoriteCards()
    }

    private fun loadFavoriteCards() {
        viewLifecycleOwner.lifecycleScope.launch {
            currentUserId?.let { userId ->
                val favCards = withContext(Dispatchers.IO) {
                    db.collection("users").document(userId).collection("favorites").get().await()
                        .documents.mapNotNull { it.toObject(Card::class.java) }
                }
                adapter.updateCards(favCards)
            }
        }
    }
}

