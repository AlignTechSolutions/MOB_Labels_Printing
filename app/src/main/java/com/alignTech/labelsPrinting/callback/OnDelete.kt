package com.alignTech.labelsPrinting.callback

import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting

interface OnDelete {

    fun deleteLabel(localId: Int, barcode: String)

    fun printLabel(label: LabelsPrinting)
}

