package com.example.project2024
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.net.ParseException
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class OformlActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oforml)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = FirebaseFirestore.getInstance()

        val stoimost = intent.getIntExtra("cost", -1)
        val client: EditText = findViewById(R.id.poluchatel)
        val address: EditText = findViewById(R.id.address)
        val date: EditText = findViewById(R.id.date)
        val cost: TextView = findViewById(R.id.cost)
        val buttonBuy: Button = findViewById(R.id.buttonBuy)
        cost.text = stoimost.toString()

        currentUserId?.let { userId ->
            lifecycleScope.launch {
                val user = withContext(Dispatchers.IO) { getUserById(userId) }
                user?.let {
                    client.setText(it.surname)
                } ?: Toast.makeText(this@OformlActivity, "Пользователь не найден", Toast.LENGTH_SHORT).show()
            }

            buttonBuy.setOnClickListener {
                handleOrder(userId, client.text.toString(), address.text.toString(), date.text.toString(), stoimost)
            }
        } ?: run {
            Toast.makeText(this, "Ошибка: пользователь не определен", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun handleOrder(
        userId: String,
        clientName: String,
        address: String,
        inputDate: String,
        cost: Int
    ) {
        val currentDate = Calendar.getInstance()
        currentDate.add(Calendar.DAY_OF_YEAR, 1)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        try {
            val parsedDate = formatter.parse(inputDate)
            if (parsedDate != null && parsedDate.before(currentDate.time)) {
                Toast.makeText(this, "Дата должна быть не раньше завтрашнего дня!", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: ParseException) {
            Toast.makeText(this, "Введите корректную дату в формате yyyy-MM-dd", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val originalCards = getPurchasedCards(userId)
                    clearCart(userId)
                    saveOrder(userId, clientName, address, inputDate, cost.toString(), originalCards)
                    originalCards.forEach { card ->
                        card.isPurch = false
                        card.quantityPurch = 0
                        sendBroadcast(Intent("ACTION_CART_UPDATED").apply {
                            putExtra("cardId", card.id)
                            putExtra("isPurch", false)
                            putExtra("quantity", 0)
                        })
                    }
                }

                Toast.makeText(this@OformlActivity, "Заказ оформлен!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@OformlActivity, MainActivity::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@OformlActivity, "Ошибка при оформлении заказа: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getUserById(userId: String): User? {
        return try {
            val document = db.collection("users").document(userId).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getPurchasedCards(userId: String): List<Card> {
        return try {
            val snapshot = db.collection("users").document(userId).collection("cart").get().await()
            snapshot.documents.mapNotNull { it.toObject(Card::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun clearCart(userId: String) {
        try {
            val cartCollection = db.collection("users").document(userId).collection("cart")
            val snapshot = cartCollection.get().await()
            for (document in snapshot.documents) {
                val card = document.toObject(Card::class.java)
                card?.let {
                    it.isPurch = false
                    it.quantityPurch = 0
                    db.collection("users").document(userId).collection("cards")
                        .document(it.id.toString()).set(it).await()
                    db.collection("users").document(userId).collection("favorites")
                        .document(it.id.toString()).set(it).await()
                }
                cartCollection.document(document.id).delete().await()
            }
        } catch (e: Exception) {
            Log.e("clearCart", "Error clearing cart", e)
        }
    }

    private suspend fun saveOrder(
        userId: String,
        clientName: String,
        address: String,
        date: String,
        cost: String,
        cards: List<Card>
    ) {
        try {
            val orderData = hashMapOf(
                "clientName" to clientName,
                "address" to address,
                "date" to date,
                "cost" to cost
            )
            val orderRef = db.collection("users").document(userId).collection("orders").document()
            orderRef.set(orderData).await()
            val cardsCollection = orderRef.collection("cards")
            for (card in cards) {
                cardsCollection.add(card).await()
            }
        } catch (e: Exception) {
            Log.e("saveOrder", "Error saving order", e)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
