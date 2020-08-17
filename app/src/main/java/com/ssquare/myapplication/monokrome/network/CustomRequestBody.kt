package com.ssquare.myapplication.monokrome.network
//
//import android.os.Handler
//import android.os.Looper
//import okhttp3.MediaType
//import okhttp3.MultipartBody
//import okhttp3.RequestBody
//import okio.BufferedSink
//import retrofit2.Call
//import retrofit2.http.*
//import java.io.File
//import java.io.FileInputStream
//
//
//class FileUploader(private val uploadInterface: MonokromeApiService) {
//
//    var fileUploaderCallback: FileUploaderCallback? = null
//    private lateinit var files: Array<File>
//    var uploadIndex = -1
//    private var uploadURL = ""
//    private var totalFileLength: Long = 0
//    private var totalFileUploaded: Long = 0
//    private var filekey = ""
//    private var authToken = ""
//    private var responses: Array<String>? = null
//
//
//    private interface UploadInterface {
//        @Multipart
//        @POST
//        fun uploadFile(
//            @Url url: String?,
//            @Part file: MultipartBody.Part?,
//            @Header("Authorization") authorization: String?
//        ): Call<JsonElement?>?
//
//        @Multipart
//        @POST
//        fun uploadFile(
//            @Url url: String?,
//            @Part file: MultipartBody.Part?
//        ): Call<JsonElement?>?
//    }
//
//    interface FileUploaderCallback {
//        fun onError()
//        fun onFinish(responses: Array<String>?)
//        fun onProgressUpdate(
//            currentPercent: Int,
//            totalPercent: Int,
//            fileNumber: Int
//        )
//    }
//
//    class CustomRequestBody() : RequestBody() {
//
//        companion object {
//            private const val DEFAULT_BUFFER_SIZE: Byte = 2048.toByte()
//        }
//
//        private lateinit var mFile: File
//
//
//        constructor(file: File) : this() {
//            mFile = file
//        }
//
//
//        override fun contentType(): MediaType? {
//            TODO("Not yet implemented")
//        }
//
//        override fun writeTo(sink: BufferedSink) {
//            val fileLength = mFile.length();
//            val buffer = byteArrayOf(DEFAULT_BUFFER_SIZE)
//            val inputStream = FileInputStream(mFile)
//            var uploaded = 0
//
//            inputStream.use { inputStream ->
//                var read = inputStream.read(buffer)
//                val handler = Handler(Looper.getMainLooper());
//                do {
//                    // update progress on UI thread
//                    handler.post(ProgressUpdater(uploaded, fileLength));
//                    uploaded += read
//                    sink.write(buffer, 0, read)
//                    read = inputStream.read(buffer)
//                } while (read != -1)
//            }
//        }
//
//
//    }
//
//
//    fun uploadFiles(
//        url: String,
//        filekey: String,
//        files: Array<File>,
//        fileUploaderCallback: FileUploaderCallback,
//        authToken: String = ""
//    ) {
//        this.fileUploaderCallback = fileUploaderCallback;
//        this.files = files;
//        this.uploadIndex = -1;
//        this.uploadURL = url;
//        this.filekey = filekey;
//        this.authToken = authToken;
//        totalFileUploaded = 0;
//        totalFileLength = 0;
//        uploadIndex = -1;
//        responses = arrayOf<String>(files.size.toString())
//        for (i in 0..files.size) {
//            totalFileLength += files[i].length();
//        }
//        uploadNext();
//    }
//
//
//    private fun uploadNext() {
//        if (files.isNotEmpty()) {
//            if (uploadIndex != -1)
//                totalFileUploaded += files[uploadIndex].length();
//            uploadIndex++;
//            if (uploadIndex < files.size) {
//                uploadSingleFile(uploadIndex);
//            } else {
//                fileUploaderCallback?.onFinish(responses);
//            }
//        } else {
//            fileUploaderCallback?.onFinish(responses);
//        }
//    }
//
//    private fun uploadSingleFile(index: Int) {
//       var fileBody: CustomRequestBody = CustomRequestBody(files[index]);
//        var filePart:MultipartBody.Part  = MultipartBody . Part . createFormData (filekey, files[index].name, fileBody);
//        Call<JsonElement> call
//        if (authToken.isEmpty()) {
//            call = uploadInterface.uploadFile(uploadURL, filePart);
//        } else {
//            call = uploadInterface.uploadFile(uploadURL, filePart, authToken);
//        }
//
//        call.enqueue(new Callback < JsonElement >() {
//            @Override
//            public void onResponse(
//                Call<JsonElement> call,
//                retrofit2.Response<JsonElement> response
//            ) {
//                if (response.isSuccessful()) {
//                    JsonElement jsonElement = response . body ();
//                    responses[index] = jsonElement.toString();
//                } else {
//                    responses[index] = "";
//                }
//                uploadNext();
//            }
//
//            @Override
//            public void onFailure(Call<JsonElement> call, Throwable t) {
//                fileUploaderCallback.onError();
//            }
//        });
//    }
//
//    private class ProgressUpdater:Runnable
//    {
//         mUploaded;
//        private long mTotal;
//        public ProgressUpdater (long uploaded, long total) {
//        mUploaded = uploaded
//        mTotal = total
//    }
//
//        @Override
//        fun run() {
//            int current_percent =(int)(100 * mUploaded / mTotal);
//            int total_percent =(int)(100 * (totalFileUploaded + mUploaded) / totalFileLength);
//            fileUploaderCallback.onProgressUpdate(current_percent, total_percent, uploadIndex + 1);
//        }
//    }
//
//
//}
//
//
//
//
