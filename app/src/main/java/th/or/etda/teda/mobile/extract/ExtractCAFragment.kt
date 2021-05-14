//package th.or.etda.teda.mobile.extract
//
//import org.koin.android.viewmodel.ext.android.viewModel
//import th.or.etda.teda.mobile.R
//import th.or.etda.teda.mobile.databinding.ExtractCertificateFragmentBinding
//import th.or.etda.teda.mobile.ui.importkey.password.ImportKeyPasswordViewModel
//import th.or.etda.teda.ui.base.BaseFragment
//import java.io.File
//
//class ExtractCAFragment : BaseFragment<ExtractCertificateFragmentBinding>(
//    layoutId = R.layout.extract_certificate_fragment
//) {
//
//    companion object {
//        fun newInstance() = ExtractCAFragment()
//    }
//
//    private val viewModel: ExtractCAViewModel by viewModel()
//
//    private val importViewModel: ImportKeyPasswordViewModel by viewModel()
////    private val viewModel: ExtractCAViewModel by viewModels {
////        ExtractCaViewModelFactory((requireActivity().application as TEDAMobileApplication).repository)
////    }
//
////    private val viewModel: ExtractCAViewModel by viewModels()
//
//    override fun onInitDependencyInjection() {
//
//    }
//
//    override fun onInitDataBinding() {
//        requireActivity().intent.data?.let {
//            println("ExtractCAFragment -> $it")
//        }
//
//        viewBinding.extractCaBtn.setOnClickListener {
//            val password = viewBinding.passwordText.text.toString()
//            val passwordBackup = viewBinding.passwordBackup.text.toString()
//            val name = viewBinding.nameCert.text.toString()
//            if (password.isNotEmpty() && name.isNotEmpty()) {
//                val intent = requireActivity().intent
//                println("sss => ${intent.data}")
//                intent.data?.path?.let {
//                    println("path is => $it")
//                }
//                intent.data?.let {
//                    var file = File(it.path)
//                    viewModel.caUri.value = it
//                    viewModel.extractCA(requireContext(), password, name)
//
//                }
//            }
//
//        }
//    }
//
//
//
//}