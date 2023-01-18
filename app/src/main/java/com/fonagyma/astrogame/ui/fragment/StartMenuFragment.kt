package com.fonagyma.astrogame.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fonagyma.astrogame.R
import com.fonagyma.astrogame.databinding.FragmentStartMenuBinding
import com.fonagyma.astrogame.databinding.FragmentStatsBinding

class StartMenuFragment: Fragment() {

    private var _binding: FragmentStartMenuBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStartMenuBinding.inflate(inflater, container, false)

        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_main, menu)

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_info-> {
                        val action = StartMenuFragmentDirections.actionStartMenuFragmentToInfoFragment()
                        findNavController().navigate(action)
                        true
                    }
                    R.id.menu_settings->{
                        val action = StartMenuFragmentDirections.actionStartMenuFragmentToSettingsFragment()
                        findNavController().navigate(action)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.statsButton.setOnClickListener {
            val action = StartMenuFragmentDirections.actionStartMenuFragmentToStatsFragment()
            findNavController().navigate(action)
        }

        binding.gamemodesButton.setOnClickListener {
            val action = StartMenuFragmentDirections.actionStartMenuFragmentToGamemodesFragment()
            findNavController().navigate(action)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}