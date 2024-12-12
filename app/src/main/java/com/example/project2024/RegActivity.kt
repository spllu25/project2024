package com.example.project2024
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userSurname: EditText = findViewById(R.id.surname)
        val userName: EditText = findViewById(R.id.name)
        val userDadname: EditText = findViewById(R.id.dadname)
        val userNumb: EditText = findViewById(R.id.numbPhone)
        val userPass: EditText = findViewById(R.id.passReg)
        val buttonReg: Button = findViewById(R.id.buttonRegister)
        val buttonLogin: Button = findViewById(R.id.buttonLogin)

        buttonReg.setOnClickListener {
            val surname = userSurname.text.toString().trim()
            val name = userName.text.toString().trim()
            val dadname = userDadname.text.toString().trim()
            val login = userNumb.text.toString().trim()
            val pass = userPass.text.toString().trim()

            if (surname.isEmpty() || name.isEmpty() || dadname.isEmpty() || login.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(surname, name, dadname, login, pass)
            }
        }

        buttonLogin.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }

    private fun registerUser(surname: String, name: String, dadname: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    saveUserDataToFirestore(userId, surname, name, dadname, email)
                } else {
                    Toast.makeText(this, "Ошибка регистрации: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка регистрации: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserDataToFirestore(userId: String, surname: String, name: String, dadname: String, email: String) {
        val userData = mapOf(
            "userId" to userId,
            "surname" to surname,
            "name" to name,
            "dadname" to dadname,
            "email" to email
        )

        db.collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка сохранения данных: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}
