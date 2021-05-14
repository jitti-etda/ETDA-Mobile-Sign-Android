//package th.or.etda.teda.mobile.ui.importkey.password
//
//import android.Manifest
//import android.app.Activity
//import android.app.AlertDialog
//import android.content.Intent
//import android.net.Uri
//import android.util.Log
//import android.view.View
//import androidx.core.os.bundleOf
//import androidx.fragment.app.setFragmentResult
//import androidx.fragment.app.setFragmentResultListener
//import androidx.lifecycle.Observer
//import androidx.navigation.fragment.findNavController
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInClient
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.android.gms.common.api.Scope
//import com.google.api.services.drive.DriveScopes
//import com.karumi.dexter.Dexter
//import com.karumi.dexter.PermissionToken
//import com.karumi.dexter.listener.PermissionDeniedResponse
//import com.karumi.dexter.listener.PermissionGrantedResponse
//import com.karumi.dexter.listener.PermissionRequest
//import com.karumi.dexter.listener.single.PermissionListener
//import org.koin.android.viewmodel.ext.android.viewModel
//import th.or.etda.teda.mobile.R
//import th.or.etda.teda.mobile.databinding.ImportKeyPasswordFragmentBinding
//import th.or.etda.teda.mobile.ui.backupkey.googledrive.DriveServiceHelper
//import th.or.etda.teda.mobile.ui.backupkey.password.BackupKeyPasswordFragment
//import th.or.etda.teda.mobile.ui.importkey.ImportHelper
//import th.or.etda.teda.ui.base.BaseFragment
//import java.io.File
//
//
//class ImportKeyPasswordFragmentBackup : BaseFragment<ImportKeyPasswordFragmentBinding>(
//    layoutId = R.layout.import_key_password_fragment
//) {
//
//    companion object {
//        fun newInstance() = ImportKeyPasswordFragmentBackup()
//        const val REQUEST_KEY = "ImportKeyPasswordFragment_Password"
//    }
//
//    private val REQUEST_CODE_SIGN_IN = 100
//    private var mGoogleSignInClient: GoogleSignInClient? = null
//    private var mDriveServiceHelper: DriveServiceHelper? = null
//
//    private val viewModel: ImportKeyPasswordViewModel by viewModel()
////    val backupViewModel: BackupKeyPasswordViewModel by viewModel()
////    private val viewModel: ExtractCAViewModel by viewModels {
////        ExtractCaViewModelFactory((requireActivity().application as TEDAMobileApplication).repository)
////    }
//
////    private val viewModel: ExtractCAViewModel by viewModels()
//
//    var filePath = ""
//    var filePathBackup: File? = null
//
//    override fun onInitDependencyInjection() {
//
//    }
//
//    override fun onInitDataBinding() {
//
//        var isPasswordResult = arguments?.let {
//            ImportKeyPasswordFragmentArgs.fromBundle(it).isResult
//        }
//
//        filePath = arguments?.let {
//            ImportKeyPasswordFragmentArgs.fromBundle(it).file
//        }.toString()
//
//        Dexter.withContext(requireContext())
//            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            .withListener(object : PermissionListener {
//                override fun onPermissionGranted(response: PermissionGrantedResponse) { /* ... */
//
//                    if (isPasswordResult == true) {
//                        viewBinding.extractCaBtn.setText("Confirm")
//                    }
//                    viewBinding.extractCaBtn.setOnClickListener {
//                        if (isPasswordResult == true) {
//                            onPasswordResult()
//                        } else {
//                            alertBackup()
//                        }
//
//                    }
//
//
//                }
//
//                override fun onPermissionDenied(response: PermissionDeniedResponse) { /* ... */
//
//                }
//
//                override fun onPermissionRationaleShouldBeShown(
//                    permission: PermissionRequest?,
//                    token: PermissionToken?
//                ) { /* ... */
//
//                }
//            }).check()
//
//        setFragmentResultListener(BackupKeyPasswordFragment.REQUEST_KEY) { key, bundle ->
//            // read from the bundle
//            var passwordBackup = bundle.getString("password")
//            val password = viewBinding.passwordText.text.toString()
////        val password2 = viewBinding.passwordEncrypt.text.toString()
//            val name = viewBinding.nameCert.text.toString()
//            passwordBackup?.let { extrack(password, it, name, true) }
//        }
//
//
//        ImportHelper.fileLiveData.observe(viewLifecycleOwner, Observer {
//            filePathBackup = it
//            Log.i("filePath", filePathBackup.toString())
//        })
//
//    }
//
//
////    fun importP12() {
////        val password = viewBinding.passwordText.text.toString()
//////        val password2 = viewBinding.passwordEncrypt.text.toString()
////        val name = viewBinding.nameCert.text.toString()
////        if (password.isNotEmpty() && name.isNotEmpty()) {
////            alertBackup()
////        }
////    }
//
//    private fun alertBackup() {
//
//        val password = viewBinding.passwordText.text.toString()
////        val password2 = viewBinding.passwordEncrypt.text.toString()
//        val name = viewBinding.nameCert.text.toString()
//        if (password.isNotEmpty() && name.isNotEmpty()) {
//            AlertDialog.Builder(requireContext())
//                .setMessage("Do you want to backup p12?")
//                .setPositiveButton("Backup") { dialog, which ->
//
//                    val action =
//                        ImportKeyPasswordFragmentDirections.nextActionBackupPassword()
//                    findNavController().navigate(action)
//
//                }.setNegativeButton("No") { dialog, which ->
//                    extrack(password, "", name, false)
//                }.show()
//        }
//    }
//
//
//    fun extrack(password: String, passwordBackup: String, name: String, isBackup: Boolean) {
//        var file =  File(filePath)
//        viewModel.caUri.value = Uri.fromFile(file)
////            backupViewModel.encryptP12(requireContext(),password,file)
////            viewModel.importTest(requireContext(),password2)
//        viewModel.extractSuccess.observe(viewLifecycleOwner, Observer {
//            if (it) {
//                if (isBackup) {
//                    authenGoogleDrive()
//                } else {
//                    alertBox("IMPORT SUCCESSFUL")
//                }
//            }
//        })
//        viewModel.extractCA(requireContext(), password, passwordBackup, name, file, isBackup)
//    }
//
//
//    private fun alertBox(message: String) {
//        AlertDialog.Builder(requireContext())
//            .setMessage(message)
//            .setPositiveButton(
//                "Close"
//            ) { dialog, which ->
//                val action =
//                    ImportKeyPasswordFragmentDirections.actionToFirst()
//                findNavController().navigate(action)
//            }.show()
//    }
//
//
//    fun authenGoogleDrive() {
//        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
//
//        if (account == null) {
//            signIn()
//        } else {
////            viewBinding.email.setText(account.email)
//            mDriveServiceHelper =
//                DriveServiceHelper(
//                    DriveServiceHelper.getGoogleDriveService(
//                        requireContext(),
//                        account, DriveServiceHelper.DRIVE_APP_NAME
//                    )
//                )
////            viewBinding.createTextFile.setOnClickListener {
////                mDriveServiceHelper?.createFile("Test TEDA")
////            }
//
////            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
////            val googleSignInClient = GoogleSignIn.getClient(requireCompatActivity(), gso)
////            googleSignInClient.signOut().addOnSuccessListener {
////                signIn()
////            }
//
//            uploadFile()
//
//
//        }
//    }
//
//
//    private fun signIn() {
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestEmail()
//            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
//            .build()
//        mGoogleSignInClient = GoogleSignIn.getClient(requireCompatActivity(), gso)
////        mGoogleSignInClient = buildGoogleSignInClient();
////        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
//
//
////        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        val signInIntent: Intent? = mGoogleSignInClient?.getSignInIntent()
//        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (requestCode) {
//            REQUEST_CODE_SIGN_IN -> if (resultCode == Activity.RESULT_OK && data != null) {
//                handleSignInResult(data)
//            }
//        }
//    }
//
//    private fun handleSignInResult(result: Intent) {
//        GoogleSignIn.getSignedInAccountFromIntent(result)
//            .addOnSuccessListener { googleSignInAccount ->
//                Log.d("MainActivity.TAG", "Signed in as " + googleSignInAccount.email)
////                viewBinding.email.setText(googleSignInAccount.email)
//                mDriveServiceHelper = DriveServiceHelper(
//                    DriveServiceHelper.getGoogleDriveService(
//                        requireContext(),
//                        googleSignInAccount,
//                        DriveServiceHelper.DRIVE_APP_NAME
//                    )
//                )
//                uploadFile()
//                Log.d("MainActivity.TAG", "handleSignInResult: $mDriveServiceHelper")
//            }
//            .addOnFailureListener { e -> Log.e("MainActivity.TAG", "Unable to sign in.", e) }
//    }
//
//    fun uploadFile() {
////       mDriveServiceHelper?.createFolderIfNotExist(getString(R.string.app_name),null)?.addOnCompleteListener {
////           var folderID = it.result.id
////           mDriveServiceHelper?.uploadFile(filePathBackup, "text/plain", folderID)?.addOnCompleteListener {
////               alertBox("IMPORT SUCCESSFUL")
////           }
////       }
////        viewModel.uploadFile(
////            requireContext(), mDriveServiceHelper!!,
////            filePathBackup!!
////        )
//        viewBinding.progressBar2.visibility = View.VISIBLE
//        mDriveServiceHelper?.let {
//            filePathBackup?.let { it1 ->
//                viewModel.uploadFile(
//                    requireContext(), it,
//                    it1
//                )
//            }
//        }
//        viewModel.uploadSuccess.observe(viewLifecycleOwner, Observer {
//            alertBox("IMPORT SUCCESSFUL")
//            viewBinding.progressBar2.visibility = View.GONE
//        })
//
//    }
//
//
//    fun onPasswordResult() {
//        val password = viewBinding.passwordText.text.toString()
////        val password2 = viewBinding.passwordEncrypt.text.toString()
//        val name = viewBinding.nameCert.text.toString()
//        if (password.isNotEmpty() && name.isNotEmpty()) {
//            if (password.isNotEmpty()) {
//                setFragmentResult(
//                    ImportKeyPasswordFragment.REQUEST_KEY,
//                    bundleOf("password" to password, "name" to name)
//                )
//                findNavController().navigateUp()
//            }
//        }
//    }
//
//}