package com.android.fixit.utils

import com.android.fixit.interfaces.IListActions

open class ListActions: IListActions {
    override fun onItemClicked(pos: Int) {}
    override fun onDeleteClicked(pos: Int) {}
    override fun onEditClicked(pos: Int) {}
    override fun onItemSelected(pos: Int) {}
}