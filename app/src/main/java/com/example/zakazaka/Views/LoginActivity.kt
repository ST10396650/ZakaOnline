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
import com.example.zakazaka.R
import com.example.zakazaka.Repository.AccountRepository
import com.example.zakazaka.Repository.BudgetGoalRepository
import com.example.zakazaka.Repository.CategoryRepository
import com.example.zakazaka.Repository.SubCategoryRepository
import com.example.zakazaka.Repository.TransactionRepository
import com.example.zakazaka.Repository.UserRepository
import com.example.zakazaka.ViewModels.AccountViewModel
import com.example.zakazaka.ViewModels.BudgetGoalViewModel
import com.example.zakazaka.ViewModels.CategoryViewModel
import com.example.zakazaka.ViewModels.HowToViewModel
import com.example.zakazaka.ViewModels.LoginRegistrationViewModel
import com.example.zakazaka.ViewModels.ViewModelFactory

class LoginActivity : AppCompatActivity() {
    lateinit var loginViewModel: LoginRegistrationViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val factory = ViewModelFactory(
            UserRepository(),
            AccountRepository(),
            BudgetGoalRepository(),
            CategoryRepository(),
            SubCategoryRepository(),
            TransactionRepository()
        )
        val howtoViewModel = HowToViewModel()



        loginViewModel = ViewModelProvider(this, factory)[LoginRegistrationViewModel::class.java]
        //user will create account
        val btnCreateAcc = findViewById<Button>(R.id.btnCreateAccount)
        btnCreateAcc.setOnClickListener {
            val intent = Intent(this, CreateAccount::class.java)//create this page
            startActivity(intent)
        }
        //functionality for the user to login
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val email = findViewById<EditText>(R.id.edEmailUnameLogin).text.toString()
            val password = findViewById<EditText>(R.id.edPasswordLogin).text.toString()
            if(email.isEmpty() || password.isEmpty()){
                val toast = Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT)
                toast.show()
            } else {
                loginViewModel.loginUser(email, password) { user, error ->
                    if (user != null) {
                        Toast.makeText(this, "Login Successful, user: ${user.userID}", Toast.LENGTH_SHORT).show()

                        howtoViewModel.isHowtoCompleted(user.userID, this) { completed ->
                            val sharedPref = getSharedPreferences("BudgetAppPrefs", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("LOGGED_USER_ID", user.userID)
                                apply()
                            }

                            val intent = if (!completed) {
                                Toast.makeText(this, "Let's get you started", Toast.LENGTH_SHORT).show()
                                Intent(this, HowToGetStarted::class.java)
                            } else {
                                Toast.makeText(this, "Welcome back ${user.firstName}", Toast.LENGTH_SHORT).show()
                                Intent(this, Dashboard::class.java)
                            }
                            intent.putExtra("USER_ID", user.userID)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this, "Login Failed: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            }


        }

    }
}