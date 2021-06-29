package th.or.etda.teda.mobile.ui.importkey

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.BiometricEncryptedSharedPreferences
import th.or.etda.teda.mobile.common.RecyclerItemClickListener
import th.or.etda.teda.mobile.databinding.ImportKeyFragmentBinding
import th.or.etda.teda.mobile.ui.cert.CertAdapter
import th.or.etda.teda.mobile.ui.cert.CertListViewModel
import th.or.etda.teda.ui.base.BaseFragment


class ImportKeyFragment : BaseFragment<ImportKeyFragmentBinding>(
    layoutId = R.layout.import_key_fragment
) {


    val viewModel: CertListViewModel by viewModel()

    private lateinit var adapterCert: CertAdapter

    override fun onInitDataBinding() {

        val data = requireActivity().intent.data
        if (data != null) {
            requireActivity().contentResolver.openInputStream(data)?.let {
                var fileDownload = ImportHelper.writeTempFile(
                    requireContext(),
                    it
                )

                val action =
                    ImportKeyFragmentDirections.nextActionImportPassword(fileDownload.path, false)
                findNavController().navigate(action)
                requireActivity().intent.replaceExtras(Bundle())
                requireActivity().intent.action = ""
                requireActivity().intent.data = null
                requireActivity().intent.flags = 0
            }

        }

        adapterCert = CertAdapter()



        viewBinding.apply {

            if (!BiometricEncryptedSharedPreferences.checkBio(requireCompatActivity())) {
                btnImportKey.isEnabled = false
                btnImportMenu.isEnabled = false
                btnRestoreKey.isEnabled = false
                btnRestoreMenu.isEnabled = false
                btnSign.isEnabled = false
            }

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

    override fun onResume() {
        super.onResume()
        getCertAll()


    }

    override fun onInitDependencyInjection() {


    }


    fun getCertAll() {
        try {
            adapterCert.currentList.clear()
        } catch (e: Exception) {

        }

        viewModel.getCertificateAll().observe(viewLifecycleOwner, Observer {

            adapterCert.submitList(it)
            if (adapterCert.currentList.isEmpty()) {
                viewBinding.layoutFirst.visibility = View.VISIBLE
                viewBinding.layoutMenu.visibility = View.GONE
            } else {
                viewBinding.layoutFirst.visibility = View.GONE
                viewBinding.layoutMenu.visibility = View.VISIBLE
            }


        })
    }

}