package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.repo

import com.alignTech.labelsPrinting.local.AppDatabase
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LabelsPrintingRepositoryImp @Inject constructor(): LabelsPrintingRepository{

    @Inject
    lateinit var  appDatabaseService: AppDatabase

    override suspend fun saveLabels(labels: ArrayList<LabelsPrinting>): List<Long> =  withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
        appDatabaseService.labelsPrintingDao().clearTable()
        appDatabaseService.labelsPrintingDao().insertList(labels)
    }

    override suspend fun getAllLabelsPrinting(): List<LabelsPrinting>? = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
        appDatabaseService.labelsPrintingDao().getAllLabels()
    }

    override suspend fun clearTable() = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
        appDatabaseService.labelsPrintingDao().clearTable()
    }

    override suspend fun deleteLabel(localId: Int): Int = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
        appDatabaseService.labelsPrintingDao().deleteLabel(localId)
    }


}