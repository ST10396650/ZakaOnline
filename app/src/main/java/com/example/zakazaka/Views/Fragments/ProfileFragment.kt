package com.example.zakazaka.Views.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.lifecycle.ViewModelProvider
import com.example.zakazaka.Data.Database.AppDatabase
import com.example.zakazaka.R
import com.example.zakazaka.Repository.AccountRepository
import com.example.zakazaka.Repository.BudgetGoalRepository
import com.example.zakazaka.Repository.CategoryRepository
import com.example.zakazaka.Repository.SubCategoryRepository
import com.example.zakazaka.Repository.TransactionRepository
import com.example.zakazaka.Repository.UserRepository
import com.example.zakazaka.ViewModels.BudgetGoalViewModel
import com.example.zakazaka.ViewModels.LoginRegistrationViewModel
import com.example.zakazaka.ViewModels.ViewModelFactory
import com.example.zakazaka.Views.AccountActivity
import com.example.zakazaka.Views.AddAccountActivity
import com.example.zakazaka.Views.MilestoneActivity


class ProfileFragment : Fragment() {
    lateinit var budgetGoalViewModel: BudgetGoalViewModel
    lateinit var loginRegistrationViewModel: LoginRegistrationViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_user, container, false)
        val btnAddAcc = view.findViewById<Button>(R.id.btnAddAccountP)
        btnAddAcc.setOnClickListener {
            val intent = Intent(activity, AccountActivity::class.java)
            startActivity(intent)
        }
        return view
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
        // Changed to getString since IDs are now strings for Firebase
        val userId = sharedPref.getString("LOGGED_USER_ID", null)


        budgetGoalViewModel = ViewModelProvider(this, factory)[BudgetGoalViewModel::class.java]
        loginRegistrationViewModel = ViewModelProvider(this, factory)[LoginRegistrationViewModel::class.java]

        // Only proceed if userId is not null
        userId?.let { uid ->
            budgetGoalViewModel.getBudgetGoalsByUserId(uid).observe(viewLifecycleOwner) { budgetGoals ->
                val latestBudgetGoals = budgetGoals.takeLast(1)
                latestBudgetGoals.forEach { budgetGoal ->
                    view.findViewById<EditText>(R.id.edMaxBud).setText(budgetGoal.maxAmount.toString())
                    view.findViewById<EditText>(R.id.edMinBud).setText(budgetGoal.minAmount.toString())
                }
            }

            loginRegistrationViewModel.getUserById(uid).observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    val fullName = user.firstName + " " + user.lastName
                    view.findViewById<EditText>(R.id.user_name).setText("${fullName}")
                    view.findViewById<EditText>(R.id.user_email).setText(user.email)
                } else {
                    view.findViewById<EditText>(R.id.user_name).setText("User Name")
                    view.findViewById<EditText>(R.id.user_email).setText("Email")
                }
            }
        }

        val btnAddbudGoal = view.findViewById<Button>(R.id.btnAddBudgetGoal)
        btnAddbudGoal.setOnClickListener {
            val intent = Intent(activity, MilestoneActivity::class.java)
            startActivity(intent)
        }
    }
}