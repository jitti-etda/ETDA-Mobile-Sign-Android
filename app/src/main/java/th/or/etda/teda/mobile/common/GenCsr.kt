package th.or.etda.teda.mobile.common

import android.content.Context
import android.security.keystore.KeyProperties
import android.util.Log
import org.spongycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.spongycastle.asn1.x500.X500Name
import org.spongycastle.asn1.x500.style.BCStyle
import org.spongycastle.asn1.x509.BasicConstraints
import org.spongycastle.asn1.x509.Extension
import org.spongycastle.asn1.x509.ExtensionsGenerator
import org.spongycastle.operator.OperatorCreationException
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder
import org.spongycastle.pkcs.PKCS10CertificationRequest
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import org.spongycastle.util.io.pem.PemObject
import org.spongycastle.util.io.pem.PemReader
import org.spongycastle.util.io.pem.PemWriter
import th.or.etda.teda.mobile.ui.csr.CsrTemp
import th.or.etda.teda.mobile.util.Constants
import java.io.*
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.regex.Pattern


object GenCsr {
    private const val DEFAULT_KEY_LENGTH_BITS = 2048

    fun createKeypair(): KeyPair {
//        val keyGen: KeyPairGenerator = KeyPairGenerator.getInstance(
//            KeyProperties.KEY_ALGORITHM_RSA,
//            "AndroidKeyStore"
//        ) // store the key in the Android KeyStore for security purposes
//
//        keyGen.initialize(
//            KeyGenParameterSpec.Builder(
//                Constants.ALIAS,
//                KeyProperties.PURPOSE_SIGN
//            )
//                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
//                .setDigests(
//                    KeyProperties.DIGEST_SHA256,
//                    KeyProperties.DIGEST_SHA384,
//                    KeyProperties.DIGEST_SHA512
//                )
//                .build()
//        ) // defaults to RSA 2048
//        return keyGen.generateKeyPair()


        // Generate an RSA key pair.
        val keypair = try {

            val keyGen = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
            keyGen.initialize(DEFAULT_KEY_LENGTH_BITS, SecureRandom())
            keyGen.generateKeyPair()
        } catch (e: NoSuchAlgorithmException) {
            // Should not reach here because every Java implementation must have RSA key pair generator.
            throw Error(e)
        }
        return keypair
    }

    //    private const val CN_PATTERN = "CN=%s, O=Etda, OU=Etda"
    private const val DEFAULT_RSA_SIGNATURE_ALGORITHM = "sha256WithRSAEncryption"
    var privateKey = "";
    val pvTest = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCicjfDksewbn4hwSGFr8B6gwdr" +
            "yRCgO8czEcTSMKDZ9HMfvQFiDV/v7l5JrxnewJGB2W4yPPdsbsH3mQjIgXHJrR4ykRxFRTgdhZQO" +
            "fJ+MVA3q1UneA7y3UkHpOtDK9GUWaKUHJiT+n2UR0sgtEz8EJVD2RLCw/F7Ege0y1BLvYOwAoASp" +
            "RuMMIZ9VhxpHriayOfk6Gr6A27iyUyLrxWOtqW7Oo/SHjgas++heLeXj/yo9nkIWR6AJBrgFSeay" +
            "cnUj72O0ua9WwGgvQi6OuAjI0Ynce3aMM6Z3pwSNXPHerelir30f7sw1WkgnlnTnOxpMkLzYPmxK" +
            "VgS2Cqud50ZjAgMBAAECggEAUD9qSsPcv8ylpJkarUb4J81IwjdObkl5i0Hw4ADDxUfcA0bEQyLm" +
            "TMmNbIsClaa9es7lgA3cEIXabiz8SMihZIaPz10eAR5DrVnKnSl1S8ZOJb79wZ5JAKqzArtaX5hB" +
            "w8MoGxL93YbyT863soS6cZrZlv6x5oyVaC/i9GQNgpCuFGEs+ClnaJ9Jcdm4dScEvaHMWLpS0Az4" +
            "yJqaSXO4foPzq4wmOSe/m3uvisS0Dc9zlba7vUMlFoo44sVs5Cq8XEoiUDZqYcqDKtZ4LR/tvnHE" +
            "9ZEd6YkS52XAJc6xIhroX2/m4gysK4MbEYhTccyDQlQMVHYmaTx3Jj2oiSUh2QKBgQDR7/9t9YqS" +
            "lF08nV+1n7LONXHl7Iel7tVMtFXYH2Yb7vpuEILZj/4UZrYWtZW8INgSgSqsNBtnyFu7pgAB+3NZ" +
            "LNwv08s0zG3GEU5e30iZg5CEv4B3SMa1YF0EmTXBkdlIYog8uli80nCdYb4rKSulNehnZaxjN8DJ" +
            "kPFegF7CjwKBgQDGFqylBQUi6dlCrYBbLydqAUPDGkhFwgQzgWgCrNJlSu63/8kNEkoQL6XEMlqj" +
            "NwXW2ORE87JBFGZzF8/QkfYlY/aIpoOpY8BrRuAvZh4d3I6KUUlxJ84quUwbVRkpWeatdbepI0VX" +
            "HMO9KQYCKgsVi6DbLCDLpTWq9wIsjfRY7QKBgAQ8HSDwtQzj5UWVvxqA0DCMr3F95faY7MCRWrHz" +
            "YDHsDi0uNBWDbBKpR78Jhq220+T7qgqzOmJLjl4oqRNhKSCBYSet1AmKniRCsRwE0QgXuVCUNFkz" +
            "pJ6ABSBUntr6wDFPm4PuFmAotpDWKeng+Lpqbfe3+1Q9CayHOfc4C7FNAoGBAKmx3P6VOVw6dsuf" +
            "NLlowNwX70PhbQ6Ncenprv+zq53ovm5UKpkPFPGez+QuuuTdixwNvCexmQAViNqKCJaFpBdN9nIE" +
            "LABdmwaLthwE4EH2owmtLzxHvF9vU9qKYT7CbFIm6Up1E77Xvnt6/FG4ULyJXCjGOeYbRsmKR4uB" +
            "pFTVAoGAXy4Io07eyzm4aG7cQDV3Sy1bIMSQC83ApoDKggHV2JQ2I+xpYVPm65XQ5zlwgqOc+E7B" +
            "1GC4apu+d1R790CH7o8nylVxAduW1p31fiM0e+WSYCbIh3DP2IIh1oeWmRMKK+Y5cs/D6kBlem1b" +
            "ByBZ9byMkhRa38Wv9znlqtWtVng="

    val pbTest = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAonI3w5LHsG5+IcEhha/A" +
            "eoMHa8kQoDvHMxHE0jCg2fRzH70BYg1f7+5eSa8Z3sCRgdluMjz3bG7B95kIyIFx" +
            "ya0eMpEcRUU4HYWUDnyfjFQN6tVJ3gO8t1JB6TrQyvRlFmilByYk/p9lEdLILRM/" +
            "BCVQ9kSwsPxexIHtMtQS72DsAKAEqUbjDCGfVYcaR64msjn5Ohq+gNu4slMi68Vj" +
            "raluzqP0h44GrPvoXi3l4/8qPZ5CFkegCQa4BUnmsnJ1I+9jtLmvVsBoL0IujrgI" +
            "yNGJ3Ht2jDOmd6cEjVzx3q3pYq99H+7MNVpIJ5Z05zsaTJC82D5sSlYEtgqrnedG" +
            "YwIDAQAB"

    @Throws(IOException::class, OperatorCreationException::class)
    fun generateCSR(
        pbKey: PublicKey,
        pvKey: PrivateKey,
        cn: String?,
        organize: String,
        unit: String,
        state: String,
        city: String,
        country: String,
        email: String
    ): PKCS10CertificationRequest? {
        val principal =
            "CN=$cn, O=$organize, OU=$unit ," + BCStyle.EmailAddress + "=$email," + BCStyle.C + "=$country," + BCStyle.L + "=$city," + BCStyle.ST + "=$state"
//        val principal = String.format(cn_pattern, cn)


        val signer =
            JcaContentSignerBuilder(DEFAULT_RSA_SIGNATURE_ALGORITHM).build(pvKey)
        val csrBuilder: PKCS10CertificationRequestBuilder = JcaPKCS10CertificationRequestBuilder(
            X500Name(principal), pbKey
        )
        val extensionsGenerator = ExtensionsGenerator()
        extensionsGenerator.addExtension(
            org.spongycastle.asn1.x509.Extension.basicConstraints, true, BasicConstraints(
                true
            )
        )
        csrBuilder.addAttribute(
            PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
            extensionsGenerator.generate()
        )

        return csrBuilder.build(signer)
    }

    fun writeData(
        context: Context,
        name: String,
        organize: String,
        unit: String,
        state: String,
        city: String,
        country: String,
        email: String
    ): CsrTemp {
        val keyPair = createKeypair()

//        val binCpk: ByteArray = android.util.Base64.decode(pvTest, android.util.Base64.NO_WRAP)
//        val keyFactory = KeyFactory.getInstance("RSA")
//        val privateKeySpec = PKCS8EncodedKeySpec(binCpk)
//        var privateKey = keyFactory.generatePrivate(privateKeySpec)
//
//        val publicBytes: ByteArray = android.util.Base64.decode(pbTest, android.util.Base64.NO_WRAP)
//        val keySpec = X509EncodedKeySpec(publicBytes)
//        val pubKey = keyFactory.generatePublic(keySpec)


        val CSRder = generateCSR(keyPair.public,keyPair.private, name, organize, unit, state, city, country, email)
        val key = keyPair.private
//        privateKey = android.util.Base64.encodeToString(key.encoded, android.util.Base64.DEFAULT)


        val writer = StringWriter()
        val pemWriter = PemWriter(writer)
        pemWriter.writeObject(PemObject("CERTIFICATE REQUEST", CSRder?.encoded))
        pemWriter.flush()
        pemWriter.close()
        val csrPEM: String = writer.toString()
//        Log.i("csr", csrPEM);
//        val folder = context.getExternalFilesDir(Constants.FolderCsr)
        getExternalStorageDirectory().let {
            val f = File(it, Constants.Folder)
            f.mkdir()
            val fileStoreEncrypt = File(
                f,
                "$name.csr"
            )
            writeToFile(csrPEM, fileStoreEncrypt)
            return CsrTemp(fileStoreEncrypt.absolutePath,key)
        }
//        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

//        val fileStoreEncrypt = File(
//            folder,
//            "$name.csr"
//        )
//        writeToFile(csrPEM, fileStoreEncrypt)
//        return key
    }

    private fun writeToFile(data: String, file: File): Boolean {
        val stream = FileOutputStream(file)
        try {
            stream.write(data.toByteArray())

        } finally {
            stream.close()
        }
        return true
    }

    private val EXTERNAL_STORAGE_DIRECTORY = getDirectory("EXTERNAL_STORAGE", "/sdcard")

    fun getDirectory(variableName: String?, defaultPath: String?): File {
        val path = System.getenv(variableName)
        return if (path == null) File(defaultPath) else File(path)
    }


    fun getExternalStorageDirectory(): File? {
        return EXTERNAL_STORAGE_DIRECTORY
    }

    fun importCrt(fileCrt: File) {
//        var fileCrt = File("/storage/emulated/0/Download/inetCert.cer")
        val fileInputStream = FileInputStream(fileCrt)
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val caInput: InputStream = BufferedInputStream(fileInputStream)
        val ca: Certificate
        try {
            ca = cf.generateCertificate(caInput)
//            System.out.println("ca=" + (ca as X509Certificate).getSubjectDN())
        } finally {
            caInput.close()
        }

        val keyStoreType: String = KeyStore.getDefaultType()
        val keyStore: KeyStore = KeyStore.getInstance(keyStoreType)
        keyStore.load(null, null)
        keyStore.setCertificateEntry("ca", ca)

    }


    private val HEADER: ByteArray = "-----".toByteArray(StandardCharsets.US_ASCII)

    @Throws(IOException::class, GeneralSecurityException::class)
//    fun createIdentityStore(certificate: Path?, key: File?, keystore: Path?, password: CharArray?) {
//
//        val cf = CertificateFactory.getInstance("X.509")
//        var pub: Certificate
//        Files.newInputStream(certificate).use { `is` -> pub = cf.generateCertificate(`is`) }
//
////        val pkcs8 = decode(Files.readAllBytes(key?.toPath()))
////        Security.addProvider(BouncyCastleProvider());
////        val pemReader: Reader = BufferedReader(InputStreamReader(key?.inputStream()))
////        val pemParser = PEMParser(pemReader)
////        val binCpk = key?.let { read(it) }
////        val key = pemParser.readObject() as PKCS8EncryptedPrivateKeyInfo
//
//        val keyFactory = KeyFactory.getInstance("RSA")
//        val privateKeySpec = PKCS8EncodedKeySpec(binCpk)
//        var privateKey = keyFactory.generatePrivate(privateKeySpec)
//
////        val kf: KeyFactory = KeyFactory.getInstance("RSA")
////        val pvt: PrivateKey = kf.generatePrivate(PKCS8EncodedKeySpec(a))
//        val pkcs12 = KeyStore.getInstance("PKCS12")
//        pkcs12.load(null, null)
//        pkcs12.setKeyEntry("identity", privateKey, password, arrayOf(pub))
//
//        Files.newOutputStream(keystore, StandardOpenOption.CREATE_NEW).use { s ->
//            pkcs12.store(
//                s,
//                password
//            )
//        }
//    }

    fun getPrivatekey(file: File): PrivateKey? {


//            byte[] encoded = Base64.decodeBase64(number);
//            ImportEncryptedPrivateKey.read(encoded);
        val pem = PemReader(StringReader(read(file))).readPemObject()
        val der = pem.content

        Log.i("PEM", pem.type)
        var key = KeyFactory.getInstance("RSA").generatePrivate(
            PKCS8EncodedKeySpec(der)
        )
        return key
    }


    fun read(file: File): String {
        val text = StringBuilder()

        try {
            val br = BufferedReader(FileReader(file))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                text.append(line)
                text.append('\n')
            }
            br.close()
        } catch (e: IOException) {
            //You'll need to add proper error handling here
        }

//        Log.i("pem",text.toString())
//        var a = text.toString().replace("-----BEGIN ENCRYPTED PRIVATE KEY-----","")
//        a = a.replace("-----END ENCRYPTED PRIVATE KEY-----","")
//        Log.i("pem",a)
//        val decodedValue: ByteArray =
//            android.util.Base64.decode(a.trim().toByteArray(), android.util.Base64.NO_WRAP)
        return text.toString()
    }


    @Throws(IOException::class)
    private fun pemFileToBytes(filename: File): ByteArray? {
        // read in PEM file, throw away the begin and end lines
        val pemLines = Files.readAllLines(filename.toPath(), StandardCharsets.US_ASCII)
        pemLines.removeAt(0)
        pemLines.removeAt(pemLines.size - 1)
        val pem = java.lang.String.join("", pemLines)

        // base64 decode and return the result.
        return Base64.getDecoder().decode(pem)
    }

    private fun decode(raw: ByteArray): ByteArray {
        if (!Arrays.equals(Arrays.copyOfRange(raw, 0, HEADER.size), HEADER)) return raw
        val pem: CharBuffer = StandardCharsets.US_ASCII.decode(ByteBuffer.wrap(raw))
        val lines: Array<String> = Pattern.compile("\\R").split(pem)
        val body: Array<String> = Arrays.copyOfRange(lines, 1, lines.size - 1)
        return Base64.getDecoder().decode(java.lang.String.join("", *body))
    }
}
