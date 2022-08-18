package com.android.fixit.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.android.fixit.BuildConfig
import com.android.fixit.R
import com.android.fixit.databinding.ActivityLoginBinding
import com.android.fixit.dtos.UserDTO
import com.android.fixit.managers.DialogsManager
import com.android.fixit.managers.PrefManager
import com.android.fixit.utils.Constants
import com.android.fixit.utils.Helper
import com.android.fixit.utils.Navigator
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private val context = this
    private lateinit var binding: ActivityLoginBinding
    private val registrationCode = 40
    private val timeInterval = 30L
    private var verificationID: String? = null
    private var countDownTimer: CountDownTimer? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialise()
        setListeners()
    }

    private fun initialise() {
        displayNumberLayout(false)
        if (BuildConfig.DEBUG) {
            binding.countryCode.setAutoDetectedCountry(true)
            binding.mobileNumber.setText("8008283461")
        }
    }

    private fun setListeners() {
        binding.continueBtn.setOnClickListener {
            if (binding.numberLayout.visibility == View.VISIBLE)
                sendOTP()
            else
                verifyOTP()
        }
        binding.resendOtp.setOnClickListener {
            sendOTP()
        }
        binding.editNumber.setOnClickListener {
            displayNumberLayout(false)
        }
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to exit?")
        builder.setPositiveButton("YES") { _: DialogInterface?, _: Int -> finish() }
        builder.setNegativeButton("NO") { _: DialogInterface?, _: Int -> }
        builder.show()
    }

    override fun onDestroy() {
        stopTimer()
        super.onDestroy()
    }

    private fun sendOTP() {
        if (!Helper.isNetworkAvailable()) {
            Helper.showToast(context, getString(R.string.no_internet))
            return
        }
        val mobile: String = binding.mobileNumber.text.toString().trim()
        when {
            mobile.isEmpty() -> DialogsManager.showErrorDialog(
                context,
                getString(R.string.empty_mobile_number),
                getString(R.string.ok), null
            )
            else -> {
                DialogsManager.showProgressDialog(context)
                val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onCodeAutoRetrievalTimeOut(p0: String) {}

                    override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                        super.onCodeSent(p0, p1)
                        verificationID = p0
                        DialogsManager.dismissProgressDialog()
                        Helper.showToast(
                            context, getString(R.string.code_sent)
                        )
                        displayVerifyLayout()
                        startTimer()
                    }

                    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                        if (phoneAuthCredential.smsCode != null)
                            binding.otpEt.setText(phoneAuthCredential.smsCode)
                        Helper.showToast(
                            context,
                            getString(R.string.verification_success)
                        )
                        stopTimer()
                        login(phoneAuthCredential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        DialogsManager.dismissProgressDialog()
                        e.printStackTrace()
                        when (e) {
                            is FirebaseTooManyRequestsException -> Helper.showToast(
                                context,
                                getString(R.string.too_many_verify_attempts)
                            )
                            is FirebaseAuthException -> Helper.showToast(
                                context,
                                context.getString(R.string.server_contact_failed)
                            )
                            else -> Helper.showToast(
                                context,
                                getString(R.string.verification_failed)
                            )
                        }
                    }
                }
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber("+${binding.countryCode.selectedCountryCode}$mobile")
                    .setTimeout(30L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(callback)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }
    }

    private fun startTimer() {
        binding.timer.text = "20S"
        binding.resendOtp.isEnabled = false
        binding.resendOtp.setTextColor(ContextCompat.getColor(context, R.color.grey))
        countDownTimer = object : CountDownTimer(timeInterval * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.timer.text = "${millisUntilFinished / 1000}S"
            }

            override fun onFinish() {
                stopTimer()
            }
        }.start()
    }

    private fun displayVerifyLayout() {
        binding.numberLayout.visibility = View.GONE
        binding.verificationLayout.visibility = View.VISIBLE
        binding.header.text = getString(R.string.verification_header)
        binding.otpEt.setText("")
        binding.continueBtn.text = getString(R.string.proceed)
        binding.continueBtn.visibility = View.VISIBLE
    }

    private fun login(credential: PhoneAuthCredential) {
        DialogsManager.showProgressDialog(context)
        if (auth.currentUser != null)
            auth.signOut()
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                when {
                    task.isSuccessful -> {
                        firestore.collection(Constants.COLLECTION_USERS)
                            .document(
                                Helper.getUserIndex(
                                    binding.countryCode.selectedCountryCode,
                                    binding.mobileNumber.text.toString().trim()
                                )
                            )
                            .get()
                            .addOnCompleteListener { task1: Task<DocumentSnapshot?> ->
                                DialogsManager.dismissProgressDialog()
                                if (task1.isSuccessful) {
                                    val document = task1.result
                                    if (document != null && document.exists()) {
                                        val user: UserDTO? = document.toObject(UserDTO::class.java)
                                        if (user != null) {
                                            PrefManager.saveUserDTO(user)
                                            navigate()
                                        } else {
                                            DialogsManager.showErrorDialog(
                                                context,
                                                getString(R.string.failed_to_find_user),
                                                getString(R.string.ok), null
                                            )
                                        }
                                    } else {
                                        Navigator.toRegistrationActivity(
                                            binding.countryCode.selectedCountryNameCode,
                                            binding.mobileNumber.text.toString().trim()
                                        )
                                        displayNumberLayout(true)
                                    }
                                } else {
                                    DialogsManager.showErrorDialog(
                                        context,
                                        getString(R.string.data_fetching_failed),
                                        getString(R.string.ok), null
                                    )
                                }
                            }
                    }
                    task.exception is FirebaseAuthInvalidCredentialsException -> {
                        DialogsManager.dismissProgressDialog()
                        Helper.showToast(
                            context,
                            getString(R.string.incorrect_code)
                        )
                    }
                    else -> {
                        DialogsManager.dismissProgressDialog()
                        Helper.showToast(
                            context, getString(R.string.sign_in_failed)
                        )
                    }
                }
            }
    }

    private fun navigate() {
        val user = PrefManager.getUserDTO()
        Helper.subscribeToTopic(Helper.getTopic(user))
        if (user.isAdmin())
            Helper.subscribeToTopic(Constants.TOPIC_ADMIN)
        when {
            user.isAdmin() -> Navigator.toMainActivity()
            user.isUser() -> Navigator.toUserMainActivity()
            else -> Navigator.toWorkerMainActivity()
        }
        finish()
    }

    private fun verifyOTP() {
        if (!Helper.isNetworkAvailable()) {
            Helper.showToast(context, getString(R.string.no_internet))
            return
        }
        val valOTP: String = binding.otpEt.text.toString().trim { it <= ' ' }
        if (valOTP.isEmpty()) {
            Helper.showToast(context, getString(R.string.empty_otp))
            return
        }
        if (verificationID == null) {
            DialogsManager.showErrorDialog(
                context,
                context.getString(R.string.error_occurred),
                getString(R.string.ok),
                null
            )
            return
        }
        stopTimer()
        DialogsManager.showProgressDialog(context)
        val credential = PhoneAuthProvider.getCredential(
            verificationID!!,
            binding.otpEt.text.toString().trim { it <= ' ' }
        )
        login(credential)
    }

    private fun displayNumberLayout(clearText: Boolean) {
        binding.numberLayout.visibility = View.VISIBLE
        binding.verificationLayout.visibility = View.GONE
        if (clearText)
            binding.mobileNumber.setText("")
        binding.header.text = getString(R.string.sign_in)
        binding.continueBtn.text = getString(R.string.continue_label)
        verificationID = null
        stopTimer()
    }

    private fun stopTimer() {
        binding.timer.text = ""
        binding.resendOtp.isEnabled = true
        binding.resendOtp.setTextColor(ContextCompat.getColor(context, R.color.black))
        countDownTimer?.cancel()
        countDownTimer = null
    }

}