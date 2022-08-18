package com.android.fixit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.fixit.R
import com.android.fixit.databinding.ItemUserBinding
import com.android.fixit.databinding.ItemWorkerBinding
import com.android.fixit.dtos.UserDTO
import com.android.fixit.interfaces.IListActions

class WorkersAdapter(
    val context: Context, val records: ArrayList<UserDTO>, val actions: IListActions?
) : RecyclerView.Adapter<WorkersAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWorkerBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.select.setOnClickListener {
                actions?.onItemSelected(absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWorkerBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val worker = records[holder.absoluteAdapterPosition]
        val binding = holder.binding
        binding.name.text = worker.name
        binding.price.text = context.getString(R.string.template_price, worker.pricePerHr)
        binding.service.text = worker.service
        binding.jobsClosed.text = context.getString(R.string.jobs_done, worker.jobsClosed)
    }

    override fun getItemCount(): Int {
        return records.size
    }
}