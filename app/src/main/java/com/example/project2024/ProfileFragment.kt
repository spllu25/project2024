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
        ordersList.adapter = cardAdapter(mutableListOf(), requireContext(), false)

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
                    val snapshot = db.collection("users").document(userId).collection("orders").get().await()
                    val orderedCards = snapshot.documents.mapNotNull { it.toObject(Card::class.java) }
                    (ordersList.adapter as cardAdapter).updateCards(orderedCards.toMutableList())
                } catch (e: Exception) {
                    Log.e("ProfileFragment", "Error loading orders", e)
                }
            }
        }
    }
}