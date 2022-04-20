package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.repo

import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting


/**
 * Created by ( Eng Ali Al Fayed)
 * Class do :
 * Date 9/27/2021 - 3:08 PM
 */
interface LabelsPrintingRepository{

    // Local Api

    suspend fun saveLabel(labels: LabelsPrinting) :Long

    suspend fun saveLabels(labels: ArrayList<LabelsPrinting>) :List<Long>

    suspend fun getAllLabelsPrinting(): List<LabelsPrinting>?

    suspend fun clearTable()

    suspend fun deleteLabel(localId: Int) : Int
}