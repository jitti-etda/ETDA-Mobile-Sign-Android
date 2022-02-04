package th.or.etda.teda.mobile.ui.csr.directory

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.MainActivity
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.common.RecyclerItemClickListener
import th.or.etda.teda.mobile.databinding.DirectoryCsrFragmentBinding
import th.or.etda.teda.ui.base.BaseActivity


class DirectoryCsrActivity : BaseActivity<DirectoryCsrFragmentBinding>() {


    private lateinit var adapterDirectory: DirectoryCsrAdapter
    private val viewModel: DirectoryCsrViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }


    override fun getLayoutId(): Int {
        return R.layout.directory_csr_fragment
    }

    override fun onViewReady(savedInstance: Bundle?) {


        var isCer = intent.getBooleanExtra("isCer", false)
        var type = intent.getStringExtra("type")

        adapterDirectory = DirectoryCsrAdapter()

        binding.apply {
            recyclerView.adapter = adapterDirectory
            recyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                    this@DirectoryCsrActivity,
                    recyclerView,
                    object : RecyclerItemClickListener.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {
//                        val action =
//                            DirectoryCsrFragmentDirections.nextActionImportPassword(
//                                adapterDirectory.currentList[position].path,
//                                false
//                            )
//                        findNavController().navigate(action)
//                            setFragmentResult(
//                                DirectoryCsrFragment.REQUEST_KEY,
//                                bundleOf("path" to adapterDirectory.currentList[position].path)
//                            )
//                            findNavController().navigateUp()

                            val intent = intent
                            intent.putExtra("path", adapterDirectory.currentList[position].path)
                            setResult(RESULT_OK, intent)
                            finish()
                        }

                        override fun onLongItemClick(view: View, position: Int) {

                        }
                    })
            )


        }

        if (isCer) {
            if (type.equals("cer")) {
                initActionBar("เลือกไฟล์ Cer")
                getAllCerFile()
            } else {
                initActionBar("เลือกไฟล์ p7b")
                getAllP7bFile()
            }

        } else {
            initActionBar("เลือกไฟล์ CSR")
            getAllFile()
        }


    }

    fun initActionBar(title: String) {
        binding.actionBar.tvTitle.setText(title)
        binding.actionBar.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getAllFile() {

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.getExternalStorageDirectory(this@DirectoryCsrActivity).let {
                it?.let { it1 ->
                    viewModel.getFile(it1)

                }
            }
        }
        lifecycleScope.launch {
            viewModel.fileListLive.observe(this@DirectoryCsrActivity, Observer {
                adapterDirectory.submitList(it)
                binding.progressBar.visibility = View.GONE
            })
        }


    }

    private fun getAllCerFile() {

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.getExternalStorageDirectoryDownload().let {
                it?.let { it1 ->
                    viewModel.getFileCer(it1)

                }
            }
        }
        lifecycleScope.launch {
            viewModel.fileListLive.observe(this@DirectoryCsrActivity, Observer {
                adapterDirectory.submitList(it)
                binding.progressBar.visibility = View.GONE
            })
        }


    }

    private fun getAllP7bFile() {

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.getExternalStorageDirectoryDownload().let {
                it?.let { it1 ->
                    viewModel.getFileP7b(it1)

                }
            }
        }
        lifecycleScope.launch {
            viewModel.fileListLive.observe(this@DirectoryCsrActivity, Observer {
                adapterDirectory.submitList(it)
                binding.progressBar.visibility = View.GONE
            })
        }


    }


}