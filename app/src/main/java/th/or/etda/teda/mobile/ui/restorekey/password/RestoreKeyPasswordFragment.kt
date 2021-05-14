package th.or.etda.teda.mobile.ui.restorekey.password

import androidx.navigation.fragment.findNavController
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.databinding.RestoreKeyPasswordFragmentBinding
import th.or.etda.teda.mobile.ui.restorekey.RestoreKeyFragment
import th.or.etda.teda.mobile.util.UtilApps
import th.or.etda.teda.ui.base.BaseFragment
import java.io.FileInputStream
import java.io.InputStream


class RestoreKeyPasswordFragment : BaseFragment<RestoreKeyPasswordFragmentBinding>(
    layoutId = R.layout.restore_key_password_fragment
) {

    companion object {

        var dataDecrypt: ByteArray? = null
    }

    val viewModel: RestoreKeyPasswordViewModel by viewModel()

//    var certificate: Certificate? = null

    override fun onInitDataBinding() {

        var file = RestoreKeyFragment.fileDownload


        viewBinding.apply {


            btnBackup.setOnClickListener {
                val password = viewBinding.edtPassword.text.toString()
                if (password.isNotEmpty()) {

                    if (file != null) {
                        val fileInputStream = FileInputStream(file)
                        decrypt(fileInputStream, password)
                    }

                }

            }
        }


    }

    fun decrypt(file: InputStream, password: String) {
        dataDecrypt = viewModel.decrypt(
            file, password
        )
        if (dataDecrypt == null) {
//            Toast.makeText(context, "Wrong password", Toast.LENGTH_SHORT)
//                .show()
            UtilApps.alertDialog(context,"Wrong password")
        } else {
            val action =
                RestoreKeyPasswordFragmentDirections.nextActionRestoreImportPassword()
            findNavController().navigate(action)

        }
    }

    override fun onInitDependencyInjection() {


    }

}