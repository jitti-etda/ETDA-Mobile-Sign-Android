package th.or.etda.teda.mobile.ui.backupkey.googledrive

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.util.Pair
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.drive.DriveFolder
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.*
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class DriveServiceHelperKT(driveService: Drive?) {


    private val mExecutor: Executor = Executors.newSingleThreadExecutor()
    private var mDriveService: Drive? = driveService

    companion object{
        var TYPE_AUDIO = "application/vnd.google-apps.audio"
        var TYPE_GOOGLE_DOCS = "application/vnd.google-apps.document"
        var TYPE_GOOGLE_DRAWING = "application/vnd.google-apps.drawing"
        var TYPE_GOOGLE_DRIVE_FILE = "application/vnd.google-apps.file"
        var TYPE_GOOGLE_DRIVE_FOLDER = DriveFolder.MIME_TYPE
        var TYPE_GOOGLE_FORMS = "application/vnd.google-apps.form"
        var TYPE_GOOGLE_FUSION_TABLES = "application/vnd.google-apps.fusiontable"
        var TYPE_GOOGLE_MY_MAPS = "application/vnd.google-apps.map"
        var TYPE_PHOTO = "application/vnd.google-apps.photo"
        var TYPE_GOOGLE_SLIDES = "application/vnd.google-apps.presentation"
        var TYPE_GOOGLE_APPS_SCRIPTS = "application/vnd.google-apps.script"
        var TYPE_GOOGLE_SITES = "application/vnd.google-apps.site"
        var TYPE_GOOGLE_SHEETS = "application/vnd.google-apps.spreadsheet"
        var TYPE_UNKNOWN = "application/vnd.google-apps.unknown"
        var TYPE_VIDEO = "application/vnd.google-apps.video"
        var TYPE_3_RD_PARTY_SHORTCUT = "application/vnd.google-apps.drive-sdk"


        var EXPORT_TYPE_HTML = "text/html"
        var EXPORT_TYPE_HTML_ZIPPED = "application/zip"
        var EXPORT_TYPE_PLAIN_TEXT = "text/plain"
        var EXPORT_TYPE_RICH_TEXT = "application/rtf"
        var EXPORT_TYPE_OPEN_OFFICE_DOC = "application/vnd.oasis.opendocument.text"
        var EXPORT_TYPE_PDF = "application/pdf"
        var EXPORT_TYPE_MS_WORD_DOCUMENT =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        var EXPORT_TYPE_EPUB = "application/epub+zip"
        var EXPORT_TYPE_MS_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        var EXPORT_TYPE_OPEN_OFFICE_SHEET = "application/x-vnd.oasis.opendocument.spreadsheet"
        var EXPORT_TYPE_CSV = "text/csv"
        var EXPORT_TYPE_TSV = "text/tab-separated-values"
        var EXPORT_TYPE_JPEG = "application/zip"
        var EXPORT_TYPE_PNG = "image/png"
        var EXPORT_TYPE_SVG = "image/svg+xml"
        var EXPORT_TYPE_MS_POWER_POINT =
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        var EXPORT_TYPE_OPEN_OFFICE_PRESENTATION = "application/vnd.oasis.opendocument.presentation"
        var EXPORT_TYPE_JSON = "application/vnd.google-apps.script+json"

        var DRIVE_APP_NAME = "ETDA"

        fun getGoogleDriveService(
            context: Context?,
            account: GoogleSignInAccount,
            appName: String?
        ): Drive? {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, setOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account
            return Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential
            )
                .setApplicationName(appName)
                .build()
        }
    }



//     fun DriveServiceHelperKT(driveService: Drive?) {
//        mDriveService = driveService
//    }


    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    fun sync() {
        mDriveService!!.changes()
    }


    fun createFile(fileName: String?): Task<String?>? {
        return Tasks.call(mExecutor, {
            val metadata = File()
                .setParents(listOf("root"))
                .setMimeType("text/plain")
                .setName(fileName)
            val googleFile = mDriveService!!.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation.")
            googleFile.id
        })
    }

    fun createFile(fileName: String?, folderId: String?): Task<String?>? {
        return Tasks.call(mExecutor, {
            val root: List<String>
            root = folderId?.let { listOf(it) } ?: listOf("root")
            val metadata = File()
                .setParents(root)
                .setMimeType("text/plain")
                .setName(fileName)
            val googleFile = mDriveService!!.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation.")
            googleFile.id
        })
    }

    fun createTextFile(
        fileName: String?,
        content: String?,
        folderId: String?
    ): Task<GoogleDriveFileHolder?>? {
        return Tasks.call(mExecutor, {
            val root: List<String>
            root = folderId?.let { listOf(it) } ?: listOf("root")
            val metadata = File()
                .setParents(root)
                .setMimeType("text/plain")
                .setName(fileName)
            val contentStream = ByteArrayContent.fromString("text/plain", content)
            val googleFile = mDriveService!!.files().create(metadata, contentStream).execute()
                ?: throw IOException("Null result when requesting file creation.")
            val googleDriveFileHolder = GoogleDriveFileHolder()
            googleDriveFileHolder.id = googleFile.id
            googleDriveFileHolder
        })
    }

    fun createTextFileIfNotExist(
        fileName: String,
        content: String?,
        folderId: String?
    ): Task<GoogleDriveFileHolder?>? {
        return Tasks.call(mExecutor, {
            val googleDriveFileHolder = GoogleDriveFileHolder()
            val result = mDriveService!!.files().list()
                .setQ("mimeType = 'text/plain' and name = '$fileName' ")
                .setSpaces("drive")
                .execute()
            if (result.files.size > 0) {
                googleDriveFileHolder.id = result.files[0].id
                googleDriveFileHolder
            } else {
                val root: List<String>
                root = folderId?.let { listOf(it) } ?: listOf("root")
                val metadata = File()
                    .setParents(root)
                    .setMimeType("text/plain")
                    .setName(fileName)
                val contentStream = ByteArrayContent.fromString("text/plain", content)
                val googleFile = mDriveService!!.files().create(metadata, contentStream).execute()
                    ?: throw IOException("Null result when requesting file creation.")
                googleDriveFileHolder.id = googleFile.id
                googleDriveFileHolder
            }
        })
    }

    fun createFolder(folderName: String?, folderId: String?): Task<GoogleDriveFileHolder?>? {
        return Tasks.call(mExecutor, {
            val googleDriveFileHolder = GoogleDriveFileHolder()
            val root: List<String>
            root = folderId?.let { listOf(it) } ?: listOf("root")
            val metadata = File()
                .setParents(root)
                .setMimeType(DriveFolder.MIME_TYPE)
                .setName(folderName)
            val googleFile = mDriveService!!.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation.")
            googleDriveFileHolder.id = googleFile.id
            googleDriveFileHolder
        })
    }

    suspend fun createFolderIfNotExist(
        folderName: String,
        parentFolderId: String?
    ): Task<GoogleDriveFileHolder?>? {
        return Tasks.call(mExecutor, {
            val googleDriveFileHolder = GoogleDriveFileHolder()
            val result = mDriveService!!.files().list()
                .setQ("mimeType = '" + DriveFolder.MIME_TYPE + "' and name = '" + folderName + "' and trashed = false ")
                .setSpaces("drive")
                .execute()
            if (result.files.size > 0) {
                googleDriveFileHolder.id = result.files[0].id
                googleDriveFileHolder.name = result.files[0].name
                //                    googleDriveFileHolder.setTrashed(result.getFiles().get(0).getTrashed());
//                googleDriveFileHolder.setModifiedTime(result.getFiles().get(0).getCreatedTime().getValue());
//                googleDriveFileHolder.setSize(result.getFiles().get(0).getSize());
                googleDriveFileHolder.id = result.files[0].id
                googleDriveFileHolder
            } else {
                Log.d(TAG, "createFolderIfNotExist: not found")
                val root: List<String>
                root = parentFolderId?.let { listOf(it) } ?: listOf("root")
                val metadata = File()
                    .setParents(root)
                    .setMimeType(DriveFolder.MIME_TYPE)
                    .setName(folderName)
                val googleFile = mDriveService!!.files().create(metadata).execute()
                    ?: throw IOException("Null result when requesting file creation.")
                googleDriveFileHolder.id = googleFile.id
                googleDriveFileHolder
            }
        })
    }

    /**
     * Opens the file identified by `fileId` and returns a [Pair] of its name and
     * contents.
     */
    fun readFile(fileId: String?): Task<Pair<String?, String?>?>? {
        return Tasks.call(mExecutor, Callable { // Retrieve the metadata as a File object.
            val metadata = mDriveService!!.files()[fileId].execute()
            val name = metadata.name
            mDriveService!!.files()[fileId].executeMediaAsInputStream().use { `is` ->
                BufferedReader(InputStreamReader(`is`)).use { reader ->
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    val contents = stringBuilder.toString()
                    return@Callable Pair.create(name, contents)
                }
            }
        })
    }

    fun deleteFolderFile(fileId: String?): Task<Void?>? {
        return Tasks.call(mExecutor, { // Retrieve the metadata as a File object.
            if (fileId != null) {
                mDriveService!!.files().delete(fileId).execute()
            }
            null
        })
    }

    fun uploadFile(
        googleDiveFile: File?,
        content: AbstractInputStreamContent?
    ): Task<GoogleDriveFileHolder?>? {
        return Tasks.call(mExecutor, { // Retrieve the metadata as a File object.
            val fileMeta = mDriveService!!.files().create(googleDiveFile, content).execute()
            val googleDriveFileHolder = GoogleDriveFileHolder()
            googleDriveFileHolder.id = fileMeta.id
            googleDriveFileHolder.name = fileMeta.name
            googleDriveFileHolder
        })
    }

    fun uploadFile(
        localFile: java.io.File,
        mimeType: String?,
        folderId: String?
    ): Task<GoogleDriveFileHolder?>? {
        return Tasks.call(mExecutor, { // Retrieve the metadata as a File object.
            val root: List<String>
            root = folderId?.let { listOf(it) } ?: listOf("root")
            val metadata = File()
                .setParents(root)
                .setMimeType(mimeType)
                .setName(localFile.name)
            val fileContent = FileContent(mimeType, localFile)
            val fileMeta = mDriveService!!.files().create(metadata, fileContent).execute()
            val googleDriveFileHolder = GoogleDriveFileHolder()
            googleDriveFileHolder.id = fileMeta.id
            googleDriveFileHolder.name = fileMeta.name
            googleDriveFileHolder
        })
    }

    suspend fun searchFolder(folderName: String): Task<List<GoogleDriveFileHolder?>?>? {
        return Tasks.call(mExecutor, {
            val googleDriveFileHolderList: MutableList<GoogleDriveFileHolder> = ArrayList()
            // Retrive the metadata as a File object.
            val result = mDriveService!!.files().list()
                .setQ("mimeType = '" + DriveFolder.MIME_TYPE + "' and name = '" + folderName + "' and trashed = false ")
                .setSpaces("drive")
                .execute()
            for (i in result.files.indices) {
                val googleDriveFileHolder = GoogleDriveFileHolder()
                googleDriveFileHolder.id = result.files[i].id
                googleDriveFileHolder.name = result.files[i].name
                googleDriveFileHolderList.add(googleDriveFileHolder)
            }
            googleDriveFileHolderList
        })
    }

    private val TAG = "DriveServiceHelper"

    fun searchFile(fileName: String, mimeType: String): Task<List<GoogleDriveFileHolder?>?>? {
        return Tasks.call(mExecutor, {
            val googleDriveFileHolderList: MutableList<GoogleDriveFileHolder> = ArrayList()
            // Retrive the metadata as a File object.
            val result = mDriveService!!.files().list()
                .setQ("name = '$fileName' and mimeType ='$mimeType'")
                .setSpaces("drive")
                .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                .execute()
            for (i in result.files.indices) {
                val googleDriveFileHolder = GoogleDriveFileHolder()
                googleDriveFileHolder.id = result.files[i].id
                googleDriveFileHolder.name = result.files[i].name
                googleDriveFileHolder.modifiedTime = result.files[i].modifiedTime
                googleDriveFileHolder.size = result.files[i].getSize()
                googleDriveFileHolderList.add(googleDriveFileHolder)
            }
            googleDriveFileHolderList
        })
    }

    fun downloadFile(fileSaveLocation: java.io.File?, fileId: String?): Task<Void?>? {
        return Tasks.call(mExecutor, { // Retrieve the metadata as a File object.
            val outputStream: OutputStream = FileOutputStream(fileSaveLocation)
            mDriveService!!.files()[fileId].executeMediaAndDownloadTo(outputStream)
            null
        })
    }

    fun downloadFile(fileId: String?): Task<InputStream?>? {
        return Tasks.call(mExecutor, { // Retrieve the metadata as a File object.
            mDriveService!!.files()[fileId].executeMediaAsInputStream()
        })
    }

    fun exportFile(
        fileSaveLocation: java.io.File?,
        fileId: String?,
        mimeType: String?
    ): Task<Void?>? {
        return Tasks.call(mExecutor, { // Retrieve the metadata as a File object.
            val outputStream: OutputStream = FileOutputStream(fileSaveLocation)
            mDriveService!!.files().export(fileId, mimeType).executeMediaAndDownloadTo(outputStream)
            null
        })
    }

    /**
     * Updates the file identified by `fileId` with the given `name` and `content`.
     */
    fun saveFile(fileId: String?, name: String?, content: String?): Task<Void?>? {
        return Tasks.call(mExecutor, { // Create a File containing any metadata changes.
            val metadata = File().setName(name)

            // Convert content to an AbstractInputStreamContent instance.
            val contentStream = ByteArrayContent.fromString("text/plain", content)

            // Update the metadata and contents.
            mDriveService!!.files().update(fileId, metadata, contentStream).execute()
            null
        })
    }

    /**
     * Returns a [FileList] containing all the visible files in the user's My Drive.
     *
     *
     * The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the [Google
 * Developer's Console](https://play.google.com/apps/publish) and be submitted to Google for verification.
     */
    fun queryFiles(): Task<List<GoogleDriveFileHolder?>?>? {
        return Tasks.call(mExecutor, {
            val googleDriveFileHolderList: MutableList<GoogleDriveFileHolder> = ArrayList()
            val result = mDriveService!!.files().list()
                .setFields("files(id, name,size,createdTime,modifiedTime,starred,mimeType)")
                .setSpaces("drive").execute()
            for (i in result.files.indices) {
                val googleDriveFileHolder = GoogleDriveFileHolder()
                googleDriveFileHolder.id = result.files[i].id
                googleDriveFileHolder.name = result.files[i].name
                if (result.files[i].getSize() != null) {
                    googleDriveFileHolder.size = result.files[i].getSize()
                }
                if (result.files[i].modifiedTime != null) {
                    googleDriveFileHolder.modifiedTime = result.files[i].modifiedTime
                }
                if (result.files[i].createdTime != null) {
                    googleDriveFileHolder.createdTime = result.files[i].createdTime
                }
                if (result.files[i].starred != null) {
                    googleDriveFileHolder.starred = result.files[i].starred
                }
                if (result.files[i].mimeType != null) {
                    googleDriveFileHolder.mimeType = result.files[i].mimeType
                }
                googleDriveFileHolderList.add(googleDriveFileHolder)
            }
            googleDriveFileHolderList
        }
        )
    }

    fun queryFiles(folderId: String?): Task<List<GoogleDriveFileHolder?>?>? {
        return Tasks.call(mExecutor, {
            val googleDriveFileHolderList: MutableList<GoogleDriveFileHolder> = ArrayList()
            var parent = "root"
            if (folderId != null) {
                parent = folderId
            }
            val result = mDriveService!!.files().list().setQ("'$parent' in parents")
                .setFields("files(id, name,size,createdTime,modifiedTime,starred,mimeType)")
                .setSpaces("drive").execute()
            for (i in result.files.indices) {
                val googleDriveFileHolder = GoogleDriveFileHolder()
                googleDriveFileHolder.id = result.files[i].id
                googleDriveFileHolder.name = result.files[i].name
                if (result.files[i].getSize() != null) {
                    googleDriveFileHolder.size = result.files[i].getSize()
                }
                if (result.files[i].modifiedTime != null) {
                    googleDriveFileHolder.modifiedTime = result.files[i].modifiedTime
                }
                if (result.files[i].createdTime != null) {
                    googleDriveFileHolder.createdTime = result.files[i].createdTime
                }
                if (result.files[i].starred != null) {
                    googleDriveFileHolder.starred = result.files[i].starred
                }
                if (result.files[i].mimeType != null) {
                    googleDriveFileHolder.mimeType = result.files[i].mimeType
                }
                googleDriveFileHolderList.add(googleDriveFileHolder)
            }
            googleDriveFileHolderList
        }
        )
    }

    suspend fun queryFilesWithoutDelete(folderId: String?): Task<List<GoogleDriveFileHolder?>?>? {
        return Tasks.call(mExecutor, {
            val googleDriveFileHolderList: MutableList<GoogleDriveFileHolder> = ArrayList()
            var parent = "root"
            if (folderId != null) {
                parent = folderId
            }
            val result = mDriveService!!.files().list().setQ("'$parent' in parents")
                .setFields("files(id, name,size,createdTime,modifiedTime,starred,mimeType,trashed)")
                .setSpaces("drive").execute()
            for (i in result.files.indices) {
//                            Map<String, String> maps = result.getFiles().get(i).getAppProperties();
//                            Log.i("file", maps.toString());
//                            File a = result.getFiles().get(i);
//                            Log.i("file", a.toString());
                if (result.files[i].trashed != null && !result.files[i].trashed) {
                    val googleDriveFileHolder = GoogleDriveFileHolder()
                    googleDriveFileHolder.id = result.files[i].id
                    googleDriveFileHolder.name = result.files[i].name
                    if (result.files[i].getSize() != null) {
                        googleDriveFileHolder.size = result.files[i].getSize()
                    }
                    if (result.files[i].modifiedTime != null) {
                        googleDriveFileHolder.modifiedTime = result.files[i].modifiedTime
                    }
                    if (result.files[i].createdTime != null) {
                        googleDriveFileHolder.createdTime = result.files[i].createdTime
                    }
                    if (result.files[i].starred != null) {
                        googleDriveFileHolder.starred = result.files[i].starred
                    }
                    if (result.files[i].mimeType != null) {
                        googleDriveFileHolder.mimeType = result.files[i].mimeType
                    }
                    googleDriveFileHolderList.add(googleDriveFileHolder)
                }
            }
            googleDriveFileHolderList
        }
        )
    }


    /**
     * Returns an [Intent] for opening the Storage Access Framework file picker.
     */
    fun createFilePickerIntent(): Intent? {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        return intent
    }

    /**
     * Opens the file at the `uri` returned by a Storage Access Framework [Intent]
     * created by [.createFilePickerIntent] using the given `contentResolver`.
     */
    fun openFileUsingStorageAccessFramework(
        contentResolver: ContentResolver, uri: Uri?
    ): Task<Pair<String?, String?>?>? {
        return Tasks.call(mExecutor, { // Retrieve the document's display name from its metadata.
            var name: String
            contentResolver.query(uri!!, null, null, null, null).use { cursor ->
                name = if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.getString(nameIndex)
                } else {
                    throw IOException("Empty cursor returned for file.")
                }
            }

            // Read the document's contents as a String.
            var content: String
            contentResolver.openInputStream(uri).use { `is` ->
                BufferedReader(InputStreamReader(`is`)).use { reader ->
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    content = stringBuilder.toString()
                }
            }
            Pair.create(name, content)
        })
    }

}