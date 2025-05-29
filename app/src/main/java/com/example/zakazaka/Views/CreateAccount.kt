package com.example.zakazaka.Views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.zakazaka.Data.Database.AppDatabase
import com.example.zakazaka.Models.UserEntity
import com.example.zakazaka.R
import com.example.zakazaka.Repository.AccountRepository
import com.example.zakazaka.Repository.BudgetGoalRepository
import com.example.zakazaka.Repository.CategoryRepository
import com.example.zakazaka.Repository.SubCategoryRepository
import com.example.zakazaka.Repository.TransactionRepository
import com.example.zakazaka.Repository.UserRepository
import com.example.zakazaka.ViewModels.LoginRegistrationViewModel
import com.example.zakazaka.ViewModels.ViewModelFactory

class CreateAccount : AppCompatActivity() {
    lateinit var loginViewModel: LoginRegistrationViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_account)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // ✅ FIX: use the same factory as LoginActivity
        val factory = ViewModelFactory(
            UserRepository(),
            AccountRepository(),
            BudgetGoalRepository(),
            CategoryRepository(),
            SubCategoryRepository(),
            TransactionRepository()
        )
        loginViewModel = ViewModelProvider(this, factory)[LoginRegistrationViewModel::class.java]

        val btnCreateAcc = findViewById<Button>(R.id.btn_create_account)
        btnCreateAcc.setOnClickListener {
            val username = findViewById<EditText>(R.id.edUsername).text.toString()
            val firstName = findViewById<EditText>(R.id.edFirstname).text.toString()
            val lastName = findViewById<EditText>(R.id.edSurname).text.toString()
            val email = findViewById<EditText>(R.id.edEmail).text.toString()
            val password = findViewById<EditText>(R.id.edPassword).text.toString()
            val retypePassword = findViewById<EditText>(R.id.edRetypePassword).text.toString()

            if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty()
                || email.isEmpty() || password.isEmpty() || retypePassword.isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (password != retypePassword) {
                Toast.makeText(this, "Passwords do not match, please try again", Toast.LENGTH_SHORT).show()
            } else {
                val user = UserEntity(
                    username = username,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password
                )

                loginViewModel.registerUser(user)

                loginViewModel.registrationStatus.observe(this) { (success, message) ->
                    if (success) {
                        Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Failed: $message", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}