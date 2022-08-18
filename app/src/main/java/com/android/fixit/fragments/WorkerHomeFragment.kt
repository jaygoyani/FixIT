package com.android.fixit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.fixit.R
import com.android.fixit.databinding.FragmentWorkerHomeBinding
import com.android.fixit.dtos.JobDTO
import com.android.fixit.managers.DatesManager
import com.android.fixit.managers.DialogsManager
import com.android.fixit.managers.PrefManager
import com.android.fixit.utils.Constants
import com.android.fixit.utils.Helper
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import kotlin.collections.ArrayList


class WorkerHomeFragment : Fragment() {
    private var _binding: FragmentWorkerHomeBinding? = null
    private val binding get() = _binding!!
    private val user = PrefManager.getUserDTO()
    private val jobs = ArrayList<JobDTO>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getJobs()
    }

    private fun getJobs() {
        DialogsManager.showProgressDialog(requireContext())
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_JOBS)
            .whereEqualTo("technicianMobile", Helper.getUserIndex(user))
            .orderBy("jobDate", Query.Direction.ASCENDING)
            .get().addOnCompleteListener { it ->
                DialogsManager.dismissProgressDialog()
                jobs.clear()
                if (it.isSuccessful && it.result != null) {
                    it.result.documents.forEach { job ->
                        jobs.add(job.toObject(JobDTO::class.java)!!)
                    }
                    if (jobs.isEmpty())
                        Helper.showToast(requireContext(), "No data found!")
                    setData()
                } else
                    Helper.showToast(requireContext(), "Failed to fetch data!")
            }
    }

    private fun setData() {
        var totalJobs = 0
        var totalRevenue = 0
        var pending = 0
        var accepted = 0
        var inProgress = 0
        var completed = 0
        jobs.forEach {
            when (it.status) {
                Constants.JOB_STATUS.PENDING.name -> pending++
                Constants.JOB_STATUS.IN_PROGRESS.name -> inProgress++
                Constants.JOB_STATUS.ACCEPTED.name -> accepted++
                else -> {
                    totalJobs++
                    totalRevenue += it.price
                    completed++
                }
            }
        }
        binding.totalJobs.text = "$totalJobs"
        binding.revenue.text = "$$totalRevenue"

        setPieChart(pending, accepted, inProgress, completed)
        setLineChart()
    }

    private fun setLineChart() {
        val entries = ArrayList<Entry>()
        val completedJobs = jobs.filter {
            it.status == Constants.JOB_STATUS.COMPLETED.name
        }
        completedJobs.forEach{
            entries.add(Entry(it.jobDate.toFloat(), it.price.toFloat()))
        }
        val dataset = LineDataSet(entries, "Revenue")
        dataset.color = ContextCompat.getColor(requireContext(), R.color.violet)
        dataset.setCircleColor(ContextCompat.getColor(requireContext(), R.color.violet))
        val data = LineData(dataset)
        binding.revenueChart.data = data

        binding.revenueChart.axisRight.isEnabled = false
        val xAxis = binding.revenueChart.xAxis
        val yAxis = binding.revenueChart.axisLeft
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        yAxis.axisMinimum = 0f
        yAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "$${value.toInt()}"
            }
        }

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return DatesManager.getTimeInF3(value.toLong())
            }
        }

        binding.revenueChart.description.isEnabled = false
        binding.revenueChart.setTouchEnabled(false)
        binding.revenueChart.setPinchZoom(false)
        binding.revenueChart.invalidate()
    }

    private fun setPieChart(pending: Int, accepted: Int, inProgress: Int, completed: Int) {
        val colors = ArrayList<Int>()
        val pieEntries: ArrayList<PieEntry> = ArrayList()

        colors.add(ContextCompat.getColor(requireContext(), R.color.error))
        pieEntries.add(PieEntry(pending.toFloat(), "Pending"))

        colors.add(ContextCompat.getColor(requireContext(), R.color.cream))
        pieEntries.add(PieEntry(accepted.toFloat(), "Accepted"))

        colors.add(ContextCompat.getColor(requireContext(), R.color.orange))
        pieEntries.add(PieEntry(inProgress.toFloat(), "In Progress"))

        colors.add(ContextCompat.getColor(requireContext(), R.color.green))
        pieEntries.add(PieEntry(completed.toFloat(), "Completed"))

        val dataset = PieDataSet(pieEntries, "Jobs")
        dataset.valueTextSize = 12f
        dataset.colors = colors
        val data = PieData(dataset)
        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%"
            }
        })
        binding.ordersChart.setUsePercentValues(true)
        binding.ordersChart.description.isEnabled = false
        binding.ordersChart.setDrawSliceText(false)
        binding.ordersChart.setHoleColor(ContextCompat.getColor(requireContext(), R.color.violet))
        binding.ordersChart.data = data
        binding.ordersChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}