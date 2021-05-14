/**
 * Copyright (C) 2020 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package th.or.etda.teda.mobile

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import th.or.etda.teda.mobile.ui.backupkey.googledrive.AESCrypt
import java.security.Security





class SigningTest {

//    private lateinit var signingApi: SigningApi
//
//    private val context = context()

//    @MockK
//    private lateinit var moviesRepository: SigningRepository

    @Before
    fun setUp() {
        Security.insertProviderAt(BouncyCastleProvider(), 1)
//        moviesRepository = SigningRepository(signingApi, context)
//        every { moviesRepository.signingSign("","","","") } returns Right(listOf(Movie.empty))
    }

//    @Test
//    fun `should get data from repository`() {
////        runBlocking { getMovies.run(UseCase.None()) }
////
////        verify(exactly = 1) { moviesRepository.movies() }
//    }

    @Test
    fun `encrypt AES`() {
        var aes = AESCrypt("1234")
        var res = aes.encrypt("hello".toByteArray())

        return Assert.assertEquals("pokpok", res)
//        runBlocking { getMovies.run(UseCase.None()) }
//
//        verify(exactly = 1) { moviesRepository.movies() }
    }
}
