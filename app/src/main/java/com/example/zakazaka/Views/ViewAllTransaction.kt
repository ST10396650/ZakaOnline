package com.example.zakazaka.Views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.example.zakazaka.ViewModels.TransactionViewModel
import com.example.zakazaka.ViewModels.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ViewAllTransaction : AppCompatActivity() {
    lateinit var transactionAdapter : TransactionAdapter
    lateinit var transactionViewModel : TransactionViewModel
    lateinit var transRecyclerView : RecyclerView
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_all_transaction)
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

        // Get user ID from SharedPreferences (now as String)
        val sharedPref = getSharedPreferences("BudgetAppPrefs", MODE_PRIVATE)
        currentUserId = sharedPref.getString("LOGGED_USER_ID", null)

        // Check if user is logged in
        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val btnBackBtn = findViewById<ImageView>(R.id.transactionBackBtn)
        btnBackBtn.setOnClickListener {
            finish()
        }

        transactionViewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        transRecyclerView = findViewById(R.id.transactionsRecyclerView)
        transRecyclerView.layoutManager = LinearLayoutManager(this)

        // Observe all transactions
        transactionViewModel.getAllTransactions().observe(this) { transactions ->
            if (transactions != null) {
                // Apply business logic - sort by date (newest first)
                val sortedTransactions = transactionViewModel.sortTransactionsByDate(transactions)

                // Setup adapter with click listener
                transactionAdapter = TransactionAdapter(sortedTransactions) { transaction ->
                    val intent = Intent(this, TransactionDetails::class.java)
                    intent.putExtra("TRANSACTION_ID", transaction.transactionID)
                    startActivity(intent)
                }
                transRecyclerView.adapter = transactionAdapter

                // Show empty state if no transactions
                if (transactions.isEmpty()) {
                    Toast.makeText(this, "No transactions found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Failed to load transactions", Toast.LENGTH_LONG).show()
            }
        }

        val btnViewAllTransaction = findViewById<Button>(R.id.btnViewAllTransations)
        btnViewAllTransaction.setOnClickListener {
            transactionViewModel.getAllTransactions().observe(this) { transactions ->
                if (transactions != null) {
                    // Apply business logic - sort by date (newest first)
                    val sortedTransactions = transactionViewModel.sortTransactionsByDate(transactions)

                    // Setup adapter with click listener
                    transactionAdapter = TransactionAdapter(sortedTransactions) { transaction ->
                        val intent = Intent(this, TransactionDetails::class.java)
                        intent.putExtra("TRANSACTION_ID", transaction.transactionID)
                        startActivity(intent)
                    }
                    transRecyclerView.adapter = transactionAdapter

                    // Show empty state if no transactions
                    if (transactions.isEmpty()) {
                        Toast.makeText(this, "No transactions found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Failed to load transactions", Toast.LENGTH_LONG).show()
                }
            }
        }

        val btnSort = findViewById<Button>(R.id.sortButton)
        val sDate = findViewById<DatePicker>(R.id.startDatePicker)
        val eDate = findViewById<DatePicker>(R.id.endDatePicker)

        val startDateStr = findViewById<EditText>(R.id.startDate)
        val endDateStr = findViewById<EditText>(R.id.endDate)

        startDateStr.setOnClickListener{
            sDate.visibility = android.view.View.VISIBLE
            eDate.visibility = android.view.View.GONE
        }
        endDateStr.setOnClickListener{
            sDate.visibility = android.view.View.GONE
            eDate.visibility = android.view.View.VISIBLE
        }
        sDate.setOnDateChangedListener {_,year,month,dayOfMonth ->
            val selectedDate = "${month+1}/$dayOfMonth/$year"
            startDateStr.setText(selectedDate)
        }
        eDate.setOnDateChangedListener {_,year,month,dayOfMonth ->
            val selectedDate = "${month+1}/$dayOfMonth/$year"
            endDateStr.setText(selectedDate)
        }
        btnSort.setOnClickListener {

            val startYear = sDate.year
            val startMonth = sDate.month
            val startDay = sDate.dayOfMonth
            val startDate = getDatePicker(startYear,startMonth,startDay)

            val endYear = eDate.year
            val endMonth = eDate.month
            val endDay = eDate.dayOfMonth
            val endDate = getDatePicker(endYear,endMonth,endDay)

            eDate.visibility = View.GONE
            sDate.visibility = View.GONE
            startDateStr.setText("")
            endDateStr.setText("")
//            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//            format.isLenient = false
//
//            try {
//                val startDate = format.parse(startDateStr)
//                val endDate = format.parse(endDateStr)
//
//                if (startDate != null && endDate != null) {
//                    // Validate date range
//                    if (startDate.after(endDate)) {
//                        Toast.makeText(this, "Start date cannot be after end date", Toast.LENGTH_LONG).show()
//                        return@setOnClickListener
//                    }
                if(startDate <= endDate) {
                    // Observe filtered transactions
                    transactionViewModel.getTransactionsBetweenDates(startDate, endDate)
                        .observe(this) { filteredTransactions ->
                            if (filteredTransactions != null) {
                                // Apply business logic - sort filtered results
                                val sortedFilteredTransactions =
                                    transactionViewModel.sortTransactionsByDate(filteredTransactions)

                                // Update adapter with filtered results
                                transactionAdapter =
                                    TransactionAdapter(sortedFilteredTransactions) { transaction ->
                                        val intent = Intent(this, TransactionDetails::class.java)
                                        intent.putExtra("TRANSACTION_ID", transaction.transactionID)
                                        startActivity(intent)
                                    }
                                transRecyclerView.adapter = transactionAdapter

                                // Show result count
//                                val resultCount = filteredTransactions.size
//                                Toast.makeText(
//                                    this,
//                                    "Found $resultCount transactions",
//                                    Toast.LENGTH_SHORT
//                                ).show()

                                // Calculate and display total amount for filtered transactions
                                val totalAmount =
                                    transactionViewModel.calculateTotalAmount(filteredTransactions)
                                // You can display this in a TextView if needed
                                // totalAmountTextView.text = "Total: $totalAmount"

                            } else {
                                Toast.makeText(
                                    this,
                                    "No transactions found for selected dates",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                }else{
                    Toast.makeText(this,"Start date cannot be after end date",Toast.LENGTH_LONG).show()
                }
//                } else {
//                    Toast.makeText(this, "Invalid date format", Toast.LENGTH_LONG).show()
//                }
//            } catch (e: Exception) {
//                Toast.makeText(this, "Invalid date format. Please use dd/MM/yyyy", Toast.LENGTH_LONG).show()
//            }
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
