package com.example.zakazaka.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.zakazaka.Data.Database.CategoryDao
import com.example.zakazaka.Models.CategoryEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class CategoryRepository() {

    private val firebaseDatabase = FirebaseDatabase.getInstance().getReference("Category")
    private val auth = FirebaseAuth.getInstance()

    fun createCategory(category: CategoryEntity, callback: (CategoryEntity?) -> Unit) {
        val userId = auth.currentUser?.uid ?: run {
            callback(null)
            return
        }

        val categoryId = firebaseDatabase.child(userId).push().key
        if (categoryId == null) {
            callback(null)
            return
        }

        val categoryMap = mapOf(
            "categoryID" to categoryId,
            "name" to category.name,
            "budgetLimit" to category.budgetLimit,
            "currentAmount" to category.currentAmount,
            "userID" to userId
        )

        firebaseDatabase.child(userId).child(categoryId).setValue(categoryMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(category.copy(categoryID = categoryId))
                } else {
                    callback(null)
                }
            }
    }

    // Get categories by user ID
    fun getCategoriesByUserId(userId: String): LiveData<List<CategoryEntity>> {
        val categoriesLiveData = MutableLiveData<List<CategoryEntity>>()

        // Add logging to see the Firebase path
        Log.d("CategoryRepository", "Fetching categories for userId: $userId")

        firebaseDatabase.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("CategoryRepository", "onDataChange called. Snapshot exists: ${snapshot.exists()}")
                Log.d("CategoryRepository", "Snapshot children count: ${snapshot.childrenCount}")

                val categories = mutableListOf<CategoryEntity>()

                for (categorySnapshot in snapshot.children) {
                    Log.d("CategoryRepository", "Processing category: ${categorySnapshot.key}")
                    Log.d("CategoryRepository", "Category data: ${categorySnapshot.value}")

                    val categoryData = categorySnapshot.value as? Map<String, Any>
                    if (categoryData != null) {
                        // Add detailed logging for each field
                        val categoryID = categoryData["categoryID"] as? String ?: ""
                        val name = categoryData["name"] as? String ?: ""
                        val budgetLimit = when (val limit = categoryData["budgetLimit"]) {
                            is Number -> limit.toDouble()
                            is String -> limit.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                        val currentAmount = when (val amount = categoryData["currentAmount"]) {
                            is Number -> amount.toDouble()
                            is String -> amount.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                        val userID = categoryData["userID"] as? String ?: ""

                        Log.d("CategoryRepository", "Parsed - ID: $categoryID, Name: $name, Budget: $budgetLimit, Current: $currentAmount, UserID: $userID")

                        val category = CategoryEntity(
                            categoryID = categoryID,
                            name = name,
                            budgetLimit = budgetLimit,
                            currentAmount = currentAmount,
                            userID = userID
                        )
                        categories.add(category)
                    } else {
                        Log.w("CategoryRepository", "Category data is null for snapshot: ${categorySnapshot.key}")
                    }
                }

                Log.d("CategoryRepository", "Total categories parsed: ${categories.size}")
                categoriesLiveData.value = categories
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CategoryRepository", "Database error: ${error.message}")
                categoriesLiveData.value = emptyList()
            }
        })

        return categoriesLiveData
    }
    // Update category current amount
    fun updateCategoryCurrentAmount(categoryId: String, additionalAmount: Double, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(false)
            return
        }

        firebaseDatabase.child(userId).child(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryData = snapshot.value as? Map<String, Any>
                if (categoryData != null) {
                    val currentAmount = when (val current = categoryData["currentAmount"]) {
                        is Double -> current
                        is Long -> current.toDouble()
                        is String -> current.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                    
                     val newAmount = currentAmount + additionalAmount

                    firebaseDatabase.child(userId).child(categoryId).child("currentAmount").setValue(newAmount)
                        .addOnCompleteListener { task ->
                            callback(task.isSuccessful)
                        }
                } else {
                    callback(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }


    fun getCategoryById(categoryId: String, callback: (CategoryEntity?) -> Unit) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            callback(null)
            return
        }

        firebaseDatabase.child(userId)
            .orderByChild("categoryID")
            .equalTo(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var foundCategory: CategoryEntity? = null

                    for (categorySnapshot in snapshot.children) {
                        val categoryData = categorySnapshot.value as? Map<String, Any>
                        if (categoryData != null) {
                            // Better handling of numeric values from Firebase
                            val budgetLimit = when (val budget = categoryData["budgetLimit"]) {
                                is Double -> budget
                                is Long -> budget.toDouble()
                                is String -> budget.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }

                            val currentAmount = when (val current = categoryData["currentAmount"]) {
                                is Double -> current
                                is Long -> current.toDouble()
                                is String -> current.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }

                            val category = CategoryEntity(
                                categoryID = categoryData["categoryID"] as? String ?: "",
                                name = categoryData["name"] as? String ?: "",
                                budgetLimit = budgetLimit,
                                currentAmount = currentAmount,
                                userID = categoryData["userID"] as? String ?: ""
                            )

                            foundCategory = category
                            break // Since we only need one category, break after finding it
                        }
                    }

                    callback(foundCategory)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    callback(null)
                }
            })
    }
//    // getting all users from the database
//    // LiveData will automatically update when data changes
//    val readAllData: LiveData<List<CategoryEntity>> = categoryDao.readAllData()
//
//     fun getCategoryById(categoryId: Long): CategoryEntity? {
//        return categoryDao.getCategoryById(categoryId)
//    }
//
//    //Inserts a new category into the database
//    suspend fun createCategory(categoryEntity: CategoryEntity): Long {
//        return categoryDao.insert(categoryEntity)
//    }
//
//    //getting all categories for a specific user
//    suspend fun getCategory(userId: Long): List<CategoryEntity> {
//        return categoryDao.getUserCategories(userId)
//    }
//
//    //getting the categories where the current amount goes over the budget limit
//    suspend fun getOverBudgetCategory(userId: Long): List<CategoryEntity> {
//        return categoryDao.getOverBudgetCategories(userId)
//    }
//
//    // updating data inside a category entity
//    suspend fun updateCategory(category: CategoryEntity): Int {
//        return categoryDao.updateCategory(category)
//    }
//
//    // updating the current amount for a specific category
//    suspend fun updateCategoryCurrentAmount(categoryID: Long, amount: Double) {
//        return categoryDao.updateCategoryAmount(categoryID, amount)
//    }
//
//    // updating the budget limit for a specific category
//    suspend fun updateCategoryLimit(categoryID: Long, newLimit: Double): Int {
//        return categoryDao.updateCategoryBudgetLimit(categoryID, newLimit)
//    }
//
//    // deleting a category by its ID
//    suspend fun deleteCategory(categoryID: Long): Int {
//        return categoryDao.deleteACategory(categoryID)
//    }
//
//    // delete all categories from the database
//    suspend fun deleteAllCategories() {
//        categoryDao.deleteAllCategories()
//    }

}
