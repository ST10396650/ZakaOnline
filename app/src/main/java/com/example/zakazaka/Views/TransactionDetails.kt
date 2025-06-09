package com.example.zakazaka.Views

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
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
import com.example.zakazaka.ViewModels.CategoryViewModel
import com.example.zakazaka.ViewModels.SubCategoryViewModel
import com.example.zakazaka.ViewModels.TransactionViewModel
import com.example.zakazaka.ViewModels.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionDetails : AppCompatActivity() {
    lateinit var transactionViewModel : TransactionViewModel
    lateinit var subCategoryViewModel : SubCategoryViewModel
    lateinit var categoryViewModel : CategoryViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction_details)
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
        val transactionId = intent.getStringExtra("TRANSACTION_ID") ?: ""
        transactionViewModel = ViewModelProvider(this,factory)[TransactionViewModel::class.java]
        subCategoryViewModel = ViewModelProvider(this,factory)[SubCategoryViewModel::class.java]
        categoryViewModel = ViewModelProvider(this,factory)[CategoryViewModel::class.java]

        val backBtn = findViewById<ImageView>(R.id.backArrow)
        backBtn.setOnClickListener {
            finish()
        }

        // Load transaction details
        loadTransactionDetails(transactionId)

    }

    private fun loadTransactionDetails(transactionId: String) {
        transactionViewModel.getTransactionById(transactionId) { transaction ->
            if (transaction != null) {
                // Set basic transaction details
                findViewById<TextView>(R.id.txtTransDescription).text = transaction.description
                findViewById<TextView>(R.id.txtTransAmount).text = "R${String.format("%.2f", transaction.amount)}"
                findViewById<TextView>(R.id.txtDateOfTransaction).text = "Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(transaction.date)}"
                findViewById<TextView>(R.id.txtType).text = "Type: ${transaction.type}"
                findViewById<TextView>(R.id.txtRecurring).text = "Recurring: ${transaction.repeat}"


                // Set image if available
                if (!transaction.imagePath.isNullOrEmpty()) {
                    findViewById<ImageView>(R.id.imgReceipt).setImageURI(Uri.parse(transaction.imagePath))
                }

                // Load subcategory and category details
                loadCategoryDetails(transaction.subCategoryID)
            } else {
                Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show()
                Log.d("TransactionDetails", "Transaction is null for ID: $transactionId")
            }
        }
    }

    private fun loadCategoryDetails(subCategoryID: String) {
        subCategoryViewModel.getSubCategoryById(subCategoryID) { subCategory ->
            if (subCategory != null) {
                val subCatName = subCategory.name
                val catId = subCategory.categoryID

                // Now get the category
                categoryViewModel.getCategoryById(catId) { category ->
                    if (category != null) {
                        val categoryString = "${category.name} -> $subCatName"
                        findViewById<TextView>(R.id.txtCategory).text = categoryString
                        Log.d("TransactionDetails", "Category set to: $categoryString")
                    } else {
                        findViewById<TextView>(R.id.txtCategory).text = "Category: $subCatName"
                        Log.d("TransactionDetails", "Category is null, showing subcategory only")
                    }
                }
            } else {
                findViewById<TextView>(R.id.txtCategory).text = "Category: Unknown"
                Log.d("TransactionDetails", "SubCategory is null for ID: $subCategoryID")
            }
        }
    }
}
