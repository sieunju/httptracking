package hmju.http.tracking.util

import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import hmju.http.tracking_interceptor.model.TrackingModel
import hmju.http.tracking_interceptor.model.HttpTrackingRequest
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

    private var logData: TrackingModel? = null
    private var workThread: WorkThread? = null
    private var listener: Listener? = null

    // Gson
    private val gson: Gson by lazy {
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .serializeNulls()
            .create()
    }

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
        private const val PATH = "/tracking"
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
    fun setLogData(data: TrackingModel?) {
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
                val ssb = StringBuilder("http:/")
                ssb.append(serverSocket?.inetAddress)
                ssb.append(":")
                ssb.append(serverSocket?.localPort)
                ssb.append(PATH)
                listener?.onServerStart(ssb.toString())
                thread.execute(this)
            }
        }

        override fun run() {
            while (bStart) {
                try {
                    val client = serverSocket?.accept() ?: return
                    val bufferReader = BufferedReader(InputStreamReader(client.getInputStream()))
                    // First Header Format -> {GET} {path} HTTP/1.1
                    val split = bufferReader.readLine().split(" ")
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

    private fun getMethodAndBaseUrl(): CharSequence {
        val data = logData ?: return ""
        return "<h4>[${data.getMethod()}] ${data.getHost()}${data.getPath()}"
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

            // 1. {MethodType} {BaseUrl + Path}
            str.append(getMethodAndBaseUrl())

            // 2. {Request Query Parameter or Request Body}
            val reqQueryList = data.getRequest().getQueryParams()?.split("&")
            if (!reqQueryList.isNullOrEmpty()) {
                str.append("<h5>[Request Query Parameters]</h5>")
                reqQueryList.forEach { str.append(it.plus("<br>")) }
            }

            // 3. {Request Body}
            if (data is TrackingModel.Default) {
                val req = data.request
                if (req is HttpTrackingRequest.Default) {
                    if (!req.body.isNullOrEmpty()) {
                        str.append("<h5>[Request Body - Json]</h5>")
                        str.append("<pre>")
                        str.append(toJsonBody(req.body).replace("\n", "<br>"))
                    }
                } else if (req is HttpTrackingRequest.MultiPart) {
                    str.append("<h5>[Request Body - MultipartType]</h5>")
                    req.binaryList.forEach {
                        str.append("MultiPart-MediaType: ")
                        str.append(it.type)
                        str.append(" Length: ")
                        str.append(it.bytes?.size)
                        str.append("<br>")
                    }
                }
            }

            // 4. {Request Headers}
            val reqHeaderMap = data.getRequest().getHeaderMap()
            if (reqHeaderMap.isNotEmpty()) {
                str.append("<h5>[Request Headers]</h5>")
                reqHeaderMap.forEach { entry ->
                    str.append(entry.key)
                    str.append(" : ")
                    str.append(entry.value)
                    str.append("<br>")
                }
            }

            // 5. Response Code
            if (data is TrackingModel.Default) {
                if (data.isSuccess) {
                    str.append("<H4><font color=\"#03A9F4\">${data.code}</font></H4>")
                } else {
                    str.append("<H4><font color=\"#C62828\">${data.code}</font></H4>")
                }

                // 6. Response Header
                val resHeaderMap = data.response.getHeaderMap()
                if (resHeaderMap.isNotEmpty()) {
                    str.append("<h5>[Response Headers]</h5>")
                    resHeaderMap.forEach { entry ->
                        str.append(entry.key)
                        str.append(" : ")
                        str.append(entry.value)
                        str.append("<br>")
                    }
                }

                // 7. Response Body
                val resBody = data.response.getBody()
                if (!resBody.isNullOrEmpty()) {
                    // 7. Response Body
                    str.append("<h5>[Body]</h5>")
                    str.append("<pre>")
                    str.append(toJsonBody(resBody).replace("\n", "<br>"))
                    str.append("</pre>")
                }
            } else if (data is TrackingModel.TimeOut) {
                // 8. Response Error
                str.append("<br>")
                str.append("<h5>[HTTP Error]</h5>")
                str.append(data.msg)
                str.append("<br>")
            } else if (data is TrackingModel.Error) {
                // 8. Response Error
                str.append("<br>")
                str.append("<h5>[HTTP Error]</h5>")
                str.append(data.msg)
                str.append("<br>")
            }
        }
        return str.toString()
    }

    fun toJsonBody(body: String?): String {
        if (body == null) return ""
        return try {
            val je = JsonParser.parseString(body)
            gson.toJson(je)
        } catch (ex: Exception) {
            ""
        }
    }
}