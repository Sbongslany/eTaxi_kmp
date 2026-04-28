package za.co.etaxi.app.core.upload

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import za.co.etaxi.app.core.network.EtaxiApi

// Uploads file bytes via HTTP PUT to a presigned URL (e.g. S3, GCS).
//
// Implementation note: we deliberately use a fresh HttpClient (not the
// authenticated EtaxiApi client) because:
//   1. The presigned URL embeds its own auth as query params; sending
//      our Bearer token to S3 would be wrong and may cause 400s.
//   2. The default JSON content-negotiation is the wrong content type
//      for a binary upload — we set the body's content-type explicitly.
//
// Usage:
//   val ok = FileUploader.uploadBytes(
//       presignedUrl = uploadResponse.uploadUrl,
//       contentType = "image/jpeg",
//       bytes = pickedBytes,
//   )
object FileUploader {

    suspend fun uploadBytes(
        presignedUrl: String,
        contentType: String,
        bytes: ByteArray,
    ): Boolean {
        val client: HttpClient = EtaxiApi.create()
        return try {
            val response: HttpResponse = client.put(presignedUrl) {
                contentType(parseContentType(contentType))
                headers {
                    // Ensure the Content-Length matches what S3 signed for.
                    append(HttpHeaders.ContentLength, bytes.size.toString())
                }
                setBody(bytes)
            }
            response.status.isSuccess()
        } catch (t: Throwable) {
            println("[FileUploader] upload failed: ${t.message}")
            false
        } finally {
            client.close()
        }
    }

    private fun parseContentType(raw: String): ContentType =
        runCatching { ContentType.parse(raw) }
            .getOrDefault(ContentType.Application.OctetStream)
}
