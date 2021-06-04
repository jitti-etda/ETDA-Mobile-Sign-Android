package th.or.etda.teda.mobile

import android.app.AlertDialog
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.common.io.BaseEncoding.base64
import io.sentry.Sentry
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.common.BiometricEncryptedSharedPreferences
import th.or.etda.teda.mobile.common.CryptLib
import th.or.etda.teda.mobile.databinding.ActivityMain2Binding
import th.or.etda.teda.mobile.databinding.ActivityMainBinding
import th.or.etda.teda.mobile.ui.importkey.ImportHelper
import th.or.etda.teda.mobile.ui.importkey.ImportKeyViewModel
import th.or.etda.teda.mobile.util.Constants
import th.or.etda.teda.ui.base.BaseActivity
import java.io.File


class MainActivity2 : BaseActivity<ActivityMain2Binding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main2
    }

    override fun onViewReady(savedInstance: Bundle?) {

    }


}