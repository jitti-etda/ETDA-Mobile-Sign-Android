package th.or.etda.teda.mobile.ui.hashdetail

import androidx.navigation.fragment.navArgs
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.databinding.HashdetailFragmentBinding
import th.or.etda.teda.ui.base.BaseFragment

class HashDetailFragment : BaseFragment<HashdetailFragmentBinding>(
    layoutId = R.layout.hashdetail_fragment
) {

    companion object {
        fun newInstance() = HashDetailFragment()
    }

    private lateinit var viewModel: HashDetailViewModel


    override fun onInitDependencyInjection() {

    }

    override fun onInitDataBinding() {
        val safeArgs: HashDetailFragmentArgs by navArgs()
        println("Url => ${safeArgs.qrResult}")
//        viewBinding.result.text = safeArgs.qrResult
    }

}