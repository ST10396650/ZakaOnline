package com.example.zakazaka.Views

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.zakazaka.R
import com.example.zakazaka.ViewModels.HowToViewModel
import androidx.lifecycle.lifecycleScope
import com.example.zakazaka.Data.Database.AppDatabase
import com.example.zakazaka.Repository.AccountRepository
import com.example.zakazaka.Repository.BudgetGoalRepository
import com.example.zakazaka.Repository.CategoryRepository
import com.example.zakazaka.ViewModels.AccountViewModel
import com.example.zakazaka.ViewModels.BudgetGoalViewModel
import com.example.zakazaka.ViewModels.CategoryViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HowToGetStarted : AppCompatActivity() {
    var hasAccount: Boolean = false
    var hasCategory: Boolean = false
    var hasBudgetGoal: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_how_to_get_started)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Get current user safely
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            // Handle case where user is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userId = firebaseUser.uid

        // Initialize HowToViewModel with all ViewModels
        val howToViewModel = HowToViewModel()
        howToViewModel.accountViewModel = AccountViewModel(AccountRepository())
        howToViewModel.categoryViewModel = CategoryViewModel(CategoryRepository())
        howToViewModel.budgetGoalViewModel = BudgetGoalViewModel(BudgetGoalRepository())

        // Check user's setup status with debug logging
        Log.d("HowToGetStarted", "Starting checks for user: $userId")

        howToViewModel.checkForAccount(userId, this) { accountResult ->
            hasAccount = accountResult
            Log.d("HowToGetStarted", "Account check result: $hasAccount")

            howToViewModel.checkForCategory(userId, this) { categoryResult ->
                hasCategory = categoryResult
                Log.d("HowToGetStarted", "Category check result: $hasCategory")

                howToViewModel.checkForBudgetGoal(userId, this) { budgetResult ->
                    hasBudgetGoal = budgetResult
                    Log.d("HowToGetStarted", "Budget Goal check result: $hasBudgetGoal")
                    Log.d(
                        "HowToGetStarted",
                        "All checks - Account: $hasAccount, Category: $hasCategory, Budget: $hasBudgetGoal"
                    )

                    setUp(userId)
                }
            }
        }
    }


        //fetches the suspended methods in a coroutine to avoid blocking the UI thread



        //checking if user has set up the account, category and budget goal


    private fun setUp(userId: String) {
        val txtSetUpAccount = findViewById<TextView>(R.id.txtSetUpAccount)
        val cbAccount = findViewById<CheckBox>(R.id.cbAccount)

        Log.d("HowToGetStarted", "Setting up UI - hasBudgetGoal: $hasBudgetGoal")

        // Account setup (your existing working code)
        cbAccount.isChecked = hasAccount
        txtSetUpAccount.setOnClickListener {
            if (hasAccount) {
                cbAccount.isChecked = true
                Toast.makeText(this, "Account already set up", Toast.LENGTH_SHORT).show()
                txtSetUpAccount.isEnabled = false
            } else {
                val intent = Intent(this, AddAccountActivity::class.java)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
            }
        }

        // Update UI based on current status
        if (hasAccount) {
            txtSetUpAccount.isEnabled = false
            txtSetUpAccount.alpha = 0.5f
        }


        val txtSetUpBudget = findViewById<TextView>(R.id.txtSetUpBudget)
        val cbBudgetGoalSetup = findViewById<CheckBox>(R.id.cbBudgetGoalSetup)

        // Set initial checkbox state
        // Budget Goal setup (fixed to match account pattern)
        // Budget Goal setup - FIXED
        cbBudgetGoalSetup.isChecked = hasBudgetGoal
        Log.d("HowToGetStarted", "Budget checkbox set to: ${cbBudgetGoalSetup.isChecked}")

        txtSetUpBudget.setOnClickListener {
            if (hasBudgetGoal) {
                cbBudgetGoalSetup.isChecked = true
                Toast.makeText(this, "Budget Goal already set up", Toast.LENGTH_SHORT).show()
                txtSetUpBudget.isEnabled = false
            } else {
                val intent = Intent(this, MilestoneActivity::class.java)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
            }
        }
        // Update UI based on current status for budget goal
        if (hasBudgetGoal) {
            txtSetUpBudget.isEnabled = false
            txtSetUpBudget.alpha = 0.5f
        }

        val txtSetUpExCategory = findViewById<TextView>(R.id.txtSetUpExCategory)
        val cbCategorySetUp = findViewById<CheckBox>(R.id.cbCategorySetUp)

        // Set initial checkbox state
        cbCategorySetUp.isChecked = hasCategory

        txtSetUpExCategory.setOnClickListener {
            if (!hasCategory) {
                val intent = Intent(this, CreateCategory::class.java)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
            } else {
                cbCategorySetUp.isChecked = true
                Toast.makeText(this, "Category already set up", Toast.LENGTH_SHORT).show()
                txtSetUpExCategory.isEnabled = false
            }
        }

        // Update UI based on current status
        if (hasCategory) {
            txtSetUpExCategory.isEnabled = false
            txtSetUpExCategory.alpha = 0.5f
        }

        // Check if all setup is complete
        val allComplete = hasAccount && hasCategory && hasBudgetGoal
        Log.d("HowToGetStarted", "All setup complete: $allComplete")

        if (allComplete) {
            Log.d("HowToGetStarted", "All setup complete - should navigate to Dashboard")
            // Optional: Add a delay to let user see the completion
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, Dashboard::class.java)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
                finish()
            }, 1000) // 1 second delay
        }
    }
}