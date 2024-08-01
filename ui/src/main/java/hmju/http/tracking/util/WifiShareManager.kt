package hmju.http.tracking.util

import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import hmju.http.model.ChildModel
import hmju.http.model.ContentsModel
import hmju.http.model.HttpBodyModel
import hmju.http.model.HttpMultipartModel
import hmju.http.model.TitleModel
import hmju.http.model.TrackingModel
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
    fun setLogData(
        data: TrackingModel?
    ) {
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
        val wifiSummary = logData
            ?.summaryModel?.wifiSummary ?: return ""
        return "<h4>${wifiSummary}</h4>"
    }

    private fun getHttpTrackingJson(): String {
        val str = StringBuilder()
        // Android Http Tracking Log
        str.append("<h3>Android HTTP Tracking Log by.hmju</h3>")
        val data = logData
        if (data != null) {
            str.append(getMethodAndBaseUrl())
            str.append("<h2>[Request]</h2>")
            str.getDescription(data.reqModels)
            str.append("<h2>[Response]</h2>")
            str.getDescription(data.resModels)
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

    private fun StringBuilder.getDescription(
        list: List<ChildModel>
    ): StringBuilder {
        list.forEach { model ->
            when (model) {
                is TitleModel -> {
                    append("<h4>${model.text}</h4>")
                }

                is ContentsModel -> {
                    append(model.text)
                    append("<br>")
                }

                is HttpBodyModel -> {
                    append("<h5>[Body - Json]</h5>")
                    append(
                        toJsonBody(
                            model.json
                        ).replace("\n", "<br>")
                    )
                }

                is HttpMultipartModel -> {
                    append("<h5>[Body - MultipartType]</h5>")
                    append(model.mimeType)
                    append(" Length: ")
                    append(model.bytes?.size)
                    append("<br>")
                }
            }
        }
        return this
    }
}