package th.or.etda.teda.mobile.ui.backupkey

import android.Manifest
import android.view.View
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.RecyclerItemClickListener
import th.or.etda.teda.mobile.databinding.BackupKeyFragmentBinding
import th.or.etda.teda.mobile.ui.importkey.directory.DirectoryAdapter
import th.or.etda.teda.mobile.ui.importkey.directory.DirectoryViewModel
import th.or.etda.teda.mobile.util.Constants
import th.or.etda.teda.ui.base.BaseFragment


class BackupKeyFragment : BaseFragment<BackupKeyFragmentBinding>(
    layoutId = R.layout.backup_key_fragment
) {

    private lateinit var adapterDirectory: DirectoryAdapter
    val viewModel: BackupKeyViewModel by viewModel()
    private val directoryViewModel : DirectoryViewModel by viewModel()

    override fun onInitDataBinding() {

        adapterDirectory = DirectoryAdapter()


        viewBinding.apply {

            viewBinding.recyclerView.adapter = adapterDirectory


            viewBinding.recyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                    context,
                    viewBinding.recyclerView,
                    object : RecyclerItemClickListener.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {
//                            val action =
//                                BackupKeyFragmentDirections.nextActionBackupPassword(adapterCert.currentList[position])
//                            findNavController().navigate(action)

                        }

                        override fun onLongItemClick(view: View, position: Int) {

                        }
                    })
            )

        }

        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) { /* ... */

                    getAllFile()

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) { /* ... */
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) { /* ... */

                }
            }).check()


    }

    override fun onInitDependencyInjection() {


    }

    private fun getAllFile() {
//        val folder = File(
//            Environment.getExternalStorageDirectory().toString() +
//                    File.separator + Constants.Folder
//        )
        val folder = requireContext().getExternalFilesDir(Constants.FolderBackup)
        adapterDirectory.submitList(folder?.let { directoryViewModel.getFileBackup(it) })

    }


}