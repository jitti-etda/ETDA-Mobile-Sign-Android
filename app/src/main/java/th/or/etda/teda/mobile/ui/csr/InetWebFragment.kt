package th.or.etda.teda.mobile.ui.csr

import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.fragment.app.setFragmentResultListener
import th.or.etda.teda.mobile.MainActivity
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.databinding.InetWebFragmentBinding
import th.or.etda.teda.mobile.ui.csr.directory.DirectoryCsrFragment
import th.or.etda.teda.mobile.ui.csr.directory.DirectoryCsrActivity
import th.or.etda.teda.mobile.util.Constants
import th.or.etda.teda.ui.base.BaseFragment
import java.io.File

import org.koin.android.viewmodel.ext.android.viewModel

import android.content.Context.DOWNLOAD_SERVICE
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat

import androidx.core.content.ContextCompat.getSystemService


class InetWebFragment : BaseFragment<InetWebFragmentBinding>(
    layoutId = R.layout.inet_web_fragment
) {


    val INPUT_FILE_REQUEST_CODE = 1
    val PICK_FILE = 1212

    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null

    private val viewModel: InetWebViewModel by viewModel()

    override fun onInitDependencyInjection() {

    }


    override fun onInitDataBinding() {

        initActionBar()

        viewBinding.apply {
            progressBar.visibility = View.VISIBLE
            web.settings.javaScriptEnabled = true
            web.settings.builtInZoomControls = false
            web.settings.displayZoomControls = false
            web.settings.domStorageEnabled = true
            web.settings.cacheMode = WebSettings.LOAD_NO_CACHE
            web.settings.databaseEnabled = true
            web.settings.loadWithOverviewMode = true
            web.settings.useWideViewPort = false
            web.loadUrl(Constants.INET_URL)
            web.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    progressBar.visibility = View.GONE


                }

            }
            web.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    Log.d(
                        "MyApplication", consoleMessage.message() + " -- From line "
                                + consoleMessage.lineNumber() + " of "
                                + consoleMessage.sourceId()
                    )
                    return super.onConsoleMessage(consoleMessage)
                }


                override fun onShowFileChooser(
                    webView: WebView,
                    filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams
                ): Boolean {
                    mFilePathCallback?.onReceiveValue(null)
                    mFilePathCallback = filePathCallback
//                    val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
//                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
//                    contentSelectionIntent.type = "*/*"
//                    val intentArray: Array<Intent?> = arrayOfNulls(0)
//                    val chooserIntent = Intent(Intent.ACTION_CHOOSER)
//                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
//                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "เลือกไฟล์ CSR")
//                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
//                    startActivityForResult(
//                        chooserIntent,
//                        INPUT_FILE_REQUEST_CODE
//                    )

                    val intent = Intent(requireCompatActivity(), DirectoryCsrActivity::class.java)
                    startActivityForResult(intent, PICK_FILE)

//                    val action =
//                        InetWebFragmentDirections.nextActionToPickFile()
//                    findNavController().navigate(action)
                    return true
                }
            }
            web.setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->

                if (url.startsWith("http")) {
                    viewModel.downloadFile(
                        requireContext(),
                        url,
                        contentDisposition,
                        mimetype,
                        userAgent
                    )
                } else {
                    val name = viewModel.writeFileCer(url)
                    if (name.isNotEmpty()) {
                        Toast.makeText(requireContext(), "Download "+name + " success", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

//                try {
//                    val i = Intent(Intent.ACTION_VIEW)
//                    i.data = Uri.parse(url)
//                    startActivity(i)
//                }catch (e:Exception){
//                    UtilApps.alertDialog(requireCompatActivity(),"Test")
//                }

            })

        }

        setFragmentResultListener(DirectoryCsrFragment.REQUEST_KEY) { key, bundle ->
            // read from the bundle
            var path = bundle.getString("path")
//            var file = File(path)
            val results = arrayOf(Uri.parse(path))
            mFilePathCallback?.onReceiveValue(results)
            mFilePathCallback = null
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                val dataString = data?.getStringExtra("path")

                if (dataString != null) {
                    val results = arrayOf(Uri.fromFile(File(dataString)))
                    mFilePathCallback?.onReceiveValue(results)
                    mFilePathCallback = null
                }
            }
        } else {
            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data)
                return
            }

            if (resultCode == Activity.RESULT_OK) {
                val dataString = data?.dataString
                if (dataString != null) {
                    val results = arrayOf(Uri.parse(dataString))
                    mFilePathCallback?.onReceiveValue(results)
                    mFilePathCallback = null
                }
            }
        }




        return
    }


    fun initActionBar() {
        viewBinding.actionBar.tvTitle.text = "Upload CSR"
        viewBinding.actionBar.btnBack.setOnClickListener {
            val ac = activity as MainActivity
            ac.onBackPressed()
        }
    }


}