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
package com.caren.music.fragments.library

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.common.ATHToolbarActivity.getToolbarBackgroundColor
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.caren.music.R
import com.caren.music.databinding.FragmentLibraryBinding
import com.caren.music.dialogs.CreatePlaylistDialog
import com.caren.music.dialogs.ImportPlaylistDialog
import com.caren.music.extensions.whichFragment
import com.caren.music.fragments.base.AbsMainActivityFragment
import com.caren.music.model.CategoryInfo
import com.caren.music.util.PreferenceUtil
import com.google.android.gms.cast.framework.CastButtonFactory

class LibraryFragment : AbsMainActivityFragment(R.layout.fragment_library) {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        mainActivity.setBottomBarVisibility(true)
        mainActivity.setSupportActionBar(binding.toolbar)
        mainActivity.supportActionBar?.title = null
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(
                R.id.searchFragment,
                null,
                navOptions
            )
        }
        setupNavigationController()
        setupTitle()
    }

    private fun setupTitle() {
        val color = ThemeStore.accentColor(requireContext())
        val hexColor = String.format("#%06X", 0xFFFFFF and color)
        val appName = HtmlCompat.fromHtml(
            "lox <span  style='color:$hexColor';></span>",
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        binding.appNameText.text = appName
    }

    private fun setupNavigationController() {
        val navHostFragment = whichFragment<NavHostFragment>(R.id.fragment_container)
        val navController = navHostFragment.navController
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.library_graph)

        val categoryInfo: CategoryInfo = PreferenceUtil.libraryCategory.first { it.visible }
        if (categoryInfo.visible) {
            navGraph.setStartDestination(categoryInfo.category.id)
        }
        navController.graph = navGraph
        NavigationUI.setupWithNavController(mainActivity.getBottomNavigationView(), navController)
        navController.addOnDestinationChangedListener { _, _, _ ->
            binding.appBarLayout.setExpanded(true, true)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(requireActivity(), binding.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(
            requireContext(),
            binding.toolbar,
            menu,
            getToolbarBackgroundColor(binding.toolbar)
        )
        //Setting up cast button
        CastButtonFactory.setUpMediaRouteButton(requireContext(), menu, R.id.action_cast)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> findNavController().navigate(
                R.id.settingsActivity,
                null,
                navOptions
            )
            R.id.action_import_playlist -> ImportPlaylistDialog().show(
                childFragmentManager,
                "ImportPlaylist"
            )
            R.id.action_add_to_playlist -> CreatePlaylistDialog.create(emptyList()).show(
                childFragmentManager,
                "ShowCreatePlaylistDialog"
            )
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
