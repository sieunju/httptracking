package com.http.tracking.wifi_share

import android.util.Log
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
class WifiShareManager {

    private var logData: TrackingHttpEntity? = null
    private var sharedAddress: String = ""
    private var serverThread: HttpThread? = null

    companion object {
        const val PATH = "/tracking"
        fun LogD(msg: String) {
            // Log.d("JLOGGER", msg)
        }
    }

    /**
     * WifiShare Start
     */
    fun start(ip: String, port: Int) {
        if (serverThread == null) {
            serverThread = HttpThread(ip, port).run {
                start()
                return@run this
            }
        }
    }

    /**
     * WifiShare Stop
     */
    fun stop() {
        if (serverThread != null) {
            serverThread?.stop()
            serverThread = null
        }
    }

    /**
     * WifiShare Setting Log Data
     */
    fun setLogData(data: TrackingHttpEntity?) {
        logData = data
    }

    fun getSharedAddress(): String = sharedAddress

    inner class HttpThread(
        private val ip: String,
        private val port: Int
    ) : Runnable {

        private var serverSocket: ServerSocket? = null
        private var bStart = true
        private val thread: ExecutorService by lazy { Executors.newFixedThreadPool(1) }

        fun start() {
            thread.execute(this)
        }

        fun stop() {
            bStart = false
            thread.shutdownNow()
            serverSocket?.close()
        }

        override fun run() {
            serverSocket = ServerSocket(port, 0, InetAddress.getByName(ip))
            LogD("Address ${serverSocket?.inetAddress}:${serverSocket?.localPort}")
            while (bStart) {
                try {
                    Thread.sleep(200)
                    val clientSocket = serverSocket?.accept()

                    val bufferReader =
                        BufferedReader(InputStreamReader(clientSocket?.getInputStream()))
                    // First Header Format -> {GET} {path} HTTP/1.1
                    val split = bufferReader.readLine().split(" ")
                    val path = split[1]
                    // 유효한 EndPoint 값으로 오는 경우에만 처리
                    if (path == PATH) {
                        val clientOutput = clientSocket?.getOutputStream()
                        val str = StringBuilder()
                        str.append("HTTP/1.1 200 OK\r\n")
                        str.append("\r\n")
                        str.append(getPrettyHttpLog().first)
                        str.append("\r\n\r\n")
                        clientOutput?.write(str.toString().encodeToByteArray())
                        clientOutput?.flush()
                        bufferReader.close()
                        clientOutput?.close()
                    } else {
                        // 유효하지 않는 EndPoint 값으로 오는 경우 Not Found 에러 처리
                        val clientOutput = clientSocket?.getOutputStream()
                        val str = StringBuilder()
                        str.append("HTTP/1.1 404 Not Found\r\n")
                        str.append("\r\n")
                        str.append("Tracking Path eg) $PATH")
                        str.append("\r\n\r\n")
                        clientOutput?.write(str.toString().encodeToByteArray())
                        clientOutput?.flush()
                        bufferReader.close()
                    }
                } catch (ex: Exception) {
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
            // 4. {Headers}
            // 5. Response Code
            // 6. Response Body
            // 7. Response Error
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

                // 4. {Headers}
                if (data.headerMap.isNotEmpty()) {
                    str.append("<h5>[Headers]</h5>")
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

            if (res != null && !res.body.isNullOrEmpty()) {
                // 6. Response Body
                str.append("<h5>[Body]</h5>")
                str.append("<pre>")
                str.append(Extensions.toJsonBody(res.body).replace("\n", "<br>"))
                str.append("</pre>")
            }

            // 7. Response Error
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