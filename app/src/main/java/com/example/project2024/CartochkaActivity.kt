package com.example.project2024
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CartochkaActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    private var cardId: Int = -1
    private var isFav: Boolean = false
    private var isPurch: Boolean = false

    private lateinit var cardTitleTextView: TextView
    private lateinit var cardDescriptionTextView: TextView
    private lateinit var cardImageView: ImageView
    private lateinit var buttonFav: Button
    private lateinit var buttonCart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cartochka)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        cardTitleTextView = findViewById(R.id.cardTitle)
        cardDescriptionTextView = findViewById(R.id.cardDescription)
        cardImageView = findViewById(R.id.cardImage)
        buttonFav = findViewById(R.id.buttonFav)
        buttonCart = findViewById(R.id.buttonCart)

        val cardTitle = intent.getStringExtra("cardTitle") ?: ""
        val cardDescription = intent.getStringExtra("cardDescription") ?: ""
        val cardImage = intent.getStringExtra("cardImage")
        cardId = intent.getIntExtra("cardId", -1)

        database = FirebaseDatabase.getInstance().reference.child("cards").child(cardId.toString())

        cardTitleTextView.text = cardTitle
        cardDescriptionTextView.text = cardDescription
        cardImage?.let {
            val imageRes = resources.getIdentifier(it, "drawable", packageName)
            cardImageView.setImageResource(imageRes)
        }

        loadCardData()
        updateButtons()

        buttonFav.setOnClickListener {
            toggleFavorite()
        }

        buttonCart.setOnClickListener {
            toggleCartStatus()
        }
    }

    private fun loadCardData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    isFav = dataSnapshot.child("isFav").getValue(Boolean::class.java) ?: false
                    isPurch = dataSnapshot.child("isPurch").getValue(Boolean::class.java) ?: false
                    updateButtons()
                } else {
                    Toast.makeText(this@CartochkaActivity, "Карточка не найдена", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showError("Ошибка загрузки данных")
            }
        })
    }

    private fun updateButtons() {
        buttonFav.text = if (isFav) "★" else "☆"
        buttonCart.text = if (isPurch) "В корзине" else "Добавить в корзину"
    }

    private fun toggleFavorite() {
        isFav = !isFav
        updateButtons()
        updateCardData("isFav", isFav)
    }

    private fun toggleCartStatus() {
        isPurch = !isPurch
        updateButtons()
        updateCardData("isPurch", isPurch)
    }

    private fun updateCardData(field: String, value: Boolean) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    database.child(field).setValue(value).await()
                }
            } catch (e: Exception) {
                showError("Ошибка при обновлении данных")
            }
        }
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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



