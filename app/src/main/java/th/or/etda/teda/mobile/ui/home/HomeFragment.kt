//package th.or.etda.teda.mobile.ui.home
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.app.AlertDialog
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.Color
//import android.graphics.Paint
//import android.os.Build
//import android.os.Bundle
//import android.provider.Settings
//import android.util.AttributeSet
//import android.util.DisplayMetrics
//import android.util.Log
//import android.view.View
//import android.widget.Toast
//import androidx.biometric.BiometricManager
//import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
//import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
//import androidx.biometric.BiometricPrompt
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.setFragmentResultListener
//import androidx.lifecycle.Observer
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.fragment.findNavController
//import com.google.mlkit.vision.barcode.Barcode
//import com.google.mlkit.vision.barcode.BarcodeScanner
//import com.google.mlkit.vision.barcode.BarcodeScannerOptions
//import com.google.mlkit.vision.barcode.BarcodeScanning
//import com.google.mlkit.vision.common.InputImage
//import com.google.zxing.Result
//import kotlinx.coroutines.launch
//import me.dm7.barcodescanner.core.IViewFinder
//import me.dm7.barcodescanner.core.ViewFinderView
//import me.dm7.barcodescanner.zxing.ZXingScannerView
//import org.koin.android.viewmodel.ext.android.viewModel
//import th.or.etda.teda.mobile.R
//import th.or.etda.teda.mobile.data.Certificate
//import th.or.etda.teda.mobile.databinding.HomeFragmentBinding
//import th.or.etda.teda.mobile.extract.ExtractCAViewModel
//import th.or.etda.teda.mobile.ui.cert.CertListFragment
//import th.or.etda.teda.ui.base.BaseFragment
//import java.util.concurrent.Executors
//import kotlin.math.abs
//import kotlin.math.max
//import kotlin.math.min
//
//
//class HomeFragment : BaseFragment<HomeFragmentBinding>(
//    layoutId = R.layout.home_fragment
//), ZXingScannerView.ResultHandler {
//
//    //    private lateinit var viewModel: HomeViewModel
//    private var lensFacing = CameraSelector.LENS_FACING_BACK
//    lateinit var cameraSelector: CameraSelector
//    lateinit var previewView: PreviewView
//    var cameraProvider: ProcessCameraProvider? = null
//    var previewUseCase: Preview? = null
//    private var analysisUseCase: ImageAnalysis? = null
//    private var mScannerView: ZXingScannerView? = null
//
//
//    private val viewModel: HomeViewModel by viewModel()
//
//
//    private val extractCAViewModel: ExtractCAViewModel by viewModel()
////    private val extractCAViewModel: ExtractCAViewModel by viewModels {
////        ExtractCaViewModelFactory((requireActivity().application as TEDAMobileApplication).repository)
////    }
//
//    var qrcodeResult = ""
//    var certName : Certificate? = null
//
//    var sum = 0
//
//    companion object {
//        private const val PERMISSION_CAMERA_REQUEST = 1
//        private const val RATIO_4_3_VALUE = 4.0 / 3.0
//        private const val RATIO_16_9_VALUE = 16.0 / 9.0
//        const val TAG = "HomeFragment"
//    }
//
//    private val screenAspectRatio: Int
//        get() {
//            // Get screen metrics used to setup camera for full screen resolution
//            val metrics = DisplayMetrics().also { previewView.display?.getRealMetrics(it) }
//            return aspectRatio(metrics.widthPixels, metrics.heightPixels)
//        }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////        loadKoinModules(homeModule)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//
//        mScannerView = object : ZXingScannerView(requireContext()) {
//            override fun createViewFinderView(context: Context?): IViewFinder {
//                return CustomViewFinderView(context)
//            }
//        }
//        mScannerView?.setBorderCornerRadius(100)
//        mScannerView?.setBorderStrokeWidth(35)
//        viewBinding.previewView.addView(mScannerView)
//    }
//
//
//    override fun onInitDependencyInjection() {
//
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//
//    }
//
//    override fun onInitDataBinding() {
////        viewModel = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(HomeViewModel::class.java)
////        val viewModel: HomeViewModel by viewModels { HomeViewModelFactory(getApplication(), "my awesome param") }
//
////        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
//        viewBinding.viewModel = viewModel
////        val binding = HomeFragmentBinding.inflate(inflater, container, false)
//
//
//        viewBinding.apply {
//            qrReader.setOnClickListener {
//                setUpCamera()
//            }
//
//            bioAuth.setOnClickListener {
//                checkBioAuth()
//            }
//            certList.setOnClickListener {
//                val action = HomeFragmentDirections.nextCertList()
//                findNavController().navigate(action)
//            }
//            drive.setOnClickListener {
//                val action = HomeFragmentDirections.nextDrive()
//                findNavController().navigate(action)
//            }
//            readFile.setOnClickListener {
//                val action = HomeFragmentDirections.nextDirectory()
//                findNavController().navigate(action)
//            }
//
//        }
//
//        setFragmentResultListener(CertListFragment.REQUEST_KEY) { key, bundle ->
//            // read from the bundle
//            certName = bundle.getParcelable<Certificate>("cert")
//            mScannerView?.setResultHandler(this@HomeFragment)
//            mScannerView?.startCamera()
//
////            certName?.let { postSigned(qrcodeResult, it) }
//            checkBioAuth()
//        }
//
//
////        signedInfoPost()
//    }
//
//
//    private fun checkBioAuth() {
//        val biometricManager = BiometricManager.from(requireContext())
//        when (
//            if (Build.VERSION.SDK_INT >= 30) {
//                biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
//            } else {
//                biometricManager.canAuthenticate()
//            }) {
//
//            BiometricManager.BIOMETRIC_SUCCESS -> setUpAuth()
//            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
//                Log.e("MY_APP_TAG", "No biometric features available on this device.")
//            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
//                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
//            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
//                // Prompts the user to create credentials that your app accepts.
//                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
//                    putExtra(
//                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
//                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
//                    )
//                }
//                //                startActivityForResult(enrollIntent, REQUEST_CODE)
//                Log.e("MY_APP_TAG", "BIOMETRIC_ERROR_NONE_ENROLLED")
//            }
//            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
//                Log.e("MY_APP_TAG", "BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED")
//            }
//            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
//                Log.e("MY_APP_TAG", "BIOMETRIC_ERROR_UNSUPPORTED")
//            }
//            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
//                Log.e("MY_APP_TAG", "BIOMETRIC_STATUS_UNKNOWN")
//            }
//        }
//    }
//
//    private fun setUpAuth() {
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
//                    mScannerView?.setResultHandler(this@HomeFragment)
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
//            .setTitle("TITLE")
//            .setSubtitle("setSubtitle")
//            .setNegativeButtonText("setNegativeButtonText")
//            .build()
//
//        biometricPrompt.authenticate(promptInfo)
//
//    }
//
//    private fun isCameraPermissionGranted(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            requireContext(),
//            Manifest.permission.CAMERA
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun setUpCamera() {
////        previewView = viewBinding.previewView
//        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
//        viewModel.cameraProvider(requireContext())?.observe(viewLifecycleOwner, { provider ->
//            cameraProvider = provider
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
//            }
//        })
//    }
//
//
//    override fun onResume() {
//        super.onResume()
////        resumeScanner()
//        mScannerView?.setResultHandler(this)
//        mScannerView?.startCamera()
//    }
//
////    fun resumeScanner() {
////        mScannerView?.setResultHandler(this)
////        mScannerView?.startCamera()
////    }
//
//    private class CustomViewFinderView : ViewFinderView {
//        val PAINT = Paint()
//
//        constructor(context: Context?) : super(context) {
//            init()
//        }
//
//        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
//            init()
//        }
//
//        private fun init() {
//            PAINT.color = Color.WHITE
//            PAINT.isAntiAlias = true
//            setSquareViewFinder(true)
//        }
//    }
//
//
//    private fun bindCameraUseCases() {
//        if (cameraProvider == null) return
//        previewUseCase?.let {
//            cameraProvider!!.unbind(previewUseCase)
//        }
//        previewUseCase = Preview.Builder().setTargetAspectRatio(screenAspectRatio)
//            .setTargetRotation(previewView.display.rotation)
//            .build()
//        previewUseCase!!.setSurfaceProvider(previewView.surfaceProvider)
//
//        try {
//            cameraProvider!!.bindToLifecycle(viewLifecycleOwner, cameraSelector, previewUseCase)
//        } catch (illegalStateException: IllegalStateException) {
//            Log.e(TAG, illegalStateException.message.toString())
//        } catch (illegalArgumentException: IllegalArgumentException) {
//            Log.e(TAG, illegalArgumentException.message.toString())
//        }
//
//    }
//
//    private fun bindAnalyseUseCase() {
//        if (cameraProvider == null) return
//        val options = BarcodeScannerOptions.Builder()
//            .setBarcodeFormats(
//                Barcode.FORMAT_QR_CODE
//            )
//            .build()
//        val barcodeScanner = BarcodeScanning.getClient(options)
//        if (analysisUseCase == null) {
//            cameraProvider!!.unbind(analysisUseCase)
//        }
//
//        analysisUseCase = ImageAnalysis.Builder()
//            .setTargetAspectRatio(screenAspectRatio)
//            .setTargetRotation(previewView.display.rotation)
//            .build()
//
//        val cameraExecutor = Executors.newSingleThreadExecutor()
//        analysisUseCase?.setAnalyzer(
//            cameraExecutor,
//            ImageAnalysis.Analyzer { image: ImageProxy ->
//                processImageProxy(barcodeScanner, image)
//            }
//        )
//
//        try {
//            cameraProvider!!.bindToLifecycle(viewLifecycleOwner, cameraSelector, analysisUseCase)
//        } catch (illegalStateException: IllegalStateException) {
//            Log.e(TAG, illegalStateException.message.toString())
//        } catch (illegalArgumentException: IllegalArgumentException) {
//            Log.e(TAG, illegalArgumentException.message.toString())
//        }
//    }
//
//
//    @SuppressLint("UnsafeExperimentalUsageError")
//    private fun processImageProxy(
//        barcodeScanner: BarcodeScanner,
//        imageProxy: ImageProxy
//    ) {
//        val inputImage = InputImage.fromMediaImage(
//            imageProxy.image!!,
//            imageProxy.imageInfo.rotationDegrees
//        )
//
//        barcodeScanner.process(inputImage)
//            .addOnSuccessListener { barcodes ->
//                barcodes.forEach {
//                    Log.d(TAG, it.rawValue)
//                }
//                println("sum =>$sum")
//            }
//            .addOnFailureListener {
//                Log.e(TAG, it.message.toString())
//            }.addOnCompleteListener {
//                imageProxy.close()
//            }
//    }
//
//    private fun aspectRatio(width: Int, height: Int): Int {
//        val previewRatio = max(width, height).toDouble() / min(width, height)
//        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
//            return AspectRatio.RATIO_4_3
//        }
//        return AspectRatio.RATIO_16_9
//    }
//
//    override fun handleResult(rawResult: Result?) {
//        println("rawResult => ${rawResult?.text}")
//        mScannerView?.stopCamera()
//        val result = rawResult?.text
//        result?.let {
//            qrcodeResult = result.trim()
//            waitForSelectCert(result.trim())
////            postSigned(result)
//
//        }
////        checkBioAuth()
//    }
//
//    private fun waitForSelectCert(result: String) {
//
//        val action = HomeFragmentDirections.nextCertList(true)
//        findNavController().navigate(action)
//
//    }
//
//    private fun postSigned(result: String, cert: Certificate) {
////        viewBinding.progressBar.visibility = View.VISIBLE
//        lifecycleScope.launch {
//            extractCAViewModel.getCertCa(cert.certName).observe(
//                viewLifecycleOwner,
//                Observer { certCa ->
//                    if (certCa != null) {
//                        extractCAViewModel.getCertChains(cert.certName).observe(
//                            viewLifecycleOwner,
//                            Observer { certChains ->
//                                if (certChains != null) {
//                                    try{
//                                        viewModel.signingSignInfo(result, certCa, certChains)
//                                    }catch (e:Exception){
//                                        Toast.makeText(requireContext(),"Qr code invalid",Toast.LENGTH_SHORT).show()
//                                        e.printStackTrace()
//                                    }
//
//                                }
//                            })
//                    }
//                })
//
//        }
//        viewModel.signedInfoError.observe(
//            viewLifecycleOwner,
//            Observer {
//                if (it != null) {
////                        viewBinding.progressBar.visibility = View.GONE
//                    alertBox(it.description)
//                }
//            })
//        viewModel.signedInfo.observe(viewLifecycleOwner, { signedInfo ->
//            signedInfo?.let {
////                    val action = HomeFragmentDirections.nextAction(signedInfo)
////                    findNavController().navigate(action)
//                it.document?.signedinfo?.signedInfo?.let { it1 ->
////                        var signedInfoHash = viewModel.getSha256Hash(it1)
////                        var signature = signedInfoHash?.let { it2 ->
////                            viewModel.signWithKeyStore(
////                                it2
////                            )
////                        }
//                    var signature = viewModel.signWithKeyStore(it1, cert)
//                    postSignedSubmit(result, signature)
//
//                }
//
//
//            }
//        })
//
//    }
//
//    private fun postSignedSubmit(url: String, signature: String) {
//        lifecycleScope.launch {
//            viewModel.signingSignInfoSubmit(url, signature)
//        }
//        lifecycleScope.launch {
//            viewModel.signedInfoSubmit.observe(viewLifecycleOwner, { signedInfo ->
//                signedInfo?.let {
//                    viewBinding.progressBar.visibility = View.GONE
//
//                }
//            })
//            viewModel.signedInfoSubmitError.observe(
//                viewLifecycleOwner,
//                Observer {
//                    if (it != null) {
//                        viewBinding.progressBar.visibility = View.GONE
//                        alertBox(it.description)
//                    }
//                })
//        }
//    }
//
//
//    private fun alertBox(message: String) {
//        AlertDialog.Builder(requireContext())
//            .setMessage(message)
//            .setPositiveButton(
//                "Close"
//            ) { dialog, which ->
//                // continue with delete
////                onResume()
//            }.show()
//    }
//
//
////    fun signedInfoPost(){
////
////        lifecycleScope.launch {
////            viewModel.signedInfoPost().observe(viewLifecycleOwner, Observer {
////
////
////            })
////        }
////
////
////
////    }
//}