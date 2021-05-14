package th.or.etda.teda.mobile.data

import androidx.room.*

@Dao
interface CertificateDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCa(vararg certificate: Certificate)

    @Query("SELECT * FROM cert_table WHERE cert_name LIKE :certName LIMIT 1")
    suspend fun findCertificate(certName: String): Certificate

    @Query("SELECT * FROM cert_table")
    suspend fun findCertificateAll(): List<Certificate>

//    @Delete
//    suspend fun delete(certificate: Certificate)

    @Query("DELETE FROM cert_table WHERE cert_name = :certName")
    suspend fun delete(certName: String)
}