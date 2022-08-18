package com.android.fixit.utils

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import com.android.fixit.R
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import java.io.File

class ImageSelector(val context: Context, val actions: IImageSelector) {
    private var fileName: String? = null
    private val codeRequestImageCapture = 538
    private val codeRequestGalleryImage = 539
    private var uCropOptions: UCrop.Options = UCrop.Options()
    private val tag = ImageSelector::class.java.simpleName

    init {
        uCropOptions.setCompressionQuality(80)
        uCropOptions.setToolbarColor(ContextCompat.getColor(context, R.color.violet))
        uCropOptions.setStatusBarColor(ContextCompat.getColor(context, R.color.blue))
        uCropOptions.setToolbarWidgetColor(ContextCompat.getColor(context, R.color.white))
        uCropOptions.setActiveWidgetColor(ActivityCompat.getColor(context, R.color.blue))
        uCropOptions.withAspectRatio(1f, 1f)
        uCropOptions.withMaxResultSize(1000, 1000)
        clearCache()
    }

    fun onEditClicked() {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            actions.onPermissionsUnavailable()
        } else {
            displayImagePicker()
        }
    }

    private fun displayImagePicker() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.lbl_set_product_photo))
        val items = arrayOf(
            context.getString(R.string.lbl_take_camera_picture),
            context.getString(R.string.lbl_choose_from_gallery)
        )
        builder.setItems(items) { _: DialogInterface?, which: Int ->
            when (which) {
                0 -> {
                    fileName = "${System.currentTimeMillis()}.png"
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    takePictureIntent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        getCacheImagePath()
                    )
                    if (takePictureIntent.resolveActivity(context.packageManager) != null)
                        actions.launchIntent(takePictureIntent, codeRequestImageCapture)
                }
                1 -> {
                    val pickPhoto = Intent(
                        Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    actions.launchIntent(pickPhoto, codeRequestGalleryImage)
                }
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun getCacheImagePath(): Uri? {
        val path = File(context.externalCacheDir, "camera")
        if (!path.exists())
            path.mkdirs()
        val image = File(path, fileName!!)
        return FileProvider.getUriForFile(context, context.packageName + ".provider", image)
    }

    private fun clearCache() {
        val path = File(context.externalCacheDir, "camera")
        if (path.exists() && path.isDirectory && path.listFiles() != null) {
            for (child in path.listFiles()!!)
                child.delete()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            codeRequestImageCapture -> {
                if (resultCode == Activity.RESULT_OK) {
                    cropImage(getCacheImagePath())
                }
            }
            codeRequestGalleryImage -> {
                if (resultCode == Activity.RESULT_OK) {
                    cropImage(data!!.data)
                }
            }
            UCrop.REQUEST_CROP -> if (resultCode == Activity.RESULT_OK) {
                if (data != null && UCrop.getOutput(data) != null)
                    actions.getImageUri(UCrop.getOutput(data)!!)
                else
                    actions.onErrorOccurred(context.getString(R.string.image_detection_failed))
            }
            UCrop.RESULT_ERROR -> {
                if (data != null) {
                    val cropError: Throwable = UCrop.getError(data)!!
                    Log.e(tag, "Crop error: $cropError")
                    actions.onErrorOccurred(context.getString(R.string.image_detection_failed))
                }
            }
        }
    }

    private fun cropImage(sourceUri: Uri?) {
        val destinationUri = Uri.fromFile(
            File(
                context.cacheDir, queryName(context.contentResolver, sourceUri)!!
            )
        )
        val uCrop = UCrop.of(sourceUri!!, destinationUri)
            .withOptions(uCropOptions)
        actions.launchUCropActivity(uCrop, UCrop.REQUEST_CROP)
    }

    private fun queryName(resolver: ContentResolver, uri: Uri?): String? {
        val returnCursor = resolver.query(uri!!, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    interface IImageSelector {
        fun onPermissionsUnavailable()
        fun onErrorOccurred(msg: String)
        fun launchIntent(intent: Intent, code: Int)
        fun launchUCropActivity(uCrop: UCrop, code: Int)
        fun getImageUri(uri: Uri)
    }

}