package com.android.fixit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.fixit.adapters.FeedbacksAdapter
import com.android.fixit.databinding.FragmentFeedbacksBinding
import com.android.fixit.dtos.FeedbackDTO
import com.android.fixit.managers.DialogsManager
import com.android.fixit.utils.Constants
import com.android.fixit.utils.Helper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FeedbacksFragment : Fragment() {
    private var binding: FragmentFeedbacksBinding? = null
    private var adapter: FeedbacksAdapter? = null
    private val records = ArrayList<FeedbackDTO>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFeedbacksBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialise()
        getRecords()
    }

    private fun getRecords() {
        DialogsManager.showProgressDialog(requireContext())
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_FEEDBACK)
            .orderBy("insertedAt", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener {
                DialogsManager.dismissProgressDialog()
                if (it.isSuccessful && it.result != null) {
                    it.result.documents.forEach {
                        val record = it.toObject(FeedbackDTO::class.java)
                        records.add(record!!)
                    }
                    adapter?.notifyDataSetChanged()
                    if (records.isEmpty())
                        Helper.showToast(requireContext(), "No record(s) found!")
                } else
                    Helper.showToast(requireContext(), "Failed to fetch the records")
            }
    }

    private fun initialise() {
        adapter = FeedbacksAdapter(requireContext(), records)
        binding?.feedbacks?.layoutManager = LinearLayoutManager(requireContext())
        binding?.feedbacks?.adapter = adapter
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}