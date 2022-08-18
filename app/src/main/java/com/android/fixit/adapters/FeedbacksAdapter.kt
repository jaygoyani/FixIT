package com.android.fixit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.fixit.databinding.ItemFeedbackBinding
import com.android.fixit.dtos.FeedbackDTO
import com.android.fixit.managers.DatesManager
import com.android.fixit.utils.Constants

class FeedbacksAdapter(
    val context: Context, val records: List<FeedbackDTO>
) : RecyclerView.Adapter<FeedbacksAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemFeedbackBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFeedbackBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[holder.absoluteAdapterPosition]
        val binding = holder.binding
        binding.subject.text = record.subject
        binding.description.text = record.description
        binding.date.text = DatesManager.getTimeInF1(record.insertedAt!!)
        binding.user.text = "${record.userName}\n${record.userMobile}"
    }

    override fun getItemCount(): Int {
        return records.size
    }

}