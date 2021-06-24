package th.or.etda.teda.mobile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Certificate::class], version = 12, exportSchema = false)
public abstract class CertificateDatabase: RoomDatabase() {
    abstract fun certificateDao(): CertificateDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: CertificateDatabase? = null

        fun getDatabase(context: Context): CertificateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, CertificateDatabase::class.java, "certificate-database")
//                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }


    }

}