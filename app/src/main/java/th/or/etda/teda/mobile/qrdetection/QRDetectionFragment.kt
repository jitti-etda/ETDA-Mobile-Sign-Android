package th.or.etda.teda.mobile.qrdetection

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import th.or.etda.teda.mobile.R

class QRDetectionFragment : Fragment() {

    companion object {
        fun newInstance() = QRDetectionFragment()
    }

    private lateinit var viewModel: QRDetectionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.qrdetection_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(QRDetectionViewModel::class.java)
        // TODO: Use the ViewModel
    }

}