package com.example.project2024

import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class NewOrderFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var baseSpinner: Spinner
    private lateinit var fillingSpinner: Spinner
    private lateinit var creamSpinner: Spinner
    private lateinit var colorSpinner: Spinner
    private lateinit var cakeImageView: ImageView
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_order, container, false)
        baseSpinner = view.findViewById(R.id.base)
        fillingSpinner = view.findViewById(R.id.filling)
        creamSpinner = view.findViewById(R.id.cream)
        colorSpinner = view.findViewById(R.id.color)
        cakeImageView = view.findViewById(R.id.cakeImageView)

        loadOptions()
        setupListeners()

        val buttonLogin: Button = view.findViewById(R.id.buttonLogin)
        buttonLogin.setOnClickListener {
            val selectedBase = baseSpinner.selectedItem.toString()
            val selectedFilling = fillingSpinner.selectedItem.toString()
            val selectedCream = creamSpinner.selectedItem.toString()
            val selectedColor = colorSpinner.selectedItem.toString()

            Toast.makeText(
                requireContext(),
                "Вы выбрали: $selectedBase, $selectedFilling, $selectedCream, $selectedColor",
                Toast.LENGTH_SHORT
            ).show()

            saveOrder(selectedBase, selectedFilling, selectedCream, selectedColor)
        }

        return view
    }

    private fun loadOptions() {
        viewLifecycleOwner.lifecycleScope.launch {
            val bases = listOf("Бисквитная", "Слоенная", "Песочная", "Медовик")
            val fillings = listOf("Карамель", "Банан", "Вишня", "Клубника-ваниль")
            val creams = listOf("Йогуртовый", "Сливочный", "Шоколадный", "Сгущенка")
            val colors = listOf("Голубой", "Красный", "Синий", "Розовый", "Белый")

            if (isAdded) {
                setupSpinner(baseSpinner, bases)
                setupSpinner(fillingSpinner, fillings)
                setupSpinner(creamSpinner, creams)
                setupSpinner(colorSpinner, colors)
            }
        }
    }

    private fun setupSpinner(spinner: Spinner, options: List<String>) {
        if (isAdded) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    private fun setupListeners() {
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                updateCakeImage()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        baseSpinner.onItemSelectedListener = listener
        fillingSpinner.onItemSelectedListener = listener
        creamSpinner.onItemSelectedListener = listener
        colorSpinner.onItemSelectedListener = listener
    }
    private fun updateCakeImage() {
        val baseColor = getBaseColor(baseSpinner.selectedItem.toString())
        val fillingColor = getFillingColor(fillingSpinner.selectedItem.toString())
        val toppingColor = getColor(colorSpinner.selectedItem.toString())

        val baseLayer = BitmapFactory.decodeResource(resources, R.drawable.base).colorize(baseColor)
        val fillingLayer = BitmapFactory.decodeResource(resources, R.drawable.filling).colorize(fillingColor)
        val toppingLayer = BitmapFactory.decodeResource(resources, R.drawable.color).colorize(toppingColor)

        val staticOverlayLayer = BitmapFactory.decodeResource(resources, R.drawable.cake)

        val resultBitmap = Bitmap.createBitmap(baseLayer.width, baseLayer.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(baseLayer, 0f, 0f, null)
        canvas.drawBitmap(fillingLayer, 0f, 0f, null)
        canvas.drawBitmap(toppingLayer, 0f, 0f, null)
        canvas.drawBitmap(staticOverlayLayer, 0f, 0f, null)

        cakeImageView.setImageBitmap(resultBitmap)
    }


    private fun Bitmap.colorize(color: Int): Bitmap {
        val mutableBitmap = this.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply { colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN) }
        canvas.drawBitmap(mutableBitmap, 0f, 0f, paint)
        return mutableBitmap
    }

    private fun getBaseColor(base: String): Int {
        return when (base) {
            "Бисквитная" -> Color.rgb(255, 228, 181)
            "Слоенная" -> Color.rgb(123, 63, 0)
            "Песочная" -> Color.rgb(255, 239, 213)
            "Медовик" -> Color.rgb(193, 140, 67)
            else -> Color.TRANSPARENT
        }
    }

    private fun getFillingColor(filling: String): Int {
        return when (filling) {
            "Карамель" -> Color.rgb(210, 105, 30)
            "Банан" -> Color.rgb(255, 225, 53)
            "Вишня" -> Color.rgb(139, 0, 0)
            "Клубника-ваниль" -> Color.rgb(255, 182, 193)
            else -> Color.TRANSPARENT
        }
    }

    private fun getColor(colorName: String): Int {
        return when (colorName) {
            "Голубой" -> Color.rgb(135, 206, 250)
            "Красный" -> Color.RED
            "Синий" -> Color.rgb(0, 0, 139)
            "Розовый" -> Color.rgb(255, 192, 203)
            "Белый" -> Color.WHITE
            else -> Color.TRANSPARENT
        }
    }

    private fun saveOrder(base: String, filling: String, cream: String, color: String) {
        val composition = """
            Состав:
            • Основа: $base
            • Начинка: $filling
            • Крем: $cream
            • Цвет: $color
        """.trimIndent()

        val newCard = FirebaseAuth.getInstance().currentUser?.uid?.let {
            Card(
                id = (1..100000).random(),
                title = "Новый заказ",
                txt = composition,
                img = "def",
                isFav = false,
                isPurch = true,
                quantityPurch = 1,
                price = 1000
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            currentUserId?.let { userId ->
                newCard?.let { card ->
                    withContext(Dispatchers.IO) {
                        try {
                            db.collection("users").document(userId).collection("cart")
                                .document(card.id.toString()).set(card).await()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Заказ сохранен и добавлен в корзину!", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Ошибка сохранения заказа: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } ?: run {
                Toast.makeText(requireContext(), "Не удалось получить идентификатор пользователя", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
