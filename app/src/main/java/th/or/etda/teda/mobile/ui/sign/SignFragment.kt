package th.or.etda.teda.mobile.ui.sign

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.launch
import me.dm7.barcodescanner.core.IViewFinder
import me.dm7.barcodescanner.core.ViewFinderView
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.MainActivity
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.BiometricEncryptedSharedPreferences
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.databinding.SignFragmentBinding
import th.or.etda.teda.mobile.ui.cert.CertListFragment
import th.or.etda.teda.mobile.ui.cert.CertListViewModel
import th.or.etda.teda.mobile.util.Constants
import th.or.etda.teda.mobile.util.SigningSingUtil
import th.or.etda.teda.mobile.util.UtilApps
import th.or.etda.teda.ui.base.BaseFragment
import java.nio.charset.StandardCharsets


class SignFragment : BaseFragment<SignFragmentBinding>(
    layoutId = R.layout.sign_fragment
), ZXingScannerView.ResultHandler {

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    lateinit var cameraSelector: CameraSelector

    var cameraProvider: ProcessCameraProvider? = null

    private var mScannerView: ZXingScannerView? = null


    private val viewModel: SignViewModel by viewModel()
    private val viewModelCert: CertListViewModel by viewModel()


    var qrcodeResult = ""
    var certName: Certificate? = null
    val CAMERA_PERMISSION = 2222
    var isDeeplink = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        loadKoinModules(homeModule)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mScannerView = object : ZXingScannerView(requireContext()) {
            override fun createViewFinderView(context: Context?): IViewFinder {
                return CustomViewFinderView(context)
            }
        }
        mScannerView?.setBorderStrokeWidth(20)
        mScannerView?.setBorderColor(resources.getColor(R.color.blue, null))
        viewBinding.previewView.addView(mScannerView)
    }


    override fun onInitDependencyInjection() {

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


    }

    override fun onInitDataBinding() {
        initActionBar()
        viewBinding.viewModel = viewModel

        val qrcode = arguments?.let {
            SignFragmentArgs.fromBundle(it).qrcode
        }
        if (qrcode != null && qrcode.isNotEmpty() && !isDeeplink) {
            decodeDeeplink(qrcode)
            isDeeplink = true
        }

        viewBinding.apply {
            qrReader.setOnClickListener {
                setUpCamera()
            }

            certList.setOnClickListener {
                val action = SignFragmentDirections.nextCertList(false)
                findNavController().navigate(action)
            }

            readFile.setOnClickListener {
                val action = SignFragmentDirections.nextDirectory()
                findNavController().navigate(action)
            }

        }

        setUpCamera()

        setFragmentResultListener(CertListFragment.REQUEST_KEY) { key, bundle ->
            // read from the bundle
            certName = bundle.getParcelable<Certificate>("cert")
            if (certName != null) {
                if (BiometricEncryptedSharedPreferences.checkBio(requireCompatActivity())) {
                    postSigned(qrcodeResult, certName!!)
                }
            } else {
                resumeScanner()
            }


        }

    }

    fun initActionBar() {
        viewBinding.actionBar.tvTitle.setText("Scan QR Code")
        viewBinding.actionBar.btnBack.setOnClickListener {
            val ac = activity as MainActivity
            ac.onBackPressed()
        }
    }


    private fun setUpCamera() {
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        viewModel.cameraProvider(requireContext())?.observe(viewLifecycleOwner, { provider ->
            cameraProvider = provider
            Dexter.withContext(requireContext())
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) { /* ... */
                        resumeScanner()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) { /* ... */
//                        Toast.makeText(
//                            requireContext(),
//                            "Please allow permission",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        findNavController().navigateUp()
                        dialogCameraPermission()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) { /* ... */
                        token?.continuePermissionRequest()
                    }
                }).check()
        })


    }


    fun resumeScanner() {
        mScannerView?.setResultHandler(this)
        mScannerView?.startCamera()
        viewBinding.progressBar.visibility = View.GONE
    }

    fun stopScanner() {
        mScannerView?.stopCamera()

    }

    private class CustomViewFinderView : ViewFinderView {
        val PAINT = Paint()

        constructor(context: Context?) : super(context) {
            init()
        }

        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
            init()
        }

        private fun init() {
            PAINT.color = Color.WHITE
            PAINT.isAntiAlias = true
            setSquareViewFinder(true)
        }
    }


    override fun handleResult(rawResult: Result?) {
        println("rawResult => ${rawResult?.text}")
        stopScanner()
        val result = rawResult?.text
        result?.let {
            checkQrcode(it)
        }
    }

    fun checkQrcode(result: String) {
        qrcodeResult = result.trim()


        try {
            var data = qrcodeResult.split(";")

            var message = "หมายเลข Reference : " + data[SigningSingUtil.REF_NUMBER.ordinal]

            val dialog = Dialog(requireCompatActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_qrcode_info)
            dialog.getWindow()
                ?.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.transparent)));
            dialog.getWindow()?.setLayout(
                ((UtilApps.getScreenWidth(getActivity()) * .9).toInt()),
                ViewGroup.LayoutParams.WRAP_CONTENT
            );

            dialog.setCancelable(false)

            val tvTitle = dialog.findViewById(R.id.tv_title) as TextView
            tvTitle.setText(message)
            val yesBtn = dialog.findViewById(R.id.btn_positive) as MaterialButton
            val noBtn = dialog.findViewById(R.id.btn_negative) as MaterialButton
            yesBtn.setOnClickListener {
                dialog.dismiss()
                waitForSelectCert()
            }
            noBtn.setOnClickListener {
                dialog.dismiss()
                resumeScanner()
            }

            dialog.show()
        } catch (e: Exception) {
            AlertDialog.Builder(requireContext())
                .setMessage("QrCode Invalid")
                .setCancelable(false)
                .setPositiveButton(
                    "Close"
                ) { dialog, which ->
                    resumeScanner()
                }.show()
        }
    }

    private fun waitForSelectCert() {
        stopScanner()
        val action = SignFragmentDirections.nextCertList(true)
        findNavController().navigate(action)

    }


    private fun postSigned(result: String, cert: Certificate) {
        viewModelCert.getCertCa(cert.certName).observe(
            viewLifecycleOwner,
            Observer { certCa ->
                if (certCa != null) {
                    viewModelCert.getCertChains(cert.certName).observe(
                        viewLifecycleOwner,
                        Observer { certChains ->
                            if (certChains != null) {
                                try {
                                    viewModel.signingSignInfo(result, certCa, certChains)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Qr code invalid",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    e.printStackTrace()
                                }

                            }
                        })
                }
            })

        viewModel.signedInfo.observe(viewLifecycleOwner, { signedInfo ->
            signedInfo?.let {
                it.signedInfo?.let { it1 ->
                    Handler().postDelayed(
                        {
                            stopScanner()
                        },
                        300 // value in milliseconds
                    )
                    signSignature(cert, it1, result)
                }


            }
        })

    }

    private fun postSignedSubmit(url: String, signature: String) {

        lifecycleScope.launch {
            viewModel.signingSignInfoSubmit(url, signature)
        }
        lifecycleScope.launch {
            viewModel.signedInfoSubmit.observe(viewLifecycleOwner, { signedInfo ->
                signedInfo?.let {
                    viewBinding.progressBar.visibility = View.GONE
                    dialogSignSuccess(url)
                }
            })
        }
    }


    fun signSignature(cert: Certificate, signedInfo: String, result: String) {


        var isCache = false
        for (i in Constants.listDataCache.indices) {
            if (cert.certName.equals(Constants.listDataCache[i].name)) {
                var signature =
                    Constants.listDataCache[i].privateKey.let { it2 ->
                        viewModel.signWithKeyStore(
                            signedInfo,
                            it2
                        )
                    }
                postSignedSubmit(result, signature)
                isCache = true
                break
            }
        }

        var allowBio = 0
        if (android.os.Build.VERSION.SDK_INT == 28 || android.os.Build.VERSION.SDK_INT == 29) {
            allowBio = BiometricManager.Authenticators.BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        } else {
            allowBio = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        }

        if (!isCache) {
            BiometricEncryptedSharedPreferences.create(
                this,
                Constants.FileName,
                1,
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.app_name))
                    .setAllowedAuthenticators(
                        allowBio
                    ).build()
            ).observe(this, Observer { it: SharedPreferences? ->

                if (it != null) {
                    viewModelCert.getCertificateAll().observe(
                        viewLifecycleOwner,
                        Observer { datas: List<Certificate> ->

                            for (i in datas.indices) {
                                it.getString(datas[i].certName, "")?.let { it2 ->
                                    var data = SignCache(datas[i].certName, it2)
                                    Constants.listDataCache.add(data)
                                }
                            }

                        })
                    var data = it.getString(cert.certName, "")

                    var signature =
                        data?.let { it2 ->
//                            Log.i("private", it2)
                            viewModel.signWithKeyStore(signedInfo, it2)
                        }
                    if (signature != null) {
                        postSignedSubmit(result, signature)
                    }
                } else {
                    viewBinding.progressBar.visibility = View.GONE
                }


            })
        }
    }

    fun dialogSignSuccess(url: String) {
        val dialog = Dialog(requireCompatActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_sign_info_success)
        dialog.getWindow()
            ?.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.transparent)));
        dialog.getWindow()?.setLayout(
            ((UtilApps.getScreenWidth(getActivity()) * .9).toInt()),
            ViewGroup.LayoutParams.WRAP_CONTENT
        );

        dialog.setCancelable(false)

        val tvDate = dialog.findViewById(R.id.tv_date) as TextView
        tvDate.setText(UtilApps.currentDate())
        val yesBtn = dialog.findViewById(R.id.btn_positive) as MaterialButton
        val noBtn = dialog.findViewById(R.id.btn_negative) as MaterialButton
        yesBtn.setOnClickListener {
            dialog.dismiss()
            val action = SignFragmentDirections.nextActionToFirst()
            findNavController().navigate(action)
//            openWeb(url)
        }
        noBtn.setOnClickListener {
            dialog.dismiss()
            val action = SignFragmentDirections.nextActionToFirst()
            findNavController().navigate(action)
        }

        dialog.show()
    }

    fun dialogCameraPermission() {
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

        val tv_title = dialog.findViewById(R.id.tv_title) as TextView
        tv_title.setText("We need to access your camera for scanning QR code.")
        val yesBtn = dialog.findViewById(R.id.btn_positive) as MaterialButton
//        val noBtn = dialog.findViewById(R.id.btn_negative) as MaterialButton
        yesBtn.setText("Go to setting")
        yesBtn.setOnClickListener {
            dialog.dismiss()
            var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            var uri = Uri.fromParts("package", requireCompatActivity().getPackageName(), null)
            intent.setData(uri);
            startActivityForResult(intent, CAMERA_PERMISSION);
        }
//        noBtn.setOnClickListener {
//            dialog.dismiss()
//            val action = SignFragmentDirections.nextActionToFirst()
//            findNavController().navigate(action)
//        }

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_PERMISSION) {
            if (resultCode == RESULT_OK) {
                setUpCamera()
            } else {
                findNavController().navigateUp()
            }

        }
    }


    fun decodeDeeplink(encode: String) {
        var byte = Base64.decode(encode, Base64.NO_WRAP)
        val signString = String(byte, StandardCharsets.UTF_8)
        checkQrcode(signString)
    }

    fun openWeb(urls: String) {
        val data = urls.split(";")
        val url =
            data[SigningSingUtil.END_POINT.ordinal] + "?request_id=" + data[SigningSingUtil.REQUEST_ID.ordinal] + "&type=" + data[SigningSingUtil.EXTENSION.ordinal]
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

}