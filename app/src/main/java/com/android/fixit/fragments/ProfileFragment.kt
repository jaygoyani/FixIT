package com.android.fixit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.fixit.R
import com.android.fixit.databinding.FragmentProfileBinding
import com.android.fixit.dtos.UserDTO
import com.android.fixit.managers.DialogsManager
import com.android.fixit.managers.PrefManager
import com.android.fixit.utils.Constants
import com.android.fixit.utils.Helper
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private val user = PrefManager.getUserDTO()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialise()
        setListeners()
    }

    private fun initialise() {
        binding?.nameEt?.setText(user.name)
        binding?.countryCode?.setDefaultCountryUsingNameCode(user.countryNameCode)
        binding?.mobileNumber?.setText(user.mobileNumber)
        binding?.emailEt?.setText(user.emailAddress)
        binding?.addressEt?.setText(user.address)
    }

    private fun setListeners() {
        binding?.mobileLayer?.setOnClickListener {
            Helper.showToast(requireContext(), getString(R.string.cannot_edit_mobile))
            return@setOnClickListener
        }
        binding?.continueBtn?.setOnClickListener {
            val name = binding?.nameEt?.text?.toString()?.trim()
            val countryCode = binding?.countryCode?.selectedCountryCode
            val countryNameCode = binding?.countryCode?.selectedCountryNameCode
            val mobile = binding?.mobileNumber?.text?.toString()?.trim()
            val email = binding?.emailEt?.text?.toString()?.trim()
            val address = binding?.addressEt?.text?.toString()?.trim()
            if (name.isNullOrEmpty() || email.isNullOrEmpty() || mobile.isNullOrEmpty() || address.isNullOrEmpty()) {
                Helper.showToast(requireContext(), getString(R.string.details_missing))
                return@setOnClickListener
            }
            if (email.indexOf("@") < 0 || email.indexOf(".") < 0) {
                Helper.showToast(requireContext(), getString(R.string.invalid_email))
                return@setOnClickListener
            }
            user.updateDetails(
                name, countryCode!!, countryNameCode!!, mobile, email, address
            )
            updateUserDetails(user)
        }
    }

    private fun updateUserDetails(user: UserDTO) {
        DialogsManager.showProgressDialog(requireContext())
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_USERS)
            .document(Helper.getUserIndex(user))
            .set(user)
            .addOnCompleteListener {
                DialogsManager.dismissProgressDialog()
                if(it.isSuccessful) {
                    PrefManager.saveUserDTO(user)
                    Helper.showToast(requireContext(), getString(R.string.updated))
                } else
                    Helper.showToast(requireContext(), getString(R.string.profile_update_failed))
            }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}