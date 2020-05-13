package com.ssquare.myapplication.monokrome.ui.main.search

import android.content.Context
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ssquare.myapplication.monokrome.databinding.FragmentSearchMagazineBinding


/**
 * A simple [Fragment] subclass.
 */
class SearchMagazineFragment : Fragment() {

    private lateinit var binding : FragmentSearchMagazineBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentSearchMagazineBinding.inflate(inflater)

        setUpSearchView()

        setUpNavigateUpButton()

        return binding.root
    }

    private fun setUpNavigateUpButton() {
        binding.searchToolBar.getChildAt(0).setOnClickListener {
            hideKeyBoard()
            findNavController().navigateUp()
        }
    }

    private fun setUpSearchView() {
        binding.searchView.requestFocus()
         showKeyBoard()
    }

    private fun showKeyBoard(){
        getInputMethodManagerInstance()?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun hideKeyBoard(){
        getInputMethodManagerInstance()?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun getInputMethodManagerInstance(): InputMethodManager? {
        return getSystemService(requireContext(), InputMethodManager::class.java)
    }

}
