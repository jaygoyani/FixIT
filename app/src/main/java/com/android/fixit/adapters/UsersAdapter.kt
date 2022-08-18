package com.android.fixit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.fixit.databinding.ItemUserBinding
import com.android.fixit.dtos.ServiceDTO
import com.android.fixit.dtos.UserDTO
import com.android.fixit.interfaces.IListActions
import com.android.fixit.utils.Constants
import com.android.fixit.utils.Helper

class UsersAdapter(
    val context: Context, val users: ArrayList<UserDTO>, val actions: IListActions?
) : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.edit.setOnClickListener {
                actions?.onEditClicked(absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[holder.absoluteAdapterPosition]
        holder.binding.name.text = user.name
        var role = user.role
        if(role == Constants.ROLES.SERVICE_PROVIDER.name)
            role = user.service?.uppercase()
        holder.binding.info.text =
            "${user.countryCode}-${user.mobileNumber}\n$role"
        if (user.role == Constants.ROLES.SERVICE_PROVIDER.name)
            holder.binding.edit.visibility = View.VISIBLE
        else
            holder.binding.edit.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return users.size
    }

}