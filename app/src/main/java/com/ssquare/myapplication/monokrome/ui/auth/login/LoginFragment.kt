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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.ssquare.myapplication.monokrome.R
import com.ssquare.myapplication.monokrome.data.AuthRepository
import com.ssquare.myapplication.monokrome.databinding.FragmentLoginBinding
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
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseAuthServer: FirebaseAuthServer
    private lateinit var authRepository: AuthRepository
    private lateinit var loginViewModelFactory: LoginViewModelFactory
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var alertDialog: AlertDialog
    private val provider: ConnectivityProvider by lazy { ConnectivityProvider.createProvider(activity as AuthActivity) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater)

        auth = FirebaseAuth.getInstance()
        firebaseAuthServer = FirebaseAuthServer(auth = auth)
        authRepository = AuthRepository.getInstance(firebaseAuthServer)
        loginViewModelFactory = LoginViewModelFactory(authRepository)
        loginViewModel =
            ViewModelProviders.of(this, loginViewModelFactory).get(LoginViewModel::class.java)

        loginViewModel.isUserSignedIn.observe(viewLifecycleOwner, Observer { isUserSignedIn ->
            if (isUserSignedIn) {
                navigateToMainActivity()
            }else{
                alertDialog.hideDialog()
                showErrorDialog(activity as AuthActivity,getString(R.string.information_error_massage),getString(
                    R.string.retry),getString(R.string.oops))
            }
        })

        setTextFieldsListeners()

        binding.loginButton.setOnClickListener {
            signInUser()
        }

        binding.joinTextView.setOnClickListener {
            this.findNavController().navigate(R.id.registerFragment)
        }

        setUpAlertDialog()

        return binding.root
    }

    private fun setUpAlertDialog() {
        alertDialog = AlertDialog.Builder(activity as AuthActivity).create()
    }

    private fun setTextFieldsListeners() {
        binding.email.editText?.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused && binding.email.editText!!.text!!.isEmpty()) {
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
                if (editable == binding.email.editText?.editableText && binding.email.editText!!.text.toString()
                        .isNotEmpty()
                ) {
                    binding.email.error = null
                } else if (editable == binding.email.editText?.editableText && binding.email.editText!!.text.toString()
                        .isEmpty()
                ) {
                    binding.email.error = getString(R.string.email_error_message)
                }

                if (editable == binding.password.editText?.editableText && binding.password.editText!!.text.toString()
                        .isNotEmpty()
                ) {
                    binding.password.error = null
                } else if (editable == binding.password.editText?.editableText && binding.password.editText!!.text.toString()
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

    private fun signInUser() {
        if(provider.getNetworkState().hasInternet()){
            alertDialog.showLoading(activity as AuthActivity, R.string.log_in_dialog_text)

            val email = binding.email.editText?.text.toString()
            val passWord = binding.password.editText?.text.toString()

            loginViewModel.logInUser(email,passWord)

        }else{
            showErrorDialog(activity as AuthActivity,getString(R.string.connectivity_error_messzge),getString(
                R.string.close),getString(R.string.oops))
        }

    }

    private fun navigateToMainActivity() {
        val intent = Intent(context, MainActivity::class.java).apply {
            startActivity(this)
            (activity as AuthActivity).finish()
        }
    }

    override fun onStop() {
        if (alertDialog.isShowing){
            alertDialog.hideDialog()
        }
        super.onStop()
    }
}