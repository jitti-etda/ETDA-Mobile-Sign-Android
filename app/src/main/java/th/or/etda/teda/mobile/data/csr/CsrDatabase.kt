package th.or.etda.teda.mobile.data.csr

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import th.or.etda.teda.mobile.data.CertificateDao

@Database(entities = [Csr::class], version = 6, exportSchema = false)
public abstract class CsrDatabase: RoomDatabase() {
    abstract fun csrDao(): CsrDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: CsrDatabase? = null

        fun getDatabase(context: Context): CsrDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, CsrDatabase::class.java, "csr-database")
//                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }


    }

}