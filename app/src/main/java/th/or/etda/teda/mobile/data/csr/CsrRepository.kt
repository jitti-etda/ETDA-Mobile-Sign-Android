package th.or.etda.teda.mobile.data.csr

import androidx.annotation.WorkerThread

class CsrRepository(val csrDao: CsrDao) {
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(csr: Csr) {
        try {
            csrDao.insertCa(csr)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getCsr(csrName: String): Csr? {
        try {
            return csrDao.findCertificate(csrName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getCsrAll(): List<Csr> {
        try {
            return csrDao.findCertificateAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ArrayList()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteCsr(csr: Csr): Boolean {
        try {
            csrDao.delete(csr.csrName)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateCsr(csr: Csr): Boolean {
        try {
            csrDao.update(csr)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}