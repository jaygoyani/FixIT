package com.android.fixit.interfaces

interface IDialogActions {
    fun onYesClicked()
    fun onNoClicked()
    fun onSubmitClicked(txt: String)
}