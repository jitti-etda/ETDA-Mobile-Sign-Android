package th.or.etda.teda.mobile.ui.importkey

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.BiometricEncryptedSharedPreferences
import th.or.etda.teda.mobile.common.RecyclerItemClickListener
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.databinding.ImportKeyFragmentBinding
import th.or.etda.teda.mobile.ui.cert.CertListViewModel
import th.or.etda.teda.mobile.ui.cert.KeyCertAdapter
import th.or.etda.teda.mobile.util.UtilApps
import th.or.etda.teda.ui.base.BaseFragment


class ImportKeyFragment : BaseFragment<ImportKeyFragmentBinding>(
    layoutId = R.layout.import_key_fragment
) {


    val viewModel: CertListViewModel by viewModel()

    //    private lateinit var adapterCert: CertAdapter
    private lateinit var adapterCert: KeyCertAdapter

    override fun onInitDataBinding() {

        val data = requireActivity().intent.data
        if (data != null) {
            if (data.scheme.equals("mobilesign")) {
                checkP12List(data)

            } else {
                requireActivity().contentResolver.openInputStream(data)?.let {
                    var fileDownload = ImportHelper.writeTempFile(
                        requireContext(),
                        it
                    )

                    val action =
                        ImportKeyFragmentDirections.nextActionImportPassword(
                            fileDownload.path,
                            false
                        )
                    findNavController().navigate(action)
                }
            }

            requireActivity().intent.replaceExtras(Bundle())
            requireActivity().intent.action = ""
            requireActivity().intent.data = null
            requireActivity().intent.flags = 0
        }

        adapterCert = KeyCertAdapter()



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
                    ImportKeyFragmentDirections.nextActionSign("")
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
                            alertDelete(adapterCert.getItem(position), position)


                        }
                    })
            )
        }

    }

    fun alertDelete(certificate: Certificate, position: Int) {
        val dialog = Dialog(requireCompatActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete)
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
            deleteCert(certificate, position)


        }
        val noBtn = dialog.findViewById(R.id.btn_negative) as MaterialButton
        noBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun deleteCert(certificate: Certificate, position: Int) {
//        lifecycleScope.launch {
//            viewModel.deleteCertificate(certificate)
//        }

        lifecycleScope.launch {
            viewModel.deleteCertificate(certificate)
            adapterCert.removeItem(certificate)
            if (adapterCert.itemCount > 0) {
                viewBinding.layoutFirst.visibility = View.GONE
                viewBinding.layoutMenu.visibility = View.VISIBLE
            } else {
                viewBinding.layoutFirst.visibility = View.VISIBLE
                viewBinding.layoutMenu.visibility = View.GONE
            }
        }

//        viewModel.isDelete.observe(this, Observer {
////            getCertAll()
//            adapterCert.currentList.remove(certificate)
//            adapterCert.notifyItemRemoved(position)
//        })

    }

    override fun onResume() {
        super.onResume()
        getCertAll()


    }

    override fun onInitDependencyInjection() {


    }


    fun getCertAll() {
        try {
            adapterCert.clear()
        } catch (e: Exception) {

        }

        viewModel.getCertificateAll().observe(viewLifecycleOwner, Observer {

            adapterCert.addAll(it as ArrayList<Certificate>)
            if (adapterCert.itemCount > 0) {
                viewBinding.layoutFirst.visibility = View.GONE
                viewBinding.layoutMenu.visibility = View.VISIBLE
            } else {
                viewBinding.layoutFirst.visibility = View.VISIBLE
                viewBinding.layoutMenu.visibility = View.GONE
            }


        })
    }

    var isLoad = false
    fun checkP12List(data: Uri) {
        viewModel.getCertificateAll().observe(viewLifecycleOwner, Observer {

            if (it.isNotEmpty()) {
                if (!isLoad) {
                    isLoad = true
                    var encode = data.toString().split("qrcode/")[1]
                    val action =
                        ImportKeyFragmentDirections.nextActionSign(encode)
                    findNavController().navigate(action)
                }

            } else {
                isLoad = false
                viewBinding.layoutFirst.visibility = View.VISIBLE
                viewBinding.layoutMenu.visibility = View.GONE
            }
        })
    }

}