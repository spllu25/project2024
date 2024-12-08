package com.example.project2024

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class NewOrderFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var baseSpinner: Spinner
    private lateinit var fillingSpinner: Spinner
    private lateinit var creamSpinner: Spinner
    private lateinit var colorSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_order, container, false)

        baseSpinner = view.findViewById(R.id.base)
        fillingSpinner = view.findViewById(R.id.filling)
        creamSpinner = view.findViewById(R.id.cream)
        colorSpinner = view.findViewById(R.id.color)

        // Загружаем опции для спиннеров
        loadOptions()

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
        // Используем viewLifecycleOwner.lifecycleScope для безопасной работы с корутинами
        viewLifecycleOwner.lifecycleScope.launch {
            val bases = getOptionsFromFirestore("bases")
            val fillings = getOptionsFromFirestore("fillings")
            val creams = getOptionsFromFirestore("creams")
            val colors = getOptionsFromFirestore("colors")

            if (isAdded) {  // Проверяем, что фрагмент еще прикреплен
                setupSpinner(baseSpinner, bases)
                setupSpinner(fillingSpinner, fillings)
                setupSpinner(creamSpinner, creams)
                setupSpinner(colorSpinner, colors)
            }
        }
    }

    private suspend fun getOptionsFromFirestore(collectionName: String): List<String> {
        return withContext(Dispatchers.IO) {
            val options = mutableListOf<String>()
            val snapshot = db.collection(collectionName).get().await()
            for (document in snapshot.documents) {
                options.add(document.getString("name") ?: "")
            }
            options
        }
    }

    private fun setupSpinner(spinner: Spinner, options: List<String>) {
        if (isAdded) {  // Проверяем, что фрагмент прикреплен
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    private fun saveOrder(base: String, filling: String, cream: String, color: String) {
        val orderData = hashMapOf(
            "base" to base,
            "filling" to filling,
            "cream" to cream,
            "color" to color
        )

        db.collection("orders").add(orderData)
            .addOnSuccessListener {
                if (isAdded) {  // Проверяем, что фрагмент прикреплен
                    Toast.makeText(requireContext(), "Заказ сохранен!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                if (isAdded) {  // Проверяем, что фрагмент прикреплен
                    Toast.makeText(requireContext(), "Ошибка сохранения заказа: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
