package hmju.tracking.model

import java.text.SimpleDateFormat

internal object Extensions {

    @Suppress("SimpleDateFormat")
    private val simpleDate = SimpleDateFormat("HH:mm:ss.SSS")

    fun Long.toDate(): String {
        return try {
            simpleDate.format(this)
        } catch (ex: Exception) {
            ""
        }
    }
}
