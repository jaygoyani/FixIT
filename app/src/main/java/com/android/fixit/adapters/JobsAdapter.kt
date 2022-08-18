package com.android.fixit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.fixit.databinding.ItemJobBinding
import com.android.fixit.dtos.JobDTO
import com.android.fixit.interfaces.IListActions
import com.android.fixit.managers.DatesManager
import com.android.fixit.utils.Helper

class JobsAdapter(
    val context: Context, val jobs: ArrayList<JobDTO>, val actions: IListActions?
) : RecyclerView.Adapter<JobsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemJobBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                actions?.onItemClicked(absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val job = jobs[holder.absoluteAdapterPosition]
        val binding = holder.binding
        binding.name.text = job.title
        binding.technician.text = job.technicianName ?: "N/A"
        binding.service.text = job.type
        binding.date.text = DatesManager.getTimeInF1(job.insertedAt)
        val details = Helper.getStatusDetails(job.status)
        binding.status.text = details.first
        binding.status.background = details.second
    }

    override fun getItemCount(): Int {
        return jobs.size
    }
}