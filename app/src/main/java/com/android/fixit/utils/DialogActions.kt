package com.android.fixit.utils

import com.android.fixit.interfaces.IDialogActions

open class DialogActions: IDialogActions {
    override fun onYesClicked() {}
    override fun onNoClicked() {}
    override fun onSubmitClicked(txt: String) {}
}