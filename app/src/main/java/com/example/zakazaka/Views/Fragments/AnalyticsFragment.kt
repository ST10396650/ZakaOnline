package com.example.zakazaka.Views.Fragments

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
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
import java.util.Calendar
import java.util.Date

class AnalyticsFragment : Fragment() {
    lateinit var barGraph : BarChart
    val analyticsViewModel = AnalyticsViewModel(
        categoryRepository = CategoryRepository(),
        subCategoryRepository = SubCategoryRepository(),
        transactionRepository = TransactionRepository()
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
        barGraph = view.findViewById(R.id.barChart)
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

        val edStart = view.findViewById<EditText>(R.id.startDateText)
        val edEnd = view.findViewById<EditText>(R.id.endDateText)
        val startDatePicker = view.findViewById<DatePicker>(R.id.startDate)
        val endDatePicker = view.findViewById<DatePicker>(R.id.endDate)

        edStart.setOnClickListener {
            startDatePicker.visibility = View.VISIBLE
            endDatePicker.visibility = View.GONE
        }
        startDatePicker.setOnDateChangedListener {_,year,month,dayOfMonth ->
           val selectedDate = "${month+1}/$dayOfMonth/$year"
            edStart.setText(selectedDate)
        }
        edEnd.setOnClickListener {
            startDatePicker.visibility = View.GONE
            endDatePicker.visibility = View.VISIBLE
        }
        endDatePicker.setOnDateChangedListener {_,year,month,dayOfMonth ->
           val selectedDate = "${month+1}/$dayOfMonth/$year"
            edEnd.setText(selectedDate)
        }



        if (userId != null) {
        val btnDownload = view.findViewById<Button>(R.id.btnDownloadPdf)
        btnDownload.setOnClickListener{
            analyticsViewModel.downloadAnalyticsData(barGraph,requireContext())
        }


        val btnApply = view.findViewById<Button>(R.id.btnApplyFilter)
        btnApply.setOnClickListener {
            val startYear = startDatePicker.year
            val startMonth = startDatePicker.month
            val startDay = startDatePicker.dayOfMonth
            val startDateVal  = getDatePicker(startYear,startMonth,startDay)

            val endYear = endDatePicker.year
            val endMonth = endDatePicker.month
            val endDay = endDatePicker.dayOfMonth
            val endDateVal  =getDatePicker(endYear,endMonth,endDay)



            startDatePicker.visibility = View.GONE
            endDatePicker.visibility = View.GONE
            analyticsViewModel.filterAnalyticsData(startDateVal,endDateVal,userId).observe(viewLifecycleOwner) { analyticsData ->
                if (!analyticsData.isNullOrEmpty()) {
                    val categoryLabels = analyticsData.map { it.categoryName }
                    val categoryAmounts = analyticsData.map { it.totalAmount }
                        setUpBarGraph(categoryLabels,categoryAmounts)
                }
            }
        }
            //resets to default values
            val resetBtn = view.findViewById<Button>(R.id.btnReset)
            resetBtn.setOnClickListener {
                startDatePicker.visibility = View.GONE
                endDatePicker.visibility = View.GONE
                analyticsViewModel.getDefaultAnalyticsData(userId).observe(viewLifecycleOwner) {analyticsData ->
                    if (analyticsData.isNotEmpty()) {
                        val categoryLabels = analyticsData.map { it.categoryName }
                        val categoryAmounts = analyticsData.map { it.totalAmount }

                        setUpBarGraph(categoryLabels,categoryAmounts)
                    }
                }
            }

            analyticsViewModel.getDefaultAnalyticsData(userId).observe(viewLifecycleOwner) {analyticsData ->
                if (analyticsData.isNotEmpty()) {
                    val categoryLabels = analyticsData.map { it.categoryName }
                    val categoryAmounts = analyticsData.map { it.totalAmount }

                    setUpBarGraph(categoryLabels,categoryAmounts)
                }
            }


        }
    }
    fun getDatePicker(year: Int, month: Int, day: Int): Date {
        val caledar = Calendar.getInstance()
        caledar.set(Calendar.YEAR,year)
        caledar.set(Calendar.MONTH,month)
        caledar.set(Calendar.DAY_OF_MONTH,day)
       return caledar.time

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

        barGraph.invalidate()
    }
}