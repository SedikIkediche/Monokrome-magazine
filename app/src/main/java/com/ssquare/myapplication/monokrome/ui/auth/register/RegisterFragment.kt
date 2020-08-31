package com.ssquare.myapplication.monokrome.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.databinding.FragmentRegisterBinding
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
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels()
    private lateinit var alertDialog: AlertDialog

    @Inject
    lateinit var provider: ConnectivityProvider
    private var isRepeatedPasswordValid = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater)
        setUpToolbar()

        registerViewModel.userState.observe(viewLifecycleOwner, Observer { isUserCreated ->
            isUserCreated?.let {
                if (it.authToken != null && it.error == null) {
                    navigateToMainActivity()
                } else if (it.authToken == null && it.error != null) {
                    handleError(it.error)
                }
            }
        })

        setTextFieldsListeners()

        binding.registerButton.setOnClickListener {
            createNewUser()
        }

        binding.loginTextView.setOnClickListener {
            this.findNavController().navigateUp()
        }

        setHasOptionsMenu(true)

        setUpAlertDialog()

        return binding.root
    }

    private fun handleError(error: Error) {
        when (error.code) {
            400 -> showError(getString(R.string.custom_error_email_registered))
            else -> showError(getString(R.string.internal_server_error))
        }
    }

    private fun showError(errorMessage: String?) {
        binding.registerButton.isClickable = true
        alertDialog.hideDialog()
        alertDialog = showOneButtonDialog(
            requireContext(),
            message = errorMessage ?: getString(R.string.credentials_error_massage),
            positiveButtonText = getString(
                R.string.retry
            ),
            title = getString(R.string.oops)
        )

    }

    private fun setUpAlertDialog() {
        alertDialog = AlertDialog.Builder(requireContext()).create()
    }

    private fun setTextFieldsListeners() {
        binding.registerEmail.editText?.requestFocus()

        binding.registerEmail.editText?.setOnFocusChangeListener { _, isFocused ->
            if (!isFocused && !isEmailValid(binding.registerEmail.editText!!.text!!.trim())) {
                binding.registerEmail.error = getString(R.string.email_error_message)
            }
        }
        binding.registerPassword.editText?.setOnFocusChangeListener { _, isFocused ->
            if (!isFocused) {
                if (binding.registerPassword.editText!!.text!!.trim().isEmpty()) {
                    binding.registerPassword.error =
                        getString(R.string.password_empty_error_message)
                } else if (binding.registerPassword.editText!!.text!!.trim().length < 5) {
                    binding.registerPassword.error =
                        getString(R.string.password_length_error_message)
                }
            }
        }
        binding.registerRepeatPassword.editText?.setOnFocusChangeListener { _, isFocused ->
            if (isFocused && !isRepeatedPasswordValid) {
                isRepeatedPasswordValid = true
            } else if (!isFocused) {
                if (binding.registerRepeatPassword.editText!!.text!!.trim().isEmpty()) {
                    binding.registerRepeatPassword.error =
                        getString(R.string.repeat_password_error_message)
                } else if (binding.registerPassword.editText!!.text!!.toString() != binding.registerRepeatPassword.editText!!.text!!.toString()
                ) {
                    Timber.d("part called 1")
                    binding.registerRepeatPassword.error =
                        getString(R.string.repeated_password_match_error)
                }

            }
        }

        val textWatcher = object : TextWatcher {

            override fun afterTextChanged(editable: Editable?) {

                val email = binding.registerEmail.editText?.text.toString()
                val password = binding.registerPassword.editText?.text.toString()
                val repeatedPassword = binding.registerRepeatPassword.editText?.text.toString()

                if (editable.toString() == email && email.trim().isNotEmpty()) {
                    binding.registerEmail.error = null
                }


                if (editable.toString() == password) {

                    if (password.trim().isNotEmpty()) {
                        binding.registerPassword.error = null
                    } else if (password.trim().isEmpty()
                        && isRepeatedPasswordValid && password != repeatedPassword
                    ) {
                        Timber.d("part called 3")
                        binding.registerRepeatPassword.error =
                            getString(R.string.repeated_password_match_error)
                    }
                }

                // is repeated empty
                if (editable.toString() == repeatedPassword && isRepeatedPasswordValid && repeatedPassword.trim()
                        .isEmpty()
                ) {

                    binding.registerRepeatPassword.error =
                        getString(R.string.repeat_password_error_message)
                }

                // comparing password and repeated
                if (password == repeatedPassword && isRepeatedPasswordValid) {
                    binding.registerRepeatPassword.error = null
                } else if (password != repeatedPassword && isRepeatedPasswordValid && repeatedPassword.trim()
                        .isNotEmpty()
                ) {
                    binding.registerRepeatPassword.error =
                        getString(R.string.repeated_password_match_error)
                }


                binding.registerButton.isEnabled =
                    isEmailValid(email.trim())
                            && isPasswordValid(password.trim())
                            && password.trim() == repeatedPassword.trim()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        }

        binding.registerEmail.editText?.addTextChangedListener(textWatcher)
        binding.registerPassword.editText?.addTextChangedListener(textWatcher)
        binding.registerRepeatPassword.editText?.addTextChangedListener(textWatcher)
    }

    private fun createNewUser() {
        if (provider.getNetworkState().hasInternet()) {

            val email = binding.registerEmail.editText?.text.toString().trim()
            val passWord = binding.registerPassword.editText?.text.toString().trim()
            val repeatedPassWord = binding.registerRepeatPassword.editText?.text.toString().trim()

            if (repeatedPassWord == passWord) {
                alertDialog.showLoading(requireContext(), R.string.register_dialog_text)
                binding.registerButton.isClickable = false
                //registeringUser ****************
                registerViewModel.registerUser(email, passWord)
            } else {
                binding.registerRepeatPassword.error = getString(R.string.invalid_repeated_password)
            }
        } else {
            showError(getString(R.string.connectivity_error_message))
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
        (activity as AuthActivity).finish()
    }

    private fun setUpToolbar() {
        (activity as AuthActivity).setSupportActionBar(binding.registerToolbar)

        (activity as AuthActivity).supportActionBar?.title = ""
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            android.R.id.home -> this.findNavController().navigateUp()
            else -> super.onOptionsItemSelected(item)
        }
    }

}