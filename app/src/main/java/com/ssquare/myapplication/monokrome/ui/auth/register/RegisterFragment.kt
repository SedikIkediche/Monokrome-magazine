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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.AuthRepository
import com.ssquare.myapplication.monokrome.databinding.FragmentRegisterBinding
import com.ssquare.myapplication.monokrome.network.FirebaseAuthServer
import com.ssquare.myapplication.monokrome.ui.auth.AuthActivity
import com.ssquare.myapplication.monokrome.ui.main.MainActivity
import com.ssquare.myapplication.monokrome.util.hasInternet
import com.ssquare.myapplication.monokrome.util.hideDialog
import com.ssquare.myapplication.monokrome.util.networkcheck.ConnectivityProvider
import com.ssquare.myapplication.monokrome.util.showErrorDialog
import com.ssquare.myapplication.monokrome.util.showLoading

/**
 * A simple [Fragment] subclass.
 */
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var firebaseAuthServer : FirebaseAuthServer
    private lateinit var authRepository: AuthRepository
    private lateinit var registerViewModelFactory: RegisterViewModelFactory
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var alertDialog: AlertDialog
    private val provider: ConnectivityProvider by lazy { ConnectivityProvider.createProvider(activity as AuthActivity) }
    private var isTheRepeatedPasswordInvalid = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentRegisterBinding.inflate(inflater)

        setUpToolbar()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firebaseAuthServer = FirebaseAuthServer(auth,database)
        authRepository = AuthRepository.getInstance(firebaseAuthServer)
        registerViewModelFactory = RegisterViewModelFactory(authRepository)
        registerViewModel = ViewModelProviders.of(this,registerViewModelFactory).get(RegisterViewModel::class.java)

        registerViewModel.isUssrCreated.observe(viewLifecycleOwner, Observer {isUserCreated ->
            if (isUserCreated){
                navigateToMainActivity()
            }else{
                alertDialog.hideDialog()
                showErrorDialog(activity as AuthActivity,getString(R.string.information_error_massage),getString(
                                    R.string.retry),getString(R.string.oops))
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
    private fun setUpAlertDialog() {
        alertDialog = AlertDialog.Builder(activity as AuthActivity).create()
    }

    private fun setTextFieldsListeners() {
        binding.registerEmail.editText?.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused && binding.registerEmail.editText!!.text!!.isEmpty()) {
                binding.registerEmail.error = getString(R.string.email_error_message)
            }
        }
        binding.registerPassword.editText?.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused && binding.registerPassword.editText!!.text!!.isEmpty()) {
                binding.registerPassword.error = getString(R.string.password_error_message)
            }
        }

        binding.registerRepeatPassword.editText?.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused && binding.registerRepeatPassword.editText!!.text!!.isEmpty()) {
                binding.registerRepeatPassword.error = getString(R.string.repeat_password_error_message)
            }
        }

        val textWatcher = object : TextWatcher {

            override fun afterTextChanged(editable: Editable?) {
                if (editable == binding.registerEmail.editText?.editableText && binding.registerEmail.editText!!.text.toString()
                        .isNotEmpty()
                ) {
                    binding.registerEmail.error = null
                } else if (editable == binding.registerEmail.editText?.editableText && binding.registerEmail.editText!!.text.toString()
                        .isEmpty()
                ) {
                    binding.registerEmail.error = getString(R.string.email_error_message)
                }

                if (editable == binding.registerPassword.editText?.editableText && binding.registerPassword.editText!!.text.toString()
                        .isNotEmpty()
                ) {
                    binding.registerPassword.error = null
                    if (isTheRepeatedPasswordInvalid){
                        binding.registerRepeatPassword.error = null
                        isTheRepeatedPasswordInvalid = false
                    }
                } else if (editable == binding.registerPassword.editText?.editableText && binding.registerPassword.editText!!.text.toString()
                        .isEmpty()
                ) {
                    binding.registerPassword.error = getString(R.string.password_error_message)
                    if (isTheRepeatedPasswordInvalid){
                        binding.registerRepeatPassword.error = null
                        isTheRepeatedPasswordInvalid = false
                    }
                }

                if (editable == binding.registerRepeatPassword.editText?.editableText && binding.registerRepeatPassword.editText!!.text.toString()
                        .isNotEmpty()
                ) {
                    binding.registerRepeatPassword.error = null
                } else if (editable == binding.registerRepeatPassword.editText?.editableText && binding.registerRepeatPassword.editText!!.text.toString()
                        .isEmpty()
                ) {
                    binding.registerRepeatPassword.error = getString(R.string.repeat_password_error_message)
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
        if (provider.getNetworkState().hasInternet()){

            val email = binding.registerEmail.editText?.text.toString()
            val passWord = binding.registerPassword.editText?.text.toString()
            val repeatedPassWord = binding.registerRepeatPassword.editText?.text.toString()

            if (repeatedPassWord == passWord){
                alertDialog.showLoading(activity as AuthActivity,R.string.register_dialog_text)
                registerViewModel.registerUser(email,passWord)
            }else{
                binding.registerRepeatPassword.error =getString(R.string.invalid_repeated_password)
                isTheRepeatedPasswordInvalid = true
            }
        }else{
            showErrorDialog(activity as AuthActivity,getString(R.string.connectivity_error_messzge),getString(
                            R.string.close),getString(R.string.oops))
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
        return when(item.itemId){

            android.R.id.home -> this.findNavController().navigateUp()
            else ->  super.onOptionsItemSelected(item)
        }
    }

}