package th.or.etda.teda.mobile.data

import androidx.annotation.WorkerThread

class CertificateRepository(val certificateDao: CertificateDao) {
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(certificate: Certificate) {
        try {
            certificateDao.insertCa(certificate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getCert(certName: String): Certificate? {
        try {
            return certificateDao.findCertificate(certName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getCertAll(): List<Certificate> {
        try {
            return certificateDao.findCertificateAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteCert(certificate: Certificate): Boolean {
        try {
            certificateDao.delete(certificate.certName)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}