package com.example.zakazaka.Views.Fragments

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zakazaka.R
import com.example.zakazaka.Repository.AccountRepository
import com.example.zakazaka.Repository.BudgetGoalRepository
import com.example.zakazaka.Repository.CategoryRepository
import com.example.zakazaka.Repository.SubCategoryRepository
import com.example.zakazaka.Repository.TransactionRepository
import com.example.zakazaka.Repository.UserRepository
import com.example.zakazaka.ViewModels.AnalyticsViewModel
import com.example.zakazaka.ViewModels.ViewModelFactory
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class AnalyticsFragment : Fragment() {
    lateinit var barGraph : BarChart
    val analyticsViewModel = AnalyticsViewModel(
        budgetGoalRepository = TODO(),
        categoryRepository = TODO(),
        subCategoryRepository = TODO(),
        transactionRepository = TODO()
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory(
            UserRepository(),
            AccountRepository(),
            BudgetGoalRepository(),
            CategoryRepository(),
            SubCategoryRepository(),
            TransactionRepository()
        )

        val sharedPref = requireContext().getSharedPreferences("BudgetAppPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("LOGGED_USER_ID", null)
        if (userId != null) {
            analyticsViewModel.getDefaultAnalyticsData(userId).observe(viewLifecycleOwner) {analyticsData ->
                if (analyticsData.isNotEmpty()) {
                    val categoryLabels = analyticsData.map { it.categoryName }
                    val categoryAmounts = analyticsData.map { it.totalAmount }
                    barGraph = view.findViewById(R.id.barChart)
                    setUpBarGraph(categoryLabels,categoryAmounts)
                }
            }
        }
    }
    fun setUpBarGraph(label : List<String>, amounts : List<Double>){
        val entries = amounts.mapIndexed { index, amount ->
            BarEntry(index.toFloat(), amount.toFloat())
        }
        val barDataSet = BarDataSet(entries, "Category Total Amounts")
        barDataSet.color = resources.getColor(R.color.primaryColour2)
        val barData = BarData(barDataSet)
        barGraph.data = barData
        val xAxis = barGraph.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(label)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = label.size
        xAxis.labelRotationAngle = -45f

        barGraph.invalidate()
    }
}