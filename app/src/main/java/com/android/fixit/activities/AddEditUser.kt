package com.android.fixit.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.android.fixit.R
import com.android.fixit.databinding.ActivityAddEditUserBinding
import com.android.fixit.dtos.ServiceDTO
import com.android.fixit.dtos.UserDTO
import com.android.fixit.managers.DialogsManager
import com.android.fixit.utils.Constants
import com.android.fixit.utils.Helper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AddEditUser : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditUserBinding
    private var user: UserDTO? = null
    private val services = ArrayList<String>()
    private val context: Context = this
    private var servicesAdapter: ArrayAdapter<String>? = null
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialise()
        setListeners()
        getRoles()
    }

    private fun getRoles() {
        DialogsManager.showProgressDialog(context)
        firestore.collection(Constants.COLLECTION_SERVICES)
            .orderBy("label", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener { it ->
                DialogsManager.dismissProgressDialog()
                if (it.isSuccessful) {
                    if (it.result != null && it.result.documents.isNotEmpty()) {
                        it.result.documents.forEach {
                            val record = it.toObject(ServiceDTO::class.java)
                            services.add(record?.label ?: "N/A")
                        }
                        servicesAdapter?.notifyDataSetChanged()
                        for (i in 0 until services.size) {
                            if (services[i] == user?.service) {
                                binding.rolesSpinner.setSelection(i)
                                break
                            }
                        }
                    } else
                        Helper.showToast(context, "Empty services list!")
                } else
                    Helper.showToast(context, "Failed to fetch the roles!")
            }
    }

    private fun setListeners() {
        binding.back.setOnClickListener {
            finish()
        }

        binding.continueBtn.setOnClickListener {
            val name = binding.nameEt.text.toString().trim()
            val number = binding.mobileNumber.text.toString().trim()
            val email = binding.emailEt.text.toString().trim()
            val pricePerHr = binding.priceEt.text.toString().trim()
            if (name.isEmpty() || email.isEmpty() || number.isEmpty() || pricePerHr.isEmpty() || binding.rolesSpinner.selectedItemPosition == 0) {
                Helper.showToast(context, getString(R.string.details_missing))
                return@setOnClickListener
            }
            if (email.indexOf("@") < 0 || email.indexOf(".") < 0) {
                Helper.showToast(context, getString(R.string.invalid_email))
                return@setOnClickListener
            }
            if (user == null) {
                val record = UserDTO(
                    name,
                    binding.countryCode.selectedCountryNameCode,
                    "+${binding.countryCode.selectedCountryCode}",
                    number, email, "", Constants.ROLES.SERVICE_PROVIDER.name,
                    binding.rolesSpinner.selectedItem.toString(),
                    pricePerHr.toInt()
                )
                addUser(record)
            } else {
                val map = HashMap<String, Any>()
                map["name"] = name
                map["countryNameCode"] = binding.countryCode.selectedCountryNameCode
                map["countryCode"] = "+${binding.countryCode.selectedCountryCode}"
                map["mobileNumber"] = number
                map["emailAddress"] = email
                map["service"] = binding.rolesSpinner.selectedItem.toString()
                map["pricePerHr"] = pricePerHr.toInt()
                updateUser(map)
            }
        }
    }

    private fun updateUser(map: HashMap<String, Any>) {
        DialogsManager.showProgressDialog(context)
        firestore.collection(Constants.COLLECTION_USERS).document(
            Helper.getUserIndex(user!!)
        ).update(map)
            .addOnCompleteListener {
                DialogsManager.dismissProgressDialog()
                if (it.isSuccessful)
                    Helper.showToast(context, "Updated!")
                else {
                    it.exception?.printStackTrace()
                    Helper.showToast(context, "Failed to update user!")
                }
            }
    }

    private fun addUser(record: UserDTO) {
        DialogsManager.showProgressDialog(context)
        firestore.collection(Constants.COLLECTION_USERS).document(Helper.getUserIndex(record))
            .set(record)
            .addOnCompleteListener {
                DialogsManager.dismissProgressDialog()
                if (it.isSuccessful) {
                    resetFields()
                    Helper.showToast(context, "Added!")
                } else
                    Helper.showToast(context, "Failed to add user!")
            }
    }

    private fun resetFields() {
        binding.nameEt.setText("")
        binding.countryCode.resetToDefaultCountry()
        binding.mobileNumber.setText("")
        binding.emailEt.setText("")
        binding.rolesSpinner.setSelection(0)
    }

    private fun initialise() {
        services.add("Select a role")
        servicesAdapter =
            ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, services)
        binding.rolesSpinner.adapter = servicesAdapter
        user = intent?.getSerializableExtra("user") as UserDTO?
        if (user == null) {
            binding.label.text = getString(R.string.add_user)
        } else {
            binding.label.text = getString(R.string.update_user)
            binding.nameEt.setText(user?.name)
            binding.emailEt.setText(user?.emailAddress)
            binding.countryCode.setDefaultCountryUsingNameCode(user?.countryNameCode)
            binding.countryCode.resetToDefaultCountry()
            binding.mobileNumber.setText(user?.mobileNumber)
        }
    }

}