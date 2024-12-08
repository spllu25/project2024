package com.example.project2024
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class User(
    val id: String = "",
    val name: String = "",
    val surname: String = "",
    val dadname: String = "",
    val login: String = "",
    val pass: String = ""
)

class UserRepository {
    private val db = FirebaseFirestore.getInstance()}

/*suspend fun addUser(user: User): Boolean = withContext(Dispatchers.IO) {
    if (isUser(user.login)) return@withContext false

    db.collection("users").add(user).await()
    return@withContext true
}

suspend fun isUser(login: String): Boolean = withContext(Dispatchers.IO) {
    val querySnapshot = db.collection("users").whereEqualTo("login", login).get().await()
    return@withContext !querySnapshot.isEmpty
}

suspend fun getUser(login: String, pass: String): User? = withContext(Dispatchers.IO) {
    val querySnapshot = db.collection("users")
        .whereEqualTo("login", login)
        .whereEqualTo("password", pass)
        .get()
        .await()

    return@withContext if (!querySnapshot.isEmpty) {
        querySnapshot.documents[0].toObject(User::class.java)
    } else {
        null
    }
}

suspend fun getUserById(userId: String): User? = withContext(Dispatchers.IO) {
    val document = db.collection("users").document(userId).get().await()
    return@withContext if (document.exists()) {
        document.toObject(User::class.java)
    } else {
        null
    }
}
suspend fun getUserIdByLogin(login: String): String? = withContext(Dispatchers.IO) {
    val querySnapshot = db.collection("users").whereEqualTo("login", login).get().await()
    return@withContext if (!querySnapshot.isEmpty) {
        querySnapshot.documents[0].id
    } else {
        null
    }
}
suspend fun updateUser(user: User, userId: String): Boolean = withContext(Dispatchers.IO) {
    val userRef = db.collection("users").document(userId)
    userRef.set(user).await()
    return@withContext true
}

}*/
