package th.or.etda.teda.mobile.data.csr

import androidx.room.*
import androidx.room.FtsOptions.Order




@Dao
interface CsrDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCa(vararg certificate: Csr)

    @Query("SELECT * FROM csr_table WHERE csr_name LIKE :csrName LIMIT 1")
    suspend fun findCertificate(csrName: String): Csr

    @Query("SELECT * FROM csr_table")
    suspend fun findCertificateAll(): List<Csr>

//    @Delete
//    suspend fun delete(certificate: Certificate)

    @Query("DELETE FROM csr_table WHERE csr_name = :csrName")
    suspend fun delete(csrName: String)

    @Update
    fun update(certificate: Csr)
}