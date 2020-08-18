package com.ssquare.myapplication.monokrome.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.ssquare.myapplication.monokrome.BuildConfig.DEBUG
import java.io.File
import java.net.URI


class FileUtils {

    companion object {

        private const val TAG = "FileUtils"
        private const val FILE_PREFIX = "file://"
        const val NO_FILE = "no_file"

        fun deleteFile(uri: String): Boolean {
            if (uri == NO_FILE) return false

            val file = File(URI.create(uri))
            return if (file.exists()) {
                file.delete()
                true
            } else
                false
        }

        fun createUriString(context: Context, id: Long): String {
            return FILE_PREFIX +
                    context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.path + id.toString() + PDF_TYPE
        }

        fun getContentUri(path: String): Uri {
            return Uri.parse(File(path).toString())
        }

        fun getFileNameFromUri(context: Context, uri: Uri): String? {
            var result: String? = null
            if (uri.scheme == "content") {
                val cursor =
                    context.contentResolver.query(uri, null, null, null, null)
                cursor.use {
                    if (it != null && it.moveToFirst()) {
                        result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
            }
            if (result == null) {
                result = uri.lastPathSegment
            }
            return result
        }

        fun getFileNameFromPath(path: String?): String? {
            if (path != null) {
                return path.substring(path.lastIndexOf("/") + 1)
            }
            return null
        }

        fun getTypeFromUri(context: Context, uri: Uri): String? {
            return context.contentResolver.getType(uri)
        }

        fun getTypeFromPath(path: String?): String? {
            if (path?.lastIndexOf(".") != -1) {
                val ext: String? = path?.substring(path.lastIndexOf(".") + 1)
                val mime = MimeTypeMap.getSingleton()
                return mime.getMimeTypeFromExtension(ext)
            }
            return null
        }

        fun getFileFromUri(context: Context?, uri: Uri?): File? {
            if (uri != null) {
                val path: String? = getPath(context, uri)
                if (path != null && isLocal(path)) {
                    return File(path)
                }
            }
            return null
        }

        fun getFileFromPath(path: String?): File? {
            if (path != null && isLocal(path)) {
                return File(path)
            }

            return null
        }

        private fun isLocal(url: String?): Boolean {
            return url != null && !url.startsWith("http://") && !url.startsWith("https://")
        }


        fun getPath(context: Context?, uri: Uri): String? {

            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory()
                            .toString() + "/" + split[1]
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri: Uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {

                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                    context,
                    uri,
                    null,
                    null
                )
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        private fun getDataColumn(
            context: Context?, uri: Uri?, selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                cursor = context?.contentResolver?.query(
                    uri!!, projection, selection, selectionArgs,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    if (DEBUG) DatabaseUtils.dumpCursor(cursor)
                    val columnIndex: Int = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(columnIndex)
                }
            } finally {
                cursor?.close()
            }
            return null
        }


        private fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        private fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }

        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }
    }


}