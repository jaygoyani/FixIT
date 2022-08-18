package com.android.fixit.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.android.fixit.R
import com.android.fixit.adapters.ImagesAdapter
import com.android.fixit.databinding.ActivityPostJobBinding
import com.android.fixit.dtos.ImageDTO
import com.android.fixit.dtos.JobDTO
import com.android.fixit.dtos.UserDTO
import com.android.fixit.managers.DialogsManager
import com.android.fixit.managers.PrefManager
import com.android.fixit.utils.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.yalantis.ucrop.UCrop
import java.util.*
import kotlin.collections.ArrayList

class PostJobActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostJobBinding
    private var worker: UserDTO? = null
    private val context: Context = this
    private var images = ArrayList<ImageDTO>()
    private var imagesAdapter: ImagesAdapter? = null
    private val user = PrefManager.getUserDTO()
    private val pictureCode = 353
    private lateinit var imageSelector: ImageSelector
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private var imageInd = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostJobBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialise()
        setListeners()
    }

    private fun setListeners() {
        binding.back.setOnClickListener {
            finish()
        }
        binding.continueBtn.setOnClickListener {
            val title = binding.titleEt.text.toString().trim()
            val description = binding.descriptionEt.text.toString().trim()
            val address = binding.addressEt.text.toString().trim()
            if (title.isEmpty() || description.isEmpty() || address.isEmpty()) {
                Helper.showToast(context, getString(R.string.details_missing))
                return@setOnClickListener
            }
            if (images.size == 1) {
                Helper.showToast(context, getString(R.string.empty_images))
                return@setOnClickListener
            }
            DialogsManager.showConfirmationDialog(
                context, getString(R.string.post_job_confirmation),
                getString(R.string.continue_label), getString(R.string.go_back),
                object : DialogActions() {
                    override fun onYesClicked() {
                        val job = JobDTO(title, description, address, worker!!)
                        if (images.size > 1) {
                            imageInd = 0
                            saveImages(job)
                        } else
                            saveJob(job)
                    }
                })
        }
    }

    private fun saveImages(job: JobDTO) {
        var uri: Uri? = null
        for (i in imageInd until images.size) {
            val image = images[i]
            if (image.uri != null && image.url == null) {
                uri = image.uri
                imageInd = i + 1
                break
            }
        }
        if (uri != null) {
            DialogsManager.showProgressDialog(context)
            val filePath = FilePathUtil(context, uri).getFilePath()
            val ref = firebaseStorage.child(Constants.FOLDER_JOBS)
                .child(UUID.randomUUID().toString() + filePath.substring(filePath.lastIndexOf(".")))
            ref.putFile(uri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    images[imageInd - 1].url = downloadUrl.toString()
                    saveImages(job)
                } else {
                    DialogsManager.dismissProgressDialog()
                    task.exception?.printStackTrace()
                    Helper.showToast(context, getString(R.string.image_upload_failed))
                }
            }
        } else
            saveJob(job)
    }

    private fun saveJob(job: JobDTO) {
        for (i in images.indices)
            job.images.add(images[i].url ?: continue)
        firebaseFirestore.collection(Constants.COLLECTION_JOBS)
            .add(job)
            .addOnCompleteListener {
                DialogsManager.dismissProgressDialog()
                if (it.isSuccessful) {
                    Helper.showToast(context, getString(R.string.job_saved))
                    finish()
                } else
                    Helper.showToast(context, getString(R.string.job_saving_failed))
            }
    }

    private fun initialise() {
        worker = intent?.getSerializableExtra("worker") as UserDTO?
        if (worker == null) {
            Helper.showToast(context, "Worker details are missing.")
            finish()
        }
        Helper.clearCache(context)
        imageSelector = ImageSelector(context, object : ImageSelector.IImageSelector {
            override fun onPermissionsUnavailable() {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    ), pictureCode
                )
            }

            override fun onErrorOccurred(msg: String) {
                Helper.showToast(context, msg)
            }

            override fun launchIntent(intent: Intent, code: Int) {
                startActivityForResult(intent, code)
            }

            override fun getImageUri(uri: Uri) {
                images[images.size - 1] = ImageDTO(uri)
                images.add(ImageDTO())
                imagesAdapter?.notifyDataSetChanged()
            }

            override fun launchUCropActivity(uCrop: UCrop, code: Int) {
                uCrop.start(this@PostJobActivity, code)
            }
        })
        if (images.size < Constants.MAX_IMAGES)
            images.add(ImageDTO())
        binding.technicianEt.setText("${worker?.name}(${worker?.service})")
        binding.priceEt.setText("${worker?.pricePerHr}")
        binding.addressEt.setText(user.address ?: "")
        binding.imagesList.layoutManager = GridLayoutManager(context, 2)
        imagesAdapter = ImagesAdapter(context, images, object : ListActions() {
            override fun onDeleteClicked(pos: Int) {
                images.removeAt(pos)
                imagesAdapter?.notifyItemRemoved(pos)
            }

            override fun onItemClicked(pos: Int) {
                val image = images[pos]
                if (image.isEmpty())
                    imageSelector.onEditClicked()
                else
                    DialogsManager.showEnlargedImage(context, image)
            }
        })
        binding.imagesList.adapter = imagesAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        imageSelector.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}