package th.or.etda.teda.mobile.ui.csr

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.InputFilter
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.button.MaterialButton
import com.google.api.services.drive.DriveScopes
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.scope.scope
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.BuildConfig
import th.or.etda.teda.mobile.MainActivity
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.BiometricEncryptedSharedPreferences
import th.or.etda.teda.mobile.common.GenCsr
import th.or.etda.teda.mobile.common.RSA
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.data.csr.Csr
import th.or.etda.teda.mobile.databinding.CsrFragmentBinding
import th.or.etda.teda.mobile.databinding.ImportKeyPasswordFragmentBinding
import th.or.etda.teda.mobile.model.ExtrackP12
import th.or.etda.teda.mobile.ui.backupkey.googledrive.DriveServiceHelper
import th.or.etda.teda.mobile.ui.backupkey.password.BackupKeyPasswordFragment
import th.or.etda.teda.mobile.ui.importkey.ImportHelper
import th.or.etda.teda.mobile.ui.importkey.ImportKeyFragmentDirections
import th.or.etda.teda.mobile.ui.importkey.password.ImportKeyPasswordFragment
import th.or.etda.teda.mobile.ui.importkey.password.ImportKeyPasswordViewModel
import th.or.etda.teda.mobile.util.Constants
import th.or.etda.teda.mobile.util.UtilApps
import th.or.etda.teda.ui.base.BaseFragment
import java.io.File
import java.security.PrivateKey


class CsrFragment : BaseFragment<CsrFragmentBinding>(
    layoutId = R.layout.csr_fragment
) {

    private val viewModel: CsrViewModel by viewModel()


    override fun onInitDependencyInjection() {

    }


    override fun onInitDataBinding() {

        initActionBar()

        viewBinding.apply {
            btnGenCsr.setOnClickListener {
                UtilApps.hideSoftKeyboard(requireCompatActivity())
                val name = viewBinding.edtName.text.toString()
                val org = viewBinding.edtOr.text.toString()
                val unit = viewBinding.edtUnit.text.toString()
                val state = viewBinding.edtState.text.toString()
                val city = viewBinding.edtCity.text.toString()
                val country = viewBinding.edtCountry.text.toString()
                val email = viewBinding.edtEmail.text.toString()
                if (name.trim().isEmpty()) {
                    alertInput()
                    return@setOnClickListener
                }
                if (org.trim().isEmpty()) {
                    alertInput()
                    return@setOnClickListener
                }
                if (unit.trim().isEmpty()) {
                    alertInput()
                    return@setOnClickListener
                }

                genCsr(requireContext(), name, org, unit,state,city,country,email)


            }
        }
//        var fileCrt = File("/storage/emulated/0/Download/inetCert.cer")
//        var fileKey = File("/storage/emulated/0/Download/private-key.pkcs8")
//        var fileOutput = File("/storage/emulated/0/Download/test.p12")
//        GenCsr.createIdentityStore(fileCrt.toPath(),fileKey,fileOutput.toPath(),"123456789".toCharArray())
//        GenCsr.importCrt();
//        RSA.getPrivateKey("/storage/emulated/0/Download/private-key.pkcs8")
//        PrivateKeyReader("/storage/emulated/0/Download/private-key.pkcs8").privateKey
//        GenCsr.getPrivatekey(File("/storage/emulated/0/Download/private-key.pkcs8"))

        Dexter.withContext(requireContext())
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) { /* ... */
                    if (report.areAllPermissionsGranted()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (!Environment.isExternalStorageManager()) {
                                managePermission()
                            }else{

                            }
                        } else {

                        }

                    }else{
                        Toast.makeText(requireContext(),"Please allow permission",Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }


                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) { /* ... */
                    token.continuePermissionRequest()
                }
            }).check()

    }


    fun initActionBar() {
        viewBinding.actionBar.tvTitle.setText("สร้าง CSR")
        viewBinding.actionBar.btnBack.setOnClickListener {
            val ac = activity as MainActivity
            ac.onBackPressed()
        }
    }



    fun genCsr(context: Context, name: String, organize: String, unit: String,state:String,city:String,country:String,email:String) {
        viewBinding.progressBar.visibility = View.VISIBLE

        viewModel.privateKeyEvent.observe(viewLifecycleOwner, Observer {
            if (it!=null) {

                saveDatabase(name,it)


            }
        })
//        viewModel.certEvent.observe(viewLifecycleOwner, Observer {
//            if (it!=null) {
//
//                viewBinding.progressBar.visibility = View.GONE
//                var privateKey = Base64.encodeToString(it.key().encoded, android.util.Base64.DEFAULT)
////                var privateKey = GenCsr.writeData(context,)
//                var sign = RSA.sign(it.key(),"test")
//
//                val csr = Csr(name,"",privateKey,sign,"",UtilApps.currentDate())
//                viewModel.addCsr(csr)
//
//                val action =
//                    CsrFragmentDirections.nextActionToInet()
//                findNavController().navigate(action)
//            }
//        })

        GlobalScope.launch {
            viewModel.genCsr(context, name, organize, unit,state,city,country,email)
//            viewModel.genCsr2(context, name, organize, unit,state,city,country,email)
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

    private fun alertSuccess( temp:CsrTemp) {

        val dialog = Dialog(requireCompatActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_import_success)
        dialog.getWindow()
            ?.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.transparent)));
        dialog.getWindow()?.setLayout(
            ((UtilApps.getScreenWidth(getActivity()) * .9).toInt()),
            ViewGroup.LayoutParams.WRAP_CONTENT
        );

        dialog.setCancelable(false)

        val tvTitle = dialog.findViewById<TextView>(R.id.tv_title)
        tvTitle.setText("Create csr success\n"+"path:"+temp.path)

        val yesBtn = dialog.findViewById(R.id.btn_positive) as MaterialButton
        yesBtn.setOnClickListener {
            dialog.dismiss()
            findNavController().popBackStack()
            inetIntent(Constants.INET_URL)
        }

        dialog.show()
    }

    fun saveDatabase(name:String,temp:CsrTemp){
        viewBinding.progressBar.visibility = View.GONE
        var privateKey = Base64.encodeToString(temp.privateKey.encoded, Base64.DEFAULT)
        var sign = RSA.sign(temp.privateKey,"test")
        val csr = Csr(name,"",privateKey,sign,"",UtilApps.currentDate(),temp.path)
        viewModel.addCsr(csr)
        alertSuccess(temp)

//                val action =
//                    CsrFragmentDirections.nextActionToInet()
//                findNavController().navigate(action)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1234){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {

                }
            }
        }
    }

    fun managePermission() {
        val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        startActivityForResult(
            Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                uri
            ),1234
        )
    }

    fun inetIntent(url :String){
        val uri = Uri.parse(url)
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(requireCompatActivity().resources.getColor(R.color.blue,null))
            .build()
        val customTabsIntent: CustomTabsIntent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(defaultColors)
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(requireContext(), uri)
    }

}