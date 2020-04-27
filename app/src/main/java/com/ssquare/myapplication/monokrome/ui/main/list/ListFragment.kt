package com.ssquare.myapplication.monokrome.ui.main.list

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
import com.ssquare.myapplication.monokrome.data.MagazineListOrException
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.databinding.FragmentListBinding
import com.ssquare.myapplication.monokrome.ui.main.MainActivity
import com.ssquare.myapplication.monokrome.util.ClickAction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.data.Repository
import com.ssquare.myapplication.monokrome.databinding.FragmentListBinding
import com.ssquare.myapplication.monokrome.db.LocalCache
import com.ssquare.myapplication.monokrome.db.MagazineDatabase
import com.ssquare.myapplication.monokrome.network.FirebaseServer
import com.ssquare.myapplication.monokrome.util.*
import com.ssquare.myapplication.monokrome.work.RefreshDataWorker
import java.util.concurrent.TimeUnit

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

    private val networkCheck: NetworkCheck by lazy {
        NetworkCheck(
            requireContext(),
            lifecycleScope
        ).apply { registerNetworkCallback() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater)

        (activity as MainActivity).setSupportActionBar(binding.listFragmentToolbar)
        NavigationUI.setupActionBarWithNavController(activity as MainActivity,this.findNavController(),binding.drawer)
        NavigationUI.setupWithNavController(binding.navigation,this.findNavController())

        val database = FirebaseDatabase.getInstance()
        val storage = FirebaseStorage.getInstance()
        val network = FirebaseServer(database, storage)
        val magazineDao = MagazineDatabase.getInstance(requireContext()).magazineDao
        val headerDao = MagazineDatabase.getInstance(requireContext()).headerDao
        val cache = LocalCache(magazineDao, headerDao)
        val repository = Repository.getInstance(lifecycleScope, cache, network)
        val factory = ListViewModelFactory(repository)
        viewModel = ViewModelProviders.of(this, factory).get(ListViewModel::class.java)

        initRecyclerView()

        setHasOptionsMenu(true)

        addSearchViewListener()


        networkCheck.isConnected.observe(viewLifecycleOwner, Observer { isConnected ->
            if (!isDataCached(requireContext())) {
                showLoading()
                when (isConnected) {
                    true -> {
                        cacheData()
                    }
                    false -> {
                        showError("Please Connect To The Internet!")
                    }
                }
            }
        })

        viewModel.networkError.observe(viewLifecycleOwner, Observer {
            setupUi(null, null, it)
        })

        viewModel.data.observe(viewLifecycleOwner, Observer {
            if (isDataCached(requireContext()))
                setupUi(it.first, it.second)
        })


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

        private fun setupUi(header: Header?, magazines: List<Magazine>?, exception: Exception? = null) {
            when {
                header == null && magazines.isNullOrEmpty() && exception == null -> {
                    return
                }
                header != null && magazines != null && exception == null -> {
                    adapter.addHeaderAndSubmitList(magazines, header)
                    showData()
                }
                exception != null -> {
                    showError("Something wrong happened: $exception")
                }
            }
        }

    private fun cacheData() {
        viewModel.loadAndCacheData()
        commitCacheData(requireContext())
        launchUpdateWorker()
    }


    override fun onPause() {
        networkCheck.unregisterNetworkCallback()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        networkCheck.registerNetworkCallback()
    }


    private fun setupUi(header: Header?, magazines: List<Magazine>?, exception: Exception? = null) {
        when {
            header == null && magazines.isNullOrEmpty() && exception == null -> {
                return
            }
            header != null && magazines != null && exception == null -> {
                adapter.addHeaderAndSubmitList(magazines, header)
                showData()
            }
            exception != null -> {
                showError("Something wrong happened: $exception")
            }
        }

    }

    private fun initRecyclerView() {
        showLoadingLayout()

        val headerListener = MagazineAdapter.HeaderListener { /*Handle header click */ }

        val magazineListener = MagazineAdapter.MagazineListener { magazine, action ->
            when (action) {
                ClickAction.PREVIEW -> {
                    navigateToDetail(magazine.path)
                }
                ClickAction.DOWNLOAD -> {
                    downloadMagazine(magazine)
                }
                ClickAction.READ -> { /*Handle Read click */
                }
            }

        }
        adapter = MagazineAdapter(magazineListener, headerListener)
        binding.recyclerview.adapter = adapter
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
    private fun downloadMagazine(magazine: Magazine) {
        if (isConnected(requireContext())) {
            downloadFile(magazine, requireContext())
        } else {
            showErrorLayout(getString(R.string.network_down))
        }
    }

    private fun launchUpdateWorker() {
        //launch work
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED).build()

        val cacheWorkRequest = OneTimeWorkRequest.Builder(RefreshDataWorker::class.java)
            .setInitialDelay(REFRESH_TIME, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(requireContext().applicationContext)
            .enqueue(cacheWorkRequest)
    }

    private fun navigateToDetail(path: String) {
        val pathBundle = Bundle().apply { putString(MAGAZINE_PATH, path) }
        findNavController().navigate(R.id.action_listFragment_to_detailFragment, pathBundle)
    }

    private fun showLoading() {
        binding.run {
            recyclerview.visibility = View.GONE
            textError.visibility = View.GONE
            shimmerLayout.visibility = View.VISIBLE
            shimmerLayout.startShimmer()
        }

    }

    private fun showError(errorText: String) {
        binding.run {
            shimmerLayout.hideShimmer()
            shimmerLayout.visibility = View.GONE
            recyclerview.visibility = View.GONE
        }
        showErrorLayout(errorText)
    }

    private fun showData() {
        binding.run {
            shimmerLayout.hideShimmer()
            shimmerLayout.visibility = View.GONE
            textError.visibility = View.GONE
            recyclerview.visibility = View.VISIBLE
        }
    }

    private fun showErrorLayout(errorText: String) {
        binding.run {
            textError.visibility = View.VISIBLE
            textError.text = errorText
        }
    }

}