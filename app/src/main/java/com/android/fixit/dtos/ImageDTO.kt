package com.android.fixit.dtos

import android.net.Uri

class ImageDTO {
    var url: String? = null
    var uri: Uri? = null

    constructor()

    constructor(uri: Uri) {
        this.uri = uri
    }

    constructor(url: String) {
        this.url = url
    }

    fun isEmpty(): Boolean {
        return uri == null && url == null
    }
}