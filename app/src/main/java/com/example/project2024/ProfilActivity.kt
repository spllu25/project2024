package com.example.project2024

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfilActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val passwordField: EditText = findViewById(R.id.pass)
        val updatePassword: Button = findViewById(R.id.updatePassword)
        val emailField: EditText = findViewById(R.id.login)

        emailField.setText(auth.currentUser?.email)
        emailField.isEnabled = false

        currentUserId?.let { userId ->
            val userSurname: EditText = findViewById(R.id.surname)
            val userName: EditText = findViewById(R.id.name)
            val userDadname: EditText = findViewById(R.id.dadname)


            loadUserData(userId, userSurname, userName, userDadname)

            findViewById<Button>(R.id.buttonSave).setOnClickListener {
                val updatedUser = User(
                    id = userId,
                    name = userName.text.toString().trim(),
                    surname = userSurname.text.toString().trim(),
                    dadname = userDadname.text.toString().trim()
                )
                saveUserData(userId, updatedUser)
            }

            findViewById<Button>(R.id.buttonLogout).setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        } ?: run {
            Toast.makeText(this, "Ошибка: пользователь не определен", Toast.LENGTH_SHORT).show()
            finish()
        }

        updatePassword.setOnClickListener {
            val newPassword = passwordField.text.toString().trim()
            val currentPassword = passwordField.text.toString().trim()

            if (newPassword.isNotEmpty() && currentPassword.isNotEmpty()) {
                updatePassword(newPassword, currentPassword)
            } else {
                Toast.makeText(this, "Введите старый и новый пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserData(
        userId: String,
        surnameField: EditText,
        nameField: EditText,
        dadnameField: EditText
    ) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        surnameField.setText(it.surname)
                        nameField.setText(it.name)
                        dadnameField.setText(it.dadname)
                    } ?: Toast.makeText(this, "Данные пользователя отсутствуют", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка загрузки данных: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun saveUserData(userId: String, user: User) {
        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Данные сохранены!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка сохранения данных: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
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
    private fun updatePassword(newPassword: String, currentPassword: String) {
        val user = auth.currentUser
        val currentEmail = user?.email ?: return

        val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
        user.reauthenticate(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(this, "Пароль обновлён!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Ошибка обновления пароля: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Ошибка аутентификации: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}