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
package com.caren.music.fragments.settings

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import code.name.monkey.appthemehelper.ThemeStore
import com.caren.music.App
import com.caren.music.R
import com.caren.music.databinding.FragmentMainSettingsBinding
import com.caren.music.extensions.hide
import com.caren.music.extensions.show
import com.caren.music.util.NavigationUtil
import com.facebook.ads.*

class MainSettingsFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentMainSettingsBinding? = null
    private val binding get() = _binding!!


    lateinit var adView: AdView
    override fun onClick(view: View) {
        when (view.id) {
            R.id.generalSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_themeSettingsFragment)
            R.id.audioSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_audioSettings)
            R.id.personalizeSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_personalizeSettingsFragment)
            R.id.imageSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_imageSettingFragment)
            R.id.notificationSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_notificationSettingsFragment)
            R.id.otherSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_otherSettingsFragment)
            R.id.aboutSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_aboutActivity)
            R.id.nowPlayingSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_nowPlayingSettingsFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainSettingsBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.generalSettings.setOnClickListener(this)
        binding.audioSettings.setOnClickListener(this)
        binding.nowPlayingSettings.setOnClickListener(this)
        binding.personalizeSettings.setOnClickListener(this)
        binding.imageSettings.setOnClickListener(this)
        binding.notificationSettings.setOnClickListener(this)
        binding.otherSettings.setOnClickListener(this)
        binding.aboutSettings.setOnClickListener(this)

        adView = AdView(context, "IMG_16_9_APP_INSTALL#532107741396486_546989073241686", AdSize.BANNER_HEIGHT_50)
        // Find the Ad Container
        val adContainer = view.findViewById<LinearLayout>(R.id.banner_container)
        // Add the ad view to your activity layout
        adContainer.addView(adView)
        // Request an ad
        adView.loadAd()
        binding.buyProContainer.apply {
            if (App.isProVersion()) hide() else show()
            setOnClickListener {
                NavigationUtil.goToProVersion(requireContext())
            }
        }
        binding.buyPremium.setOnClickListener {
            NavigationUtil.goToProVersion(requireContext())
        }
        ThemeStore.accentColor(requireContext()).let {
            binding.buyPremium.setTextColor(it)
            binding.diamondIcon.imageTintList = ColorStateList.valueOf(it)
        }
        adslistenerCallbacks()
    }


    private fun adslistenerCallbacks() {
        val adListener: AdListener = object : AdListener {
            override fun onError(ad: Ad, adError: AdError) {
                // Ad error callback
                Toast.makeText(
                    context,
                    "Error: " + adError.errorMessage,
                    Toast.LENGTH_LONG
                )
                    .show()
            }

            override fun onAdLoaded(ad: Ad) {
                // Ad loaded callback
            }

            override fun onAdClicked(ad: Ad) {
                // Ad clicked callback
            }

            override fun onLoggingImpression(ad: Ad) {
                // Ad impression logged callback
            }
        }
        // Request an ad
        adView.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build())

    }


    override fun onDestroyView() {
        if (adView != null) {
            adView.destroy()
        }
        super.onDestroyView()
        _binding = null
    }
}
