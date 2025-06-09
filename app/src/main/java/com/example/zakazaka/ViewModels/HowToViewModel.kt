package com.example.zakazaka.ViewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.zakazaka.Models.AccountEntity
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.Observer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HowToViewModel() : ViewModel() {
    var budgetGoalViewModel: BudgetGoalViewModel? = null
    var accountViewModel: AccountViewModel? = null
    var categoryViewModel: CategoryViewModel? = null

    private val databaseRef = FirebaseDatabase.getInstance().getReference("Users")
    private val auth = FirebaseAuth.getInstance()

    //main function that handles is called when the user logs in.
    // Main function that handles tutorial completion check
    fun isHowtoCompleted(
        userId: String,
        lifecycleOwner: LifecycleOwner,
        callback: (Boolean) -> Unit
    ) {
        // Use coroutines to avoid callback chaos
        var accountComplete = false
        var categoryComplete = false
        var budgetComplete = false
        var completedChecks = 0

        val totalChecks = 3

        fun checkIfAllComplete() {
            completedChecks++
            if (completedChecks == totalChecks) {
                val allComplete = accountComplete && categoryComplete && budgetComplete
                callback(allComplete)
            }
        }

        // Check account
        checkForAccount(userId, lifecycleOwner) { hasAccount ->
            accountComplete = hasAccount
            checkIfAllComplete()
        }

        // Check category
        checkForCategory(userId, lifecycleOwner) { hasCategory ->
            categoryComplete = hasCategory
            checkIfAllComplete()
        }

        // Check budget goal
        checkForBudgetGoal(userId, lifecycleOwner) { hasBudgetGoal ->
            budgetComplete = hasBudgetGoal
            checkIfAllComplete()
        }
    }

    private fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: Observer<T>) {
        observe(owner, object : Observer<T> {
            override fun onChanged(value: T) {
                observer.onChanged(value)
                removeObserver(this)
            }
        })
    }

    fun checkForAccount(
        userId: String,
        lifecycleOwner: LifecycleOwner,
        callback: (Boolean) -> Unit
    ) {
        try {
            val viewModel = accountViewModel
            if (viewModel == null) {
                Log.e("HowToViewModel", "AccountViewModel is not initialized")
                callback(false)
                return
            }

            viewModel.getAccountsByUserId(userId).observeOnce(lifecycleOwner) { accounts ->
                val hasAccounts = !accounts.isNullOrEmpty()
                callback(hasAccounts)
            }
        } catch (e: Exception) {
            Log.e("HowToViewModel", "Error checking for account: ${e.message}", e)
            callback(false)
        }
    }

    // Check if user has categories using ViewModel pattern (same as account)
    fun checkForCategory(
        userId: String,
        lifecycleOwner: LifecycleOwner,
        callback: (Boolean) -> Unit
    ) {
        try {
            val viewModel = categoryViewModel
            if (viewModel == null) {
                Log.e("HowToViewModel", "CategoryViewModel is not initialized")
                callback(false)
                return
            }

            viewModel.getCategoriesByUserId(userId).observeOnce(lifecycleOwner) { categories ->
                val hasCategories = !categories.isNullOrEmpty()
                callback(hasCategories)
            }
        } catch (e: Exception) {
            Log.e("HowToViewModel", "Error checking for categories: ${e.message}", e)
            callback(false)
        }
    }

    fun checkForBudgetGoal(
        userId: String,
        lifecycleOwner: LifecycleOwner,
        callback: (Boolean) -> Unit
    ) {
        try {
            val viewModel = budgetGoalViewModel
            if (viewModel == null) {
                Log.e("HowToViewModel", "BudgetGoalViewModel is not initialized")
                callback(false)
                return
            }

            Log.d("HowToViewModel", "Checking budget goals for user: $userId")

            viewModel.getBudgetGoalsByUserId(userId).observeOnce(lifecycleOwner) { budgetGoals ->
                val hasBudgetGoals = !budgetGoals.isNullOrEmpty()
                Log.d("HowToViewModel", "Budget goals found: ${budgetGoals?.size ?: 0}, hasBudgetGoals: $hasBudgetGoals")

                // Debug: Print each budget goal
                budgetGoals?.forEach { goal ->
                    Log.d("HowToViewModel", "Budget Goal: ID=${goal.budgetGoalID}, UserID=${goal.userID}, Month=${goal.month}")
                }

                callback(hasBudgetGoals)
            }
        } catch (e: Exception) {
            Log.e("HowToViewModel", "Error checking for budget goals: ${e.message}", e)
            callback(false)
        }
    }
    override fun onCleared() {
        super.onCleared()
        // Clean up any resources if needed
    }
}