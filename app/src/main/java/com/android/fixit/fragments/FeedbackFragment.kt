package com.android.fixit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.fixit.R
import com.android.fixit.databinding.FragFeedbackBinding
import com.android.fixit.dtos.FeedbackDTO
import com.android.fixit.managers.DialogsManager
import com.android.fixit.utils.Constants
import com.android.fixit.utils.Helper
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackFragment : Fragment() {
    private var binding: FragFeedbackBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragFeedbackBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setListeners()
    }

    private fun setListeners() {
        binding?.submit?.setOnClickListener {
            val subject = binding?.subject?.text?.trim()
            val description = binding?.description?.text?.trim()
            if (subject.isNullOrEmpty() || description.isNullOrEmpty()) {
                Helper.showToast(requireContext(), getString(R.string.details_missing))
                return@setOnClickListener
            }
            saveFeedback(subject.toString(), description.toString())
        }
    }

    private fun saveFeedback(subject: String, description: String) {
        DialogsManager.showProgressDialog(requireContext())
        val record = FeedbackDTO(subject, description)
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_FEEDBACK)
            .add(record)
            .addOnCompleteListener {
                DialogsManager.dismissProgressDialog()
                if (it.isSuccessful) {
                    binding?.subject?.setText("")
                    binding?.description?.setText("")
                    Helper.showToast(requireContext(), "Posted Successfully!!!")
                } else
                    Helper.showToast(requireContext(), "Failed to post the feedback")
            }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}