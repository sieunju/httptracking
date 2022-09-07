package com.http.tracking_interceptor.model

import okhttp3.MediaType

data class Part(
    val type: MediaType? = null,
    val binaryBytes : ByteArray? = null,
    val binary: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Part

        if (type != other.type) return false
        if (binaryBytes != null) {
            if (other.binaryBytes == null) return false
            if (!binaryBytes.contentEquals(other.binaryBytes)) return false
        } else if (other.binaryBytes != null) return false
        if (binary != other.binary) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type?.hashCode() ?: 0
        result = 31 * result + (binaryBytes?.contentHashCode() ?: 0)
        result = 31 * result + (binary?.hashCode() ?: 0)
        return result
    }
}
