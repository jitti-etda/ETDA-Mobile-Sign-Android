package th.or.etda.teda.mobile.extract

import android.os.Bundle
import th.or.etda.teda.mobile.BaseActivityDemo
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.databinding.ActivityDeepLinkBinding

class DeepLinkActivity : BaseActivityDemo() {

    private val viewBinding : ActivityDeepLinkBinding by binding(R.layout.activity_deep_link)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)


//        val filePath = File(intent.data?.path)
    }
}


