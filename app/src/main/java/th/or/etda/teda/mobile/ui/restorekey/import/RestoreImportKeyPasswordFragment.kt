package th.or.etda.teda.mobile.ui.restorekey.import

import android.app.AlertDialog
import android.content.SharedPreferences
import android.util.Base64
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.BiometricEncryptedSharedPreferences
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.databinding.RestoreImportKeyPasswordFragmentBinding
import th.or.etda.teda.mobile.ui.home.HomeViewModel
import th.or.etda.teda.mobile.ui.restorekey.password.RestoreKeyPasswordFragment
import th.or.etda.teda.mobile.util.UtilApps
import th.or.etda.teda.ui.base.BaseFragment
import java.security.PrivateKey


class RestoreImportKeyPasswordFragment : BaseFragment<RestoreImportKeyPasswordFragmentBinding>(
    layoutId = R.layout.restore_import_key_password_fragment
) {


    private val viewModel: RestoreImportKeyPasswordViewModel by viewModel()


    override fun onInitDependencyInjection() {

    }

    override fun onInitDataBinding() {
        var dataDecrypt = RestoreKeyPasswordFragment.dataDecrypt


        viewBinding.apply {

            extractCaBtn.setOnClickListener {

                var password = passwordText.text.toString()
                var name = nameCert.text.toString()
                if (password.isNotEmpty() && name.isNotEmpty()) {
                    if (dataDecrypt != null) {
                        restore(dataDecrypt, password, name)
                    }
                }

            }

        }


    }


    fun restore(data: ByteArray, password: String, name: String) {
//        viewModel.restore(requireContext(), data, password, name)
//        viewModel.restoreSuccess.observe(viewLifecycleOwner, Observer {
//            if (it) {
//                alertComplete("Restore complete")
//            }
//
//        })
        viewModel.extractP12Success.observe(viewLifecycleOwner, Observer {
            saveData(name, it)
        })
        viewModel.restoreP12(requireContext(), data, password, name)

    }

    fun saveData(key: String, privateKey: PrivateKey) {
        var name = key + "_" + UtilApps.timestampName()
        val privKeyBytes: ByteArray? = privateKey?.encoded
        val privKeyStr = String(Base64.encode(privKeyBytes, 2))
//
//        val editor = sharedPreferences.edit()
//        editor.putString(name, privKeyStr)
//        editor.apply()


        BiometricEncryptedSharedPreferences.create(
            this,
            HomeViewModel.FileName,
            1,
            BiometricPrompt.PromptInfo.Builder().setTitle(getString(R.string.app_name))
                .setAllowedAuthenticators(
                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                ).build()
        ).observe(this, Observer { it: SharedPreferences? ->
            if (it != null) {
                it.edit().putString(name, privKeyStr).apply()
                viewModel.addCertificate(Certificate(name, "", ""))
                alertComplete("Restore complete")
            }

        })
//            .setTitle("Please Sign Signature")
//            .setSubtitle("Teda Mobile")
//            .setNegativeButtonText("Cancel")


    }

    private fun alertComplete(message: String) {
        AlertDialog.Builder(requireActivity())
            .setTitle("Complete")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(
                "Close"
            ) { dialog, which ->

                dialog.cancel()

                val action =
                    RestoreImportKeyPasswordFragmentDirections.nextActionToFirst()
                findNavController().navigate(action)

            }.show()
    }

}