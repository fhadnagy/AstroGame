package com.fonagyma.astrogame.game.fragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fonagyma.astrogame.game.logic.OLDLiveDrawingView
import com.fonagyma.astrogame.databinding.FragmentFullscreenBaseGameBinding



class FullscreenBaseGameFragment : Fragment() {


    private var _binding: FragmentFullscreenBaseGameBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFullscreenBaseGameBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        val ldView = OLDLiveDrawingView(requireContext(),binding.root.display.width.toInt(),binding.root.display.height.toInt())
        binding.root.addView(ldView)
        ldView.resume()

        //game.start
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}