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
import com.ssquare.myapplication.monokrome.ui.auth.AuthActivity
import com.ssquare.myapplication.monokrome.ui.main.MainActivity
import com.ssquare.myapplication.monokrome.util.hasInternet
import com.ssquare.myapplication.monokrome.util.hideDialog
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider
import com.ssquare.myapplication.monokrome.util.showLoading
import com.ssquare.myapplication.monokrome.util.showOneButtonDialog
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {

    @Inject
    lateinit var provider: ConnectivityProvider
    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var alertDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater)

        loginViewModel.userState.observe(viewLifecycleOwner, Observer { isUserSignedIn ->
            Timber.d("authTokenOrException: $isUserSignedIn")
            isUserSignedIn?.let {
                if (it.authToken != null && it.error == null) {
                    navigateToMainActivity()
                } else if (it.authToken == null && it.error != null) {
                    showError(it.error.message)
                }
            }
        })

        setTextFieldsListeners2()

        binding.loginButton.setOnClickListener {
            signInUser()
        }

        binding.joinTextView.setOnClickListener {
            this.findNavController().navigate(R.id.registerFragment)
        }

        setUpAlertDialog()

        return binding.root
    }

    override fun onStop() {
        if (alertDialog.isShowing) {
            alertDialog.hideDialog()
        }
        super.onStop()
    }


    private fun showError(errorMessage: String?) {
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
        binding.email.editText?.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused && binding.password.editText?.isFocused == true && binding.email.editText!!.text!!.isEmpty()) {
                binding.email.error = getString(R.string.email_error_message)
            }
        }

        binding.password.editText?.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused && binding.password.editText!!.text!!.isEmpty()) {
                binding.password.error = getString(R.string.password_error_message)
            }
        }

        val textWatcher = object : TextWatcher {

            override fun afterTextChanged(editable: Editable?) {
                if (editable.toString() == binding.email.editText?.text.toString() && binding.email.editText!!.text.toString()
                        .isNotEmpty()
                ) {
                    binding.email.error = null
                } else if (editable.toString() == binding.email.editText?.text.toString() && binding.email.editText!!.text.toString()
                        .isEmpty()
                ) {
                    binding.email.error = getString(R.string.email_error_message)
                }

                if (editable.toString() == binding.password.editText?.text.toString() && binding.password.editText!!.text.toString()
                        .isNotEmpty()
                ) {
                    binding.password.error = null
                } else if (editable.toString() == binding.password.editText?.text.toString() && binding.password.editText!!.text.toString()
                        .isEmpty()
                ) {
                    binding.password.error = getString(R.string.password_error_message)
                }

                binding.loginButton.isEnabled =
                    binding.email.editText!!.text.isNotEmpty()
                            && binding.password.editText!!.text.isNotEmpty()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        }

        binding.email.editText?.addTextChangedListener(textWatcher)
        binding.password.editText?.addTextChangedListener(textWatcher)
    }


    private fun setTextFieldsListeners2() {
        binding.email.editText?.setOnFocusChangeListener { _, isFocused ->
            Timber.d("email OnFocusChangeListener called")
            if (!isFocused && binding.password.editText?.isFocused == true && binding.email.editText!!.text!!.isEmpty()) {
                binding.email.error = getString(R.string.email_error_message)
            }
        }

        binding.password.editText?.setOnFocusChangeListener { _, isFocused ->
            Timber.d("password OnFocusChangeListener called")
            if (!isFocused && binding.password.editText!!.text!!.isEmpty()) {
                binding.password.error = getString(R.string.password_error_message)
            }
        }

        val emailTextWatcher = object : TextWatcher {

            override fun afterTextChanged(editable: Editable?) {
                Timber.d("email afterTextChanged() called")
                if (binding.email.editText!!.text.toString().isNotEmpty()) {
                    binding.email.error = null
                } else if (binding.email.editText!!.text.toString().isEmpty()) {
                    binding.email.error = getString(R.string.email_error_message)
                }

                binding.loginButton.isEnabled =
                    binding.email.editText!!.text.isNotEmpty()
                            && binding.password.editText!!.text.isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        }

        val passwordTextWatcher = object : TextWatcher {

            override fun afterTextChanged(editable: Editable?) {
                 Timber.d("password afterTextChanged() called")
                if (binding.password.editText!!.text.toString().isNotEmpty()) {
                    binding.password.error = null
                } else if (binding.password.editText!!.text.toString().isEmpty()) {
                    binding.password.error = getString(R.string.password_error_message)
                }

                binding.loginButton.isEnabled =
                    binding.email.editText!!.text.isNotEmpty()
                            && binding.password.editText!!.text.isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        }

        binding.email.editText?.addTextChangedListener(emailTextWatcher)
        binding.password.editText?.addTextChangedListener(passwordTextWatcher)

    }

    private fun clearTextFieldsListeners() {
        Timber.d("clearTextFieldsListeners() called")
        binding.email.run {
            error = null
            editText?.onFocusChangeListener = null

        }

        binding.password.run {
            error = null
            editText?.onFocusChangeListener = null
        }
    }

    private fun signInUser() {
        if (provider.getNetworkState().hasInternet()) {
            alertDialog.showLoading(activity as AuthActivity, R.string.log_in_dialog_text)

            val email = binding.email.editText?.text.toString()
            val passWord = binding.password.editText?.text.toString()

            //login user
            loginViewModel.logInUser(email, passWord)

        } else {
            showOneButtonDialog(
                activity as AuthActivity,
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

}