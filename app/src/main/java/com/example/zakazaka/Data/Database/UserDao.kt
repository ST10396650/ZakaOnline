package com.example.zakazaka.Data.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.zakazaka.Models.UserEntity


@Dao
interface UserDao {
    // Insert operation for  when the user registers
    @Insert
    suspend fun insert(user: UserEntity): Long

    //when we want to get all the information for the specific user.
    @Query("SELECT * FROM User WHERE userID = :userId")
    suspend fun getUserByID(userId: Long): UserEntity?

    //it can be with the username/ email
    //when the user logs in and the information from the user prompt is correct when need to verify.
    @Query("SELECT * FROM User WHERE username = :userNameOrEmail OR email = :userNameOrEmail AND password = :password")
    suspend fun  login(userNameOrEmail: String, password: String) : UserEntity?

    // Read all users ordered by userId
    @Query("SELECT * FROM User ORDER BY userID ASC")
    fun readAllData(): LiveData<List<UserEntity>>


    //when the user wants to edit their information that is already stored inside the database
    @Update
    suspend fun update(user: UserEntity) :Int

    //when user wants to delete their account
    @Query("DELETE FROM User WHERE userID = :userId")
    suspend fun deleteUserbyID(userId: Long): Int

    // deleting all users
    @Query("DELETE FROM User")
    suspend fun deleteAllUsers()

}//(The IIE, 2025)
