package th.or.etda.teda.mobile.ui.restorekey.import

import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.util.Base64
import android.view.ViewGroup
import android.view.Window
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.MainActivity
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.BiometricEncryptedSharedPreferences
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.databinding.RestoreImportKeyPasswordFragmentBinding
import th.or.etda.teda.mobile.model.ExtrackP12
import th.or.etda.teda.mobile.ui.restorekey.password.RestoreKeyPasswordFragment
import th.or.etda.teda.mobile.util.Constants
import th.or.etda.teda.mobile.util.UtilApps
import th.or.etda.teda.ui.base.BaseFragment


class RestoreImportKeyPasswordFragment : BaseFragment<RestoreImportKeyPasswordFragmentBinding>(
    layoutId = R.layout.restore_import_key_password_fragment
) {


    private val viewModel: RestoreImportKeyPasswordViewModel by viewModel()


    override fun onInitDependencyInjection() {

    }

    override fun onInitDataBinding() {

        initActionBar()

        var dataDecrypt = RestoreKeyPasswordFragment.dataDecrypt


        viewBinding.apply {

            extractCaBtn.setOnClickListener {

                var password = edtPassword.text.toString()
                var name = edtName.text.toString()

                if (name.trim().isEmpty()) {
                    alertInput()
                    return@setOnClickListener
                }
                if (password.isEmpty()) {
                    alertInput()
                    return@setOnClickListener
                }
                if (password.isNotEmpty() && name.trim().isNotEmpty()) {
                    if (dataDecrypt != null) {
                        restore(dataDecrypt, password, name)
                    }
                }

            }

        }


    }

    fun initActionBar() {
        viewBinding.actionBar.tvTitle.setText("Import P12 password")
        viewBinding.actionBar.btnBack.setOnClickListener {
            val ac = activity as MainActivity
            ac.onBackPressed()
        }
    }

    fun restore(data: ByteArray, password: String, name: String) {
        viewModel.extractP12Success.observe(viewLifecycleOwner, Observer {
            saveData(it.name, it)
        })
        viewModel.restoreP12(requireContext(), data, password, name)

    }

    fun saveData(key: String, extrackP12: ExtrackP12) {
        var name = key + "_" + UtilApps.timestampName()
        val privKeyBytes: ByteArray? = extrackP12.privateKey?.encoded
        val privKeyStr = String(Base64.encode(privKeyBytes, 2))

        BiometricEncryptedSharedPreferences.create(
            this,
            Constants.FileName,
            1,
            BiometricPrompt.PromptInfo.Builder().setTitle(getString(R.string.app_name))
                .setAllowedAuthenticators(
                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                ).build()
        ).observe(this, Observer { it: SharedPreferences? ->
            if (it != null) {
                it.edit().putString(name, privKeyStr).apply()
                viewModel.addCertificate(
                    Certificate(
                        name,
                        extrackP12.cert,
                        extrackP12.chains,
                        UtilApps.currentDate()
                    )
                )
                alertComplete()
            }

        })


    }

    private fun alertComplete() {


        val dialog = Dialog(requireCompatActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_restore_success)
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
            val action =
                RestoreImportKeyPasswordFragmentDirections.nextActionToFirst()
            findNavController().navigate(action)
        }

        dialog.show()
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