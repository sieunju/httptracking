package com.http.tracking

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun mimeTypeParserTest() {
        val blobStr = """
            --997bfb75-3b4a-4f71-a169-c8d85ace012b
            Content-Disposition: form-data; name="files"; filename="1662479784760.jpg"
            Content-Type: image/jpg
            Content-Length: 271676
            ������JFIF��������������(ICC_PROFILE������������������mn

            --997bfb75-3b4a-4f71-a169-c8d85ace012b
        """.trimIndent()
        val boundaryStartIdx = blobStr.indexOf("--")
        val boundaryEndIdx = blobStr.indexOf("Content-Disposition:")
        if (boundaryStartIdx != -1 && boundaryEndIdx != -1) {
            val boundary = blobStr.substring(boundaryStartIdx, boundaryEndIdx)
            val split = blobStr.split(boundary)
            split.forEach { str ->
                if (str.isEmpty()) return@forEach
                val fileName = str.substringRange("filename=", "\n").replace("\n","")
                val contentType = str.substringRange("Content-Type:", "\n").replace("\n","")
                val contentLength = str.substringRange("Content-Length:", "\n").replace("\n","")
                val binary = str.substringAfter(contentLength)
                println("FileName  $fileName")
                println("contentType $contentType")
                println("contentLength $contentLength")
                println("Binary $binary")
            }
        }
    }

    fun String.substringRange(startDelimiter: String, endDelimiter: String): String {
        val startIdx = indexOf(startDelimiter)
        return if (startIdx != -1) {
            val endStr = substring(startIdx + startDelimiter.length)
            val endIdx = endStr.indexOf(endDelimiter)
            if (endIdx != -1) {
                endStr.substring(0, Math.min(endIdx.plus(endDelimiter.length), endStr.length))
            } else {
                endStr
            }
        } else {
            this
        }
    }
}