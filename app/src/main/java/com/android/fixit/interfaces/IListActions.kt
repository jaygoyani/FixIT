package com.android.fixit.interfaces

interface IListActions {
    fun onItemClicked(pos: Int)
    fun onDeleteClicked(pos: Int)
    fun onEditClicked(pos: Int)
    fun onItemSelected(pos: Int)
}