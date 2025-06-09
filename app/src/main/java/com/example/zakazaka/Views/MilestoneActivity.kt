package com.example.zakazaka.Views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.zakazaka.Data.Database.AppDatabase
import com.example.zakazaka.Models.BudgetGoalEntity
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
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class MilestoneActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_milestone)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val howtoViewModel = HowToViewModel()
        // Initialize the budgetGoalViewModel in howtoViewModel
        howtoViewModel.budgetGoalViewModel = BudgetGoalViewModel(BudgetGoalRepository())

        val factory = ViewModelFactory(
            UserRepository(),
            AccountRepository(),
            BudgetGoalRepository(), // Firebase-based repository
            CategoryRepository(),
            SubCategoryRepository(),
            TransactionRepository()
        )
        val backBtn = findViewById<ImageView>(R.id.backArrow)
        backBtn.setOnClickListener{
            finish()
        }
        val budgetGoalViewModel = ViewModelProvider(this, factory)[BudgetGoalViewModel::class.java]
        val month = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())

        val sharedPref = getSharedPreferences("BudgetAppPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("LOGGED_USER_ID", null) ?: ""
        if (userId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Check for existing budget goals for current month
        budgetGoalViewModel.getBudgetGoalsByUserId(userId).observe(this) { budgetGoals ->
            val currentMonthGoals = budgetGoals?.filter { it.month == month }
            if (!currentMonthGoals.isNullOrEmpty()) {
                findViewById<TextView>(R.id.txtWarning).visibility = View.VISIBLE
            }
        }


        val btnSaveGoal = findViewById<Button>(R.id.btnSaveBudgetGoal)
        btnSaveGoal.setOnClickListener {
            val minimumBudget = findViewById<EditText>(R.id.edMinimumBudget).text.toString()
            val maximumBudget = findViewById<EditText>(R.id.budgetGoalInput).text.toString()

            if (maximumBudget.isNotEmpty() && minimumBudget.isNotEmpty()) {
                val budgetGoal = BudgetGoalEntity(
                    minAmount = minimumBudget.toDouble(),
                    maxAmount = maximumBudget.toDouble(),
                    month = month,
                    status = "Beginner",
                    userID = userId
                )

                budgetGoalViewModel.addBudgetGoal(budgetGoal).observe(this) { success ->
                    if (success) {
                        Toast.makeText(this, "Budget Goal Created", Toast.LENGTH_SHORT).show()
                        howtoViewModel.isHowtoCompleted(userId, this) { completed ->
                            val intent = if (!completed) {
                                Intent(this, HowToGetStarted::class.java)
                            } else {
                                Intent(this, Dashboard::class.java)
                            }
                            intent.putExtra("USER_ID", userId)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this, "Error Creating Budget Goal", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}