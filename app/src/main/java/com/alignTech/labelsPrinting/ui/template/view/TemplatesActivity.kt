package com.alignTech.labelsPrinting.ui.template.view

import android.graphics.*
import android.os.Bundle
import android.util.Log
import com.alfayedoficial.kotlinutils.kuHide
import com.alfayedoficial.kotlinutils.kuScreenshot
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.core.base.view.BaseActivity
import com.alignTech.labelsPrinting.core.util.ExtensionsApp
import com.alignTech.labelsPrinting.databinding.ActivityTemplatesBinding
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting
import com.alignTech.labelsPrinting.ui.dialog.configPrint.view.PrintConfig
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils.BarcodeUtils
import com.alignTech.labelsPrinting.ui.template.turboimageview.MultiTouchObject
import com.alignTech.labelsPrinting.ui.template.turboimageview.TurboImageView
import com.alignTech.labelsPrinting.ui.template.turboimageview.TurboImageViewListener
import com.google.zxing.BarcodeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TemplatesActivity : BaseActivity<ActivityTemplatesBinding>() , TurboImageViewListener {

    private var label: LabelsPrinting = LabelsPrinting(barCode = "123456789012" , price = 500.00 , nameProduct = "testProject")
    private val config by lazy { ExtensionsApp.kuGetCustomModel(appPreferences, PrintConfig::class.java, "printConfig") }
    private val bitmapBarcode by lazy { BarcodeUtils.generateBarcodeWriter(label.barCode!!, 500, 500, BarcodeFormat.valueOf("QR_CODE")) }
    private var bitmapTvBarcode : Bitmap ?= null
    private var bitmap : Bitmap? = null
    private var canvas : Canvas? = null
    private var turboImageView: TurboImageView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override val layoutResourceId: Int
        get() = R.layout.activity_templates

    override fun onActivityCreated(dataBinder: ActivityTemplatesBinding) {
        dataBinder.apply {
            activity = this@TemplatesActivity
            lifecycleOwner = this@TemplatesActivity

            turboImageView = imgBarcode
            turboImageView!!.setListener(this@TemplatesActivity)
            imgBarcode.addObject(this@TemplatesActivity , bitmapBarcode)
            turboImageView!!.objectSelectedBorderColor = Color.BLACK
            tvBarcode.text = label.barCode.toString()
            tvPriceProduct.text = label.price.toString()
            tvNameProduct.text = label.nameProduct.toString()
            MainScope().launch {
                delay(1000)
                imgBarcode.addObject(this@TemplatesActivity ,  dataBinder.lyTvBarcode.kuScreenshot())
                tvBarcode.kuHide()
                tvPriceProduct.kuHide()
                tvNameProduct.kuHide()
                tvPrice.kuHide()
            }



        }
    }

    companion object{
        const val TAG = "TAG_TurboImage"
    }

    override fun onImageObjectSelected(`object`: MultiTouchObject?) {
        Log.d(TAG, "image object selected");
    }

    override fun onImageObjectDropped() {
        Log.d(TAG, "image object dropped");
    }

    override fun onCanvasTouched() {
        turboImageView!!.deselectAll();
        Log.d(TAG, "canvas touched");
    }


}