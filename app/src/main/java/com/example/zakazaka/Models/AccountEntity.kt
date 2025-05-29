package com.example.zakazaka.Models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Account",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class,
            parentColumns = ["userID"],
            childColumns = ["userID"],
            onDelete = ForeignKey.CASCADE )
    ],

    indices = [Index(value = ["userID"])]
)
data class AccountEntity (
    @PrimaryKey(autoGenerate = false) val accountID : String = "",
    var accountName : String = "",
    var amount : Double = 0.0,
    var type : String = "",
    var bankName : String = "",
    var userID : String = ""
){
    override fun toString():String{
        return accountName
    }
}
