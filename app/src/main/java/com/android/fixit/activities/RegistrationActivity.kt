package com.android.fixit.activities

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.fixit.BuildConfig
import com.android.fixit.R
import com.android.fixit.databinding.ActivityRegistrationBinding
import com.android.fixit.dtos.UserDTO
import com.android.fixit.utils.Constants
import com.android.fixit.utils.Helper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationActivity : AppCompatActivity() {
    private val context: Context = this
    private lateinit var binding: ActivityRegistrationBinding
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialise()
        setListeners()
    }

    private fun setListeners() {
        binding.back.setOnClickListener {
            onBackPressed()
        }
        binding.continueBtn.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name = binding.nameEt.text.toString().trim()
        val number = binding.mobileNumber.text.toString().trim()
        val email = binding.emailEt.text.toString().trim()
        val address = binding.addressEt.text.toString().trim()
        if (name.isEmpty() || email.isEmpty() || number.isEmpty() || address.isEmpty()) {
            Helper.showToast(context, getString(R.string.details_missing))
            return
        }
        if (email.indexOf("@") < 0 || email.indexOf(".") < 0) {
            Helper.showToast(context, getString(R.string.invalid_email))
            return
        }
        val user = UserDTO(
            name,
            binding.countryCode.selectedCountryNameCode,
            "+${binding.countryCode.selectedCountryCode}",
            number,
            email,
            address,
            Constants.ROLES.USER.name
        )
        firestore.collection(Constants.COLLECTION_USERS)
            .document(Helper.getUserIndex(user))
            .set(user)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Helper.showToast(context, getString(R.string.registered))
                    FirebaseAuth.getInstance().signOut()
                    onBackPressed()
                } else
                    Helper.showToast(context, getString(R.string.registration_failed))
            }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }

    private fun initialise() {
        val countryCode = intent?.getStringExtra("country_code")
        val mobile = intent?.getStringExtra("mobile")
        binding.countryCode.setDefaultCountryUsingNameCode(countryCode)
        binding.countryCode.resetToDefaultCountry()
        binding.mobileNumber.setText(mobile)
    }

}