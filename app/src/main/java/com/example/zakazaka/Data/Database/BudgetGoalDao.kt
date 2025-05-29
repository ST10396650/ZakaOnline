package com.example.zakazaka.Data.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.zakazaka.Models.BudgetGoalEntity


@Dao
interface BudgetGoalDao {

    //getting all budget goals
    @Query("SELECT * FROM Budget_Goal")
    fun getAllBudgetGoals(): LiveData<List<BudgetGoalEntity>>

    //getting budget goals by user ID
    @Query("SELECT * FROM Budget_Goal WHERE userID = :userId")
    suspend fun getBudgetGoalsByUserId(userId: Long): List<BudgetGoalEntity>


    /*getting budget goal by ID
    @Query("SELECT * FROM Budget_Goal WHERE budgetGoalID = :budgetGoalId")
    suspend fun getBudgetGoalById(budgetGoalId: Long): BudgetGoalEntity?
*/

    //getting budget goals by using the the userID and month
    @Query("SELECT * FROM Budget_Goal WHERE userID = :userId AND month = :month ")
    fun getBudgetGoalsByUserIdAndMonth(userId: Long, month: String): LiveData<List<BudgetGoalEntity>>

    //viewing the budget gaols by filtering by userID and status
    @Query("SELECT * FROM Budget_Goal WHERE userID = :userId AND status = :status")
    fun getBudgetGoalsByUserIdAndStatus(userId: Long, status: String): LiveData<List<BudgetGoalEntity>>

    //inserting a new budget goal
    @Insert
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoalEntity): Long

    //updating the budget goal
    @Update
    suspend fun updateBudgetGoal(budgetGoal: BudgetGoalEntity)

    //deleting a budget goal
    @Query("DELETE FROM Budget_Goal WHERE budgetGoalID = :budgetGoalId")
    suspend fun deleteBudgetGoalById(budgetGoalId: Long)


    //deleting all budget goals
    @Query("DELETE FROM Budget_Goal")
    suspend fun deleteAllBudgetGoals()

    //updating the status when the milestone has been reached
    @Query("UPDATE Budget_Goal SET status = :status WHERE budgetGoalID = :budgetGoalId")
    suspend fun updateBudgetGoalStatus(budgetGoalId: Long, status: String)

}