package com.ssquare.myapplication.monokrome.main.list
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import br.com.mauker.materialsearchview.MaterialSearchView
import br.com.mauker.materialsearchview.MaterialSearchView.SearchViewListener
import com.google.firebase.database.FirebaseDatabase
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.databinding.FragmentListBinding
import com.ssquare.myapplication.monokrome.main.MainActivity
import com.ssquare.myapplication.monokrome.main.data.MagazineListOrException
import com.ssquare.myapplication.monokrome.main.data.Repository
import com.ssquare.myapplication.monokrome.main.util.ClickAction


/**
 * A simple [Fragment] subclass.
 */
class ListFragment : Fragment(){
    lateinit var binding: FragmentListBinding
    private lateinit var viewModel: ListViewModel
    private lateinit var adapter: MagazineAdapter

     val  onBackPressedCallback = object : OnBackPressedCallback(true) {
         override fun handleOnBackPressed() {
             if (binding.searchView.isOpen) {
                 binding.searchView.closeSearch()
             }
             if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                 binding.drawer.closeDrawer(GravityCompat.START)
             }
         }
     }
    companion object{
      private  const val REQUEST_CODE: Int = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater)

        (activity as MainActivity).setSupportActionBar(binding.listFragmentToolbar)

        NavigationUI.setupActionBarWithNavController(activity as MainActivity,this.findNavController(),binding.drawer)

        NavigationUI.setupWithNavController(binding.navigation,this.findNavController())

        val database = FirebaseDatabase.getInstance()
        val repository = Repository.getInstance(database)
        val factory = ListViewModelFactory(repository)
        viewModel = ViewModelProviders.of(this, factory).get(ListViewModel::class.java)

        initRecyclerView()

        viewModel.magazines.observe(viewLifecycleOwner, Observer {
            setupUi(it)
        })

        setHasOptionsMenu(true)

        addSearchViewListener()



        return binding.root
    }

    private fun addSearchViewListener() {
        binding.searchView.setSearchViewListener(object : SearchViewListener {
            override fun onSearchViewOpened() {
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
            }

            override fun onSearchViewClosed() {
               onBackPressedCallback.remove()
            }
        })

        binding.searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {

                binding.searchView.closeSearch()

                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        binding.searchView.setOnVoiceClickedListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice searching...")
            startActivityForResult(intent, REQUEST_CODE)
        }

        binding.searchView.findViewById<View>(R.id.transparent_view).visibility = View.GONE
        binding.searchView.findViewById<LinearLayout>(R.id.search_bar).setBackgroundResource(R.drawable.search_view_background)
    }

    private fun setupUi(response: MagazineListOrException) {
        hideLoadingLayout()
        if (!response.data.isNullOrEmpty() && response.exception == null) {
            adapter.addHeaderAndSubmitList(response.data)
            hideError()
        } else {
            showError(response.exception!!.message!!)
        }

    }

    private fun initRecyclerView() {
        showLoadingLayout()
        val headerListener = MagazineAdapter.HeaderListener {
            Toast.makeText(requireContext(), "Header Clicked", Toast.LENGTH_SHORT).show()
        }

        val magazineListener = MagazineAdapter.MagazineListener { path, action ->
            when (action) {
                ClickAction.PREVIEW -> this.findNavController().navigate(R.id.detailFragment)
                ClickAction.DOWNLOAD -> Toast.makeText(
                    context,
                    "item $path clicked action is DOWNLOAD",
                    Toast.LENGTH_SHORT
                ).show()
                ClickAction.READ -> Toast.makeText(
                    context,
                    "item $path clicked action is READ",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        adapter = MagazineAdapter(magazineListener, headerListener)
        binding.recyclerview.adapter = adapter
    }

    private fun showLoadingLayout() {
        binding.shimmerLayout.startShimmer()
        binding.recyclerview.visibility = View.GONE
    }

    private fun hideLoadingLayout() {
        binding.shimmerLayout.hideShimmer()
        binding.recyclerview.visibility = View.VISIBLE
    }

    private fun showError(errorText: String) {
        binding.textError.visibility = View.VISIBLE
        binding.textError.text = errorText
    }

    private fun hideError() {
        binding.textError.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fagment_loolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.search -> {
               showSearchView()
                true
            }
            R.id.filter_list -> {
                true
            }
            android.R.id.home -> {
                if (!binding.drawer.isDrawerOpen(GravityCompat.START)) {
                    binding.drawer.openDrawer(GravityCompat.START)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showSearchView(){
        binding.searchView.openSearch()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches =
                data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (matches != null && matches.size > 0) {
                val searchWrd = matches[0]
                if (!TextUtils.isEmpty(searchWrd)) {
                    binding.searchView.setQuery(searchWrd, false)
                    Log.d("search", "result : $searchWrd")
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
