package th.or.etda.teda.mobile.ui.importkey.password

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.InputFilter
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.button.MaterialButton
import com.google.api.services.drive.DriveScopes
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.MainActivity
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.BiometricEncryptedSharedPreferences
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.databinding.ImportKeyPasswordFragmentBinding
import th.or.etda.teda.mobile.model.ExtrackP12
import th.or.etda.teda.mobile.ui.backupkey.googledrive.DriveServiceHelper
import th.or.etda.teda.mobile.ui.backupkey.password.BackupKeyPasswordFragment
import th.or.etda.teda.mobile.ui.importkey.ImportHelper
import th.or.etda.teda.mobile.util.Constants
import th.or.etda.teda.mobile.util.UtilApps
import th.or.etda.teda.ui.base.BaseFragment
import java.io.File


class ImportKeyPasswordFragment : BaseFragment<ImportKeyPasswordFragmentBinding>(
    layoutId = R.layout.import_key_password_fragment
) {

    companion object {
        fun newInstance() = ImportKeyPasswordFragment()
        const val REQUEST_KEY = "ImportKeyPasswordFragment_Password"
    }

    private val REQUEST_CODE_SIGN_IN = 100
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mDriveServiceHelper: DriveServiceHelper? = null

    private val viewModel: ImportKeyPasswordViewModel by viewModel()

    var filePath = ""
    var filePathBackup: File? = null
    var nameTimestamp = ""


//    private val blockCharacterSet = "~#^|$%&*!@"
//
//    private val filter =
//        InputFilter { source, start, end, dest, dstart, dend ->
//            if (source != null && blockCharacterSet.contains("" + source)) {
//                ""
//            } else null
//        }

    override fun onInitDependencyInjection() {

    }


    override fun onInitDataBinding() {

        initActionBar()

        var isPasswordResult = arguments?.let {
            ImportKeyPasswordFragmentArgs.fromBundle(it).isResult
        }


        filePath = arguments?.let {
            ImportKeyPasswordFragmentArgs.fromBundle(it).file
        }.toString()

//        viewBinding.edtName.filters = arrayOf(filter)

        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) { /* ... */

                    if (isPasswordResult == true) {
                        viewBinding.extractCaBtn.text = "Confirm"
                    }
                    viewBinding.extractCaBtn.setOnClickListener {
                        if (isPasswordResult == true) {
                            onPasswordResult()
                        } else {
                            submit()
                        }

                    }


                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) { /* ... */
                    Toast.makeText(requireContext(),"Please allow permission", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) { /* ... */
                    token?.continuePermissionRequest()
                }
            }).check()

        setFragmentResultListener(BackupKeyPasswordFragment.REQUEST_KEY) { key, bundle ->
            // read from the bundle
            var passwordBackup = bundle.getString("password")
            var file = File(filePath)
            passwordBackup?.let { backup(requireContext(), it, file) }
        }
        setFragmentResultListener(BackupKeyPasswordFragment.REQUEST_KEY_BACK) { key, bundle ->
            // read from the bundle
            authenGoogleDrive()
        }

        ImportHelper.fileLiveData.observe(viewLifecycleOwner, Observer {
            filePathBackup = it
            uploadFile()
        })


    }


    fun initActionBar() {
        viewBinding.actionBar.tvTitle.setText("Import P12 password")
        viewBinding.actionBar.btnBack.setOnClickListener {
            val ac = activity as MainActivity
            ac.onBackPressed()
        }
    }

    fun saveData(name: String, extrackP12: ExtrackP12) {

        nameTimestamp = name
        val privKeyBytes: ByteArray? = extrackP12.privateKey?.encoded
        val privKeyStr = String(Base64.encode(privKeyBytes, Base64.NO_WRAP))

        var allowBio = 0
        if(android.os.Build.VERSION.SDK_INT==28||android.os.Build.VERSION.SDK_INT==29){
            allowBio = BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        }else{
            allowBio = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        }

        BiometricEncryptedSharedPreferences.create(
            this,
            Constants.FileName,
            1,
            BiometricPrompt.PromptInfo.Builder().setTitle(getString(R.string.app_name))
                .setAllowedAuthenticators(
                    allowBio
                ).build()
        ).observe(this, Observer { it: SharedPreferences? ->
            if (it != null) {
                it.edit().putString(name, privKeyStr).apply()

                viewModel.addCertificate(
                    Certificate(
                        name,
                        extrackP12.cert,
                        extrackP12.chains,
                        UtilApps.currentDate()
                    )
                )


                alertBackupDialog()
            }

        })


    }

    private fun submit() {

        val password = viewBinding.edtPassword.text.toString()
        val name = viewBinding.edtName.text.toString()
        if (name.trim().isEmpty()) {
            alertInput()
            return
        }
        if (password.isEmpty()) {
            alertInput()
            return
        }
        if (password.isNotEmpty() && name.trim().isNotEmpty()) {
            var nameTime = name + "_" + UtilApps.timestampName()
            viewModel.extractP12Success.observe(viewLifecycleOwner, Observer {
                saveData(it.name, it)
            })
            var file = File(filePath)
            viewModel.caUri.value = Uri.fromFile(file)
            viewModel.extractP12(requireContext(), password, nameTime)

        }
    }


    fun backup(context: Context, passwordBackup: String, file: File) {
        ImportHelper.encryptP12forBackup(context, nameTimestamp, passwordBackup, file)

    }


    private fun alertBox() {
        UtilApps.hideSoftKeyboard(requireCompatActivity())
        val dialog = Dialog(requireCompatActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_import_success)
        dialog.getWindow()
            ?.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.transparent)));
        dialog.getWindow()?.setLayout(
            ((UtilApps.getScreenWidth(getActivity()) * .9).toInt()),
            ViewGroup.LayoutParams.WRAP_CONTENT
        );

        dialog.setCancelable(false)


        val yesBtn = dialog.findViewById(R.id.btn_positive) as MaterialButton
        yesBtn.setOnClickListener {
            dialog.dismiss()
            val action =
                ImportKeyPasswordFragmentDirections.actionToFirst()
            findNavController().navigate(action)
        }

        dialog.show()
    }

    private fun alertBackupDialog() {

        val dialog = Dialog(requireCompatActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.getWindow()
            ?.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.transparent)));
        val window: Window? = dialog.getWindow()
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_backup)

        val yesBtn = dialog.findViewById(R.id.btn_positive) as MaterialButton
        val noBtn = dialog.findViewById(R.id.btn_negative) as MaterialButton
        yesBtn.setOnClickListener {
            dialog.dismiss()
            authenGoogleDrive()
        }
        noBtn.setOnClickListener {
            dialog.dismiss()
            alertBox()
        }
        dialog.show()
    }

    fun authenGoogleDrive() {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())

        if (account == null) {
            signIn()
        } else {
            mDriveServiceHelper =
                DriveServiceHelper(
                    DriveServiceHelper.getGoogleDriveService(
                        requireContext(),
                        account, DriveServiceHelper.DRIVE_APP_NAME
                    )
                )

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = GoogleSignIn.getClient(requireCompatActivity(), gso)
            googleSignInClient.signOut().addOnSuccessListener {
                signIn()
            }


        }
    }


    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope(DriveScopes.DRIVE_FILE),
//                Scope(DriveScopes.DRIVE),
                Scope(DriveScopes.DRIVE_APPDATA),
//                Scope(DriveScopes.DRIVE_METADATA)
            )
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireCompatActivity(), gso)
        val signInIntent: Intent? = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> if (resultCode == Activity.RESULT_OK && data != null) {
                handleSignInResult(data)
            }else{
                val action =
                    ImportKeyPasswordFragmentDirections.actionToFirst()
                findNavController().navigate(action)
//                findNavController().navigateUp()
            }
        }
    }

    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleSignInAccount ->
                Log.d("MainActivity.TAG", "Signed in as " + googleSignInAccount.email)
//                viewBinding.email.setText(googleSignInAccount.email)
                mDriveServiceHelper = DriveServiceHelper(
                    DriveServiceHelper.getGoogleDriveService(
                        requireContext(),
                        googleSignInAccount,
                        DriveServiceHelper.DRIVE_APP_NAME
                    )
                )
                val action =
                    ImportKeyPasswordFragmentDirections.nextActionBackupPassword()
                findNavController().navigate(action)
                Log.d("MainActivity.TAG", "handleSignInResult: $mDriveServiceHelper")
            }
            .addOnFailureListener { e -> Log.e("MainActivity.TAG", "Unable to sign in.", e) }
    }

    fun uploadFile() {
        viewBinding.progressBar2.visibility = View.VISIBLE
        mDriveServiceHelper?.let {
            filePathBackup?.let { it1 ->
                viewModel.uploadFile(
                    requireContext(), it,
                    it1
                )
            }
        }
        viewModel.uploadSuccess.observe(viewLifecycleOwner, Observer {
            viewBinding.progressBar2.visibility = View.GONE
            alertBox()

        })
        viewModel.isSuccess.observe(viewLifecycleOwner, Observer {
            if (!it) {
                alertLogin()
            }
        })
    }


    fun onPasswordResult() {
        val password = viewBinding.edtPassword.text.toString()
//        val password2 = viewBinding.passwordEncrypt.text.toString()
        val name = viewBinding.edtName.text.toString()
        if (password.isNotEmpty() && name.isNotEmpty()) {
            if (password.isNotEmpty()) {
                setFragmentResult(
                    ImportKeyPasswordFragment.REQUEST_KEY,
                    bundleOf("password" to password, "name" to name)
                )
                findNavController().navigateUp()
            }
        }
    }

    private fun alertInput() {

        val dialog = Dialog(requireCompatActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_alert)
        dialog.getWindow()
            ?.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.transparent)));
        dialog.getWindow()?.setLayout(
            ((UtilApps.getScreenWidth(getActivity()) * .9).toInt()),
            ViewGroup.LayoutParams.WRAP_CONTENT
        );

        dialog.setCancelable(false)


        val yesBtn = dialog.findViewById(R.id.btn_positive) as MaterialButton
        yesBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun alertLogin() {

        val dialog = Dialog(requireCompatActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_alert)
        dialog.getWindow()
            ?.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.transparent)));
        dialog.getWindow()?.setLayout(
            ((UtilApps.getScreenWidth(getActivity()) * .9).toInt()),
            ViewGroup.LayoutParams.WRAP_CONTENT
        );

        dialog.setCancelable(false)

        val tvTitle = dialog.findViewById<TextView>(R.id.tv_title)
        val yesBtn = dialog.findViewById(R.id.btn_positive) as MaterialButton

        tvTitle.setText(getString(R.string.alert_google_drive))
        yesBtn.setOnClickListener {
            dialog.dismiss()
            authenGoogleDrive()
        }


        dialog.show()
    }

}