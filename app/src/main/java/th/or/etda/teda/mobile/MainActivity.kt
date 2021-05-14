package th.or.etda.teda.mobile

import android.app.AlertDialog
import android.os.Bundle
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
import org.koin.android.viewmodel.ext.android.viewModel
import th.or.etda.teda.mobile.common.BiometricEncryptedSharedPreferences
import th.or.etda.teda.mobile.databinding.ActivityMainBinding
import th.or.etda.teda.mobile.ui.importkey.ImportKeyViewModel
import th.or.etda.teda.ui.base.BaseActivity


class MainActivity : BaseActivity<ActivityMainBinding>() {
    //
//    private val viewBinding: ActivityMainBinding by binding(R.layout.activity_main)
//
//    private lateinit var privateKey: PrivateKey
//    private lateinit var publicKey: PublicKey
//    private val message: String ="Message"
//    private lateinit var signMessage: String
//    private lateinit var signWithKeyStore: String
//
//    companion object {
//        const val SIGN_ALGORITHM = "MD5WithRSA"
//        const val ANDROID_KEY_STORE = "AndroidKeyStore"
//        const val ALIAS = "TEDA_KEY"
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//
////
//
//
//    }
//
//    private fun addCertificateToKeyStore(c: X509Certificate) {
//        try {
//            val ks = KeyStore.getInstance("AndroidKeyStore")
//            ks.load(null)
//            ks.setCertificateEntry(ALIAS, c)
//            println("done")
//        } catch (e: Exception){
//            e.printStackTrace()
//        }
//
//    }
    val viewModel: ImportKeyViewModel by viewModel()
    lateinit var navView: NavigationView

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onViewReady(savedInstance: Bundle?) {

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
//        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        navView = binding.navView
//        val navView: NavigationView = findViewById(R.id.nav_view)
        val navFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_import, R.id.nav_restore, R.id.nav_sign
            ), drawerLayout
        )

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,R.string.open,R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        checkMenu()


    }


    fun checkMenu(){
        if(!BiometricEncryptedSharedPreferences.checkBio(this)){
            val menuView: Menu = navView.getMenu()
            menuView.getItem(0).isEnabled = false
            menuView.getItem(1).isEnabled = false
            menuView.getItem(2).isEnabled = false
            alertFingerDialog()
        }else{
            getCertAll(navView)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


    fun getCertAll(navView: NavigationView) {
        viewModel.getCertificateAll().observe(this, Observer {
            val menuView: Menu = navView.menu
            menuView.getItem(2).isEnabled = it.isNotEmpty()

        })
    }

    private fun alertFingerDialog() {
        AlertDialog.Builder(this)
            .setMessage("Please set Fingerprint")
            .setCancelable(false)
            .setPositiveButton("Close") { dialog, which ->

                finish()

            }.show()
    }

}