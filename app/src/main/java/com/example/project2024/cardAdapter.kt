package com.example.project2024

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class cardAdapter(
    private var cards: MutableList<Card>,
    private val context: Context,
    private val isCartFragment: Boolean = false,
    private val isProfileFragment: Boolean = false
) : RecyclerView.Adapter<cardAdapter.MyViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun getCards(): List<Card> = cards

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.image)
        val title: TextView = view.findViewById(R.id.textTitle)
        val likedButton: Button = view.findViewById(R.id.buttonLike)
        val toBuyButton: Button = view.findViewById(R.id.buttonBuy)
        val container: View = view.findViewById(R.id.cardContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = cards.size

    fun updateCards(newCards: List<Card>) {
        cards.clear()
        cards.addAll(newCards)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val card = cards[position]
        val shortComposition = card.txt.lines()
            .take(2)
            .joinToString(separator = "\n") { it.trim() }

        holder.title.text = "${card.title}\n$shortComposition"

        val imgId = context.resources.getIdentifier(card.img, "drawable", context.packageName)
        holder.img.setImageResource(imgId)

        if (isProfileFragment) {
            holder.likedButton.visibility = View.GONE
            holder.toBuyButton.visibility = View.GONE
        } else {
            holder.likedButton.visibility = View.VISIBLE
            holder.toBuyButton.visibility = View.VISIBLE

            holder.likedButton.text = if (card.isFav) "★" else "☆"
            holder.toBuyButton.text = when {
                card.quantityPurch > 0 -> "+(${card.quantityPurch})"
                else -> "+"
            }

            holder.likedButton.setOnClickListener {
                card.isFav = !card.isFav
                holder.likedButton.text = if (card.isFav) "★" else "☆"
                updateCardInFirestore(card, "favorites", card.isFav)
            }

            holder.toBuyButton.setOnClickListener {
                if (isCartFragment) {
                    removeFromCart(card, holder)
                } else {
                    addToCart(card, holder)
                }
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, CartochkaActivity::class.java).apply {
                putExtra("cardId", card.id)
                putExtra("cardTitle", card.title)
                putExtra("cardDescription", card.txt)
                putExtra("cardImage", card.img)
                putExtra("isFav", card.isFav)
                putExtra("isPurch", card.isPurch)
                putExtra("quantityPurch", card.quantityPurch)
                putExtra("cardPrice", card.price)
                }
            context.startActivity(intent)
        }
    }

    private fun addToCart(card: Card, holder: MyViewHolder) {
        card.isPurch = true
        card.quantityPurch++
        holder.toBuyButton.text = "+(${card.quantityPurch})"
        updateCardInFirestore(card, "cart", true)
        context.sendBroadcast(Intent("ACTION_CART_UPDATED"))
    }

    private fun removeFromCart(card: Card, holder: MyViewHolder) {
        if (card.quantityPurch > 1) {
            card.quantityPurch--
        } else {
            card.isPurch = false
            card.quantityPurch = 0
        }
        holder.toBuyButton.text = if (card.quantityPurch > 0) "+(${card.quantityPurch})" else "+"
        updateCardInFirestore(card, "cart", card.isPurch)
        context.sendBroadcast(Intent("ACTION_CART_UPDATED"))
    }

    private fun updateCardInFirestore(card: Card, collection: String, shouldExist: Boolean) {
        currentUserId?.let { userId ->
            CoroutineScope(Dispatchers.IO).launch {
                val userCardsRef = db.collection("users").document(userId).collection("cards")
                userCardsRef.document(card.id.toString()).set(card)

                val collectionRef = db.collection("users").document(userId).collection(collection)
                if (shouldExist) {
                    collectionRef.document(card.id.toString()).set(card)
                } else {
                    collectionRef.document(card.id.toString()).delete()
                }
            }
        }
    }
}
