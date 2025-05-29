package com.example.zakazaka.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.zakazaka.Data.Database.BudgetGoalDao
import com.example.zakazaka.Models.BudgetGoalEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class BudgetGoalRepository() {
    private val firebaseDatabase = FirebaseDatabase.getInstance().getReference("Budget_Goals")
    private val auth = FirebaseAuth.getInstance()
    companion object {
        private const val TAG = "BudgetGoalRepository"
    }

    fun getBudgetGoalsByUserId(userId: String): LiveData<List<BudgetGoalEntity>> {
        val budgetGoalsLiveData = MutableLiveData<List<BudgetGoalEntity>>()

        Log.d(TAG, "Starting to fetch budget goals for userId: $userId")
        Log.d(TAG, "Firebase reference path: Budget_Goals/$userId")

        // Use same pattern as your working account repository
        firebaseDatabase.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange triggered for userId: $userId")
                Log.d(TAG, "Snapshot exists: ${snapshot.exists()}")
                Log.d(TAG, "Snapshot children count: ${snapshot.childrenCount}")

                val budgetGoals = mutableListOf<BudgetGoalEntity>()

                for (budgetGoalSnapshot in snapshot.children) {
                    Log.d(TAG, "Processing child snapshot with key: ${budgetGoalSnapshot.key}")

                    val budgetGoalData = budgetGoalSnapshot.value as? Map<String, Any>

                    if (budgetGoalData != null) {
                        Log.d(TAG, "Budget goal data: $budgetGoalData")

                        // Let's be more careful with type casting and add null checks
                        val budgetGoal = BudgetGoalEntity(
                            budgetGoalID = budgetGoalData["id"] as? String
                                ?: budgetGoalData["budgetGoalID"] as? String
                                ?: budgetGoalSnapshot.key ?: "",
                            minAmount = when (val minAmt = budgetGoalData["minAmount"]) {
                                is Number -> minAmt.toDouble()
                                is String -> minAmt.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            },
                            maxAmount = when (val maxAmt = budgetGoalData["maxAmount"]) {
                                is Number -> maxAmt.toDouble()
                                is String -> maxAmt.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            },
                            month = budgetGoalData["month"] as? String ?: "",
                            status = budgetGoalData["status"] as? String ?: "",
                            userID = budgetGoalData["userID"] as? String
                                ?: budgetGoalData["userId"] as? String
                                ?: userId
                        )

                        Log.d(TAG, "Created BudgetGoalEntity: $budgetGoal")
                        budgetGoals.add(budgetGoal)
                    } else {
                        Log.w(TAG, "Budget goal data is null for snapshot: ${budgetGoalSnapshot.key}")
                        Log.w(TAG, "Raw snapshot value: ${budgetGoalSnapshot.value}")
                        Log.w(TAG, "Raw snapshot value type: ${budgetGoalSnapshot.value?.javaClass?.simpleName}")
                    }
                }

                Log.d(TAG, "Final budget goals list size: ${budgetGoals.size}")
                budgetGoalsLiveData.value = budgetGoals
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error getting budget goals: ${error.message}")
                Log.e(TAG, "Error code: ${error.code}")
                Log.e(TAG, "Error details: ${error.details}")
                budgetGoalsLiveData.value = emptyList()
            }
        })

        return budgetGoalsLiveData
    }

    fun addBudgetGoal(budgetGoal: BudgetGoalEntity, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(false)
            return
        }

        val goalId = firebaseDatabase.child(userId).push().key
        if (goalId == null) {
            callback(false)
            return
        }

        val goalMap = mapOf(
            "budgetGoalID" to goalId,
            "minAmount" to budgetGoal.minAmount,
            "maxAmount" to budgetGoal.maxAmount,
            "month" to budgetGoal.month,
            "status" to budgetGoal.status,
            "userID" to userId
        )

        firebaseDatabase.child(userId).child(goalId).setValue(goalMap)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    // Read all budget goals
    fun getAllBudgetGoals(callback: (List<BudgetGoalEntity>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return callback(emptyList())

        firebaseDatabase.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val goals = mutableListOf<BudgetGoalEntity>()
                for (goalSnap in snapshot.children) {
                    goalSnap.getValue(BudgetGoalEntity::class.java)?.let {
                        goals.add(it)
                    }
                }
                callback(goals)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

//    //nia please add the function where the user has to add a minimum and maximum budget goal
//    // Directly expose all budget goals as a property
//    val readAllBudgetGoals: LiveData<List<BudgetGoalEntity>> = budgetGoalDao.getAllBudgetGoals()
//
//    //get the budget goals for a for a specific user
//    suspend fun getBudgetGoalsByUserId(userId: Long): List<BudgetGoalEntity> {
//        return budgetGoalDao.getBudgetGoalsByUserId(userId)
//    }
//
//
//    /*
//    suspend fun getBudgetGoalById(budgetGoalId: Long): BudgetGoalEntity? {
//        return budgetGoalDao.getBudgetGoalById(budgetGoalId)
//    }*/
//
//
//    //viewing the budget goal of user by month
//    fun getBudgetGoalsByUserIdAndMonth(userId: Long, month: String): LiveData<List<BudgetGoalEntity>> {
//        return budgetGoalDao.getBudgetGoalsByUserIdAndMonth(userId, month)
//    }
//
//    //viewing the budget goal of user by status
//    fun getBudgetGoalsByUserIdAndStatus(userId: Long, status: String): LiveData<List<BudgetGoalEntity>> {
//        return budgetGoalDao.getBudgetGoalsByUserIdAndStatus(userId, status)
//    }
//
//    //creating a new budget goal
//    suspend fun addBudgetGoal(budgetGoal: BudgetGoalEntity): Long {
//        return budgetGoalDao.insertBudgetGoal(budgetGoal)
//    }
//
//    //updating a budget goal
//    suspend fun updateBudgetGoal(budgetGoal: BudgetGoalEntity) {
//        budgetGoalDao.updateBudgetGoal(budgetGoal)
//    }
//
//    //deleting a budget goal by ID
//    suspend fun deleteBudgetGoal(budgetGoalId: Long) {
//        budgetGoalDao.deleteBudgetGoalById(budgetGoalId)
//    }
//
//    //deleting all budget goals
//    suspend fun deleteAllBudgetGoals() {
//        budgetGoalDao.deleteAllBudgetGoals()
//    }
//
//    //updating budget goal status
//    suspend fun updateBudgetGoalStatus(budgetGoalId: Long, status: String) {
//        budgetGoalDao.updateBudgetGoalStatus(budgetGoalId, status)
//    }
}