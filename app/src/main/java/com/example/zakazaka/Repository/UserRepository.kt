package com.example.zakazaka.Repository


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.zakazaka.Data.Database.UserDao
import com.example.zakazaka.Models.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class UserRepository() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

    fun registerUser(user: UserEntity, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    uid?.let {
                        val firebaseUser = user.copy(userID = it)
                        databaseRef.child(it).setValue(firebaseUser)
                            .addOnSuccessListener {
                                callback(true, null) // no error message on success
                            }
                            .addOnFailureListener { exception ->
                                callback(false, exception.message)
                            }
                    } ?: callback(false, "User ID is null")
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun loginUser(email: String, password: String, callback: (UserEntity?, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        databaseRef.child(uid).addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val user = snapshot.getValue(UserEntity::class.java)
                                if (user != null) {
                                    callback(user, null)
                                } else {
                                    callback(null, "User data not found in database")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                callback(null, error.message)
                            }
                        })
                    } else {
                        callback(null, "User ID is null")
                    }
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }

    fun getUserById(userId: String): LiveData<UserEntity?> {
        val liveData = MutableLiveData<UserEntity?>()

        databaseRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserEntity::class.java)
                liveData.value = user
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserRepository", "Error getting user: ${error.message}")
                liveData.value = null
            }
        })

        return liveData
    }

//    //was thinking about CRUD operations here
//
//    // getting all users from the database
//    // LiveData will automatically update when data changes
//    val readAllData: LiveData<List<UserEntity>> = userDao.readAllData()
//
//    //registers a new user in the database
//    suspend fun registerUser(user: UserEntity): Long {
//        return userDao.insert(user)
//    }
//
//    //user logs in by username/email and password
//    suspend fun loginUser(userNameOrEmail: String, password: String): UserEntity? {
//        return userDao.login(userNameOrEmail, password)
//    }
//
//    //getting the user id
//    suspend fun getUserById(userId: Long):UserEntity? {
//        return userDao.getUserByID(userId)
//    }
//
//    //updates a user's information
//    suspend fun updateUserDetails(user: UserEntity): Int {
//        return userDao.update(user)
//    }
//
//    //when the user wants to delete their account by ID
//    suspend fun deleteUserById(userId: Long): Int {
//        return userDao.deleteUserbyID(userId)
//    }
//
//    // delete all users from the database
//    suspend fun deleteAllUsers() {
//        userDao.deleteAllUsers()
//    }
//(Adhiguna, 2021)
}
