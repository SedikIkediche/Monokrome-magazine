package com.ssquare.myapplication.monokrome.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.databinding.FragmentLoginBinding
import com.ssquare.myapplication.monokrome.network.Error
import com.ssquare.myapplication.monokrome.ui.auth.AuthActivity
import com.ssquare.myapplication.monokrome.ui.main.MainActivity
import com.ssquare.myapplication.monokrome.util.*
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider.Companion.hasInternet
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
class LoginFragment : Fragment(), ConnectivityProvider.ConnectivityStateListener {

    @Inject
    lateinit var provider: ConnectivityProvider
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private var isLoggingIn = false
    private lateinit var alertDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater)

        viewModel.userState.observe(viewLifecycleOwner, Observer { isUserSignedIn ->
            Timber.d("authTokenOrException: $isUserSignedIn")
            isUserSignedIn?.let {
                if (it.authToken != null && it.error == null) {
                    navigateToMainActivity()
                } else if (it.authToken == null && it.error != null) {
                    handleError(it.error)
                }
            }
        })

        setTextFieldsListeners()
        setButtonClickListeners()
        setUpAlertDialog()

        return binding.root
    }

    private fun setButtonClickListeners() {
        binding.loginButton.setOnClickListener {
            signInUser()
        }

        binding.joinTextView.setOnClickListener {
            this.findNavController().navigate(R.id.registerFragment)
        }
    }

    override fun onStop() {
        if (alertDialog.isShowing) {
            alertDialog.hideDialog()
        }
        super.onStop()
    }

    private fun handleError(error: Error) {
        isLoggingIn = false
        when {
            error.code == 400 -> showError(getString(R.string.custom_invalid_password))
            error.code == 404 -> showError(getString(R.string.custom_error_email_does_not_exist))
            error.message == getString(R.string.software_connection_abort) -> {
                showError(getString(R.string.network_down))
            }
            error.message == getString(R.string.error_connection_timed_out) || error.message == getString(
                R.string.error_timeout
            ) -> {
                viewModel.abortLogin()
                showError(getString(R.string.failed_connect_to_server))
            }

            else -> showError(getString(R.string.internal_server_error))
        }
    }

    private fun showError(errorMessage: String?) {
        binding.loginButton.isClickable = true
        alertDialog.hideDialog()
        showOneButtonDialog(
            activity as AuthActivity,
            message = errorMessage ?: getString(R.string.credentials_error_massage),
            positiveButtonText = getString(
                R.string.retry
            ),
            title = getString(R.string.oops)
        )

    }

    private fun setUpAlertDialog() {
        alertDialog = AlertDialog.Builder(activity as AuthActivity).create()
    }

    private fun setTextFieldsListeners() {
        binding.email.editText?.setOnFocusChangeListener { _, isFocused ->
            if (!isFocused && binding.password.editText?.isFocused == true && !isEmailValid(binding.email.editText!!.text!!.trim())) {
                binding.email.error = getString(R.string.email_error_message)
            }
        }

        binding.password.editText?.setOnFocusChangeListener { _, isFocused ->
            if (!isFocused) {
                if (binding.password.editText!!.text!!.trim().isEmpty()) {
                    binding.password.error = getString(R.string.password_empty_error_message)
                } else if (binding.password.editText!!.text!!.trim().length < 5) {
                    binding.password.error = getString(R.string.password_length_error_message)
                }
            }
        }

        val textWatcher = object : TextWatcher {

            override fun afterTextChanged(editable: Editable?) {

                val email = binding.email.editText?.text.toString()
                val password = binding.password.editText?.text.toString()

                if (editable.toString() == email && email.trim().isNotEmpty()
                ) {
                    binding.email.error = null
                }

                if (editable.toString() == password && password.trim().isNotEmpty()
                ) {
                    binding.password.error = null
                }

                binding.loginButton.isEnabled =
                    isEmailValid(email.trim())
                            && isPasswordValid(password.trim())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        }

        binding.email.editText?.addTextChangedListener(textWatcher)
        binding.password.editText?.addTextChangedListener(textWatcher)
    }


    private fun signInUser() {
        if (provider.getNetworkState().hasInternet()) {
            alertDialog.showLoading(requireContext(), R.string.log_in_dialog_text)
            binding.loginButton.isClickable = false
            val email = binding.email.editText?.text.toString().trim()
            val passWord = binding.password.editText?.text.toString().trim()

            //login user
            isLoggingIn = true
            viewModel.logInUser(email, passWord)

        } else {
            showOneButtonDialog(
                requireContext(),
                message = getString(R.string.connectivity_error_message),
                positiveButtonText = getString(
                    R.string.close
                ),
                title = getString(R.string.oops)
            )
        }
    }

    private fun navigateToMainActivity() {
        Intent(context, MainActivity::class.java).apply {
            startActivity(this)
            (activity as AuthActivity).finish()
        }
    }

    override fun onStateChange(state: ConnectivityProvider.NetworkState) {
        if (!state.hasInternet() && isLoggingIn) {
            viewModel.abortLogin()
        }
    }

}