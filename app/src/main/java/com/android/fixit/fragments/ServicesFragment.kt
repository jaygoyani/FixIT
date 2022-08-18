package com.android.fixit.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.fixit.R
import com.android.fixit.adapters.ServicesAdapter
import com.android.fixit.databinding.FragmentServicesBinding
import com.android.fixit.dtos.ServiceDTO
import com.android.fixit.interfaces.IListActions
import com.android.fixit.managers.DialogsManager
import com.android.fixit.utils.Constants
import com.android.fixit.utils.DialogActions
import com.android.fixit.utils.Helper
import com.android.fixit.utils.ListActions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.lang.Thread.sleep

class ServicesFragment : Fragment() {
    private var _binding: FragmentServicesBinding? = null
    private val binding get() = _binding!!
    private var adapter: ServicesAdapter? = null
    private val services = ArrayList<ServiceDTO>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialise()
        setListeners()
        getServiceRecords()
    }

    private fun setListeners() {
        binding.layout.setOnClickListener {
            DialogsManager.showInputDialog(requireContext(),
                getString(R.string.add_service),
                getString(R.string.enter_service_name),
                object : DialogActions() {
                    override fun onSubmitClicked(txt: String) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            addServiceRecord(txt)
                        }, 500)
                    }
                })
        }
    }

    private fun addServiceRecord(txt: String) {
        DialogsManager.showProgressDialog(requireContext())
        val record = ServiceDTO(txt)
        val id = firestore.collection(Constants.COLLECTION_SERVICES).document().id
        firestore.collection(Constants.COLLECTION_SERVICES).document(id).set(record)
            .addOnCompleteListener {
                DialogsManager.dismissProgressDialog()
                if (it.isSuccessful) {
                    record.refId = id
                    services.add(record)
                    adapter?.notifyDataSetChanged()
                    Helper.showToast(requireContext(), getString(R.string.success_msg))
                } else
                    Helper.showToast(requireContext(), "Failed to add service record")
            }
    }

    private fun getServiceRecords() {
        DialogsManager.showProgressDialog(requireContext())
        firestore.collection(Constants.COLLECTION_SERVICES)
            .orderBy("insertedAt", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener {
                DialogsManager.dismissProgressDialog()
                if (it.isSuccessful) {
                    if (it.result != null && !it.result.isEmpty) {
                        services.clear()
                        for (i in 0 until it.result.documents.size) {
                            val record = it.result.documents[i].toObject(ServiceDTO::class.java)
                                ?: continue
                            record.refId = it.result.documents[i].id
                            services.add(record)
                        }
                        adapter?.notifyDataSetChanged()
                    } else
                        Helper.showToast(requireContext(), "No Service Record(s) found!")
                } else
                    Helper.showToast(requireContext(), "Failed to fetch service records")
            }
    }

    private fun initialise() {
        binding.servicesList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = ServicesAdapter(requireContext(), services, object : ListActions() {
            override fun onEditClicked(pos: Int) {
                DialogsManager.showEditInputDialog(requireContext(),
                    getString(R.string.update_service),
                    getString(R.string.enter_service_name),
                    services[pos].label!!,
                    object : DialogActions() {
                        override fun onSubmitClicked(txt: String) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                editServiceRecord(pos, txt)
                            }, 500)
                        }
                    })
            }
        })
        binding.servicesList.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        binding.servicesList.adapter = adapter
    }

    private fun editServiceRecord(pos: Int, txt: String) {
        DialogsManager.showProgressDialog(requireContext())
        val map = HashMap<String, Any>()
        map["label"] = txt
        firestore.collection(Constants.COLLECTION_SERVICES).document(services[pos].refId!!)
            .update(map)
            .addOnCompleteListener {
                DialogsManager.dismissProgressDialog()
                if (it.isSuccessful) {
                    services[pos].label = txt
                    adapter?.notifyItemChanged(pos)
                    Helper.showToast(requireContext(), getString(R.string.success_msg))
                } else
                    Helper.showToast(requireContext(), "Failed to update service record")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}