package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.viewModel

import androidx.lifecycle.ViewModel
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.repo.LabelsPrintingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LabelsPrintingViewModel @Inject constructor(private var  repo: LabelsPrintingRepository) : ViewModel() {

    suspend fun saveLabelLists(labels: ArrayList<LabelsPrinting>) = repo.saveLabels(labels)

    suspend fun deleteLabel(localId: Int) = repo.deleteLabel(localId)

}