package com.example.zakazaka.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.zakazaka.Data.Database.SubCategoryDao
import com.example.zakazaka.Models.SubCategoryEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SubCategoryRepository() {

    private val firebaseDatabase = FirebaseDatabase.getInstance().getReference("Sub_Category")
    private val auth = FirebaseAuth.getInstance()

    fun addSubCategory(subCategory: SubCategoryEntity, callback: (SubCategoryEntity?) -> Unit) {
        val userId = auth.currentUser?.uid ?: run {
            callback(null)
            return
        }

        val subCategoryId = firebaseDatabase.child(userId).push().key
        if (subCategoryId == null) {
            callback(null)
            return
        }

        val subCategoryMap = mapOf(
            "subCategoryID" to subCategoryId,
            "name" to subCategory.name,
            "description" to subCategory.description,
            "budgetLimit" to subCategory.budgetLimit,
            "currentAmount" to subCategory.currentAmount,
            "categoryID" to subCategory.categoryID
        )

        firebaseDatabase.child(userId).child(subCategoryId).setValue(subCategoryMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(subCategory.copy(subCategoryID = subCategoryId))
                } else {
                    callback(null)
                }
            }
    }

    // Get subcategories for a specific category
    fun getSubCategoriesForCategory(categoryId: String): LiveData<List<SubCategoryEntity>> {
        val subCategoriesLiveData = MutableLiveData<List<SubCategoryEntity>>()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            subCategoriesLiveData.value = emptyList()
            return subCategoriesLiveData
        }

        firebaseDatabase.child(userId)
            .orderByChild("categoryID")
            .equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val subCategories = mutableListOf<SubCategoryEntity>()
                    for (subCategorySnapshot in snapshot.children) {
                        val subCategoryData = subCategorySnapshot.value as? Map<String, Any>
                        if (subCategoryData != null) {
                            // Better handling of numeric values from Firebase
                            val budgetLimit = when (val budget = subCategoryData["budgetLimit"]) {
                                is Double -> budget
                                is Long -> budget.toDouble()
                                is String -> budget.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }

                            val currentAmount = when (val current = subCategoryData["currentAmount"]) {
                                is Double -> current
                                is Long -> current.toDouble()
                                is String -> current.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }

                            val subCategory = SubCategoryEntity(
                                subCategoryID = subCategoryData["subCategoryID"] as? String ?: "",
                                name = subCategoryData["name"] as? String ?: "",
                                description = subCategoryData["description"] as? String ?: "",
                                budgetLimit = budgetLimit,
                                currentAmount = currentAmount,
                                categoryID = subCategoryData["categoryID"] as? String ?: ""
                            )
                            subCategories.add(subCategory)
                        }
                    }
                    subCategoriesLiveData.value = subCategories
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    subCategoriesLiveData.value = emptyList()
                }
            })

        return subCategoriesLiveData
    }

    // Update subcategory current amount
    fun updateSubCategoryCurrentAmount(subCategoryId: String, additionalAmount: Double, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(false)
            return
        }

        firebaseDatabase.child(userId).child(subCategoryId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val subCategoryData = snapshot.value as? Map<String, Any>
                if (subCategoryData != null) {
                     val currentAmount = when (val current = subCategoryData["currentAmount"]) {
                        is Double -> current
                        is Long -> current.toDouble()
                        is String -> current.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                      val newAmount = currentAmount + additionalAmount
                    

                    firebaseDatabase.child(userId).child(subCategoryId).child("currentAmount").setValue(newAmount)
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

    fun getSubCategoryById(subCategoryId: String, callback: (SubCategoryEntity?) -> Unit) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            callback(null)
            return
        }

        firebaseDatabase.child(userId)
            .orderByChild("subCategoryID")
            .equalTo(subCategoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var foundSubCategory: SubCategoryEntity? = null

                    for (subCategorySnapshot in snapshot.children) {
                        val subCategoryData = subCategorySnapshot.value as? Map<String, Any>
                        if (subCategoryData != null) {
                            val budgetLimit = when (val budget = subCategoryData["budgetLimit"]) {
                                is Double -> budget
                                is Long -> budget.toDouble()
                                is String -> budget.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }

                            val currentAmount = when (val current = subCategoryData["currentAmount"]) {
                                is Double -> current
                                is Long -> current.toDouble()
                                is String -> current.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }

                            val subCategory = SubCategoryEntity(
                                subCategoryID = subCategoryData["subCategoryID"] as? String ?: "",
                                name = subCategoryData["name"] as? String ?: "",
                                description = subCategoryData["description"] as? String ?: "",
                                budgetLimit = budgetLimit,
                                currentAmount = currentAmount,
                                categoryID = subCategoryData["categoryID"] as? String ?: ""
                            )

                            foundSubCategory = subCategory
                            break
                        }
                    }

                    callback(foundSubCategory)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }
//    // Get all subcategories as LiveData
//    val readAllData: LiveData<List<SubCategoryEntity>> = subCategoryDao.readAllData() //(Android Developer, 2025)
//
//    // Insert a new subcategory
//    suspend fun addSubCategory(subCategory: SubCategoryEntity): Long {
//        return subCategoryDao.insert(subCategory)
//    }
//
//    // Get a specific subcategory by ID
//     fun getSubCategoryById(subCategoryId: Long): SubCategoryEntity? {
//        return subCategoryDao.getSubCategoryById(subCategoryId)
//    }
//
//    // Get all subcategories for a specific category
//    suspend fun getSubCategoriesForCategory(categoryId: Long): List<SubCategoryEntity> {
//        return subCategoryDao.getSubCategoriesForCategory(categoryId)
//    }
//
//    // Update the budget limit for a subcategory
//    suspend fun updateSubCategoryBudgetLimit(subCategoryId: Long, budgetLimit: Double): Int {
//        return subCategoryDao.updateSubCategoryBudgetLimit(subCategoryId, budgetLimit)
//    }
//
//    // Update name and description for a subcategory
//    suspend fun updateSubCategoryDetails(subCategoryId: Long, name: String, description: String): Int {
//        return subCategoryDao.updateSubCategoryDetails(subCategoryId, name, description)
//    }
//
//    // Update the current amount for a subcategory
//    suspend fun updateSubCategoryAmount(subCategoryId: Long, currentAmount: Double): Int {
//        return subCategoryDao.updateSubCategoryAmount(subCategoryId, currentAmount)
//    }
//
//    // Delete all subcategories for a specific category
//    suspend fun deleteSubCategoriesForCategory(categoryId: Long): Int {
//        return subCategoryDao.deleteASubCategoriesForCategory(categoryId)
//    }
//
//    /*method takes in 3(parameters) values  and calls the method inside the DAO and passes the
//    * variables to the method*/
//    suspend fun transferBetweenSubCategories(
//        fromSubCategoryId: Long,
//        toSubCategoryId: Long,
//        amount: Double
//    ): SubCategoryDao.TransferResult {
//        return subCategoryDao.transferBetweenSubCategories(
//            fromSubCategoryId,
//            toSubCategoryId,
//            amount
//        )
//    }
//
//    /*so the result parameter gets its value from the dao's transferBetweenSubCategories method,
//    which returns one of the enum values from SubCategoryDao.TransferResult.*/
//    fun getTransferResultMessage(result: SubCategoryDao.TransferResult): String {
//        return when (result) {// "when" acts like a switch/case
//            SubCategoryDao.TransferResult.SUCCESS ->
//                "Transfer completed successfully"
//
//            SubCategoryDao.TransferResult.SUCCESS_BUT_OVER_BUDGET ->
//                "Transfer completed, but the destination subcategory now exceeds its budget limit"
//
//            SubCategoryDao.TransferResult.ERROR_INSUFFICIENT_FUNDS ->
//                "Error: Insufficient funds in the source subcategory"
//
//            SubCategoryDao.TransferResult.ERROR_SUBCATEGORY_NOT_FOUND ->
//                "Error: One or both subcategories not found"
//        }
//    }
}
