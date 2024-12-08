package com.example.project2024
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private var userId: Int = -1
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = FirebaseDatabase.getInstance().reference

        userId = intent.getIntExtra("userId", -1)

        loadUserData(userId)

        replaceFragment(HomeFragment())

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val fragment = when (menuItem.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_favorites -> FavFragment()
                R.id.nav_profile -> ProfileFragment()
                R.id.nav_cart -> CartFragment()
                R.id.nav_new_order -> NewOrderFragment()
                else -> null
            }
            fragment?.let { replaceFragment(it) }
            true
        }
    }

    private fun loadUserData(userId: Int) {
        database.child("users").child(userId.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    userName = dataSnapshot.child("name").getValue(String::class.java)
                } else {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

