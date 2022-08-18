package com.android.fixit.activities

import android.app.DatePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.DatePicker
import androidx.recyclerview.widget.GridLayoutManager
import com.android.fixit.R
import com.android.fixit.adapters.ImagesAdapter
import com.android.fixit.databinding.ActivityJobDetailsBinding
import com.android.fixit.dtos.ImageDTO
import com.android.fixit.dtos.JobDTO
import com.android.fixit.dtos.UserDTO
import com.android.fixit.interfaces.IDialogActions
import com.android.fixit.managers.DatesManager
import com.android.fixit.managers.DialogsManager
import com.android.fixit.managers.PrefManager
import com.android.fixit.utils.Constants
import com.android.fixit.utils.DialogActions
import com.android.fixit.utils.Helper
import com.android.fixit.utils.ListActions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.rpc.Help
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class JobDetailsActivity : AppCompatActivity() {
    private val context: Context = this
    private lateinit var binding: ActivityJobDetailsBinding
    private lateinit var job: JobDTO
    private val technicians = ArrayList<UserDTO>()
    private val firestore = FirebaseFirestore.getInstance()
    private val user = PrefManager.getUserDTO()

    companion object {
        var isJobUpdated = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        isJobUpdated = false
        title = getString(R.string.job_details)
        job = intent?.getSerializableExtra("job") as JobDTO
        initialise()
        setListeners()
    }

    private fun getTechnicians() {
        DialogsManager.showProgressDialog(context)
        firestore.collection(Constants.COLLECTION_USERS)
            .whereEqualTo("service", job.type)
            .orderBy("insertedAt", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener {
                DialogsManager.dismissProgressDialog()
                if (it.isSuccessful) {
                    technicians.clear()
                    if (it.result != null && !it.result.isEmpty) {
                        for (i in 0 until it.result.documents.size) {
                            val record = it.result.documents[i].toObject(UserDTO::class.java)
                                ?: continue
                            technicians.add(record)
                        }
                    } else
                        Helper.showToast(context, "No Technician Record(s) found!")
                } else
                    Helper.showToast(context, "Failed to fetch technician records")
            }
    }

    private fun setListeners() {
        binding.editTechnician.setOnClickListener {
            DialogsManager.displayTechniciansDialog(context, technicians, object : DialogActions() {
                override fun onSubmitClicked(txt: String) {
                    val technician = technicians[txt.toInt()]
                    val map = HashMap<String, Any>()
                    map["technicianMobile"] = Helper.getUserIndex(technician)
                    map["technicianName"] = technician.name!!
                    updateJob(map)
                }
            })
        }

        binding.accept.setOnClickListener {
            DialogsManager.showConfirmationDialog(context,
                "Are you sure that you want to accept this job?", object : DialogActions() {
                    override fun onYesClicked() {
                        val map = HashMap<String, Any>()
                        map["status"] = Constants.JOB_STATUS.ACCEPTED.name
                        updateJob(map)
                    }
                })
        }

        binding.reject.setOnClickListener {
            DialogsManager.showConfirmationDialog(context,
                "Are you sure that you want to reject this job?", object : DialogActions() {
                    override fun onYesClicked() {
                        val map = HashMap<String, Any>()
                        map["technicianName"] = "N/A"
                        map["technicianMobile"] = "N/A"
                        updateJob(map)
                    }
                })
        }

        binding.date.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context, onDateSetListener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.saveBtn.setOnClickListener {
            DialogsManager.showConfirmationDialog(
                context,
                "Are you sure that you want to update the job?",
                object : DialogActions() {
                    override fun onYesClicked() {
                        val cost = binding.cost.text.toString().trim()
                        if (cost.isNotEmpty())
                            job.price = cost.toInt()
                        if (binding.statusSpinner.selectedItemPosition == 1 && (cost.isEmpty() || cost.toInt() == 0)) {
                            Helper.showToast(context, "Please enter the agreed amount for the job")
                            return
                        }
                        job.status = if (binding.statusSpinner.selectedItemPosition == 0)
                            Constants.JOB_STATUS.IN_PROGRESS.name
                        else
                            Constants.JOB_STATUS.COMPLETED.name
                        val map = HashMap<String, Any>()
                        map["status"] = job.status
                        map["price"] = job.price
                        map["jobDate"] = job.jobDate
                        updateJob(map)
                    }
                })
        }
    }

    private val onDateSetListener = DatePickerDialog.OnDateSetListener { p0, p1, p2, p3 ->
        val calendar = Calendar.getInstance()
        calendar.set(p1, p2, p3)
        job.jobDate = calendar.timeInMillis
        binding.date.setText(DatesManager.getTimeInF2(job.jobDate))
    }

    private fun updateJob(map: HashMap<String, Any>) {
        DialogsManager.showProgressDialog(context)
        firestore.collection(Constants.COLLECTION_JOBS)
            .document(job.id!!)
            .update(map)
            .addOnCompleteListener {
                DialogsManager.dismissProgressDialog()
                if (it.isSuccessful) {
                    if (map.containsKey("status") &&
                        map["status"] == Constants.JOB_STATUS.ACCEPTED.name
                    ) {
                        job.status = map["status"] as String
                        setLayout()
                    } else if (map.containsKey("technicianMobile") && map["technicianMobile"] == "N/A") {
                        finish()
                    } else if (map.containsKey("technicianMobile") && map["technicianMobile"] != "N/A") {
                        job.technicianName = map["technicianName"] as String
                        job.technicianMobile = map["technicianMobile"] as String
                        binding.technician.text = "${job.technicianName}"
                    } else {
                        setLayout()
                    }
                    binding.acceptanceLayout.visibility = View.GONE
                    Helper.showToast(context, getString(R.string.job_updated))
                    isJobUpdated = true
                } else
                    Helper.showToast(context, getString(R.string.job_update_failed))
            }
    }

    private fun initialise() {
        binding.titleTv.text = job.title
        binding.description.text = job.description
        binding.address.text = job.address

        if (user.isTechnician()) {
            binding.userLayout.visibility = View.VISIBLE
            binding.user.text = "${job.userName}\n${job.userMobile}"
        } else {
            binding.userLayout.visibility = View.GONE
            if (job.status == Constants.JOB_STATUS.PENDING.name) {
                binding.technician.text = "${job.technicianName}"
                binding.editTechnician.visibility = View.VISIBLE
                getTechnicians()
            } else {
                binding.technician.text = "${job.technicianName}\n${job.technicianMobile}"
                binding.editTechnician.visibility = View.GONE
            }
        }

        binding.imagesList.layoutManager = GridLayoutManager(context, 2)
        val images = ArrayList<ImageDTO>()
        job.images.forEach {
            images.add(ImageDTO(it))
        }
        val adapter = ImagesAdapter(context, images, object : ListActions() {
            override fun onItemClicked(pos: Int) {
                DialogsManager.showEnlargedImage(context, images[pos])
            }
        })
        adapter.disableEditing = true
        binding.imagesList.adapter = adapter


        val entries = arrayListOf(
            "IN PROGRESS", "COMPLETED"
        )
        binding.statusSpinner.adapter =
            ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, entries)

        setLayout()
    }

    private fun setLayout() {
        setJobStatus()

        binding.date.setText(DatesManager.getTimeInF2(job.jobDate))
        if (job.price != 0)
            binding.cost.setText("${job.price}")

        if (job.status == Constants.JOB_STATUS.PENDING.name && user.isTechnician())
            binding.acceptanceLayout.visibility = View.VISIBLE
        else
            binding.acceptanceLayout.visibility = View.GONE

        if (job.status != Constants.JOB_STATUS.PENDING.name) {
            binding.detailsLayout.visibility = View.VISIBLE
            if (user.isTechnician() && job.status != Constants.JOB_STATUS.COMPLETED.name) {
                binding.statusLayout.visibility = View.VISIBLE
                binding.saveBtn.visibility = View.VISIBLE
            } else {
                binding.statusLabel.visibility = View.GONE
                binding.statusLayout.visibility = View.GONE
                binding.saveBtn.visibility = View.GONE
                binding.cost.isEnabled = false
                binding.date.isEnabled = false
            }
        } else
            binding.detailsLayout.visibility = View.GONE
    }

    private fun setJobStatus() {
        val details = Helper.getStatusDetails(job.status)
        binding.status.text = details.first
        binding.status.background = details.second
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}