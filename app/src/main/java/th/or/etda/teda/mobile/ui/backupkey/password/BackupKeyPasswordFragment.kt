package th.or.etda.teda.mobile.ui.backupkey.password

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import th.or.etda.teda.mobile.MainActivity
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.databinding.BackupKeyPasswordFragmentBinding
import th.or.etda.teda.mobile.util.UtilApps
import th.or.etda.teda.ui.base.BaseFragment


class BackupKeyPasswordFragment : BaseFragment<BackupKeyPasswordFragmentBinding>(
    layoutId = R.layout.backup_key_password_fragment
) {

    companion object {
        const val REQUEST_KEY = "BackupKeyPasswordFragment_Password"
    }



    override fun onInitDataBinding() {

        initActionBar()

        var isRestore = arguments?.let {
            BackupKeyPasswordFragmentArgs.fromBundle(it).isRestore
        }


        viewBinding.apply {

            if (isRestore == true) {
                btnBackup.setText("Confirm")
            }

            btnBackup.setOnClickListener {
                val password = viewBinding.edtPassword.text.toString()
                if (password.trim().length < 8) {
                    alertMinLength(getString(R.string.alert_min_length))
                    return@setOnClickListener
                }
                if (password.trim().isNotEmpty()) {
                    setFragmentResult(
                        REQUEST_KEY,
                        bundleOf("password" to password)
                    )
                    findNavController().navigateUp()
                } else {
                    alertInput()
                }

            }
        }


    }

    override fun onInitDependencyInjection() {


    }

    fun initActionBar() {
        viewBinding.actionBar.tvTitle.setText("Set Password")
        viewBinding.actionBar.btnBack.setOnClickListener {
            val ac = activity as MainActivity
            ac.onBackPressed()
        }
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

    private fun alertMinLength(title: String) {

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
        var tv_title = dialog.findViewById<TextView>(R.id.tv_title)
        tv_title.setText(title)
        val yesBtn = dialog.findViewById(R.id.btn_positive) as MaterialButton
        yesBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}