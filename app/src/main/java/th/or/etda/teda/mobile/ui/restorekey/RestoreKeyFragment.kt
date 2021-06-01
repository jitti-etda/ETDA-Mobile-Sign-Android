package th.or.etda.teda.mobile.ui.restorekey

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.RecyclerItemClickListener
import th.or.etda.teda.mobile.databinding.RestoreKeyFragmentBinding
import th.or.etda.teda.mobile.ui.backupkey.googledrive.DriveServiceHelper
import th.or.etda.teda.mobile.ui.backupkey.googledrive.GoogleDriveFileHolder
import th.or.etda.teda.mobile.util.Constants
import th.or.etda.teda.ui.base.BaseFragment
import java.io.File


class RestoreKeyFragment : BaseFragment<RestoreKeyFragmentBinding>(
    layoutId = R.layout.restore_key_fragment
) {

    companion object {
//        var fileDownload: InputStream? = null
        var fileDownload: File? = null
    }

    private lateinit var adapterRestore: RestoreAdapter
    val viewModel: RestoreKeyViewModel by viewModel()

    private val REQUEST_CODE_SIGN_IN = 100
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mDriveServiceHelper: DriveServiceHelper? = null

    override fun onInitDataBinding() {
        adapterRestore = RestoreAdapter()

        authenGoogleDrive()


        viewBinding.apply {

            viewBinding.recyclerView.adapter = adapterRestore


            viewBinding.recyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                    context,
                    viewBinding.recyclerView,
                    object : RecyclerItemClickListener.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {
                            var holder = adapterRestore.currentList[position]
                            alertConfirm("Confirm restore file " + holder.name + "?", holder)

                        }

                        override fun onLongItemClick(view: View, position: Int) {

                        }
                    })
            )

        }

//        setFragmentResultListener(BackupKeyPasswordFragment.REQUEST_KEY) { key, bundle ->
//            // read from the bundle
//            lifecycleScope.launch {
//                var password = bundle.getString("password")
//
//                password?.let {
//                    var data = viewModel.decrypt(fileDownload!!, it)
//                    if (data == null) {
//                        Toast.makeText(requireContext(), "Wrong password", Toast.LENGTH_SHORT)
//                            .show()
//                        return@let
//                    }
//
//                    val action =
//                        RestoreKeyFragmentDirections.nextActionImportPassword("", true)
//                    findNavController().navigate(action)
//
//                    setFragmentResultListener(ImportKeyPasswordFragment.REQUEST_KEY) { key, bundle ->
//                        // read from the bundle
//                        val passwordP12 = bundle.getString("password")
//
//                        val name = bundle.getString("name")
//
//                        if (passwordP12 != null) {
//                            data?.let {
//                                name?.let { it1 ->
//                                    viewModel.restore(
//                                        requireContext(), it, passwordP12,
//                                        it1
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        viewModel.restoreSuccess.observe(viewLifecycleOwner, Observer {
//            if (it) {
//                alertComplete("Restore complete")
//            }
//
//        })


    }

    override fun onInitDependencyInjection() {


    }


    fun authenGoogleDrive() {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())

        if (account == null) {
            signIn()
        } else {
            viewBinding.tvEmail.setText(account.email)
            mDriveServiceHelper =
                DriveServiceHelper(
                    DriveServiceHelper.getGoogleDriveService(
                        requireContext(),
                        account,
                        DriveServiceHelper.DRIVE_APP_NAME
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

//            getFileBackup()


        }
    }


    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestScopes(Scope(DriveScopes.DRIVE))
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .requestScopes(Scope(DriveScopes.DRIVE_METADATA))
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
            REQUEST_CODE_SIGN_IN -> if ( data != null) {
                if(resultCode == Activity.RESULT_OK){
                    handleSignInResult(data)
                }else{
                    findNavController().navigateUp()
                }

            }

        }
    }

    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleSignInAccount ->
                Log.d("MainActivity.TAG", "Signed in as " + googleSignInAccount.email)
                viewBinding.tvEmail.setText(googleSignInAccount.email)
                mDriveServiceHelper = DriveServiceHelper(
                    DriveServiceHelper.getGoogleDriveService(
                        requireContext(),
                        googleSignInAccount,
                        DriveServiceHelper.DRIVE_APP_NAME
                    )
                )
                getFileBackup()
                Log.d("MainActivity.TAG", "handleSignInResult: $mDriveServiceHelper")
            }
            .addOnFailureListener { e -> Log.e("MainActivity.TAG", "Unable to sign in.", e) }
    }


    var listGoogleDrive = ArrayList<GoogleDriveFileHolder>()
    fun getFileBackup() {
        listGoogleDrive.clear()

        viewBinding.progressBar.visibility = View.VISIBLE
        mDriveServiceHelper?.let {
            viewModel.getFileBackup(requireContext(), it)

//            val CrearEventoHilo: Thread = object : Thread() {
//                override fun run() {
//                    val fileMetadata = File()
//                    fileMetadata.setName("photo.jpg")
//                    val filePath = File("files/photo.jpg")
//                    val mediaContent = FileContent("image/jpeg", filePath)
//                    val file: File = driveService.files().create(fileMetadata, mediaContent)
//                        .setFields("id")
//                        .execute()
//                    System.out.println("File ID: " + file.getId())
//                }
//            }
//            CrearEventoHilo.start()

        }
        viewModel.folderGoogleDriveLive.observe(viewLifecycleOwner, Observer {
            viewBinding.progressBar.visibility = View.GONE
        })
        viewModel.fileGoogleDriveLive.observe(viewLifecycleOwner, Observer {
            listGoogleDrive.addAll(it)
            adapterRestore.submitList(listGoogleDrive)
            adapterRestore.notifyDataSetChanged()
            viewBinding.progressBar.visibility = View.GONE
        })


    }


    suspend fun download(file: GoogleDriveFileHolder) {
        viewBinding.progressBar.visibility = View.VISIBLE
        mDriveServiceHelper?.let {
            viewModel.downloadFile(requireContext(),it, file)
        }
        viewModel.downloadSuccess.observe(viewLifecycleOwner, Observer {
            viewBinding.progressBar.visibility = View.GONE


            val folder = requireCompatActivity().getExternalFilesDir(Constants.Folder)
            val fileStoreEncrypt = File(
                folder,
                file.name
            )

            viewModel.copyStreamToFile(it,fileStoreEncrypt)
            fileDownload = fileStoreEncrypt
//            val action =
//                RestoreKeyFragmentDirections.nextActionBackupPassword(true)
            val action =
                RestoreKeyFragmentDirections.nextActionRestorePassword()
            findNavController().navigate(action)

        })


    }


    private fun alertConfirm(message: String, file: GoogleDriveFileHolder) {
        AlertDialog.Builder(requireActivity())
            .setTitle("Restore")
            .setMessage(message)
            .setPositiveButton(
                "Confirm"
            ) { dialog, which ->
                lifecycleScope.launch {
                    download(file)
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun alertComplete(message: String) {
        AlertDialog.Builder(requireActivity())
            .setTitle("Complete")
            .setMessage(message)
            .setPositiveButton(
                "Close"
            ) { dialog, which ->


            }.show()
    }

}