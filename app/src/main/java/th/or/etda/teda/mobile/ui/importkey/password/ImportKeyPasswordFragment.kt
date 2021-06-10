package th.or.etda.teda.mobile.ui.importkey.password

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
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
import th.or.etda.teda.mobile.MainActivity2
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.BiometricEncryptedSharedPreferences
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.databinding.ImportKeyPasswordFragmentBinding
import th.or.etda.teda.mobile.model.ExtrackP12
import th.or.etda.teda.mobile.ui.backupkey.googledrive.DriveServiceHelper
import th.or.etda.teda.mobile.ui.backupkey.password.BackupKeyPasswordFragment
import th.or.etda.teda.mobile.ui.home.HomeViewModel
import th.or.etda.teda.mobile.ui.importkey.ImportHelper
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
//    val backupViewModel: BackupKeyPasswordViewModel by viewModel()
//    private val viewModel: ExtractCAViewModel by viewModels {
//        ExtractCaViewModelFactory((requireActivity().application as TEDAMobileApplication).repository)
//    }

//    private val viewModel: ExtractCAViewModel by viewModels()

    var filePath = ""
    var filePathBackup: File? = null
    var nameTimestamp = ""

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
//            val password = viewBinding.passwordText.text.toString()
//        val password2 = viewBinding.passwordEncrypt.text.toString()
//            val name = viewBinding.nameCert.text.toString()
            var file = File(filePath)
            passwordBackup?.let { backup(requireContext(), it, file) }
        }


        ImportHelper.fileLiveData.observe(viewLifecycleOwner, Observer {
            filePathBackup = it
            Log.i("filePath", filePathBackup.toString())

            uploadFile()
        })


//        cryptographyManager = CryptographyManager()
//        // e.g. secretKeyName = "biometric_sample_encryption_key"
//        biometricPrompt = createBiometricPrompt()
//        promptInfo = createPromptInfo()
//        authenticateToEncrypt()


    }


//    fun importP12() {
//        val password = viewBinding.passwordText.text.toString()
////        val password2 = viewBinding.passwordEncrypt.text.toString()
//        val name = viewBinding.nameCert.text.toString()
//        if (password.isNotEmpty() && name.isNotEmpty()) {
//            alertBackup()
//        }
//    }

//    private lateinit var biometricPrompt: BiometricPrompt
//    private lateinit var promptInfo: BiometricPrompt.PromptInfo
//    private var readyToEncrypt: Boolean = false
//    private lateinit var cryptographyManager: CryptographyManager
//    private lateinit var ciphertext: ByteArray
//    private lateinit var initializationVector: ByteArray
//    var privateEncrypt = ""
//
//    private fun createBiometricPrompt(): BiometricPrompt {
//        val executor = ContextCompat.getMainExecutor(requireContext())
//
//        val callback = object : BiometricPrompt.AuthenticationCallback() {
//            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                super.onAuthenticationError(errorCode, errString)
//                Log.d("TAG", "$errorCode :: $errString")
//            }
//
//            override fun onAuthenticationFailed() {
//                super.onAuthenticationFailed()
//                Log.d("TAG", "Authentication failed for an unknown reason")
//            }
//
//            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                super.onAuthenticationSucceeded(result)
//                Log.d("TAG", "Authentication was successful")
//                processData(result.cryptoObject)
//            }
//        }
//
//        //The API requires the client/Activity context for displaying the prompt view
//        val biometricPrompt = BiometricPrompt(this, executor, callback)
//        return biometricPrompt
//    }
//
//    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
//        val promptInfo = BiometricPrompt.PromptInfo.Builder()
//            .setTitle("Please Sign Signature")
//            .setSubtitle("Teda Mobile")
//            .setNegativeButtonText("Cancel")
//            // .setDeviceCredentialAllowed(true) // Allow PIN/pattern/password authentication.
//            // Also note that setDeviceCredentialAllowed and setNegativeButtonText are
//            // incompatible so that if you uncomment one you must comment out the other
//            .build()
//        return promptInfo
//    }


//    private fun authenticateToEncrypt() {
//        readyToEncrypt = true
//        if (BiometricManager.from(requireContext())
//                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager
//                .BIOMETRIC_SUCCESS
//        ) {
//            val cipher = cryptographyManager.getInitializedCipherForEncryption(HomeViewModel.ALIAS)
//            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
//        }
//    }

//    private fun authenticateToDecrypt() {
//        readyToEncrypt = false
//        if (BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager
//                .BIOMETRIC_SUCCESS) {
//            val cipher = cryptographyManager.getInitializedCipherForDecryption(HomeViewModel.ALIAS,initializationVector)
//            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
//        }
//
//    }

//    private fun processData(cryptoObject: BiometricPrompt.CryptoObject?) {
//        val data = if (readyToEncrypt) {
//            val encryptedData = privateKey?.encoded?.let {
//                cryptographyManager.encryptData(
//                    it,
//                    cryptoObject?.cipher!!
//                )
//            }
//            ciphertext = encryptedData?.ciphertext!!
//            initializationVector = encryptedData.initializationVector
//
//            String(ciphertext, Charset.forName("UTF-8"))
//        } else {
//            cryptographyManager.decryptData(ciphertext, cryptoObject?.cipher!!)
//        }
//        privateEncrypt = data
////        Log.i("asd", privateEncrypt)
//        val name = viewBinding.nameCert.text.toString()
////        saveData(name, privateEncrypt)
//
//    }

    fun initActionBar() {
        viewBinding.actionBar.tvTitle.setText("Import P12 password")
        viewBinding.actionBar.btnBack.setOnClickListener {
            val ac = activity as MainActivity2
            ac.onBackPressed()
        }
    }

    fun saveData(name: String, extrackP12: ExtrackP12) {

        nameTimestamp = name
//        val masterKey = MasterKey.Builder(requireContext())
//            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
//            .build()
//
//        val sharedPreferences = EncryptedSharedPreferences.create(
//            requireContext(),
//            HomeViewModel.ALIAS,
//            masterKey,
//            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//        )
        val privKeyBytes: ByteArray? = extrackP12.privateKey?.encoded
        val privKeyStr = String(Base64.encode(privKeyBytes, Base64.NO_WRAP))
//
//        val editor = sharedPreferences.edit()
//        editor.putString(name, privKeyStr)
//        editor.apply()


//        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
//
//        val sharedPreferences = EncryptedSharedPreferences.create(
//            "secret_shared_prefs",
//            masterKeyAlias,
//            requireContext(),
//            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//        )
//
//        // use the shared preferences and editor as you normally would
//
//        // use the shared preferences and editor as you normally would
//        val editor = sharedPreferences.edit()
//        editor.putString(name, value)
//        editor.apply()
//        viewModel.addCertificate(Certificate(name, "", ""))


        BiometricEncryptedSharedPreferences.create(
            this,
            HomeViewModel.FileName,
            1,
            BiometricPrompt.PromptInfo.Builder().setTitle(getString(R.string.app_name))
                .setAllowedAuthenticators(
                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                ).build()
        ).observe(this, Observer { it: SharedPreferences? ->
            if (it != null) {
                it.edit().putString(name, privKeyStr).apply()

                viewModel.addCertificate(Certificate(name, extrackP12.cert, extrackP12.chains))


                alertBackupDialog()
            }

        })


//            .setTitle("Please Sign Signature")
//            .setSubtitle("Teda Mobile")
//            .setNegativeButtonText("Cancel")


    }

    //    var privateKey: PrivateKey? = null
    private fun submit() {

        val password = viewBinding.edtPassword.text.toString()
//        val password2 = viewBinding.passwordEncrypt.text.toString()
        val name = viewBinding.edtName.text.toString()

        if (password.isNotEmpty() && name.isNotEmpty()) {
            var nameTime = name + "_" + UtilApps.timestampName()
//            extrack(password, name)
            viewModel.extractP12Success.observe(viewLifecycleOwner, Observer {

//                privateKey = it
//                authenticateToEncrypt()
                saveData(nameTime, it)
            })
            var file = File(filePath)
            viewModel.caUri.value = Uri.fromFile(file)
            viewModel.extractP12(requireContext(), password,nameTime)

        }
    }


    fun extrack(password: String, name: String) {
        var file = File(filePath)
        viewModel.caUri.value = Uri.fromFile(file)
//            backupViewModel.encryptP12(requireContext(),password,file)
//            viewModel.importTest(requireContext(),password2)
        viewModel.extractSuccess.observe(viewLifecycleOwner, Observer {
            if (it != null && it.isNotEmpty()) {
                nameTimestamp = it


//                var dia = Dialog(requireContext())
//                dia.setContentView(R.layout.dialog_alert)
//                var title = dia.findViewById<TextView>(R.id.tv_title)
//                title.setText("Do you want to backup p12?")
//                var btnPositive = dia.findViewById<Button>(R.id.btn_positive)
//                var btnNegative = dia.findViewById<Button>(R.id.btn_negative)
//
//                btnPositive.setText("Backup")
//                btnPositive.setOnClickListener {
//                    dia.dismiss()
//                    authenGoogleDrive()
//                }
//                btnNegative.setText("No")
//                btnNegative.setOnClickListener {
//                    dia.dismiss()
//                    alertBox("IMPORT SUCCESSFUL")
//                }
//                dia.show()

                alertBackupDialog()


            } else {
                UtilApps.alertDialog(requireCompatActivity(), "Wrong password or corrupted file")
            }
        })
        viewModel.extractCA(requireActivity(), password, name)
    }


    fun backup(context: Context, passwordBackup: String, file: File) {
        ImportHelper.encryptP12forBackup(context, nameTimestamp, passwordBackup, file)

    }


    private fun alertBox(message: String) {
        UtilApps.hideSoftKeyboard(requireCompatActivity())
//        AlertDialog.Builder(requireCompatActivity())
//            .setMessage(message)
//            .setCancelable(false)
//            .setPositiveButton(
//                "Close"
//            ) { dialog, which ->
//                dialog.cancel()
//                val action =
//                    ImportKeyPasswordFragmentDirections.actionToFirst()
//                findNavController().navigate(action)
//            }.show()

        val dialog = Dialog(requireCompatActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_import_success)
        dialog.getWindow()?.setBackgroundDrawable( ColorDrawable(getResources().getColor(R.color.transparent)));
        dialog.getWindow()?.setLayout(((UtilApps.getScreenWidth(getActivity()) * .9).toInt()), ViewGroup.LayoutParams.WRAP_CONTENT );

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
//        AlertDialog.Builder(requireCompatActivity())
//            .setMessage("Do you want to backup p12?")
//            .setPositiveButton("Backup") { dialog, which ->
//                dialog.dismiss()
//                dialog.cancel()
//
//                authenGoogleDrive()
//
//            }.setNegativeButton("No") { dialog, which ->
//                dialog.cancel()
//                alertBox("IMPORT SUCCESSFUL")
//            }.show()


        val dialog = Dialog(requireCompatActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.getWindow()?.setBackgroundDrawable( ColorDrawable(getResources().getColor(R.color.transparent)));
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
            alertBox("IMPORT SUCCESSFUL")
        }
        dialog.show()
    }

    fun authenGoogleDrive() {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())

        if (account == null) {
            signIn()
        } else {
//            viewBinding.email.setText(account.email)
            mDriveServiceHelper =
                DriveServiceHelper(
                    DriveServiceHelper.getGoogleDriveService(
                        requireContext(),
                        account, DriveServiceHelper.DRIVE_APP_NAME
                    )
                )
//            viewBinding.createTextFile.setOnClickListener {
//                mDriveServiceHelper?.createFile("Test TEDA")
//            }

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = GoogleSignIn.getClient(requireCompatActivity(), gso)
            googleSignInClient.signOut().addOnSuccessListener {
                signIn()
            }


//            val action =
//                ImportKeyPasswordFragmentDirections.nextActionBackupPassword()
//            findNavController().navigate(action)

        }
    }


    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope(DriveScopes.DRIVE_FILE),
                Scope(DriveScopes.DRIVE),
                Scope(DriveScopes.DRIVE_APPDATA),
                Scope(DriveScopes.DRIVE_METADATA)
            )
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireCompatActivity(), gso)
//        mGoogleSignInClient = buildGoogleSignInClient();
//        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);


//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        val signInIntent: Intent? = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> if (resultCode == Activity.RESULT_OK && data != null) {
                handleSignInResult(data)
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
//       mDriveServiceHelper?.createFolderIfNotExist(getString(R.string.app_name),null)?.addOnCompleteListener {
//           var folderID = it.result.id
//           mDriveServiceHelper?.uploadFile(filePathBackup, "text/plain", folderID)?.addOnCompleteListener {
//               alertBox("IMPORT SUCCESSFUL")
//           }
//       }
//        viewModel.uploadFile(
//            requireContext(), mDriveServiceHelper!!,
//            filePathBackup!!
//        )
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
            alertBox("IMPORT SUCCESSFUL")

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


}