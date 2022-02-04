package th.or.etda.teda.mobile.extract

import android.content.Intent
import android.os.Bundle
import th.or.etda.teda.mobile.BaseActivityDemo
import th.or.etda.teda.mobile.MainActivity
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.databinding.ActivityDeepLinkBinding
import th.or.etda.teda.mobile.databinding.ActivityMainBinding
import th.or.etda.teda.ui.base.BaseActivity

class DeepLinkActivity : BaseActivity<ActivityDeepLinkBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent.data
        if (data != null) {
            val intent = Intent(this, MainActivity::class.java)
//            intent.putExtra("data", data);
            intent.setData(data)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

            intent.replaceExtras(Bundle())
            intent.action = ""
            intent.data = null
            intent.flags = 0
        }
        finish()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_deep_link
    }

    override fun onViewReady(savedInstance: Bundle?) {


    }
}


