package com.alignTech.labelsPrinting.ui.oneSingleActivity.view

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.alfayedoficial.kotlinutils.kuRes
import com.alfayedoficial.kotlinutils.kuRunDelayed
import com.alfayedoficial.kotlinutils.kuSnackBarError
import com.alfayedoficial.kotlinutils.kuToast
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.R.id
import com.alignTech.labelsPrinting.callback.DialogCallBack
import com.alignTech.labelsPrinting.core.base.view.BaseActivity
import com.alignTech.labelsPrinting.core.util.TokenUtil
import com.alignTech.labelsPrinting.databinding.ActivityOneSingleBinding
import com.alignTech.labelsPrinting.domain.model.auth.LogoutData
import com.alignTech.labelsPrinting.domain.network.networkSettings.ServerCallBack
import com.alignTech.labelsPrinting.ui.dialog.changePassword.view.ChangePasswordDialog
import com.alignTech.labelsPrinting.ui.dialog.logout.view.DialogLogoutFragment
import com.alignTech.labelsPrinting.ui.dialog.resetDatabase.view.DialogResetDatabaseFragment
import com.alignTech.labelsPrinting.ui.oneSingleActivity.viewModel.OneSingleViewModel
import com.anggrayudi.storage.SimpleStorageHelper
import dagger.hilt.android.AndroidEntryPoint
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.xmlbeans.impl.piccolo.io.FileFormatException
import java.lang.reflect.InvocationTargetException

@AndroidEntryPoint
class OneSingleActivity : BaseActivity<ActivityOneSingleBinding>() , DialogCallBack {

    val activityViewModel : OneSingleViewModel by viewModels()
    private val storageHelper = SimpleStorageHelper(this)
    private var closeStatus : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Call database and get all labels */
        activityViewModel.getAllLabelsPrinting()

        storageHelper.onFileSelected = { requestCode, files ->
            // do stuff
            val contentResolver = applicationContext.contentResolver
            if (files.isNotEmpty()){
                contentResolver.openInputStream(files[0].uri)?.use { inputStream ->
                    try {
                        val workBook = WorkbookFactory.create(inputStream)
                        activityViewModel.workbookMutableLiveData.value = workBook
                    }catch (ex : InvocationTargetException){
                        print(ex.message)
                        activityViewModel.workbookMutableLiveData.value = null
                    }catch (ex : FileFormatException){
                        print(ex.message)
                        activityViewModel.workbookMutableLiveData.value = null
                    }catch (ex : NoSuchFileException){
                        print(ex.message)
                        activityViewModel.workbookMutableLiveData.value = null
                    }catch (ex: Exception){
                        print(ex.message)
                        activityViewModel.workbookMutableLiveData.value = null
                    }finally {
                        print("finally Exception")
                        activityViewModel.workbookMutableLiveData.value = null
                    }
                }
            }

            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }
        }
    }

    override val layoutResourceId: Int
        get() = R.layout.activity_one_single

    override fun onActivityCreated(dataBinder: ActivityOneSingleBinding) {
       dataBinder.apply {
           activity = this@OneSingleActivity
           lifecycleOwner = this@OneSingleActivity

           val navHostFragment = supportFragmentManager.findFragmentById(id.navGraph) as NavHostFragment
           navController = navHostFragment.navController

           appBarConfiguration = AppBarConfiguration(topLevelDestinationIds = setOf(id.labelsPrintingFragment), drawerLayout ) // include your drawer_layout
           drawNavigationView.setupWithNavController(navController)

           drawNavigationView.setNavigationItemSelectedListener { item ->
               when (item.itemId) {
                   id.reset -> {
                       drawerLayout.closeDrawer(GravityCompat.START)
                       inflateResetDatabaseDialog()
                   }
                   id.labelsPrintingFragment -> {
                       drawerLayout.closeDrawer(GravityCompat.START)
                       navController.navigate(id.labelsPrintingFragment)
                   }
                   id.changePassword ->{
                       drawerLayout.closeDrawer(GravityCompat.START)
                       inflateChangePasswordDialog()
                   }
               }
               true
           }


       }
    }

    fun startSessionCounter() {
        if (!activityViewModel.startCountDown()) inflateLogOutDialog(kuRes.getString(R.string.session_is_finished))
        else setCounterDownText()
    }

    private fun setCounterDownText() {
        dataBinder.apply {
            try {
                val remainingTextView = drawNavigationView.getHeaderView(0).findViewById<TextView>(R.id.remain_time)
                activityViewModel.remainingTime.observe(this@OneSingleActivity) {
                    if (it == "00:00:00") {
                        remainingTextView.setTextColor(ContextCompat.getColor(this@OneSingleActivity, R.color.TemplateRed))
                        inflateLogOutDialog(null)
                    }
                    remainingTextView.text = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun inflateResetDatabaseDialog(){
        val dialog = DialogResetDatabaseFragment()
        dialog.init(dataBinder.drawerLayout )
        dialog.show(supportFragmentManager ,dialog.tag )
    }

    private fun inflateChangePasswordDialog(){
        val dialog = ChangePasswordDialog()
        dialog.show(supportFragmentManager, dialog.tag)
    }

    fun inflateLogOutDialog(message :String?){
        val dialog = DialogLogoutFragment()
        dialog.initDialogCallBack(this)
        dialog.initMessage(message)
        dialog.show(supportFragmentManager ,dialog.tag )
    }

    fun snackBarError(msg:String , bgColor : Int , tvColor : Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            kuSnackBarError(msg, kuRes.getColor(bgColor ,theme), kuRes.getColor(tvColor ,theme))
        }else{
            kuSnackBarError(msg, kuRes.getColor(bgColor), kuRes.getColor(tvColor))
        }
    }

    fun loadingProgress(status : Boolean){
        if (status){
            loadingProgress.show(supportFragmentManager, loadingProgress.tag)
        }else{
            loadingProgress.dismiss()
        }
    }


    fun logOut() {
        val logoutData = LogoutData(TokenUtil.getTokenId(appPreferences), TokenUtil.getJobId(appPreferences))
        loadingProgress(true)
        activityViewModel.requestLogout(logoutData).observe(this) {
            if (it.status == ServerCallBack.Status.SUCCESS) {
                TokenUtil.removeToken(appPreferences)
                navController.navigate(R.id.authFragment)
            } else if (it.status == ServerCallBack.Status.ERROR) {
                kuToast(kuRes.getString(R.string.network_connection_failed))
            }
            loadingProgress(false)
        }
    }

    fun openFilePicker() =  storageHelper.openFilePicker()

    override fun dialogOnClick(idView: Int) {
        when (idView) {
            R.id.btnLogout -> {
                logOut()
            }
        }
    }

    companion object {
        init {
            System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLInputFactory",
                "com.fasterxml.aalto.stax.InputFactoryImpl"
            )
            System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLOutputFactory",
                "com.fasterxml.aalto.stax.OutputFactoryImpl"
            )
            System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLEventFactory",
                "com.fasterxml.aalto.stax.EventFactoryImpl"
            )
        }
    }

    override fun onBackPressed() {
        val destination = navController.currentDestination
        when (destination?.id) {
            R.id.labelsPrintingFragment ->{
                if (closeStatus){
                    finish()
                }else{
                    kuToast("Please press back again")
                    closeStatus = true
                    kuRunDelayed(2500) { closeStatus = false }
                }
            }
            else ->{ super.onBackPressed()}
        }

    }


}