package th.or.etda.teda.mobile.ui.importkey

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.MainActivity
import th.or.etda.teda.mobile.MainActivity2
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.RecyclerItemClickListener
import th.or.etda.teda.mobile.databinding.ImportKeyFragmentBinding
import th.or.etda.teda.mobile.ui.cert.CertAdapter
import th.or.etda.teda.ui.base.BaseFragment


class ImportKeyFragment : BaseFragment<ImportKeyFragmentBinding>(
    layoutId = R.layout.import_key_fragment
) {


    val viewModel: ImportKeyViewModel by viewModel()

    //    var isChangePage = false
    private lateinit var adapterCert: CertAdapter

    override fun onInitDataBinding() {

//        if(!isChangePage){
        val data = requireActivity().intent.data
        if (data != null) {
            requireActivity().contentResolver.openInputStream(data)?.let {
                var fileDownload = ImportHelper.writeTempFile(
                    requireContext(),
                    it
                )

//                   var file = data.toFile()
                val action =
                    ImportKeyFragmentDirections.nextActionImportPassword(fileDownload.path, false)
                findNavController().navigate(action)
//                isChangePage = true
//            }
                requireActivity().intent.replaceExtras(Bundle())
                requireActivity().intent.action = ""
                requireActivity().intent.data = null
                requireActivity().intent.flags = 0
            }

        }

        adapterCert = CertAdapter()

        getCertAll()

        viewBinding.apply {

            btnImportKey.setOnClickListener {
                val action =
                    ImportKeyFragmentDirections.nextActionImport()
                findNavController().navigate(action)
            }
            btnImportMenu.setOnClickListener {
                val action =
                    ImportKeyFragmentDirections.nextActionImport()
                findNavController().navigate(action)
            }

            btnRestoreKey.setOnClickListener {

                val action =
                    ImportKeyFragmentDirections.nextActionRestore()
                findNavController().navigate(action)
            }
            btnRestoreMenu.setOnClickListener {

                val action =
                    ImportKeyFragmentDirections.nextActionRestore()
                findNavController().navigate(action)
            }
            btnSign.setOnClickListener {
                val action =
                    ImportKeyFragmentDirections.nextActionSign()
                findNavController().navigate(action)
            }


            recyclerView.adapter = adapterCert
            recyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                    context,
                    viewBinding.recyclerView,
                    object : RecyclerItemClickListener.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {
//                            readDate(adapterCert.currentList[position].certName)
                        }

                        override fun onLongItemClick(view: View, position: Int) {
                        }
                    })
            )
        }

    }

//    fun readDate(key: String) {
////        val masterKey = MasterKey.Builder(requireContext())
////            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
////            .build()
////
////        val sharedPreferences = EncryptedSharedPreferences.create(
////            requireContext(),
////            HomeViewModel.ALIAS,
////            masterKey,
////            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
////            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
////        )
////        sharedPreferences.getString(key, "")?.let { Log.i("read", it) }
//        BiometricEncryptedSharedPreferences.create(
//            this,
//            HomeViewModel.FileName,
//            1,
//            BiometricPrompt.PromptInfo.Builder().setTitle(getString(R.string.app_name)).setNegativeButtonText("Cancel").setAllowedAuthenticators(
//                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
//            ).build()
//        ).observe(this, Observer { it: SharedPreferences ->
//           var data= it.getString(key, "")
//            println(data)
//        })
//    }

    override fun onInitDependencyInjection() {


    }

    fun getPath(uri: Uri?): String? {
        var result: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? =
            uri?.let { requireContext().contentResolver.query(it, proj, null, null, null) }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val column_index: Int = cursor.getColumnIndexOrThrow(proj[0])
                result = cursor.getString(column_index)
            }
            cursor.close()
        }
        if (result == null) {
            result = "Not found"
        }
        return result
    }

    fun getCertAll() {
        adapterCert.currentList.clear()
        viewModel.getCertificateAll().observe(viewLifecycleOwner, Observer {

            adapterCert.submitList(it)
            if(adapterCert.currentList.isEmpty()){
                viewBinding.layoutFirst.visibility =  View.VISIBLE
                viewBinding.layoutMenu.visibility =  View.GONE
            }else{
                viewBinding.layoutFirst.visibility =  View.GONE
                viewBinding.layoutMenu.visibility =  View.VISIBLE
            }
//            var ac = activity as MainActivity
//            ac.checkMenu()
//            if(it.isEmpty()){
//                viewBinding.recyclerView.visibility = View.GONE
//                viewBinding.layoutImport.visibility = View.VISIBLE
//            }else{
//                viewBinding.recyclerView.visibility = View.VISIBLE
//                viewBinding.layoutImport.visibility = View.GONE
//            }


        })
    }

}