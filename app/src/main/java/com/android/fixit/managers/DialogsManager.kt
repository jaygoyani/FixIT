package com.android.fixit.managers

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.android.fixit.R
import com.android.fixit.databinding.*
import com.android.fixit.dtos.ImageDTO
import com.android.fixit.dtos.UserDTO
import com.android.fixit.interfaces.IDialogActions
import com.android.fixit.utils.DialogActions
import com.android.fixit.utils.Helper
import com.bumptech.glide.Glide

object DialogsManager {
    private var loader: Dialog? = null

    fun showProgressDialog(context: Context) {
        dismissProgressDialog()
        try {
            loader = Dialog(context)
            loader?.setContentView(R.layout.dialog_loader)
            loader?.setCancelable(false)
            loader?.window?.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            loader?.window?.setWindowAnimations(R.style.scaling_from_center)
            loader?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            loader?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismissProgressDialog() {
        try {
            loader?.dismiss()
        } catch (err: Exception) {
            err.printStackTrace()
        }
    }

    fun showConfirmationDialog(context: Context, msg: String, actions: IDialogActions?) {
        val dialog = Dialog(context)
        val binding = DialogConfirmationBinding.inflate(
            LayoutInflater.from(context)
        )
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setWindowAnimations(R.style.scaling_from_center)
        binding.message.text = msg
        binding.submit.setOnClickListener {
            actions?.onYesClicked()
            dialog.dismiss()
        }
        binding.cancel.setOnClickListener {
            actions?.onNoClicked()
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showConfirmationDialog(
        context: Context, msg: String, yesBtn: String, noBtn: String, actions: IDialogActions?
    ) {
        val dialog = Dialog(context)
        val binding = DialogConfirmationBinding.inflate(
            LayoutInflater.from(context)
        )
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setWindowAnimations(R.style.scaling_from_center)
        binding.message.text = msg
        binding.cancel.text = noBtn
        binding.submit.text = yesBtn
        binding.submit.setOnClickListener {
            actions?.onYesClicked()
            dialog.dismiss()
        }
        binding.cancel.setOnClickListener {
            actions?.onNoClicked()
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showErrorDialog(
        context: Context,
        msg: String, btn: String, actions: IDialogActions?
    ) {
        val dialog = Dialog(context)
        val binding = DialogErrorBinding.inflate(
            LayoutInflater.from(context)
        )
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setWindowAnimations(R.style.scaling_from_center)
        binding.message.text = msg
        binding.submit.text = btn
        binding.submit.setOnClickListener {
            actions?.onYesClicked()
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showInputDialog(context: Context, header: String, msg: String, actions: IDialogActions?) {
        val dialog = Dialog(context)
        val binding = DialogInputBinding.inflate(
            LayoutInflater.from(context)
        )
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setWindowAnimations(R.style.scaling_from_center)
        binding.header.text = header
        binding.message.text = msg
        binding.submit.setOnClickListener {
            val txt = binding.inputEt.text.toString().trim()
            if (txt.isEmpty()) {
                Helper.showToast(context, "Input field cannot be empty!")
                return@setOnClickListener
            }
            actions?.onSubmitClicked(txt)
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showEditInputDialog(
        context: Context,
        header: String,
        msg: String,
        text: String,
        actions: IDialogActions?
    ) {
        val dialog = Dialog(context)
        val binding = DialogInputBinding.inflate(
            LayoutInflater.from(context)
        )
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setWindowAnimations(R.style.scaling_from_center)
        binding.header.text = header
        binding.message.text = msg
        binding.inputEt.setText(text)
        binding.submit.setOnClickListener {
            val txt = binding.inputEt.text.toString().trim()
            if (txt.isEmpty()) {
                Helper.showToast(context, "Input field cannot be empty!")
                return@setOnClickListener
            }
            actions?.onSubmitClicked(txt)
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showEnlargedImage(context: Context, image: ImageDTO) {
        val dialog = Dialog(context)
        val binding = DialogImageBinding.inflate(
            LayoutInflater.from(context)
        )
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setWindowAnimations(R.style.scaling_from_center)
        Glide.with(context)
            .load(image.url ?: image.uri)
            .into(binding.image)
        dialog.show()
    }

    fun displayTechniciansDialog(
        context: Context, users: ArrayList<UserDTO>, actions: DialogActions?
    ) {
        val dialog = Dialog(context)
        val binding = DialogTechniciansBinding.inflate(
            LayoutInflater.from(context)
        )
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setWindowAnimations(R.style.scaling_from_center)
        val names = ArrayList<String>()
        users.forEach { user ->
            names.add("${user.name} ($${user.pricePerHr}/Hr)")
        }
        names.add(0, "Select a Technician")
        binding.spinner.adapter =
            ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, names)
        binding.continueBtn.setOnClickListener {
            val pos = binding.spinner.selectedItemPosition
            if (pos == 0) {
                Helper.showToast(context, "Please select a technician")
                return@setOnClickListener
            }
            actions?.onSubmitClicked("${pos - 1}")
            dialog.dismiss()
        }
        dialog.show()
    }
}