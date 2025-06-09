package com.example.zakazaka.Views.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zakazaka.Adapters.CategoryAdapter
import com.example.zakazaka.Data.Database.AppDatabase
import com.example.zakazaka.R
import com.example.zakazaka.Repository.AccountRepository
import com.example.zakazaka.Repository.BudgetGoalRepository
import com.example.zakazaka.Repository.CategoryRepository
import com.example.zakazaka.Repository.SubCategoryRepository
import com.example.zakazaka.Repository.TransactionRepository
import com.example.zakazaka.Repository.UserRepository
import com.example.zakazaka.ViewModels.CategoryViewModel
import com.example.zakazaka.ViewModels.ViewModelFactory
import com.example.zakazaka.Views.CategoryDetails
import com.example.zakazaka.Views.CreateCategory


class CategoryFragment : Fragment() {
    lateinit var categoryViewModel : CategoryViewModel
    lateinit var categoryAdapter : CategoryAdapter
    lateinit var categoryRecyclerView : RecyclerView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_category, container, false)
        val createCategoryButton = view.findViewById<TextView>(R.id.btnCreateCategory)
        createCategoryButton.setOnClickListener {
            val intent = Intent(requireContext(), CreateCategory::class.java)
            startActivity(intent)
        }

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext().applicationContext
        val factory = ViewModelFactory(
            UserRepository(),
            AccountRepository(),
            BudgetGoalRepository(),
            CategoryRepository(),
            SubCategoryRepository(),
            TransactionRepository()
        )

        val sharedPref = requireContext().getSharedPreferences("BudgetAppPrefs", MODE_PRIVATE)
        // Changed to getString since you mentioned IDs are now strings for Firebase
        val userId = sharedPref.getString("LOGGED_USER_ID", null)

        categoryViewModel = ViewModelProvider(this, factory)[CategoryViewModel::class.java]
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView)
        categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Only proceed if userId is not null
        userId?.let { uid ->
            categoryViewModel.getCategoriesByUserId(uid).observe(viewLifecycleOwner) { categories ->
                categoryAdapter = CategoryAdapter(categories) { category ->
                    val intent = Intent(requireContext(), CategoryDetails::class.java)
                    intent.putExtra("categoryID", category.categoryID)
                    startActivity(intent)
                }
                categoryRecyclerView.adapter = categoryAdapter
            }
        }
    }
}

