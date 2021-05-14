package th.or.etda.teda.mobile.ui.importkey.directory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.BuildConfig.APPLICATION_ID
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.RecyclerItemClickListener
import th.or.etda.teda.mobile.databinding.DirectoryFragmentBinding
import th.or.etda.teda.mobile.ui.backupkey.password.BackupKeyPasswordViewModel
import th.or.etda.teda.ui.base.BaseFragment


class DirectoryFragment : BaseFragment<DirectoryFragmentBinding>(
    layoutId = R.layout.directory_fragment
) {

    companion object {
        const val PERMISSION_EXTRANAL_REQUEST = 1111
    }


    private lateinit var adapterDirectory: DirectoryAdapter
    private val viewModel: DirectoryViewModel by viewModel()
    private val backupPassViewModel: BackupKeyPasswordViewModel by viewModel()

    override fun onInitDependencyInjection() {

    }

    override fun onInitDataBinding() {
        adapterDirectory = DirectoryAdapter()
        viewBinding.recyclerView.adapter = adapterDirectory
        viewBinding.recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(
                context,
                viewBinding.recyclerView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
//                        var intent = Intent(requireContext(), DeepLinkActivity::class.java)
//                        intent.setData()
//                        startActivity(intent)
//                        requireCompatActivity().finish()

//                        val myMime = MimeTypeMap.getSingleton()
//                        val newIntent = Intent(Intent.ACTION_VIEW)
//                        newIntent.setDataAndType(Uri.fromFile(File(adapterDirectory.currentList[position].path)), "application/x-pkcs12")
//                        newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                        try {
//                            startActivity(newIntent)
//                            requireCompatActivity().finish()
//                        } catch (e: ActivityNotFoundException) {
//                            Toast.makeText(
//                                requireContext(),
//                                "No handler for this type of file.",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }

//                        driveViewModel.encrypt(requireContext(),adapterDirectory.currentList[position].path)
//                        backupPassViewModel.encryptP12(requireContext(),"123456".toCharArray(),"",adapterDirectory.currentList[position].path)

                        val action =
                            DirectoryFragmentDirections.nextActionImportPassword(
                                adapterDirectory.currentList[position].path,
                                false
                            )
                        findNavController().navigate(action)
                    }

                    override fun onLongItemClick(view: View, position: Int) {
                    }
                })
        )


//        if (isExtranal()) {
//            getAllFile()
//        } else {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                PERMISSION_EXTRANAL_REQUEST
//            ).let {
//                getAllFile()
//            }
//        }


        Dexter.withContext(requireContext())
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) { /* ... */
                    if (report.areAllPermissionsGranted()) {
                        if (SDK_INT >= Build.VERSION_CODES.R) {
                            if (!Environment.isExternalStorageManager()) {
                                managePermission()
                            }else{
                                getAllFile()
                            }
                        } else {
                            getAllFile()
                        }

                    }else{
                        Toast.makeText(requireContext(),"Please allow permission",Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }


                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) { /* ... */
                    token.continuePermissionRequest()
                }
            }).check()


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1234){
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    getAllFile()
                }
            }
        }
    }

    fun managePermission() {
        val uri = Uri.parse("package:$APPLICATION_ID")
        startActivityForResult(
            Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                uri
            ),1234
        )
    }


    private fun getAllFile() {

        viewBinding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {

//            requireContext().getExternalFilesDir()

            viewModel.getExternalStorageDirectory().let {
                it?.let { it1 ->
                    viewModel.getFile(it1)

                }
            }
        }
        lifecycleScope.launch {
            viewModel.fileListLive.observe(viewLifecycleOwner, Observer {
                adapterDirectory.submitList(it)
                viewBinding.progressBar.visibility = View.GONE
            })
        }


    }


    private fun isExtranal(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }


}