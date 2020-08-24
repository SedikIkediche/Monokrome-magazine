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
import com.ssquare.myapplication.monokrome.ui.auth.AuthActivity
import com.ssquare.myapplication.monokrome.ui.main.MainActivity
import com.ssquare.myapplication.monokrome.util.hasInternet
import com.ssquare.myapplication.monokrome.util.hideDialog
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider
import com.ssquare.myapplication.monokrome.util.showLoading
import com.ssquare.myapplication.monokrome.util.showOneButtonDialog
import dagger.hilt.android.AndroidEntryPoint
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
    private var isTheRepeatedPasswordInvalid = false

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
                    showError(it.error.message)
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
        binding.registerEmail.editText?.requestFocus()
        binding.registerEmail.editText?.setOnFocusChangeListener { _, isFocused ->

            if (!isFocused && binding.registerEmail.editText!!.text!!.isEmpty()) {
                binding.registerEmail.error = getString(R.string.email_error_message)
            }
        }
        binding.registerPassword.editText?.setOnFocusChangeListener { _, isFocused ->
            if (!isFocused && binding.registerPassword.editText!!.text!!.isEmpty()) {
                binding.registerPassword.error = getString(R.string.password_error_message)
            }
        }
        binding.registerRepeatPassword.editText?.setOnFocusChangeListener { _, isFocused ->
            if (!isFocused && binding.registerRepeatPassword.editText!!.text!!.isEmpty()) {
                binding.registerRepeatPassword.error =
                    getString(R.string.repeat_password_error_message)
            }
        }

        val textWatcher = object : TextWatcher {

            override fun afterTextChanged(editable: Editable?) {

                if (editable.toString() == binding.registerEmail.editText?.text.toString() && binding.registerEmail.editText!!.text.toString()
                        .isNotEmpty()
                ) {
                    binding.registerEmail.error = null
                } else if (binding.registerEmail.editText!!.isFocused && editable.toString() == binding.registerEmail.editText?.text.toString() && binding.registerEmail.editText!!.text.toString()
                        .isEmpty()
                ) {
                    binding.registerEmail.error = getString(R.string.email_error_message)
                }

                if (editable.toString() == binding.registerPassword.editText?.text.toString() && binding.registerPassword.editText!!.text.toString()
                        .isNotEmpty()
                ) {
                    binding.registerPassword.error = null
                    if (isTheRepeatedPasswordInvalid) {
                        binding.registerRepeatPassword.error = null
                        isTheRepeatedPasswordInvalid = false
                    }
                } else if (binding.registerPassword.editText!!.isFocused && editable.toString() == binding.registerPassword.editText?.text.toString() && binding.registerPassword.editText!!.text.toString()
                        .isEmpty()
                ) {
                    binding.registerPassword.error = getString(R.string.password_error_message)
                    if (isTheRepeatedPasswordInvalid) {
                        binding.registerRepeatPassword.error = null
                        isTheRepeatedPasswordInvalid = false
                    }
                }

                if (editable.toString() == binding.registerRepeatPassword.editText?.text.toString() && binding.registerRepeatPassword.editText!!.text.toString()
                        .isNotEmpty()
                ) {
                    binding.registerRepeatPassword.error = null
                } else if (binding.registerRepeatPassword.editText!!.isFocused && editable.toString() == binding.registerRepeatPassword.editText?.text.toString() && binding.registerRepeatPassword.editText!!.text.toString()
                        .isEmpty()
                ) {
                    binding.registerRepeatPassword.error =
                        getString(R.string.repeat_password_error_message)
                }

                binding.registerButton.isEnabled =
                    binding.registerEmail.editText!!.text.isNotEmpty()
                            && binding.registerPassword.editText!!.text.isNotEmpty()
                            && binding.registerRepeatPassword.editText!!.text.isNotEmpty()
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

            val email = binding.registerEmail.editText?.text.toString()
            val passWord = binding.registerPassword.editText?.text.toString()
            val repeatedPassWord = binding.registerRepeatPassword.editText?.text.toString()

            if (repeatedPassWord == passWord) {
                alertDialog.showLoading(activity as AuthActivity, R.string.register_dialog_text)
                //registeringUser ****************
                registerViewModel.registerUser(email, passWord)
            } else {
                binding.registerRepeatPassword.error = getString(R.string.invalid_repeated_password)
                isTheRepeatedPasswordInvalid = true
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