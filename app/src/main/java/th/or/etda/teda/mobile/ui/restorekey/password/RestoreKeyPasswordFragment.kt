package th.or.etda.teda.mobile.ui.restorekey.password

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.MainActivity2
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.databinding.RestoreKeyPasswordFragmentBinding
import th.or.etda.teda.mobile.ui.importkey.ImportHelper
import th.or.etda.teda.mobile.ui.restorekey.RestoreKeyFragment
import th.or.etda.teda.mobile.util.UtilApps
import th.or.etda.teda.ui.base.BaseFragment
import java.io.File
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
        initActionBar()

        var file = RestoreKeyFragment.fileDownload


        viewBinding.apply {


            btnBackup.setOnClickListener {
                val password = viewBinding.edtPassword.text.toString()

                if (password.trim().isNotEmpty()) {

                    if (file != null) {
                        val fileInputStream = FileInputStream(file)
                        decrypt(fileInputStream, password)

                    }

                }else{
//                    viewBinding.edtPassword.setError(getString(R.string.please_input))
                    alertInput()
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

    fun initActionBar() {
        viewBinding.actionBar.tvTitle.setText("Password Backup")
        viewBinding.actionBar.btnBack.setOnClickListener {
            val ac = activity as MainActivity2
            ac.onBackPressed()
        }
    }



    override fun onInitDependencyInjection() {


    }

    private fun alertInput() {

        val dialog = Dialog(requireCompatActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_alert)
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
        }

        dialog.show()
    }
}