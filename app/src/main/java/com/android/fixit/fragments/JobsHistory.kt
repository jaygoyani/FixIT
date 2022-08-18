package com.android.fixit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.fixit.R
import com.android.fixit.activities.JobDetailsActivity
import com.android.fixit.adapters.JobsAdapter
import com.android.fixit.databinding.FragmentJobsHistoryBinding
import com.android.fixit.dtos.JobDTO
import com.android.fixit.managers.DialogsManager
import com.android.fixit.managers.PrefManager
import com.android.fixit.utils.Constants
import com.android.fixit.utils.Helper
import com.android.fixit.utils.ListActions
import com.android.fixit.utils.Navigator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class JobsHistory : Fragment() {
    private var _binding: FragmentJobsHistoryBinding? = null
    private val binding get() = _binding!!
    private var adapter: JobsAdapter? = null
    private val jobs = ArrayList<JobDTO>()
    private val firestore = FirebaseFirestore.getInstance()
    private val user = PrefManager.getUserDTO()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobsHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialise()
        getJobs()
    }

    override fun onResume() {
        if (JobDetailsActivity.isJobUpdated)
            getJobs()
        super.onResume()
    }

    private fun getJobs() {
        DialogsManager.showProgressDialog(requireContext())
        val query = if (user.role == Constants.ROLES.SERVICE_PROVIDER.name) {
            firestore.collection(Constants.COLLECTION_JOBS)
                .whereEqualTo("technicianMobile", Helper.getUserIndex(user))
        } else {
            firestore.collection(Constants.COLLECTION_JOBS)
                .whereEqualTo("userMobile", Helper.getUserIndex(user))
        }
        query.orderBy("insertedAt", Query.Direction.DESCENDING)
            .get().addOnCompleteListener {
                DialogsManager.dismissProgressDialog()
                if (it.isSuccessful && it.result != null) {
                    jobs.clear()
                    it.result!!.documents.forEach { doc ->
                        val dto = doc.toObject(JobDTO::class.java)
                        dto?.id = doc.id
                        jobs.add(dto!!)
                    }
                    adapter?.notifyDataSetChanged()
                    if (jobs.isEmpty())
                        binding.noRecords.visibility = View.VISIBLE
                    else
                        binding.noRecords.visibility = View.GONE
                } else
                    Helper.showToast(requireContext(), getString(R.string.jobs_fetching_failed))
            }
    }

    private fun initialise() {
        binding.jobsList.layoutManager = LinearLayoutManager(context)
        adapter = JobsAdapter(requireContext(), jobs, object : ListActions() {
            override fun onItemClicked(pos: Int) {
                val job = jobs[pos]
                Navigator.toJobDetailsActivity(job)
            }
        })
        binding.jobsList.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}