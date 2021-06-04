package th.or.etda.teda.mobile.ui.sign.detail

import androidx.navigation.fragment.findNavController
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.databinding.SignDetailFragmentBinding
import th.or.etda.teda.mobile.util.UtilApps
import th.or.etda.teda.ui.base.BaseFragment


class SignDetailFragment : BaseFragment<SignDetailFragmentBinding>(
    layoutId = R.layout.sign_detail_fragment
) {






    override fun onInitDependencyInjection() {

    }

    override fun onInitDataBinding() {
        var signInfo = arguments?.let {
            SignDetailFragmentArgs.fromBundle(it).signInfo
        }
//        viewModel = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(HomeViewModel::class.java)
//        val viewModel: HomeViewModel by viewModels { HomeViewModelFactory(getApplication(), "my awesome param") }

//        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
//        viewBinding.viewModel = viewModel
//        val binding = HomeFragmentBinding.inflate(inflater, container, false)


        viewBinding.apply {

            tvName.setText(signInfo?.description)
            tvDate.setText(UtilApps.currentDate())

            btnAccept.setOnClickListener {
                val action = SignDetailFragmentDirections.nextActionToFirst()
                findNavController().navigate(action)
            }
            btnClose.setOnClickListener {
                val action = SignDetailFragmentDirections.nextActionToFirst()
                findNavController().navigate(action)
            }

        }

    }


}