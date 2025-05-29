package com.example.zakazaka.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.zakazaka.Models.UserEntity
import com.example.zakazaka.Repository.UserRepository
import javax.inject.Inject

class LoginRegistrationViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    private val _registrationStatus = MutableLiveData<Pair<Boolean, String?>>()
    val registrationStatus: LiveData<Pair<Boolean, String?>> = _registrationStatus

    fun registerUser(user: UserEntity) {
        userRepository.registerUser(user) { success, message ->
            _registrationStatus.postValue(Pair(success, message))
        }
    }
    fun loginUser(email: String, password: String, callback: (UserEntity?, String?) -> Unit) {
        userRepository.loginUser(email, password, callback)
    }
    fun getUserById(userId: String): LiveData<UserEntity?> {
        return userRepository.getUserById(userId)
    }


//    fun registerUser(user: UserEntity): LiveData<Long>{
//        val id = MutableLiveData<Long>()
//        viewModelScope.launch(Dispatchers.IO) {
//            id.postValue(userRepository.registerUser(user))
//        }
//        return id
//    }
//    fun loginUser(email:String,password:String): LiveData<UserEntity?> {
//        val user = MutableLiveData<UserEntity>()
//        viewModelScope.launch(Dispatchers.IO) {
//            user.postValue(userRepository.loginUser(email, password))
//        }
//        return user
//    }
//    fun getUserById(userId:Long):LiveData<UserEntity?>{
//        val user = MutableLiveData<UserEntity?>()
//        viewModelScope.launch(Dispatchers.IO) {
//            user.postValue(userRepository.getUserById(userId))
//        }
//        return user
//    }

}