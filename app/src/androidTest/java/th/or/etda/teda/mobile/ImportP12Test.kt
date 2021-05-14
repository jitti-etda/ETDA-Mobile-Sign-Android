package th.or.etda.teda.mobile

import androidx.core.content.ContentProviderCompat.requireContext
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import th.or.etda.teda.mobile.extract.ExtractCAViewModel
import th.or.etda.teda.mobile.ui.importkey.directory.DirectoryViewModel
import java.io.File

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ImportP12Test {

    val viewModel = DirectoryViewModel()
    var path : File?=null


    @Before
    fun createPath() {
        // Context of the app under test.

        path = viewModel.getExternalStorageDirectory()

    }

    @Test
    fun getAllFile_zero() {
        // Context of the app under test.

//        path?.let {
//
//            assertEquals(0, viewModel.getFile(it).size)
//        }

    }

}