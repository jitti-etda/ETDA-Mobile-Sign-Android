package th.or.etda.teda.mobile.ui.sign

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
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
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.BiometricEncryptedSharedPreferences
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.databinding.SignFragmentBinding
import th.or.etda.teda.mobile.extract.ExtractCAViewModel
import th.or.etda.teda.mobile.ui.cert.CertListFragment
import th.or.etda.teda.mobile.ui.home.HomeViewModel
import th.or.etda.teda.mobile.ui.importkey.ImportKeyViewModel
import th.or.etda.teda.mobile.util.Constants
import th.or.etda.teda.mobile.util.SigningSingUtil
import th.or.etda.teda.ui.base.BaseFragment
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class SignFragment : BaseFragment<SignFragmentBinding>(
    layoutId = R.layout.sign_fragment
), ZXingScannerView.ResultHandler {

    var REQUEST_CODE = 1234

    //    private lateinit var viewModel: HomeViewModel
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    lateinit var cameraSelector: CameraSelector
    lateinit var previewView: PreviewView
    var cameraProvider: ProcessCameraProvider? = null
    var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var mScannerView: ZXingScannerView? = null


    private val viewModel: SignViewModel by viewModel()
    private val importViewModel: ImportKeyViewModel by viewModel()

    private val extractCAViewModel: ExtractCAViewModel by viewModel()

//    private val extractCAViewModel: ExtractCAViewModel by viewModels {
//        ExtractCaViewModelFactory((requireActivity().application as TEDAMobileApplication).repository)
//    }

    var qrcodeResult = ""
    var certName: Certificate? = null

    var sum = 0

    companion object {
        private const val PERMISSION_CAMERA_REQUEST = 1
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        const val TAG = "HomeFragment"
    }

    private val screenAspectRatio: Int
        get() {
            // Get screen metrics used to setup camera for full screen resolution
            val metrics = DisplayMetrics().also { previewView.display?.getRealMetrics(it) }
            return aspectRatio(metrics.widthPixels, metrics.heightPixels)
        }

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
        mScannerView?.setBorderCornerRadius(100)
        mScannerView?.setBorderStrokeWidth(35)
        viewBinding.previewView.addView(mScannerView)
    }


    override fun onInitDependencyInjection() {

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


    }

    override fun onInitDataBinding() {
//        viewModel = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(HomeViewModel::class.java)
//        val viewModel: HomeViewModel by viewModels { HomeViewModelFactory(getApplication(), "my awesome param") }

//        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        viewBinding.viewModel = viewModel
//        val binding = HomeFragmentBinding.inflate(inflater, container, false)


        viewBinding.apply {
            qrReader.setOnClickListener {
                setUpCamera()
            }

            bioAuth.setOnClickListener {
//                checkBioAuth()
            }
            certList.setOnClickListener {
                val action = SignFragmentDirections.nextCertList(false)
                findNavController().navigate(action)
            }
            drive.setOnClickListener {
                val action = SignFragmentDirections.nextDrive()
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
//            mScannerView?.setResultHandler(this@SignFragment)
//            mScannerView?.startCamera()
//            resumeScanner()

//            certName?.let { postSigned(qrcodeResult, it) }
            certName?.let {
//                checkBioAuth(it?.certName)
                if (BiometricEncryptedSharedPreferences.checkBio(requireCompatActivity())) {
                    setUpAuth(it.certName)
                }
            }
        }


//        signedInfoPost()
    }


    private fun checkBioAuth(name: String) {
        val biometricManager = BiometricManager.from(requireContext())
        when (
            if (Build.VERSION.SDK_INT >= 30) {
                biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            } else {
                biometricManager.canAuthenticate()
            }) {

            BiometricManager.BIOMETRIC_SUCCESS -> setUpAuth(name)
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
//                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
//                    putExtra(
//                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
//                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
//                    )
//                }
//                startActivityForResult(enrollIntent, REQUEST_CODE)
                Log.e("MY_APP_TAG", "BIOMETRIC_ERROR_NONE_ENROLLED")
                // Prompts the user to create credentials that your app accepts.
//                var enrollIntent: Intent? = null
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
//                    enrollIntent.putExtra(
//                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
//                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
//                    )
//                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                    enrollIntent = Intent(Settings.ACTION_FINGERPRINT_ENROLL)
//                } else {
//                    enrollIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
//                }
//                startActivityForResult(enrollIntent, REQUEST_CODE)
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Log.e("MY_APP_TAG", "BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED")
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Log.e("MY_APP_TAG", "BIOMETRIC_ERROR_UNSUPPORTED")
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Log.e("MY_APP_TAG", "BIOMETRIC_STATUS_UNKNOWN")
            }
        }
    }

    private fun setUpAuth(name: String) {

//        mScannerView?.setResultHandler(this@SignFragment)
//        mScannerView?.startCamera()
//        resumeScanner()

        certName?.let {
            postSigned(qrcodeResult, it)
        }

//        val executor = ContextCompat.getMainExecutor(requireContext())
//        val biometricPrompt = BiometricPrompt(
//            this,
//            executor,
//            object : BiometricPrompt.AuthenticationCallback() {
//                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                    super.onAuthenticationError(errorCode, errString)
//                    Toast.makeText(
//                        requireContext(),
//                        "Authentication error: $errString", Toast.LENGTH_SHORT
//                    )
//                        .show()
//                }
//
//                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                    super.onAuthenticationSucceeded(result)
//                    println("result => $result")
//                    mScannerView?.setResultHandler(this@SignFragment)
//                    mScannerView?.startCamera()
//
//                    certName?.let { postSigned(qrcodeResult, it) }
//
//                    Toast.makeText(
//                        requireContext(),
//                        "Authentication succeeded!", Toast.LENGTH_SHORT
//                    )
//                        .show()
//                }
//
//                override fun onAuthenticationFailed() {
//                    super.onAuthenticationFailed()
//                    Toast.makeText(
//                        requireContext(), "Authentication failed",
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//                }
//            })
//
//        val promptInfo = BiometricPrompt.PromptInfo.Builder()
//            .setTitle("Please Sign Signature")
//            .setSubtitle("Teda Mobile")
//            .setNegativeButtonText("Cancel")
//            .build()
//
//        biometricPrompt.authenticate(promptInfo)


    }


    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setUpCamera() {
//        previewView = viewBinding.previewView
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
                        Toast.makeText(
                            requireContext(),
                            "Please allow permission",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigateUp()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) { /* ... */
                        token?.continuePermissionRequest()
                    }
                }).check()
//            if (isCameraPermissionGranted()) {
//                mScannerView?.startCamera()
////                bindCameraUseCases()
////                bindAnalyseUseCase()
//            } else {
//                ActivityCompat.requestPermissions(
//                    requireActivity(),
//                    arrayOf(Manifest.permission.CAMERA),
//                    PERMISSION_CAMERA_REQUEST
//                ).let {
//                    mScannerView?.startCamera()
////                    bindCameraUseCases()
////                    bindAnalyseUseCase()
//                }
//
//            }
        })


    }


//    override fun onResume() {
//        super.onResume()
//        resumeScanner()
//
//
//    }


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


    private fun bindCameraUseCases() {
        if (cameraProvider == null) return
        previewUseCase?.let {
            cameraProvider!!.unbind(previewUseCase)
        }
        previewUseCase = Preview.Builder().setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(previewView.display.rotation)
            .build()
        previewUseCase!!.setSurfaceProvider(previewView.surfaceProvider)

        try {
            cameraProvider!!.bindToLifecycle(viewLifecycleOwner, cameraSelector, previewUseCase)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message.toString())
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message.toString())
        }

    }

    private fun bindAnalyseUseCase() {
        if (cameraProvider == null) return
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE
            )
            .build()
        val barcodeScanner = BarcodeScanning.getClient(options)
        if (analysisUseCase == null) {
            cameraProvider!!.unbind(analysisUseCase)
        }

        analysisUseCase = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(previewView.display.rotation)
            .build()

        val cameraExecutor = Executors.newSingleThreadExecutor()
        analysisUseCase?.setAnalyzer(
            cameraExecutor,
            ImageAnalysis.Analyzer { image: ImageProxy ->
                processImageProxy(barcodeScanner, image)
            }
        )

        try {
            cameraProvider!!.bindToLifecycle(viewLifecycleOwner, cameraSelector, analysisUseCase)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message.toString())
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message.toString())
        }
    }


    @SuppressLint("UnsafeExperimentalUsageError")
    private fun processImageProxy(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        val inputImage = InputImage.fromMediaImage(
            imageProxy.image!!,
            imageProxy.imageInfo.rotationDegrees
        )

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes.forEach {
                    Log.d(TAG, it.rawValue)
                }
                println("sum =>$sum")
            }
            .addOnFailureListener {
                Log.e(TAG, it.message.toString())
            }.addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    override fun handleResult(rawResult: Result?) {
        println("rawResult => ${rawResult?.text}")
//        mScannerView?.stopCamera()
        stopScanner()
        val result = rawResult?.text
        result?.let {
            qrcodeResult = result.trim()

            try {
                var data = qrcodeResult.split(";")

                var message =
//                    "url : " + data[SigningSingUtil.URL.ordinal] + "\n" +
//                            "request_id : " + data[SigningSingUtil.REQUEST_ID.ordinal] + "\n" +
//                            "token : " + data[SigningSingUtil.TOKEN.ordinal] + "\n" +
                            "ref_number : " + data[SigningSingUtil.REF_NUMBER.ordinal]

                AlertDialog.Builder(requireContext())
                    .setTitle("ข้อมูลเอกสารที่ลงนาม")
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(
                        "Confirm"
                    ) { dialog, which ->
                        dialog.dismiss()
                        waitForSelectCert()
                    }.setNegativeButton("Cancel") { dialog, whict ->
                        resumeScanner()
                    }.show()
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
//            postSigned(result)

        }
//        checkBioAuth()
    }

    private fun waitForSelectCert() {
        stopScanner()
        val action = SignFragmentDirections.nextCertList(true)
        findNavController().navigate(action)

    }

    private fun postSigned(result: String, cert: Certificate) {
//        viewBinding.progressBar.visibility = View.VISIBLE
//        lifecycleScope.launch {
        extractCAViewModel.getCertCa(cert.certName).observe(
            viewLifecycleOwner,
            Observer { certCa ->
                if (certCa != null) {
                    extractCAViewModel.getCertChains(cert.certName).observe(
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

//        }
//        viewModel.signedInfoError.observe(
//            viewLifecycleOwner,
//            Observer {
//                if (it != null) {
////                        viewBinding.progressBar.visibility = View.GONE
//                    alertBox(it.description)
//                }
//            })
        viewModel.signedInfo.observe(viewLifecycleOwner, { signedInfo ->
            signedInfo?.let {
                it.signedInfo?.let { it1 ->
                    var message =
//                        "business_type : " + it.document?.documentInfo?.businessType + "\n" +
//                                "document_description : " + it.document?.documentInfo?.documentDescription + "\n" +
//                                "document_name : " + it.document?.documentInfo?.documentName + "\n" +
//                                "document_type : " + it.document?.documentInfo?.documentType + "\n" +
                                "description : " + it.description


                    val dialog = AlertDialog.Builder(activity as Context)
                    dialog.setTitle("Sign info")
                    dialog.setMessage(message)
                    dialog.setCancelable(false)
                    dialog.setPositiveButton("Confirm") { text, listener ->
                        var isCache = false
                        for (i in Constants.listDataCache.indices) {
                            Log.i(
                                "Cache",
                                Constants.listDataCache[i].name + " ==== " + Constants.listDataCache[i].privateKey
                            )
                            if (cert.certName.equals(Constants.listDataCache[i].name)) {
                                var signature =
                                    Constants.listDataCache[i].privateKey.let { it2 ->
                                        viewModel.signWithKeyStore(
                                            it1,
                                            it2
                                        )
                                    }
                                postSignedSubmit(result, signature)
                                isCache = true
                                break
                            }
                        }

                        if (!isCache) {
                            BiometricEncryptedSharedPreferences.create(
                                this,
                                HomeViewModel.FileName,
                                1,
                                BiometricPrompt.PromptInfo.Builder()
                                    .setTitle(getString(R.string.app_name))
                                    .setAllowedAuthenticators(
                                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                                    ).build()
                            ).observe(this, Observer { it: SharedPreferences? ->

                                if (it != null) {
                                    importViewModel.getCertificateAll().observe(
                                        viewLifecycleOwner,
                                        Observer { datas: List<Certificate> ->

                                            for (i in datas.indices) {
                                                it.getString(datas[i].certName, "")?.let { it2 ->
                                                    var data = SignCache(datas[i].certName, it2)
                                                    Log.i(
                                                        "SignCache",
                                                        data.name + " ==== " + data.privateKey
                                                    )
                                                    Constants.listDataCache.add(data)
                                                }
                                            }

                                        })
                                    var data = it.getString(cert.certName, "")
                                    var signature =
                                        data?.let { it2 -> viewModel.signWithKeyStore(it1, it2) }
                                    if (signature != null) {
                                        postSignedSubmit(result, signature)
                                    }
                                } else {
                                    viewBinding.progressBar.visibility = View.GONE
                                }


                            })
                        }
                    }
                    dialog.setNegativeButton("Cancel") { dlgInterface: DialogInterface, listener ->


                        dlgInterface.dismiss()
                        waitForSelectCert()

                    }

                    Handler().postDelayed(
                        {
                            stopScanner()
                        },
                        300 // value in milliseconds
                    )

                    dialog.create()
                    dialog.show()


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

                    val action = SignFragmentDirections.nextSignDetail(it)
                    findNavController().navigate(action)

//                    AlertDialog.Builder(requireContext())
//                        .setTitle("Complete")
//                        .setMessage("คุณได้ลงนามเรียบร้อยแล้ว")
//                        .setPositiveButton(
//                            "Close"
//                        ) { dialog, which ->
//                            dialog.dismiss()
//
//
//
//                        }.show()
                }
            })
//            viewModel.signedInfoSubmitError.observe(
//                viewLifecycleOwner,
//                Observer {
//                    if (it != null) {
//                        viewBinding.progressBar.visibility = View.GONE
//                        alertBox(it.description)
//                    }
//                })
        }
    }


//    fun signedInfoPost(){
//
//        lifecycleScope.launch {
//            viewModel.signedInfoPost().observe(viewLifecycleOwner, Observer {
//
//
//            })
//        }
//
//
//
//    }
}