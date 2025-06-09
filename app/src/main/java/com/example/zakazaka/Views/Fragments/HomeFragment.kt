package com.example.zakazaka.Views.Fragments

import android.content.ContentValues.TAG
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zakazaka.Adapters.TransactionAdapter
import com.example.zakazaka.Data.Database.AppDatabase
import com.example.zakazaka.R
import com.example.zakazaka.Repository.AccountRepository
import com.example.zakazaka.Repository.BudgetGoalRepository
import com.example.zakazaka.Repository.CategoryRepository
import com.example.zakazaka.Repository.SubCategoryRepository
import com.example.zakazaka.Repository.TransactionRepository
import com.example.zakazaka.Repository.UserRepository
import com.example.zakazaka.ViewModels.BudgetGoalViewModel
import com.example.zakazaka.ViewModels.TransactionViewModel
import com.example.zakazaka.ViewModels.ViewModelFactory
import com.example.zakazaka.Views.AccountActivity

class HomeFragment : Fragment() {
    private lateinit var recentTransactionRecyclerView: RecyclerView
    private lateinit var transactionAdpater : TransactionAdapter
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var budgetGoalViewModel: BudgetGoalViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext().applicationContext
        val factory = ViewModelFactory(
            UserRepository(),
            AccountRepository(),
            BudgetGoalRepository(),
            CategoryRepository(),
            SubCategoryRepository(),
            TransactionRepository()
        )

        val sharedPref = requireContext().getSharedPreferences("BudgetAppPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("LOGGED_USER_ID", null)

        transactionViewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        budgetGoalViewModel = ViewModelProvider(this, factory)[BudgetGoalViewModel::class.java]

        //handles the recycler view for transactions showing the lastests 2 transactions
        recentTransactionRecyclerView = view.findViewById(R.id.recentTransactionRecyclerView)
        recentTransactionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val minProgressbar = view.findViewById<ProgressBar>(R.id.userProgressBar)
        val maxProgressbar = view.findViewById<ProgressBar>(R.id.userMaxProgressBar)
        // Only proceed if userId is not null
        userId?.let { uid ->
//            // First, observe transactions for the RecyclerView (this can work independently)
//            transactionViewModel.getTransactionsByUserId(uid).observe(viewLifecycleOwner) { transactions ->
//                val latestTransactions = transactions.sortedByDescending { it.date }.take(3)
//                transactionAdpater = TransactionAdapter(latestTransactions) { transaction ->
//                    Toast.makeText(requireContext(), "Clicked on ${transaction.description}", Toast.LENGTH_SHORT).show()
//                }
//                recentTransactionRecyclerView.adapter = transactionAdpater
//            }

            // First, observe transactions for the RecyclerView (this can work independently)
            var totalAmount : Double = 0.0
            var minBudget : Double = 0.0
            var maxBudget : Double = 0.0


            budgetGoalViewModel.getBudgetGoalsByUserId(userId).observe(viewLifecycleOwner) { budGoals ->
                val lastBudgetGoal = budGoals.takeLast(1).firstOrNull()
                if(lastBudgetGoal != null){
                    minBudget = lastBudgetGoal.minAmount
                    maxBudget = lastBudgetGoal.maxAmount
                }
            }

            transactionViewModel.getTransactionsByUserId(uid).observe(viewLifecycleOwner) { transactions ->
                Log.d(TAG, "RecyclerView - Received ${transactions.size} transactions")

                val latestTransactions = transactions.sortedByDescending { it.date }.take(3)
                transactionAdpater = TransactionAdapter(latestTransactions) { transaction ->
                    Toast.makeText(requireContext(), "Clicked on ${transaction.description}", Toast.LENGTH_SHORT).show()
                }
                recentTransactionRecyclerView.adapter = transactionAdpater

                //filling the progress bars
                totalAmount = transactions.sumOf{it.amount}
                minProgressbar.progress = (totalAmount/minBudget   * 100).toInt()
                maxProgressbar.progress = (totalAmount/maxBudget * 100).toInt()
            }

            // Now observe budget goals and handle transactions inside this observer
            budgetGoalViewModel.getBudgetGoalsByUserId(uid).observe(viewLifecycleOwner) { budgetGoals ->
                if (budgetGoals.isNotEmpty()) {
                    // Get the latest budget goal
                    val latestBudgetGoal = budgetGoals.takeLast(1).firstOrNull()

                    latestBudgetGoal?.let { budgetGoal ->
                        val budgetGoalAmt = budgetGoal.maxAmount

                        // Update the budget display
                        view.findViewById<TextView>(R.id.txtTotalMonthlyBudget).text =
                            "Total Monthly Budget R${budgetGoalAmt}"

                        // Now observe transactions and calculate remaining/spent amounts
                        transactionViewModel.getTransactionsByUserId(uid).observe(viewLifecycleOwner) { transactions ->
                            Log.d(TAG, "Calculating amounts with ${transactions.size} transactions")

                            // Log each transaction for debugging
                            transactions.forEachIndexed { index, transaction ->
                                Log.d(TAG, "Transaction $index: ${transaction.description} - Amount: ${transaction.amount} - Type: ${transaction.type}")
                            }

                            val totalSpent = transactions.sumOf { it.amount }
                            val remainingAmt = budgetGoalAmt - totalSpent

                            Log.d(TAG, "Calculation Results:")
                            Log.d(TAG, "  Total Spent: $totalSpent")
                            Log.d(TAG, "  Budget Goal: $budgetGoalAmt")
                            Log.d(TAG, "  Remaining: $remainingAmt")

                            // Update UI with calculated values
                            view.findViewById<TextView>(R.id.txtAvailableRemaining).text =
                                "Available Remaining R${String.format("%.2f", remainingAmt)}"

                            view.findViewById<TextView>(R.id.txtAmountSpent).text =
                                "You have spent\nR${String.format("%.2f", totalSpent)}"

                            Log.d(TAG, "UI updated successfully")
                        }
                    }
                } else {
                    // Handle case when no budget goals exist
                    view.findViewById<TextView>(R.id.txtTotalMonthlyBudget).text =
                        "No Budget Set"
                    view.findViewById<TextView>(R.id.txtAvailableRemaining).text =
                        "Available Remaining R0.00"
                    view.findViewById<TextView>(R.id.txtAmountSpent).text =
                        "You have spent\nR0.00"
                }
            }
        }

        val btnDigitalAccount = view.findViewById<Button>(R.id.btnDigitalAccount)
        btnDigitalAccount.setOnClickListener {
            val intent = Intent(activity, AccountActivity::class.java)
            startActivity(intent)
        }
    }
}