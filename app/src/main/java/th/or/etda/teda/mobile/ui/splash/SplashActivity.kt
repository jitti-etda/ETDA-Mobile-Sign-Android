package th.or.etda.teda.mobile.ui.splash

import android.content.Intent
import android.os.Bundle
import th.or.etda.teda.mobile.MainActivity
import th.or.etda.teda.mobile.MainActivity2
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.databinding.ActivityMainBinding
import th.or.etda.teda.mobile.databinding.ActivitySplashBinding
import th.or.etda.teda.ui.base.BaseActivity
import java.util.*


class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    private var timerTask: TimerTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun onViewReady(savedInstance: Bundle?) {

        timerTask = object : TimerTask() {
            override fun run() {
                val `in` = Intent(this@SplashActivity, MainActivity2::class.java)
                startActivity(`in`)
                finish()
            }
        }


        Timer().schedule(timerTask, 2000)

    }


    override fun onDestroy() {
        super.onDestroy()
        timerTask!!.cancel()
    }


}