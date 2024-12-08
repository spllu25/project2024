package com.example.project2024

import android.content.Context
import android.content.Intent
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

class cardAdapter(
    private var cards: MutableList<Card>,
    private val context: Context,
    private val isCartFragment: Boolean
) : RecyclerView.Adapter<cardAdapter.MyViewHolder>() {

    fun getCards(): List<Card> = cards


    private val db = FirebaseFirestore.getInstance()
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.image)
        val title: TextView = view.findViewById(R.id.textTitle)
        val likedButton: Button = view.findViewById(R.id.buttonLike)
        val toBuyButton: Button = view.findViewById(R.id.buttonBuy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card, parent, false)
        return MyViewHolder(view)
    }
    fun setCards(newCards: List<Card>) {
        cards.clear()
        cards.addAll(newCards)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = cards.size

    fun updateCards(newCards: List<Card>) {
        cards.clear()
        cards.addAll(newCards)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val card = cards[position]

        holder.title.text = card.title
        val imgId = context.resources.getIdentifier(card.img, "drawable", context.packageName)
        holder.img.setImageResource(imgId)

        holder.likedButton.text = if (card.isFav) "★" else "☆"

        holder.toBuyButton.text = if (card.quantityPurch > 0) "+(${card.quantityPurch})" else "+"

        holder.likedButton.setOnClickListener {
            card.isFav = !card.isFav
            holder.likedButton.text = if (card.isFav) "★" else "☆"

            currentUserId?.let { userId ->
                CoroutineScope(Dispatchers.IO).launch {
                    val favCollection = db.collection("users").document(userId).collection("favorites")
                    if (card.isFav) {
                        favCollection.document(card.id.toString()).set(card)
                    } else {
                        favCollection.document(card.id.toString()).delete()
                    }
                }
            }
        }
        holder.toBuyButton.setOnClickListener {
            if (isCartFragment) {
                if (card.quantityPurch > 1) {
                    card.quantityPurch -= 1
                    holder.toBuyButton.text = "+(${card.quantityPurch})"
                } else {
                    card.isPurch = false
                    card.quantityPurch = 0
                    holder.toBuyButton.text = "+"
                    currentUserId?.let { userId ->
                        CoroutineScope(Dispatchers.IO).launch {
                            db.collection("users").document(userId).collection("cart").document(card.id.toString()).delete()
                        }
                    }
                }
            } else {
                if (!card.isPurch) {
                    card.isPurch = true
                    card.quantityPurch = 1
                    currentUserId?.let { userId ->
                        CoroutineScope(Dispatchers.IO).launch {
                            db.collection("users").document(userId).collection("cart").document(card.id.toString()).set(card)
                        }
                    }
                } else {
                    card.quantityPurch += 1
                }
                holder.toBuyButton.text = "+(${card.quantityPurch})"
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
            }
            context.startActivity(intent)
        }
    }
}
