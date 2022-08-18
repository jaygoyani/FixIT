package com.android.fixit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.fixit.R
import com.android.fixit.databinding.ItemServiceBinding
import com.android.fixit.dtos.ServiceDTO
import com.android.fixit.interfaces.IListActions

class ServicesAdapter(
    val context: Context, val services: ArrayList<ServiceDTO>, val actions: IListActions?
) : RecyclerView.Adapter<ServicesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemServiceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.edit.setOnClickListener {
                actions?.onEditClicked(absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = services[holder.absoluteAdapterPosition]
        holder.binding.label.text = service.label
    }

    override fun getItemCount(): Int {
        return services.size
    }
}