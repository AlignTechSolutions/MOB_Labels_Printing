package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.adapter

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncListDiffer
import com.alfayedoficial.kotlinutils.*
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.callback.OnDelete
import com.alignTech.labelsPrinting.core.base.adapter.BaseAdapter
import com.alignTech.labelsPrinting.core.base.adapter.BaseViewHolder
import com.alignTech.labelsPrinting.core.base.adapter.DiffCallBack
import com.alignTech.labelsPrinting.core.util.setBindString
import com.alignTech.labelsPrinting.databinding.ItemRvExcelBinding
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting


/**
 * Created by ( Eng Ali Al Fayed)
 * Class do :
 * Date 9/13/2021 - 3:28 PM
 */
class LabelsPrintingTableRvAdapter : BaseAdapter<LabelsPrinting>() {

    private var mDiffer = AsyncListDiffer(this, DiffCallBack<LabelsPrinting>())
    private var dataList: List<LabelsPrinting> = arrayListOf()
    private var id: Int = 5000
    private lateinit var onDelete : OnDelete
    private lateinit var context : Context
    private lateinit var recyclerviewPosition : RecyclerviewPosition

    interface RecyclerviewPosition{
        fun onPosition(position: Int)
    }

    override fun setDataList(dataList: List<LabelsPrinting>) {
        this.dataList = dataList
        mDiffer.submitList(dataList)
    }

    override fun addDataList(dataList: List<LabelsPrinting>) {
        clearDataList()
        setDataList(dataList)
        notifyDataSetChanged()
    }

    override fun clearDataList() {
        this.dataList= arrayListOf()
    }

    fun initInterface(recyclerviewPosition : RecyclerviewPosition , context : Context , onDelete : OnDelete){
        this.context = context
        this.recyclerviewPosition = recyclerviewPosition
        this.onDelete = onDelete
    }

    fun smoothScrollToPosition(id: Int, position: Int){
        this.id = id
        try {
            notifyItemChanged(position)
        }catch (e : Exception){
            context.kuToast("من فضلك قم بالزول إلى أسفل القائمة")
            print(e.message)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<LabelsPrinting> {
        return ViewHolderLabelsPrinting(kuGetBindingRow(parent, R.layout.item_rv_excel) as ItemRvExcelBinding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: BaseViewHolder<LabelsPrinting>, position: Int) {
        val model = mDiffer.currentList[position]
        holder.apply {
            bind(model)

            (itemRowBinding as ItemRvExcelBinding).apply {

                tvId.setBindString((position+1).toString())

                if (model.localId == id) {
                    recyclerviewPosition.onPosition(position)
                    itemView.kuRes.apply {
                        val colorTvFrom: Int = getColor(R.color.TemplateRed)
                        val colorTvTo: Int = getColor(R.color.black)
                        val styleFrom = Typeface.BOLD
                        val styleTo = Typeface.NORMAL

                        ValueAnimator.ofObject(ArgbEvaluator(), colorTvFrom, colorTvTo).apply {
                            duration = 4000 // milliseconds
                            addUpdateListener { animator ->
                                tvBarCode.setTextColor(animator.animatedValue as Int)
                                tvNameProduct.setTextColor(animator.animatedValue as Int)
                                tvTotalCount.setTextColor(animator.animatedValue as Int)
                            }
                            start()
                        }

                        ValueAnimator.ofObject(ArgbEvaluator(), styleFrom, styleTo).apply {
                            duration = 4000 // milliseconds
                            addUpdateListener { animator ->
                                tvBarCode.setTypeface(tvBarCode.typeface, animator.animatedValue as Int)
                                tvNameProduct.setTypeface(tvNameProduct.typeface,animator.animatedValue as Int)
                                tvTotalCount.setTypeface(tvTotalCount.typeface,animator.animatedValue as Int)
                            }
                            start()
                        }

                    }

                    id = 5000

                }

                itemView.setOnLongClickListener { view ->
                    imgBtnDelete.kuShow()
                    imgBtnPrint.kuShow()
                    kuRunDelayed(2500) {
                        imgBtnDelete.kuHide()
                        imgBtnPrint.kuHide()
                    }
                    true
                }

                imgBtnDelete.setOnClickListener {
                    imgBtnDelete.kuHide()
                    imgBtnPrint.kuHide()
                    onDelete.deleteLabel(model.localId!! , model.barCode!!)
                }

                imgBtnPrint.setOnClickListener {
                    imgBtnDelete.kuHide()
                    imgBtnPrint.kuHide()
                    onDelete.printLabel(model) }


            }
        }
    }

    override fun getItemCount(): Int = mDiffer.currentList.size

    class ViewHolderLabelsPrinting(binding: ItemRvExcelBinding) : BaseViewHolder<LabelsPrinting>(binding) {

        override var itemRowBinding: ViewDataBinding = binding

        override fun bind(result: LabelsPrinting) {
            itemRowBinding.apply {
                setVariable(BR.model, result)
                executePendingBindings()
            }
        }
    }
}



