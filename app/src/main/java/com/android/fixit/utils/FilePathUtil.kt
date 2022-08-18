package com.android.fixit.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import java.util.*

class FilePathUtil
{
    private var context: Context
    private lateinit var uri: Uri
    private var filePath: String? = null

    constructor(context: Context) {
        this.context = context
    }

    constructor(context: Context, fileUri: Uri) {
        this.context = context
        this.uri = fileUri
        findFilePath()
    }

    fun getFilePathFromUri(uri: Uri) {
        this.uri = uri
        findFilePath()
    }

    fun getFilePath(): String {
        return filePath ?: "N/A"
    }

    private fun findFilePath() {
        when {
            DocumentsContract.isDocumentUri(context, uri) -> {
                if(isExternalStorageDocument()) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true))
                        filePath = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else if(isDownloadsDocument()) {
                    val id = DocumentsContract.getDocumentId(uri)
                    if (!TextUtils.isEmpty(id)) {
                        filePath = if (id.startsWith("raw:")) {
                            id.replaceFirst("raw:".toRegex(), "")
                        } else {
                            val contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                java.lang.Long.valueOf(id)
                            )
                            getDataColumn(contentUri, null, null)
                        }
                    }
                } else if(isMediaDocument()) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]

                    var contentUri: Uri? = null
                    when (type) {
                        "image" -> {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        }
                        "video" -> {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        }
                        "audio" -> {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )

                    filePath = getDataColumn(contentUri, selection, selectionArgs)
                }
            }
            "content" == uri.scheme?.toLowerCase(Locale.ROOT) -> {
                filePath = getDataColumn(uri, null, null)
            }
            "file" == uri.scheme?.toLowerCase(Locale.ROOT) -> {
                filePath = uri.path
            }
        }
    }

    private fun isExternalStorageDocument(): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun getDataColumn(uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

}