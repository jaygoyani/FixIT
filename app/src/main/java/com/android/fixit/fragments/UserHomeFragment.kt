package com.android.fixit.fragments

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.fixit.adapters.WorkersAdapter
import com.android.fixit.databinding.FragmentUserHomeBinding
import com.android.fixit.dtos.ServiceDTO
import com.android.fixit.dtos.UserDTO
import com.android.fixit.managers.DialogsManager
import com.android.fixit.utils.Constants
import com.android.fixit.utils.Helper
import com.android.fixit.utils.ListActions
import com.android.fixit.utils.Navigator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import kotlin.collections.ArrayList

class UserHomeFragment : Fragment() {
    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private val services = ArrayList<ServiceDTO>()
    private val servicesList = ArrayList<String>()
    private var servicesAdapter: ArrayAdapter<String>? = null
    private var workers = ArrayList<UserDTO>()
    private var filteredWorkers = ArrayList<UserDTO>()
    private var adapter: WorkersAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialise()
        setListeners()
        getServices()
    }

    private fun setListeners() {
        binding.rolesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val service = if (p2 == 0)
                    ""
                else
                    servicesList[p2]
                filterWorkers(service)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun filterWorkers(service: String) {
        filteredWorkers.clear()
        workers.forEach {
            if (it.service?.toLowerCase(Locale.ROOT)
                    ?.contains(service.toLowerCase(Locale.ROOT)) == true
            )
                filteredWorkers.add(it)
        }
        adapter?.notifyDataSetChanged()
        if (filteredWorkers.isEmpty())
            binding.noRecords.visibility = View.VISIBLE
        else
            binding.noRecords.visibility = View.GONE
    }

    private fun initialise() {
        servicesAdapter =
            ArrayAdapter(requireContext(), R.layout.simple_spinner_dropdown_item, servicesList)
        binding.rolesSpinner.adapter = servicesAdapter
        binding.workersList.layoutManager = LinearLayoutManager(requireContext())
        binding.workersList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        adapter = WorkersAdapter(requireContext(), filteredWorkers, object : ListActions() {
            override fun onItemSelected(pos: Int) {
                val worker = workers[pos]
                Navigator.toPostJobActivity(worker)
            }
        })
        binding.workersList.adapter = adapter
    }

    private fun getServices() {
        DialogsManager.showProgressDialog(requireContext())
        firestore.collection(Constants.COLLECTION_SERVICES)
            .orderBy("label", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener { it ->
                services.clear()
                servicesList.clear()
                if (it.isSuccessful && it.result != null) {
                    val doc = ServiceDTO("Select a service")
                    services.add(doc)
                    servicesList.add(doc.label!!)
                    if (it.result.documents.isNotEmpty()) {
                        it.result.documents.forEach {
                            val record = it.toObject(ServiceDTO::class.java)
                            services.add(record!!)
                            servicesList.add(record.label ?: "N/A")
                        }
                        servicesAdapter?.notifyDataSetChanged()
                    }
                    getServiceProviders()
                } else {
                    Helper.logError(it.exception?.message)
                    it.exception?.printStackTrace()
                    DialogsManager.dismissProgressDialog()
                    Helper.showToast(requireContext(), "Failed to fetch the roles!")
                }
            }
    }

    private fun getServiceProviders() {
        firestore.collection(Constants.COLLECTION_USERS)
            .whereEqualTo("role", Constants.ROLES.SERVICE_PROVIDER.name)
            .orderBy("name", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener {
                DialogsManager.dismissProgressDialog()
                if (it.isSuccessful) {
                    if (it.result != null && !it.result.isEmpty) {
                        workers.clear()
                        for (i in 0 until it.result.documents.size) {
                            val record = it.result.documents[i].toObject(UserDTO::class.java)
                                ?: continue
                            workers.add(record)
                        }
                        filterWorkers("")
                    } else
                        Helper.showToast(requireContext(), "No Worker Record(s) found!")
                } else
                    Helper.showToast(requireContext(), "Failed to fetch worker(s) records")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}