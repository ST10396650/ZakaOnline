package com.example.zakazaka.Views

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.zakazaka.Data.Database.AppDatabase
import com.example.zakazaka.Models.CategoryEntity
import com.example.zakazaka.Models.SubCategoryEntity
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
import com.example.zakazaka.ViewModels.SubCategoryViewModel
import com.example.zakazaka.ViewModels.ViewModelFactory
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.widget.ImageView


class CreateCategory : AppCompatActivity() {
    private var firebaseUserId: String? = null
    private lateinit var howtoViewModel: HowToViewModel
    lateinit var categoryViewModel : CategoryViewModel
    lateinit var subCategoryViewModel: SubCategoryViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_category)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        howtoViewModel = HowToViewModel()

        val sharedPref = getSharedPreferences("BudgetAppPrefs", MODE_PRIVATE)
        firebaseUserId = sharedPref.getString("LOGGED_USER_ID", null)

        if (firebaseUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        //initialize the ViewModelFactory
        val factory = ViewModelFactory(
            UserRepository(),
            AccountRepository(),
            BudgetGoalRepository(),
            CategoryRepository(),
            SubCategoryRepository(),
            TransactionRepository()
        )
        categoryViewModel = ViewModelProvider(this, factory)[CategoryViewModel::class.java]
        subCategoryViewModel = ViewModelProvider(this, factory)[SubCategoryViewModel::class.java]

        val btnCreateCategory = findViewById<Button>(R.id.btnCategory)

        val backBtn = findViewById<ImageView>(R.id.backArrowbtn)
        backBtn.setOnClickListener {
            finish()
        }

        //use the button click listener events to call the methods repsonsible for adding a new category
        btnCreateCategory.setOnClickListener {
            val edCategoryName = findViewById<EditText>(R.id.edCategoryName).text.toString()
            val edBudgetLimit = findViewById<EditText>(R.id.edCategoryLimit).text.toString()
            if (edCategoryName.isEmpty() || edBudgetLimit.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()

            } else {
                setupCategoryCreation(edCategoryName, edBudgetLimit.toDouble())
            }
        }

    }



    private fun setupCategoryCreation(categoryName:String, budgetLimit:Double) {

        try {
            val newCategory = CategoryEntity(
                name = categoryName,
                budgetLimit = budgetLimit,
                currentAmount = 0.0,
                userID = firebaseUserId ?: ""
            )

            val createdCategoryLiveData = categoryViewModel.createCategoryAndReturn(newCategory)

            createdCategoryLiveData.observe(this) { category ->
                if (category != null) {
                    Toast.makeText(this, "Category created successfully", Toast.LENGTH_SHORT).show()

                    // Save the categoryID as String
                    val sharedPref = getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("CREATED_CATEGORY_ID", category.categoryID)
                        apply()
                    }

                    firebaseUserId?.let {
                        howtoViewModel.isHowtoCompleted(it,this) { completed ->
                            val intent = if (!completed) {
                                Intent(this, HowToGetStarted::class.java)
                            } else {
                                Intent(this, Dashboard::class.java)
                            }
                            intent.putExtra("USER_ID", firebaseUserId)
                            startActivity(intent)
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to create category", Toast.LENGTH_SHORT).show()
                }
                createdCategoryLiveData.removeObservers(this)
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter a valid budget limit", Toast.LENGTH_SHORT).show()
        }
    }



//    private fun setupSubCategoryCreation(subCategoryName: String, budgetLimit: Double) {
//
//        try {
//            val sharedPrefs = getSharedPreferences("BudgetPrefs", MODE_PRIVATE)
//            val categoryId = sharedPrefs.getString("CREATED_CATEGORY_ID", null)
//
//            if (categoryId == null) {
//                Toast.makeText(this, "Category ID not found", Toast.LENGTH_SHORT).show()
//                return
//            }
//
//            val newSubCategory = SubCategoryEntity(
//                name = subCategoryName,
//                description = "",
//                budgetLimit = budgetLimit,
//                currentAmount = 0.0,
//                categoryID = categoryId
//            )
//
//            val createdSubCategoryLiveData = subCategoryViewModel.createSubCategory(newSubCategory)
//            createdSubCategoryLiveData.observe(this) { subCategory ->
//                if (subCategory != null) {
//                    Toast.makeText(this, "Subcategory created successfully", Toast.LENGTH_SHORT).show()
//                    // Save the categoryID as String
//
//                    howtoViewModel.isHowtoCompleted(firebaseUserId!!, this) { completed ->
//                        val intent = if (!completed) {
//                            Intent(this, Dashboard::class.java)
//                        } else {
//                            Intent(this, Dashboard::class.java)
//                        }
//                        intent.putExtra("USER_ID", firebaseUserId)
//                        startActivity(intent)
//                    }
//                } else {
//                    Toast.makeText(this, "Failed to create subcategory", Toast.LENGTH_SHORT).show()
//                }
//                createdSubCategoryLiveData.removeObservers(this)
//            }
//
//        } catch (e: NumberFormatException) {
//            Toast.makeText(this, "Please enter a valid budget limit", Toast.LENGTH_SHORT).show()
//        }
//    }
}

