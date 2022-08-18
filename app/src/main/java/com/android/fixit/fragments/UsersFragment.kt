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
import com.android.fixit.R
import com.android.fixit.adapters.ServicesAdapter
import com.android.fixit.adapters.UsersAdapter
import com.android.fixit.databinding.FragmentUsersBinding
import com.android.fixit.dtos.ServiceDTO
import com.android.fixit.dtos.UserDTO
import com.android.fixit.managers.DialogsManager
import com.android.fixit.utils.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UsersFragment : Fragment() {
    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!
    private var adapter: UsersAdapter? = null
    private val records = ArrayList<UserDTO>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialise()
        setListeners()
        getUsers()
    }

    private fun getUsers() {
        DialogsManager.showProgressDialog(requireContext())
        firestore.collection(Constants.COLLECTION_USERS)
            .orderBy("insertedAt", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener {
                DialogsManager.dismissProgressDialog()
                if (it.isSuccessful) {
                    if (it.result != null && !it.result.isEmpty) {
                        records.clear()
                        for (i in 0 until it.result.documents.size) {
                            val record = it.result.documents[i].toObject(UserDTO::class.java)
                                ?: continue
                            records.add(record)
                        }
                        adapter?.notifyDataSetChanged()
                    } else
                        Helper.showToast(requireContext(), "No User Record(s) found!")
                } else
                    Helper.showToast(requireContext(), "Failed to fetch user records")
            }
    }

    private fun setListeners() {
        binding.layout.setOnClickListener {
            Navigator.toAddEditUserActivity(null)
        }
    }

    override fun onResume() {
        getUsers()
        super.onResume()
    }

    private fun initialise() {
        binding.usersList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = UsersAdapter(requireContext(), records, object : ListActions() {
            override fun onEditClicked(pos: Int) {
                Navigator.toAddEditUserActivity(records[pos])
            }
        })
        binding.usersList.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        binding.usersList.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}