package th.or.etda.teda.mobile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import th.or.etda.teda.mobile.common.GenCsr
import th.or.etda.teda.mobile.databinding.ActivityMainBinding
import th.or.etda.teda.ui.base.BaseActivity


class MainActivity : BaseActivity<ActivityMainBinding>() {
    var myUri:Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

         myUri = intent.data

    }

    fun deeplink(): Uri? {
        return myUri
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onViewReady(savedInstance: Bundle?) {

    }


}