package com.alignTech.labelsPrinting.ui.oneSingleActivity.viewModel

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alfayedoficial.kotlinutils.KUPreferences
import com.alignTech.labelsPrinting.core.util.TokenUtil
import com.alignTech.labelsPrinting.domain.model.auth.LogoutData
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.firstView.auth.repo.AuthRepositoryImp
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.repo.LabelsPrintingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Workbook
import javax.inject.Inject

@HiltViewModel
class OneSingleViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var appPreferences: KUPreferences
    @Inject
    lateinit var repositoryAuthImp: AuthRepositoryImp
    @Inject
    lateinit var repositoryLabelsPrintingImp: LabelsPrintingRepository

    private var _labelsPrintingMutableLiveData : MutableLiveData<List<LabelsPrinting>> = MutableLiveData(arrayListOf())
    var labelsPrintingMutableLiveData : LiveData<List<LabelsPrinting>> = _labelsPrintingMutableLiveData
    var workbookMutableLiveData : MutableLiveData<Workbook> = MutableLiveData()
    val remainingTime: MutableLiveData<String> = MutableLiveData("")
    var currentRequestCode = -1

    fun startCountDown(): Boolean {
        val time = TokenUtil.getRemainingTime(appPreferences)
        return if (time > 0L) {
            object : CountDownTimer(time, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val seconds = (millisUntilFinished / 1000).toInt() % 60
                    val minutes = (millisUntilFinished / (1000 * 60) % 60)
                    val hours = (millisUntilFinished / (1000 * 60 * 60) % 24)
                    val timeRemain = "${String.format("%02d", hours)}:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
                    remainingTime.value = timeRemain
                }

                override fun onFinish() {
                    remainingTime.value = "00:00:00"
                }
            }.start()
            true
        } else false
    }

    suspend fun clearData() = repositoryLabelsPrintingImp.clearTable()

    fun getAllLabelsPrinting()  = viewModelScope.launch {
        _labelsPrintingMutableLiveData.value =  repositoryLabelsPrintingImp.getAllLabelsPrinting()
    }

    fun requestLogout(tokenId: LogoutData) = repositoryAuthImp.requestLogout(tokenId)


}