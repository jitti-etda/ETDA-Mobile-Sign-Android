package th.or.etda.teda.home

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import th.or.etda.teda.home.databinding.HomeFragmentBinding
import th.or.etda.teda.mobile.MainActivity
import th.or.etda.teda.ui.base.BaseFragment
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

class HomeFragment : Fragment() {

    lateinit var viewDataBinding: HomeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = DataBindingUtil.inflate(layoutInflater, R.layout.home_fragment, container, false)
        viewDataBinding.register.setOnClickListener {
            println("sad")
        }
        return viewDataBinding.root
    }

//    lateinit var viewBinding: HomeFragmentBinding
//
//    companion object {
//        fun newInstance() = HomeFragment()
//        const val SIGN_ALGORITHM = "MD5WithRSA"
//        const val ANDROID_KEY_STORE = "AndroidKeyStore"
//        const val ALIAS = "TEDA_KEY"
//    }
//
//    private lateinit var privateKey: PrivateKey
//    private lateinit var publicKey: PublicKey
//    private val message: String ="Message"
//    private lateinit var signMessage: String
//    private lateinit var signWithKeyStore: String
//
//
//    private fun signWithKeyStore() {
//        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
//        keyStore.load(null)
//        val pvKey = keyStore.getKey(ALIAS, null) as PrivateKey
//        println("KEY == >$pvKey")
//        val signature = Signature.getInstance(SIGN_ALGORITHM)
//        signature.initSign(pvKey)
////        signature.update(viewBinding.editText.text.toString().toByteArray(Charsets.UTF_8))
//        val encodeSign = Base64.encodeToString(signature.sign(), Base64.DEFAULT)
//        signMessage = encodeSign
//        println("signMessage => $signMessage")
//    }
//
//    private fun verifySignature() {
//        val publicSignature = Signature.getInstance(SIGN_ALGORITHM)
//        publicSignature.initVerify(publicKey)
////        publicSignature.update(viewBinding.editText.text.toString().toByteArray(Charsets.UTF_8))
//        val signature = Base64.decode(signMessage, Base64.DEFAULT)
//        println("Verify ==> ${publicSignature.verify(signature)}")
//    }
//
//    private fun singWithFile() {
//        println("private key <<<<< ${privateKey.algorithm} ${privateKey.format}")
//        //            privateKey.algorithm
//        val signature = Signature.getInstance(SIGN_ALGORITHM)
//        signature.initSign(privateKey)
////        signature.update(viewBinding.editText.text.toString().toByteArray(Charsets.UTF_8))
//        val encodeSign = Base64.encodeToString(signature.sign(), Base64.DEFAULT)
//        signMessage = encodeSign
//        println("signMessage => $signMessage")
//    }
//
//    private fun extractKeyStore() {
//        val p12: KeyStore = KeyStore.getInstance("pkcs12")
//        val keyFile = requireActivity().assets.open("CN_TEST_2021.p12")
//        val passphrase = "123456789".toCharArray()
//        p12.load(keyFile, passphrase)
//        println("----------------------------")
//        val priKey: PrivateKey = p12.getKey("CN_TEST_2021", passphrase) as PrivateKey
//        privateKey = priKey
//        println(p12.aliases())
//        publicKey = p12.getCertificate(p12.aliases().toList().first()).publicKey
//        //        val e: Enumeration<String> = p12.aliases()
//        //        while (e.hasMoreElements()) {
//        //            val alias = e.nextElement() as String
//        //            val c = p12.getCertificate(alias) as X509Certificate
//        //        }
//        val cert = p12.getCertificateChain("CN_TEST_2021")
//        val ks = KeyStore.getInstance(MainActivity.ANDROID_KEY_STORE)
//        ks.load(null)
//        ks.setKeyEntry(MainActivity.ALIAS, priKey, null, cert)
//    }
//
//    override val setLayout: Int
//        get() = R.layout.home_fragment
//
//    override fun startView(viewBinding: ViewDataBinding) {
//        this.viewBinding = viewBinding as HomeFragmentBinding
//        viewBinding.apply {
//            register.setOnClickListener {
//                extractKeyStore()
//            }
//
//            sign.setOnClickListener {
//                singWithFile()
//            }
//
//            verify.setOnClickListener {
//                println("saghsjkrtr")
//                verifySignature()
//            }
//
//            signWithKeyStore.setOnClickListener {
//                signWithKeyStore()
//            }
//        }
//    }

}