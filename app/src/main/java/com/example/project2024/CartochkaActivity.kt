package com.example.project2024
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class CartochkaActivity : AppCompatActivity() {
    private lateinit var cardTitleTextView: TextView
    private lateinit var cardImageView: ImageView
    private lateinit var compositionText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cartochka)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        cardTitleTextView = findViewById(R.id.cardTitle)
        cardImageView = findViewById(R.id.cardImage)
        compositionText = findViewById(R.id.cardDescription)

        val cardTitle = intent.getStringExtra("cardTitle") ?: ""
        val cardImage = intent.getStringExtra("cardImage")
        val cardDescription = intent.getStringExtra("cardDescription") ?: ""
        val cardPrice = intent.getIntExtra("cardPrice", 0)

        cardTitleTextView.text = cardTitle
        compositionText.text = cardDescription
        cardTitleTextView.text = "$cardPrice₽"

        cardImage?.let {
            val imageRes = resources.getIdentifier(it, "drawable", packageName)
            cardImageView.setImageResource(imageRes)
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
