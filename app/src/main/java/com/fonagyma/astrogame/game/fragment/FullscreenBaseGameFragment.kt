package com.fonagyma.astrogame.game.fragment

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import androidx.fragment.app.Fragment
import com.fonagyma.astrogame.game.logic.LiveDrawingView
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

        val ldView = LiveDrawingView(requireContext(),binding.root.display.width.toInt(),binding.root.display.height.toInt())
        binding.root.addView(ldView)
        ldView.resume()

        //game.start
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}