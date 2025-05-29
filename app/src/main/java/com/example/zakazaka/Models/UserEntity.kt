package com.example.zakazaka.Models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "User")
data class UserEntity (
    @PrimaryKey(autoGenerate = false) val userID: String = "", //I want the firebase string user ID to be stored in here
    var username : String = "",
    var firstName : String = "",
    var lastName : String = "",
    var email : String = "",
    var password :String = "",
)//(Adhiguna, 2021)
