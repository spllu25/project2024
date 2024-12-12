package com.example.project2024

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


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
            if (isCartFragment) {
                holder.likedButton.visibility = View.GONE
            } else {
                holder.likedButton.visibility = View.VISIBLE
            }
            holder.toBuyButton.visibility = View.VISIBLE

            holder.likedButton.text = if (card.isFav) "★" else "☆"
            holder.toBuyButton.text = when {
                card.quantityPurch > 0 -> "(${card.quantityPurch})"
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
        holder.toBuyButton.text = "(${card.quantityPurch})"
        updateCardInFirestore(card, "cart", true)

        context.sendBroadcast(Intent("ACTION_CART_UPDATED").apply {
            putExtra("cardId", card.id)
            putExtra("isPurch", true)
            putExtra("quantity", card.quantityPurch)
        })
    }

    private fun removeFromCart(card: Card, holder: MyViewHolder) {
        if (card.quantityPurch > 1) {
            card.quantityPurch--
        } else {
            card.isPurch = false
            card.quantityPurch = 0
        }
        holder.toBuyButton.text = if (card.quantityPurch > 0) "(${card.quantityPurch})" else "+"
        updateCardInFirestore(card, "cart", card.isPurch)

        context.sendBroadcast(Intent("ACTION_CART_UPDATED").apply {
            putExtra("cardId", card.id)
            putExtra("isPurch", card.isPurch)
            putExtra("quantity", card.quantityPurch)
        })
    }

    private fun updateCardInFirestore(card: Card, triggeringCollection: String, shouldExist: Boolean) {
        currentUserId?.let { userId ->
            CoroutineScope(Dispatchers.IO).launch {
                val userRef = db.collection("users").document(userId)

                val cardRef = userRef.collection("cards").document(card.id.toString())
                cardRef.set(card)
                val collectionsToSync = listOf("favorites", "cart").filter { it != triggeringCollection }

                for (collection in collectionsToSync) {
                    val collectionRef = userRef.collection(collection)
                    val cardDoc = collectionRef.document(card.id.toString()).get().await()

                    if (cardDoc.exists()) {
                        collectionRef.document(card.id.toString()).update(
                            mapOf(
                                "isPurch" to card.isPurch,
                                "quantityPurch" to card.quantityPurch
                            )
                        )
                    }
                }

                val triggeringCollectionRef = userRef.collection(triggeringCollection)
                if (shouldExist) {
                    triggeringCollectionRef.document(card.id.toString()).set(card)
                } else {
                    triggeringCollectionRef.document(card.id.toString()).delete()
                }
            }
        }
    }

    fun updateCardStatus(cardId: Int, isPurchased: Boolean, quantity: Int = 0) {
        val cardIndex = cards.indexOfFirst { it.id == cardId }
        if (cardIndex != -1) {
            val card = cards[cardIndex]
            card.isPurch = isPurchased
            card.quantityPurch = if (isPurchased) quantity.coerceAtLeast(1) else 0
            notifyItemChanged(cardIndex)
        } else {
            Log.d("cardAdapter", "Card not found: $cardId")
        }
    }
}
