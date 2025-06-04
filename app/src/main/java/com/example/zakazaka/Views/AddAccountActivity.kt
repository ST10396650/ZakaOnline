package com.example.zakazaka.Views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.zakazaka.Data.Database.AppDatabase
import com.example.zakazaka.Models.AccountEntity
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
import com.example.zakazaka.ViewModels.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class AddAccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_account)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initializing HowToViewModel (unchanged as requested)
        val howtoViewModel = HowToViewModel()

        // Firebase User ID (optional use)
        val firebaseUserId = FirebaseAuth.getInstance().currentUser?.uid

        // ViewModelFactory with repositories
        val factory = ViewModelFactory(
            UserRepository(),
            AccountRepository(),
            BudgetGoalRepository(),
            CategoryRepository(),
            SubCategoryRepository(),
            TransactionRepository()
        )

        val accountViewModel = ViewModelProvider(this, factory)[AccountViewModel::class.java]

        val sharedPref = getSharedPreferences("BudgetAppPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("LOGGED_USER_ID", null)
        val backBtn = findViewById<ImageView>(R.id.addAccountBackBtn)
        backBtn.setOnClickListener {
            finish()
        }
        val cancelBtn = findViewById<Button>(R.id.cancelBtn)
        cancelBtn.setOnClickListener {
            finish()
        }
        val btnSaveAcc = findViewById<Button>(R.id.btnSaveAcc)
        btnSaveAcc.setOnClickListener {
            val eAccountName = findViewById<EditText>(R.id.edAccountName).text.toString()
            val eAmount = findViewById<EditText>(R.id.edAmount).text.toString()
            val eBankName = findViewById<EditText>(R.id.edBankName).text.toString()
            val eTypeOfAcc = findViewById<EditText>(R.id.edTypeOfAcc).text.toString()

            if (eAccountName.isEmpty() || eAmount.isEmpty() || eBankName.isEmpty() || eTypeOfAcc.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (userId == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            } else {
                val account = AccountEntity(
                    accountName = eAccountName,
                    amount = eAmount.toDouble(),
                    type = eTypeOfAcc,
                    bankName = eBankName,
                    userID = userId // using SharedPreferences user ID as requested
                )

                val newAccId = accountViewModel.createNewAccount(account)
                newAccId.observe(this){
                    Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show()
                }
                howtoViewModel.isHowtoCompleted(userId, this){ completed ->
                    if(!completed) {//checks if user has completed the tutorial
                        val intent = Intent(this, HowToGetStarted::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                    } else{// if user completed the tutorial they will be sent to the dashboard page
                        val intent = Intent(this, Dashboard::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                    }
                }

//                accountViewModel.createNewAccount(account).observe(this) { success ->
//                    if (success) {
//                        Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show()
//                        howtoViewModel.isHowtoCompleted(userId) { completed ->
//                            val intent = if (!completed) {
//                                Intent(this, HowToGetStarted::class.java)
//                            } else {
//                                Intent(this, Dashboard::class.java)
//                            }
//                            intent.putExtra("USER_ID", userId)
//                            startActivity(intent)
//                        }
//                    } else {
//                        Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show()
//                    }
//                }
            }
        }
    }
}
