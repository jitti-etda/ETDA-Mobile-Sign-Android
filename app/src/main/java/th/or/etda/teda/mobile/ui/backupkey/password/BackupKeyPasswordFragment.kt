package th.or.etda.teda.mobile.ui.backupkey.password

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.databinding.BackupKeyPasswordFragmentBinding
import th.or.etda.teda.ui.base.BaseFragment


class BackupKeyPasswordFragment : BaseFragment<BackupKeyPasswordFragmentBinding>(
    layoutId = R.layout.backup_key_password_fragment
) {

    companion object {
        const val REQUEST_KEY = "BackupKeyPasswordFragment_Password"
    }

    val viewModel: BackupKeyPasswordViewModel by viewModel()

//    var certificate: Certificate? = null

    override fun onInitDataBinding() {

        var isRestore = arguments?.let {
            BackupKeyPasswordFragmentArgs.fromBundle(it).isRestore
        }


        viewBinding.apply {

            if (isRestore == true) {
                btnBackup.setText("Confirm")
            }

            btnBackup.setOnClickListener {
                val password = viewBinding.edtPassword.text.toString()
                if (password.isNotEmpty()) {
                    setFragmentResult(
                        REQUEST_KEY,
                        bundleOf("password" to password)
                    )
                    findNavController().navigateUp()
//                    viewModel.encryptP12(requireContext(),edtPassword.text.toString().toCharArray()).let {
//                        UtilApps.alertDialog(requireContext(),"Create p12 Success")
//                    }
                }

            }
        }


    }

    override fun onInitDependencyInjection() {


    }

}