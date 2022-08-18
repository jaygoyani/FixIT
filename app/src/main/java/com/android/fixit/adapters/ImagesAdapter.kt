package com.android.fixit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.fixit.R
import com.android.fixit.databinding.ItemImageBinding
import com.android.fixit.dtos.ImageDTO
import com.android.fixit.interfaces.IListActions
import com.android.fixit.managers.DialogsManager
import com.android.fixit.utils.DialogActions
import com.bumptech.glide.Glide

class ImagesAdapter(
    val context: Context, val images: ArrayList<ImageDTO>, val actions: IListActions?
) : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {
    var disableEditing = false

    inner class ViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        init {
            binding.root.setOnClickListener {
                actions?.onItemClicked(absoluteAdapterPosition)
            }
            binding.close.setOnClickListener {
                DialogsManager.showConfirmationDialog(
                    context,
                    context.getString(R.string.remove_image_confirmation),
                    object : DialogActions() {
                        override fun onYesClicked() {
                            actions?.onDeleteClicked(absoluteAdapterPosition)
                        }
                    })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemImageBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = images[holder.absoluteAdapterPosition]
        val binding = holder.binding
        if (image.uri != null || image.url != null) {
            binding.background.visibility = View.GONE
            Glide.with(context)
                .load(image.url ?: image.uri)
                .into(binding.image)
        } else
            binding.background.visibility = View.VISIBLE
        if (image.isEmpty() || disableEditing)
            binding.close.visibility = View.GONE
        else
            binding.close.visibility = View.VISIBLE
    }

    override fun getItemCount(): Int {
        return images.size
    }
}