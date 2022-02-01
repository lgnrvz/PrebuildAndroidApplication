package com.nrvz.prebuildapplication.utilities.click

import android.view.View

class RecyclerClick(
    val click: (Any) -> Unit
) {
    fun onClick(model: Any) = click(model)
}

class RecyclerClickView(
    val click: (Any) -> Unit,
    val clickView: (Any, View) -> Unit
) {
    fun onClick(model: Any) = click(model)
    fun onClickView(model: Any, view: View) = clickView(model, view)
}