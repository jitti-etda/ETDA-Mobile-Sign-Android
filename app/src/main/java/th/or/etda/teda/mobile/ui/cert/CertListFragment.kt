package th.or.etda.teda.mobile.ui.cert

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.RecyclerItemClickListener
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.databinding.CertListFragmentBinding
import th.or.etda.teda.mobile.ui.home.HomeViewModel
import th.or.etda.teda.ui.base.BaseFragment


class CertListFragment : BaseFragment<CertListFragmentBinding>(
    layoutId = R.layout.cert_list_fragment
) {

    companion object {
        const val REQUEST_KEY = "FragmentCert_REQUEST_KEY"
    }

    private lateinit var adapterCert: CertAdapter
    var isResult = false


    private val viewModel: CertListViewModel by viewModel()
    val homeViewModel: HomeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isResult = arguments?.let { CertListFragmentArgs.fromBundle(it).isResult } == true

        Log.i("result", isResult.toString())

    }


    override fun onInitDependencyInjection() {

    }

    override fun onInitDataBinding() {

        adapterCert = CertAdapter()
        viewBinding.recyclerView.adapter = adapterCert
        getCertAll()

        viewBinding.recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(
                context,
                viewBinding.recyclerView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        if (isResult) {
                            setFragmentResult(
                                REQUEST_KEY,
                                bundleOf("cert" to adapterCert.currentList[position])
                            )
                            findNavController().navigateUp()
                        }

                    }

                    override fun onLongItemClick(view: View, position: Int) {
                        alertBoxDelete(adapterCert.currentList[position], position)
                    }
                })
        )

    }




    fun getCertAll() {
//        adapterCert.currentList.clear()
        viewModel.certLiveData.observe(viewLifecycleOwner, Observer {
            adapterCert.submitList(it)
            adapterCert.notifyDataSetChanged()
            if (it.isEmpty()) {
                alertBox("ไม่พบ Cert")
            }
        })
        viewModel.getCertificateAll()
    }

    fun deleteCert(certificate: Certificate,position: Int) {
//        lifecycleScope.launch {
//            viewModel.deleteCertificate(certificate)
//        }

        lifecycleScope.launch {
            viewModel.deleteCertificate(certificate)
//            adapterCert.notifyItemRemoved(position)
//            adapterCert.notifyDataSetChanged()
        }

//        viewModel.isDelete.observe(this, Observer {
////            getCertAll()
//            adapterCert.currentList.remove(certificate)
//            adapterCert.notifyItemRemoved(position)
//        })

    }


    private fun alertBox(message: String) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton(
                "Close"
            ) { dialog, which ->
                // continue with delete
//                onResume()
            }.show()
    }

    private fun alertBoxDelete(certificate: Certificate, position: Int) {
        AlertDialog.Builder(requireContext())
            .setMessage("Delete Certification?")
            .setPositiveButton(
                "Yes"
            ) { dialog, which ->
                deleteCert(certificate,position)

            }.setNegativeButton("Cancel", null).show()
    }

}