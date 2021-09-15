/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.caren.music.activities

import android.content.Intent
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.util.ATHUtil
import code.name.monkey.appthemehelper.util.TintHelper
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.caren.music.BuildConfig
import com.caren.music.R
import com.caren.music.activities.base.AbsBaseActivity
import com.caren.music.databinding.ActivityDonationBinding
import com.caren.music.extensions.textColorPrimary
import com.caren.music.extensions.textColorSecondary
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.SkuDetails
import com.anjlab.android.iab.v3.TransactionDetails
import java.lang.ref.WeakReference
import java.util.*

class SupportDevelopmentActivity : AbsBaseActivity(), BillingProcessor.IBillingHandler {

    lateinit var binding: ActivityDonationBinding

    companion object {
        val TAG: String = SupportDevelopmentActivity::class.java.simpleName
        const val DONATION_PRODUCT_IDS = R.array.donation_ids
        private const val TEZ_REQUEST_CODE = 123
    }

    var billingProcessor: BillingProcessor? = null
    private var skuDetailsLoadAsyncTask: AsyncTask<*, *, *>? = null

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun donate(i: Int) {
        val ids = resources.getStringArray(DONATION_PRODUCT_IDS)
        billingProcessor?.purchase(this, ids[i])
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_donation)

        setStatusbarColorAuto()
        setNavigationbarColorAuto()
        setTaskDescriptionColorAuto()
        setLightNavigationBar(true)

        setupToolbar()

        billingProcessor = BillingProcessor(this, BuildConfig.GOOGLE_PLAY_LICENSING_KEY, this)
        TintHelper.setTint(binding.progress, ThemeStore.accentColor(this))
        binding.donation.setTextColor(ThemeStore.accentColor(this))
    }

    private fun setupToolbar() {
        val toolbarColor = ATHUtil.resolveColor(this, R.attr.colorSurface)
        binding.toolbar.setBackgroundColor(toolbarColor)
        ToolbarContentTintHelper.colorBackButton(binding.toolbar)
        setSupportActionBar(binding.toolbar)
    }

    override fun onBillingInitialized() {
        loadSkuDetails()
    }

    private fun loadSkuDetails() {
        if (skuDetailsLoadAsyncTask != null) {
            skuDetailsLoadAsyncTask!!.cancel(false)
        }
        skuDetailsLoadAsyncTask = SkuDetailsLoadAsyncTask(this).execute()
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        // loadSkuDetails();
        Toast.makeText(this, R.string.thank_you, Toast.LENGTH_SHORT).show()
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        Log.e(TAG, "Billing error: code = $errorCode", error)
    }

    override fun onPurchaseHistoryRestored() {
        // loadSkuDetails();
        Toast.makeText(this, R.string.restored_previous_purchases, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor!!.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == TEZ_REQUEST_CODE) {
            // Process based on the data in response.
            // Log.d("result", data!!.getStringExtra("Status"))
        }
    }

    override fun onDestroy() {
        billingProcessor?.release()
        skuDetailsLoadAsyncTask?.cancel(true)
        super.onDestroy()
    }
}

private class SkuDetailsLoadAsyncTask(supportDevelopmentActivity: SupportDevelopmentActivity) :
    AsyncTask<Void, Void, List<SkuDetails>>() {

    private val weakReference: WeakReference<SupportDevelopmentActivity> = WeakReference(
        supportDevelopmentActivity
    )

    override fun onPreExecute() {
        super.onPreExecute()
        val supportDevelopmentActivity = weakReference.get() ?: return

        supportDevelopmentActivity.binding.progressContainer.visibility = View.VISIBLE
        supportDevelopmentActivity.binding.recyclerView.visibility = View.GONE
    }

    override fun doInBackground(vararg params: Void): List<SkuDetails>? {
        val dialog = weakReference.get()
        if (dialog != null) {
            val ids =
                dialog.resources.getStringArray(SupportDevelopmentActivity.DONATION_PRODUCT_IDS)
            return dialog.billingProcessor!!.getPurchaseListingDetails(ArrayList(Arrays.asList(*ids)))
        }
        cancel(false)
        return null
    }

    override fun onPostExecute(skuDetails: List<SkuDetails>?) {
        super.onPostExecute(skuDetails)
        val dialog = weakReference.get() ?: return

        if (skuDetails == null || skuDetails.isEmpty()) {
            dialog.binding.progressContainer.visibility = View.GONE
            return
        }

        dialog.binding.progressContainer.visibility = View.GONE
        dialog.binding.recyclerView.apply {
            itemAnimator = DefaultItemAnimator()
            layoutManager = GridLayoutManager(dialog, 2)
            adapter = SkuDetailsAdapter(dialog, skuDetails)
            visibility = View.VISIBLE
        }
    }
}

class SkuDetailsAdapter(
    private var donationsDialog: SupportDevelopmentActivity,
    objects: List<SkuDetails>
) : RecyclerView.Adapter<SkuDetailsAdapter.ViewHolder>() {

    private var skuDetailsList: List<SkuDetails> = ArrayList()

    init {
        skuDetailsList = objects
    }

    private fun getIcon(position: Int): Int {
        return when (position) {
            0 -> R.drawable.ic_cookie
            1 -> R.drawable.ic_take_away
            2 -> R.drawable.ic_take_away_coffe
            3 -> R.drawable.ic_beer
            4 -> R.drawable.ic_fast_food_meal
            5 -> R.drawable.ic_popcorn
            6 -> R.drawable.ic_card_giftcard
            7 -> R.drawable.ic_food_croissant
            else -> R.drawable.ic_card_giftcard
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(donationsDialog).inflate(
                LAYOUT_RES_ID,
                viewGroup,
                false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val skuDetails = skuDetailsList[i]
        viewHolder.title.text = skuDetails.title.replace("music player - mp3 player - lox", "")
            .trim { it <= ' ' }
        viewHolder.text.text = skuDetails.description
        viewHolder.text.visibility = View.GONE
        viewHolder.price.text = skuDetails.priceText
        viewHolder.image.setImageResource(getIcon(i))

        val purchased = donationsDialog.billingProcessor!!.isPurchased(skuDetails.productId)
        val titleTextColor = if (purchased) ATHUtil.resolveColor(
            donationsDialog,
            android.R.attr.textColorHint
        ) else donationsDialog.textColorPrimary()
        val contentTextColor =
            if (purchased) titleTextColor else donationsDialog.textColorSecondary()

        viewHolder.title.setTextColor(titleTextColor)
        viewHolder.text.setTextColor(contentTextColor)
        viewHolder.price.setTextColor(titleTextColor)

        strikeThrough(viewHolder.title, purchased)
        strikeThrough(viewHolder.text, purchased)
        strikeThrough(viewHolder.price, purchased)

        viewHolder.itemView.setOnTouchListener { _, _ -> purchased }
        viewHolder.itemView.setOnClickListener { donationsDialog.donate(i) }
    }

    override fun getItemCount(): Int {
        return skuDetailsList.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.itemTitle)
        var text: TextView = view.findViewById(R.id.itemText)
        var price: TextView = view.findViewById(R.id.itemPrice)
        var image: AppCompatImageView = view.findViewById(R.id.itemImage)
    }

    companion object {
        @LayoutRes
        private val LAYOUT_RES_ID = R.layout.item_donation_option

        private fun strikeThrough(textView: TextView, strikeThrough: Boolean) {
            textView.paintFlags =
                if (strikeThrough) textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                else textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }
}
