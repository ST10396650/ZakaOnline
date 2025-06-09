package com.example.zakazaka.ViewModels

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.zakazaka.Models.AnalyticsModel
import com.example.zakazaka.Models.CategoryEntity
import com.example.zakazaka.Models.SubCategoryEntity
import com.example.zakazaka.Models.TransactionEntity
import com.example.zakazaka.Repository.AccountRepository
import com.example.zakazaka.Repository.BudgetGoalRepository
import com.example.zakazaka.Repository.CategoryRepository
import com.example.zakazaka.Repository.SubCategoryRepository
import com.example.zakazaka.Repository.TransactionRepository
import com.example.zakazaka.Repository.UserRepository
import com.github.mikephil.charting.charts.BarChart
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import javax.inject.Inject

class AnalyticsViewModel @Inject constructor(

    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubCategoryRepository,
    private val transactionRepository: TransactionRepository
) {
    lateinit var allSubCategories: MutableLiveData<List<SubCategoryEntity>>
    private val _graphData = MutableLiveData<List<TransactionEntity>>()

    //filtering analytics data (filtering transactions by date)
    fun filterAnalyticsData(startDate: Date, endDate:Date, userId: String) :LiveData<List<AnalyticsModel>?> {
        // Implement logic to fetch and process analytics data based on date range
            val transactionLd = MutableLiveData<List<TransactionEntity>>()
            val categoriesLiveData = categoryRepository.getCategoriesByUserId(userId)
            val result = MediatorLiveData<List<AnalyticsModel>?>()
            var currentTransactions : List<TransactionEntity>? = null
            var currentCategories : List<CategoryEntity>? = null
        transactionRepository.getTransactionsBetweenDates(startDate,endDate){ transactions->
            transactionLd.postValue(transactions)
        }
        fun tryEmitAnalytics() {
            val transactions = currentTransactions
            val categories = currentCategories
            if (transactions != null && categories != null) {
                val analyticsLiveData = processAnalyticsData(categories, transactions, userId)

                val observer = object : Observer<List<AnalyticsModel>?> {
                    override fun onChanged(analytics: List<AnalyticsModel>?) {
                        result.value = analytics ?: emptyList()
                        analyticsLiveData.removeObserver(this)
                    }
                }

                analyticsLiveData.observeForever(observer)
            }
        }

        result.addSource(transactionLd) { transactions ->
            currentTransactions = transactions
            tryEmitAnalytics()
        }
        result.addSource(categoriesLiveData) { categories ->
            currentCategories = categories
            tryEmitAnalytics()
        }


            return result

    }

    fun getDefaultAnalyticsData(userId: String) :LiveData<List<AnalyticsModel>> {
        // Implement logic to fetch and process analytics data
        val categoriesLiveData = categoryRepository.getCategoriesByUserId(userId)
        val transactionsLiveData = transactionRepository.getTransactionsByUserId(userId)
        val result = MediatorLiveData<List<AnalyticsModel>>()


        result.addSource(categoriesLiveData) { categories ->
            val transaction = transactionsLiveData.value
            if (transaction != null) {
                val analyticsLiveData = processAnalyticsData(categories, transaction, userId)
                analyticsLiveData.observeForever { analyticsData ->
                result.value = analyticsData
                }
            }
        }
        result.addSource(transactionsLiveData){transactions ->
            val categories = categoriesLiveData.value
            if(categories != null){
                val anayticsLiveData = processAnalyticsData(categories,transactions,userId)
                anayticsLiveData.observeForever { analyticsData ->
                    result.value = analyticsData
                }
            }
        }

        return result
    }
    //fun to make it easier to process the analytics data,to prevent memory leaks
    fun processAnalyticsData(categories: List<CategoryEntity>, transactions: List<TransactionEntity>, userId: String): LiveData<List<AnalyticsModel>> {
        val resultLiveData = MediatorLiveData<List<AnalyticsModel>>()
        val combinedSubCategories = mutableListOf<SubCategoryEntity>()

        var respondedSources = 0
        val totalSources = categories.size

        categories.forEach { category ->
            val subCategoryLiveData = subCategoryRepository.getSubCategoriesForCategory(category.categoryID)

            resultLiveData.addSource(subCategoryLiveData) { subCategories ->
                if (subCategories != null) {
                    combinedSubCategories.addAll(subCategories)
                }

                respondedSources++

                if (respondedSources == totalSources) {
                    //  Perform the analytic calculation
                    val subCategoryToCategoryMap = combinedSubCategories.associate { it.subCategoryID to it.categoryID }
                    val categoryToNameMap = categories.associate { it.categoryID to it.name }
                    val categoryTotals = mutableMapOf<String, Double>()

                    transactions.forEach { transaction ->
                        val categoryID = subCategoryToCategoryMap[transaction.subCategoryID]
                        if (categoryID != null) {
                            val currentTotal = categoryTotals.getOrDefault(categoryID, 0.0)
                            categoryTotals[categoryID] = currentTotal + transaction.amount
                        }
                    }

                    val analyticsList = categoryTotals.mapNotNull { (categoryID, totalAmount) ->
                        categoryToNameMap[categoryID]?.let { categoryName ->
                            AnalyticsModel(categoryName, totalAmount)
                        }
                    }

                    resultLiveData.value = analyticsList
                }

                // Prevent memory leaks
                resultLiveData.removeSource(subCategoryLiveData)
            }
        }

        return resultLiveData
    }
    //(GeeksforGeeks, 2022)
    fun downloadAnalyticsData(barGraph: BarChart, context: Context) {
        //creating the pdf document
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(barGraph.width, barGraph.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        barGraph.draw(page.canvas)
        pdfDocument.finishPage(page)
        //creating unique name for file
        val timestamp = System.currentTimeMillis()
        val fileName = "Analytics_$timestamp.pdf"
        //getting the file directory and saving it to the public storage
        val documentDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(documentDirectory, fileName)
        try{
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
                Toast.makeText(context,"Pdf document saved to: ${file.absolutePath}",Toast.LENGTH_SHORT).show()
            }
        }catch(e: IOException){
            e.printStackTrace()
            Toast.makeText(context,"Error saving pdf document",Toast.LENGTH_SHORT).show()
        }finally {
            pdfDocument.close()

        }
    }
}
//GeeksforGeeks (2022). How to Build PDF Downloader App in Android with Kotlin? [online] GeeksforGeeks. Available at: https://www.geeksforgeeks.org/how-to-build-pdf-downloader-app-in-android-with-kotlin/ [Accessed 7 Jun. 2025].