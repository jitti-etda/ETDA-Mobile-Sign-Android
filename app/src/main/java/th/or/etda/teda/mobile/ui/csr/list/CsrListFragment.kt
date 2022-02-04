package th.or.etda.teda.mobile.ui.csr.list

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.MainActivity
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.BiometricEncryptedSharedPreferences
import th.or.etda.teda.mobile.common.RecyclerItemClickListener
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.data.csr.Csr
import th.or.etda.teda.mobile.databinding.CsrListFragmentBinding
import th.or.etda.teda.mobile.model.ExtrackP12
import th.or.etda.teda.mobile.ui.csr.directory.DirectoryCsrActivity
import th.or.etda.teda.mobile.ui.importkey.password.ImportKeyPasswordViewModel
import th.or.etda.teda.mobile.util.Constants
import th.or.etda.teda.mobile.util.UtilApps
import th.or.etda.teda.ui.base.BaseFragment
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import androidx.browser.customtabs.CustomTabColorSchemeParams





class CsrListFragment : BaseFragment<CsrListFragmentBinding>(
    layoutId = R.layout.csr_list_fragment
) {

    private val viewModel: CsrListViewModel by viewModel()
    lateinit var adapterCsr: CsrAdapter
    private val viewModelImport: ImportKeyPasswordViewModel by viewModel()

    val PICK_FILE = 1223
    override fun onInitDependencyInjection() {

    }

    var crtPath = ""
    override fun onInitDataBinding() {

        initActionBar()

        crtPath = arguments?.let {
            CsrListFragmentArgs.fromBundle(it).file
        }.toString()
        if (crtPath.isNotEmpty() && crtPath.contains("temp_cer.cer")) {
            import(crtPath)
        } else if (crtPath.isNotEmpty() && crtPath.contains("temp_chains.p7b")) {
            importChain(crtPath)
        }



        adapterCsr = CsrAdapter(requireCompatActivity())
        getCsrAll()
        viewBinding.apply {

            recyclerView.adapter = adapterCsr
            adapterCsr.setOnEventListener(object : CsrAdapter.OnEventListener {
                override fun onEvent(type: Int, item: Csr) {
                    if (type == 0) {
                        val intent = Intent(context, DirectoryCsrActivity::class.java)
                        intent.putExtra("isCer", true)
                        intent.putExtra("type", "cer")
                        startActivityForResult(intent, PICK_FILE)
                    } else if (type == 1) {
                        val intent = Intent(context, DirectoryCsrActivity::class.java)
                        intent.putExtra("isCer", true)
                        intent.putExtra("type", "p7b")
                        startActivityForResult(intent, PICK_FILE)
                    } else if (type == 2) {
                        val model = item
                        if (!model.csrKey.isNullOrEmpty() && !model.chains.isNullOrEmpty()) {
                            val binCpk: ByteArray =
                                Base64.decode(model.privateKey, Base64.NO_WRAP)
                            val keyFactory = KeyFactory.getInstance("RSA")
                            val privateKeySpec = PKCS8EncodedKeySpec(binCpk)
                            val privateKey = keyFactory.generatePrivate(privateKeySpec)

                            Log.i("chains", model.chains!!)
                            alertImport(
                                ExtrackP12(
                                    model.csrName,
                                    model.chains!!,
                                    model.csrKey!!,
                                    privateKey
                                )
                            )
                        }
                    } else {

                        alertDelete(item)
                    }
                }
            })


//            recyclerView.addOnItemTouchListener(
//                RecyclerItemClickListener(
//                    context,
//                    recyclerView,
//                    object : RecyclerItemClickListener.OnItemClickListener {
//                        override fun onItemClick(view: View, position: Int) {
//                            adapterCsr.currentList[position].privateKey?.let { Log.i("pv", it) }
//
//
//                            val intent = Intent(context, DirectoryCsrActivity::class.java)
//                            intent.putExtra("isCer", true)
//                            startActivityForResult(intent, PICK_FILE)
//                        }
//
//                        override fun onLongItemClick(view: View, position: Int) {
//                            val model = adapterCsr.currentList[position]
//                            if (!model.csrKey.isNullOrEmpty() && !model.chains.isNullOrEmpty()) {
//                                val binCpk: ByteArray =
//                                    Base64.decode(model.privateKey, Base64.NO_WRAP)
//                                val keyFactory = KeyFactory.getInstance("RSA")
//                                val privateKeySpec = PKCS8EncodedKeySpec(binCpk)
//                                val privateKey = keyFactory.generatePrivate(privateKeySpec)
//
//                                Log.i("chains", model.chains!!)
//                                alertImport(
//                                    ExtrackP12(
//                                        model.csrName,
//                                        model.chains!!,
//                                        model.csrKey!!,
//                                        privateKey
//                                    )
//                                )
//                            }
//
//                        }
//                    })
//            )
            btnGenCsr.setOnClickListener {
                val action =
                    CsrListFragmentDirections.nextActionToCsr()
                findNavController().navigate(action)
            }

            btnUpload.setOnClickListener {
//                val action =
//                    CsrListFragmentDirections.nextActionToWeb()
//                findNavController().navigate(action)

                inetIntent(Constants.INET_URL)

            }

        }

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


    fun initActionBar() {
        viewBinding.actionBar.tvTitle.setText("รายการ CSR")
        viewBinding.actionBar.btnBack.setOnClickListener {
            val ac = activity as MainActivity
            ac.onBackPressed()
        }
    }

    fun getCsrAll() {

        viewModel.csrLiveData.observe(viewLifecycleOwner, Observer {
            Log.i("size", it.size.toString())
            adapterCsr.submitList(it)
            adapterCsr.notifyDataSetChanged()
        })
        viewModel.getCsrAll()

    }

    fun import(file: String) {
//        viewModel.extrackP12LiveData.observe(this@CsrListFragment, Observer {
//            saveData(it.name, it)
//        })
//        viewModel.csrLiveData2.observe(this@CsrListFragment, Observer {
//            it.csrKey = "complete"
//            lifecycleScope.launch(Dispatchers.IO) {
//                viewModel.updateStatus(it)
//            }
//        })
//        viewModel.databaseSuccess.observe(this@CsrListFragment, Observer {
//            adapterCsr.notifyDataSetChanged()
//        })
        viewModel.certUpdateLiveData.observe(this, Observer {
            getCsrAll()
        })
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.importCrt(file)
        }

    }


    fun saveData(name: String, extrackP12: ExtrackP12) {

        val privKeyBytes: ByteArray? = extrackP12.privateKey?.encoded
        val privKeyStr = String(Base64.encode(privKeyBytes, Base64.NO_WRAP))

        var allowBio = 0
        if (android.os.Build.VERSION.SDK_INT == 28 || android.os.Build.VERSION.SDK_INT == 29) {
            allowBio = BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        } else {
            allowBio = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        }

        BiometricEncryptedSharedPreferences.create(
            this,
            Constants.FileName,
            1,
            BiometricPrompt.PromptInfo.Builder().setTitle(getString(R.string.app_name))
                .setAllowedAuthenticators(
                    allowBio
                ).build()
        ).observe(this, Observer { it: SharedPreferences? ->
            if (it != null) {
                it.edit().putString(name, privKeyStr).apply()


                viewModelImport.addCertificate(
                    Certificate(
                        name,
                        extrackP12.cert,
                        extrackP12.chains,
                        UtilApps.currentDate()
                    )
                )
                alertBox()
            }

        })


    }

    fun importChain(file: String) {
        viewModel.chainsUpdateLiveData.observe(this, Observer {
            getCsrAll()
        })
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.importChains(file)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                var crtPath = data.getStringExtra("path")
                if (crtPath != null && crtPath.endsWith(".cer")) {
                    import(crtPath)
                } else if (crtPath != null && crtPath.endsWith(".p7b")) {
                    importChain(crtPath)
                }
            }
        }
    }

    private fun alertBox() {
        UtilApps.hideSoftKeyboard(requireCompatActivity())
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


        val yesBtn = dialog.findViewById(R.id.btn_positive) as MaterialButton
        yesBtn.setOnClickListener {
            dialog.dismiss()

        }

        dialog.show()
    }

    private fun alertImport(data: ExtrackP12) {
        UtilApps.hideSoftKeyboard(requireCompatActivity())
        val dialog = Dialog(requireCompatActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_save_p12)
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
            saveData(data.name, data)
        }
        val noBtn = dialog.findViewById(R.id.btn_negative) as MaterialButton
        noBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun alertDelete(item: Csr) {
        UtilApps.hideSoftKeyboard(requireCompatActivity())
        val dialog = Dialog(requireCompatActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete)
        dialog.getWindow()
            ?.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.transparent)));
        dialog.getWindow()?.setLayout(
            ((UtilApps.getScreenWidth(getActivity()) * .9).toInt()),
            ViewGroup.LayoutParams.WRAP_CONTENT
        );

        dialog.setCancelable(false)


        val yesBtn = dialog.findViewById(R.id.btn_positive) as MaterialButton
        val noBtn = dialog.findViewById(R.id.btn_negative) as MaterialButton
        yesBtn.setOnClickListener {
            dialog.dismiss()
            deleteCsr(item)
        }
        noBtn.setOnClickListener {
            dialog.dismiss()

        }

        dialog.show()
    }

    fun deleteCsr(item: Csr) {

        viewModel.deleteCsr(item)
        viewModel.databaseSuccess.observe(this, Observer {
            getCsrAll()
        })
    }

}