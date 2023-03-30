package com.http.tracking.util

import androidx.annotation.WorkerThread
import com.http.tracking.Extensions
import com.http.tracking_interceptor.model.TrackingHttpEntity
import com.http.tracking_interceptor.model.TrackingRequestEntity
import com.http.tracking_interceptor.model.TrackingRequestMultipartEntity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * Description : Wifi Share Manager
 *
 * Created by juhongmin on 2023/01/26
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
internal class WifiShareManager {

    private var logData: TrackingHttpEntity? = null
    private var workThread: WorkThread? = null
    private var listener: Listener? = null

    interface Listener {
        /**
         * Server Start Callback
         *
         * @param address ex.) 127.0.0.1:1111
         */
        @WorkerThread
        fun onServerStart(address: String)
    }

    companion object {
        const val PATH = "/tracking"
        fun logD(str: String) {
            // Log.d("JTracking", str)
        }
    }

    /**
     * WifiShare Start
     */
    fun start(ip: String, port: Int) {
        if (workThread == null) {
            workThread = WorkThread(ip, port)
        }
    }

    /**
     * WifiShare Stop
     */
    fun stop() {
        if (workThread != null) {
            workThread?.stop()
            workThread = null
        }
    }

    /**
     * WifiShare Setting Log Data
     */
    fun setLogData(data: TrackingHttpEntity?) {
        logData = data
    }

    fun setListener(l: Listener): WifiShareManager {
        listener = l
        return this
    }

    private inner class WorkThread(
        private val ip: String,
        private val port: Int
    ) : Runnable {

        private var serverSocket: ServerSocket? = null
        private var bStart = true
        private val thread: ExecutorService by lazy { Executors.newFixedThreadPool(1) }

        init {
            initServer()
        }

        fun stop() {
            bStart = false
            thread.shutdownNow()
            serverSocket?.close()
        }

        /**
         * init Server Setting
         */
        private fun initServer() {
            Executors.newCachedThreadPool().submit {
                serverSocket = ServerSocket(port, 0, InetAddress.getByName(ip))
                val shareUrl =
                    "http:/${serverSocket?.inetAddress}:${serverSocket?.localPort}$PATH"
                logD(shareUrl)
                listener?.onServerStart(shareUrl)
                thread.execute(this)
            }
        }

        override fun run() {
            while (bStart) {
                try {
                    val client = serverSocket?.accept() ?: return
                    logD("ClientConnected $client")
                    val bufferReader = BufferedReader(InputStreamReader(client.getInputStream()))
                    // First Header Format -> {GET} {path} HTTP/1.1
                    val split = bufferReader.readLine().split(" ")
                    logD("Client Headers $split")
                    val path = split[1]
                    // 유효한 EndPoint 값으로 오는 경우에만 처리
                    if (path == PATH) {
                        val clientOutput = client.getOutputStream()
                        val prettyLog = getPrettyHttpLog()
                        val str = StringBuilder()
                        str.append("HTTP/1.1 200 OK\r\n")
                        str.append("Content-Length: ${prettyLog.second}\n")
                        str.append("Content-Type: text/html; charset=UTF-8")
                        // End Headers Separator \r\n\r\n
                        str.append("\r\n\r\n")
                        str.append(prettyLog.first)
                        clientOutput?.write(str.toString().encodeToByteArray())
                        clientOutput?.flush()
                        bufferReader.close()
                        clientOutput?.close()
                    } else {
                        // 유효하지 않는 EndPoint 값으로 오는 경우 Not Found 에러 처리
                        val clientOutput = client.getOutputStream()
                        val str = StringBuilder()
                        str.append("HTTP/1.1 404 Not Found\r\n")
                        str.append("\r\n")
                        clientOutput?.write(str.toString().encodeToByteArray())
                        clientOutput?.flush()
                        bufferReader.close()
                    }
                } catch (ex: Exception) {
                    logD("Client Error $ex")
                    bStart = false
                }
            }
        }
    }

    private fun getPrettyHttpLog(): Pair<CharSequence, Long> {
        val sb = StringBuilder()
        sb.append(
            "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "\n" +
                    "<head>\n" +
                    "    <meta charset=\"utf-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n" +
                    "    <title>Http Tracking</title>\n" +
                    "</head>"
        )
        sb.append("\n")
        sb.append("<body>")
        sb.append("\n")
        sb.append(getHttpTrackingJson())
        sb.append("\n")
        sb.append("</body>")
        sb.append("\n")
        sb.append("</html>")
        return sb to sb.toString().encodeToByteArray().size.toLong()
    }

    private fun getHttpTrackingJson(): String {
        val str = StringBuilder()
        // Android Http Tracking Log
        str.append("<h3>Android HTTP Tracking Log by.hmju</h3>")
        val data = logData
        if (data != null) {
            // Log Print Format
            // 1. {MethodType} {BaseUrl + Path}
            // 2. {Request Query Parameter}
            // 3. {Request Body}
            // 4. {Request Headers}
            // 5. Response Code
            // 6. Response Header
            // 7. Response Body
            // 8. Response Error
            val req = data.req
            // Request Setting
            if (req != null) {
                // 1. {MethodType} {BaseUrl + Path}
                str.append("<h4>[${data.method}] ${data.scheme}://${data.baseUrl}${data.path}</h4>")

                // 2. {Request Query Parameter or Request Body}
                val queryList = Extensions.toReqQueryList(req.fullUrl)
                if (queryList.isNotEmpty()) {
                    str.append("<h5>[Request Query Parameters]</h5>")
                    queryList.forEach {
                        str.append(it.plus("<br>"))
                    }
                }

                // 3. {Request Body}
                if (req is TrackingRequestEntity) {
                    if (!req.body.isNullOrEmpty()) {
                        str.append("<h5>[Request Body - Json]</h5>")
                        str.append("<pre>")
                        str.append(Extensions.toJsonBody(req.body).replace("\n", "<br>"))
                        str.append("</pre>")
                    }
                } else if (req is TrackingRequestMultipartEntity) {
                    str.append("<h5>[Request Body - MultipartType]</h5>")
                    req.binaryList.forEach {
                        str.append("MultiPart-MediaType: ")
                        str.append(it.type)
                        str.append(" Length: ")
                        str.append(it.bytes?.size)
                        str.append("<br>")
                    }
                }

                // 4. {Request Headers}
                if (data.headerMap.isNotEmpty()) {
                    str.append("<h5>[Request Headers]</h5>")
                    data.headerMap.forEach { entry ->
                        str.append(entry.key)
                        str.append(" : ")
                        str.append(entry.value)
                        str.append("<br>")
                    }
                }

                // 5. Response Code
                if (data.isSuccess()) {
                    str.append("<H4><font color=\"#03A9F4\">${data.code}</font></H4>")
                } else {
                    str.append("<H4><font color=\"#C62828\">${data.code}</font></H4>")
                }
            }

            val res = data.res

            if (res != null) {

                if (res.headerMap.isNotEmpty()) {
                    // 6. Response Header
                    str.append("<h5>[Response Headers]</h5>")
                    res.headerMap.forEach { entry ->
                        str.append(entry.key)
                        str.append(" : ")
                        str.append(entry.value)
                        str.append("<br>")
                    }
                }

                if (!res.body.isNullOrEmpty()) {
                    // 7. Response Body
                    str.append("<h5>[Body]</h5>")
                    str.append("<pre>")
                    str.append(Extensions.toJsonBody(res.body).replace("\n", "<br>"))
                    str.append("</pre>")
                }
            }

            // 8. Response Error
            if (data.error != null) {
                str.append("<br>")
                str.append("<h5>[HTTP Error]</h5>")
                str.append(data.error)
                str.append("<br>")
            }
        }
        return str.toString()
    }
}