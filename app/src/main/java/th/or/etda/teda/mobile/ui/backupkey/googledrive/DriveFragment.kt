package th.or.etda.teda.mobile.ui.backupkey.googledrive

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.databinding.DriveFragmentBinding
import th.or.etda.teda.mobile.ui.backupkey.googledrive.DriveServiceHelper.getGoogleDriveService
import th.or.etda.teda.mobile.ui.importkey.directory.DirectoryFragment
import th.or.etda.teda.ui.base.BaseFragment


class DriveFragment : BaseFragment<DriveFragmentBinding>(
    layoutId = R.layout.drive_fragment
) {

    private val REQUEST_CODE_SIGN_IN = 100
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mDriveServiceHelper: DriveServiceHelper? = null


    val viewModel: DriveViewModel by viewModel()

    override fun onInitDataBinding() {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())

        if (account == null) {
            signIn()
        } else {
            viewBinding.email.setText(account.email)
            mDriveServiceHelper =
                DriveServiceHelper(getGoogleDriveService(requireContext(), account, DriveServiceHelper.DRIVE_APP_NAME))
            viewBinding.createTextFile.setOnClickListener {
                mDriveServiceHelper?.createFile("Test TEDA")
            }
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = GoogleSignIn.getClient(requireCompatActivity(), gso)
            googleSignInClient.signOut().addOnSuccessListener {
                signIn()
            }

            viewBinding.encrypt.setOnClickListener {

                if (isExtranal()) {
                    encrypt()
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        DirectoryFragment.PERMISSION_EXTRANAL_REQUEST
                    ).let {
                        encrypt()
                    }
                }

            }

        }
    }

    override fun onInitDependencyInjection() {


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

        //
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        val signInIntent: Intent? = mGoogleSignInClient?.getSignInIntent()
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
                viewBinding.email.setText(googleSignInAccount.email)
                mDriveServiceHelper = DriveServiceHelper(
                    getGoogleDriveService(
                        requireContext(),
                        googleSignInAccount,
                        DriveServiceHelper.DRIVE_APP_NAME
                    )
                )
                Log.d("MainActivity.TAG", "handleSignInResult: $mDriveServiceHelper")
            }
            .addOnFailureListener { e -> Log.e("MainActivity.TAG", "Unable to sign in.", e) }
    }

    private fun isExtranal(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun encrypt() {
//        var res = viewModel.encryptAES()
//        viewModel.decryptAES(res)


//        var file = viewModel.encryptFile(requireContext(), "", "")
//        mDriveServiceHelper?.uploadFile(file, "text/plain", null)
//        viewModel.decrypt(requireContext())
    }
}