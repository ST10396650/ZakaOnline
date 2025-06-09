package com.example.zakazaka.Views

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zakazaka.Adapters.SubCategoryAdapter
import com.example.zakazaka.Data.Database.AppDatabase
import com.example.zakazaka.Models.SubCategoryEntity
import com.example.zakazaka.R
import com.example.zakazaka.Repository.AccountRepository
import com.example.zakazaka.Repository.BudgetGoalRepository
import com.example.zakazaka.Repository.CategoryRepository
import com.example.zakazaka.Repository.SubCategoryRepository
import com.example.zakazaka.Repository.TransactionRepository
import com.example.zakazaka.Repository.UserRepository
import com.example.zakazaka.ViewModels.CategoryViewModel
import com.example.zakazaka.ViewModels.SubCategoryViewModel
import com.example.zakazaka.ViewModels.TransactionViewModel
import com.example.zakazaka.ViewModels.ViewModelFactory
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class CategoryDetails : AppCompatActivity() {
    lateinit var subCategoryRecyclerView : RecyclerView
    lateinit var subCategoryAdapter : SubCategoryAdapter
    lateinit var categoryViewModel : CategoryViewModel
    lateinit var subCategoryViewModel : SubCategoryViewModel
    lateinit var transactionViewMode : TransactionViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
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
        val sharedPref = getSharedPreferences("BudgetAppPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("LOGGED_USER_ID", null) // Changed to getString
        val categoryID = intent.getStringExtra("categoryID") ?: "" // Changed to getStringExtra
        val btnBackBtn = findViewById<ImageView>(R.id.expenseSummaryBackBtn)
        btnBackBtn.setOnClickListener {
            finish()
        }

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        subCategoryViewModel = ViewModelProvider(this, factory)[SubCategoryViewModel::class.java]
        categoryViewModel = ViewModelProvider(this,factory)[CategoryViewModel::class.java]

        // Get category data using callback
        categoryViewModel.getCategoryById(categoryID) { category ->
            if(category != null){
                findViewById<TextView>(R.id.txtCategoryText).text = category.name
                findViewById<TextView>(R.id.txtCategoryBudget).text = "Budget : R${category.budgetLimit}"
                findViewById<TextView>(R.id.txtCategoryCurrentAmt).text = "Current : R${category.currentAmount}"
                findViewById<TextView>(R.id.txtRemainingBalance).text = "Remaining Balance : R${category.budgetLimit - category.currentAmount}"
            } else {
                // Add debug logging to see if category is null
                Log.d("CategoryDetails", "Category is null for ID: $categoryID")
                Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show()
            }
        }

        subCategoryRecyclerView = findViewById(R.id.subcategoryRecyclerView)
        subCategoryRecyclerView.layoutManager = LinearLayoutManager(this)

        // Observe subcategories using LiveData
        subCategoryViewModel.getSubCategoriesForCategory(categoryID).observe(this){ subCategories ->
            subCategoryAdapter = SubCategoryAdapter(subCategories){}
            subCategoryRecyclerView.adapter = subCategoryAdapter
        }

        //making add subcategory button visible
        val btnAddSubCategory = findViewById<Button>(R.id.btnAddSubCategory)
        btnAddSubCategory.setOnClickListener{
            findViewById<EditText>(R.id.edAddsubCatName).visibility = View.VISIBLE
            findViewById<EditText>(R.id.edAddsubCatBudget).visibility = View.VISIBLE
            findViewById<Button>(R.id.btnSaveSubCategory).visibility = View.VISIBLE
        }

        //saving the sub category to database.
        val btnSaveSubCategory = findViewById<Button>(R.id.btnSaveSubCategory)
        btnSaveSubCategory.setOnClickListener {
            val edAddsubCatName = findViewById<EditText>(R.id.edAddsubCatName)
            val edAddsubCatBudget = findViewById<EditText>(R.id.edAddsubCatBudget)
            if(edAddsubCatName.text.isNotEmpty() && edAddsubCatBudget.text.isNotEmpty()){
                val subcategory = SubCategoryEntity(
                    name = edAddsubCatName.text.toString(),
                    budgetLimit = edAddsubCatBudget.text.toString().toDouble(),
                    currentAmount = 0.0,
                    description = "",
                    categoryID = categoryID
                )
                subCategoryViewModel.createSubCategory(subcategory).observe(this){ cat ->
                    if(cat != null){
                        Toast.makeText(this,"SubCategory Successfully created", Toast.LENGTH_SHORT).show()
                        edAddsubCatName.text.clear()
                        edAddsubCatBudget.text.clear()
                        // Hide the input fields after successful creation
                        findViewById<EditText>(R.id.edAddsubCatName).visibility = View.GONE
                        findViewById<EditText>(R.id.edAddsubCatBudget).visibility = View.GONE
                        findViewById<Button>(R.id.btnSaveSubCategory).visibility = View.GONE
                    } else {
                        Toast.makeText(this,"Failed to create subcategory", Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this,"Please fill in all fields",Toast.LENGTH_LONG).show()
            }
        }

        transactionViewMode = ViewModelProvider(this,factory)[TransactionViewModel::class.java]

        var outputText :String = ""
        var categoryAmt :Double = 0.0
        val startDate = findViewById<EditText>(R.id.edStart)
        val endDate = findViewById<EditText>(R.id.edEnd)
        val datePickerStart = findViewById<DatePicker>(R.id.expStartDate)
        val datePickerEnd = findViewById<DatePicker>(R.id.expEndDate)

        startDate.setOnClickListener {
            datePickerStart.visibility = View.VISIBLE
            datePickerEnd.visibility = View.GONE
        }
        endDate.setOnClickListener {
            datePickerStart.visibility = View.GONE
            datePickerEnd.visibility = View.VISIBLE
        }
        val startYear = datePickerStart.year
        val startMonth = datePickerStart.month
        val startDay = datePickerStart.dayOfMonth


        val endYear = datePickerEnd.year
        val endMonth = datePickerEnd.month
        val endDay = datePickerEnd.dayOfMonth

        val backBtn = findViewById<Button>(R.id.expSummBackBtn)
        backBtn.setOnClickListener {
            finish()
        }
        datePickerStart.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
            startDate.setText(selectedDate)
        }
        datePickerEnd.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
            endDate.setText(selectedDate)
        }


        val btnCheckAmtBetweenDates = findViewById<Button>(R.id.btnCheckAmtBetweenDates)
        btnCheckAmtBetweenDates.setOnClickListener {
            findViewById<TextView>(R.id.txtOutput).visibility = View.VISIBLE
            val selectedStartDate = getDatePicker(datePickerStart.year,datePickerStart.month,datePickerStart.dayOfMonth)
            val selectedEndDate = getDatePicker(datePickerEnd.year,datePickerEnd.month,datePickerEnd.dayOfMonth)
            var subcats : List<String>? = null
                    if(startDate.text.isNotEmpty() && endDate.text.isNotEmpty()){
                        // Get subcategories for this category
                        subCategoryViewModel.getSubCategoriesForCategory(categoryID).observe(this){subcategories ->
                            if(!subcategories.isNullOrEmpty())
                                subcats = subcategories.map{it.subCategoryID}
                            // Get transactions between dates
                            transactionViewMode.getTransactionsBetweenDates(selectedStartDate, selectedEndDate).observe(this){transactions->
                                if(transactions != null && subcats != null) {
                                    val filteredTrans = transactions.filter { it.subCategoryID in subcats!! }
                                    categoryAmt = filteredTrans.sumOf { it.amount }
                                    findViewById<TextView>(R.id.txtOutput).text = "You've spent R${categoryAmt} in this category"
                                }
                            }
                        }
                    }
        }
    }
    fun getDatePicker(year: Int, month: Int, day: Int): Date {
        val caledar = Calendar.getInstance()
        caledar.set(Calendar.YEAR,year)
        caledar.set(Calendar.MONTH,month)
        caledar.set(Calendar.DAY_OF_MONTH,day)
        return caledar.time

    }
}