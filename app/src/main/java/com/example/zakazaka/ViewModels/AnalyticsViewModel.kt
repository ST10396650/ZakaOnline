package com.example.zakazaka.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
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
import java.util.Date
import javax.inject.Inject

class AnalyticsViewModel @Inject constructor(

    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubCategoryRepository,
    private val transactionRepository: TransactionRepository
) {
    lateinit var allSubCategories: MutableLiveData<List<SubCategoryEntity>>
    private val _graphData = MutableLiveData<List<TransactionEntity>>()


    fun filterAnalyticsData(startDate: Date, endDate:Date, userId: String) :LiveData<List<AnalyticsModel>> {
        // Implement logic to fetch and process analytics data based on date range

            val categoriesLiveData = categoryRepository.getCategoriesByUserId(userId)
            val transactionsList = transactionRepository.getTransactionsBetweenDates(startDate, endDate){transactions ->
                _graphData.value = transactions
            }
            val result = MediatorLiveData<List<AnalyticsModel>>()

            result.addSource(categoriesLiveData) { categories ->
                val transaction = _graphData.value
                if (transaction != null) {
                    val analyticsLiveData = processAnalyticsData(categories, transaction, userId)
                    analyticsLiveData.observeForever { analyticsData ->
                        result.value = analyticsData
                    }
                }
            }
            result.addSource(_graphData) { transactions ->
                val categories = categoriesLiveData.value
                if (categories != null) {
                    val anayticsLiveData = processAnalyticsData(categories, transactions, userId)
                    anayticsLiveData.observeForever { analyticsData ->
                        result.value = analyticsData
                    }
                }
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
                    // --- Perform analytics calculation ---
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

}