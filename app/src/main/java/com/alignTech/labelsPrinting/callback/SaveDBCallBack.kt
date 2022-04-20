package com.alignTech.labelsPrinting.callback

import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting


interface SaveDBCallBack {

    fun saveNewLabelsPrinting(model : LabelsPrinting)

}