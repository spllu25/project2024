package com.example.project2024

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userLogin: EditText = findViewById(R.id.login)
        val userPass: EditText = findViewById(R.id.password)
        val buttonLogin: Button = findViewById(R.id.buttonSignin)

        buttonLogin.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val pass = userPass.text.toString().trim()

            if (login.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            } else {
                authenticateUser(login, pass)
            }
        }

        val buttonSignup: Button = findViewById(R.id.buttonSignup)
        buttonSignup.setOnClickListener {
            val intent = Intent(this, RegActivity::class.java)
            startActivity(intent)
        }
    }

    private fun authenticateUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val userId = currentUser.uid
                        navigateToMain(userId)
                    }
                } else {
                    Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка авторизации: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToMain(userId: String) {
        val intent = Intent(this@AuthActivity, MainActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("navigateToHome", true)
        startActivity(intent)
        finish()
    }
}
