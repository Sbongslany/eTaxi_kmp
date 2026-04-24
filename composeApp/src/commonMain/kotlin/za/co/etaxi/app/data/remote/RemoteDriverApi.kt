package za.co.etaxi.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import za.co.etaxi.app.core.network.AddVehicleRequest
import za.co.etaxi.app.core.network.ApiResult
import za.co.etaxi.app.core.network.DocumentDto
import za.co.etaxi.app.core.network.DriverProfileDto
import za.co.etaxi.app.core.network.EtaxiApi
import za.co.etaxi.app.core.network.GoOnlineRequest
import za.co.etaxi.app.core.network.SetActiveVehicleRequest
import za.co.etaxi.app.core.network.TokenManager
import za.co.etaxi.app.core.network.UpdateLocationRequest
import za.co.etaxi.app.core.network.UploadDocumentRequest
import za.co.etaxi.app.core.network.UploadUrlRequest
import za.co.etaxi.app.core.network.UploadUrlResponse
import za.co.etaxi.app.core.network.VehicleDto
import za.co.etaxi.app.core.network.authorized

// Raw HTTP client for the driver endpoints.
// All endpoints require a Bearer access token. The authorized() extension
// attaches the header by reading the current token from TokenManager.
// Backend role enforcement (DRIVER vs ADMIN) happens server-side.
//
// Endpoints:
//   GET  drivers/me                  -> DriverProfileDto
//   POST drivers/online              -> Unit
//   POST drivers/offline             -> Unit
//   POST drivers/location            -> Unit
//   POST drivers/vehicles            -> VehicleDto (created vehicle)
//   POST drivers/vehicles/active     -> VehicleDto (active vehicle)
//   POST drivers/documents           -> DocumentDto
//   POST storage/upload-url          -> UploadUrlResponse (presigned URL)
class RemoteDriverApi(
    private val http: HttpClient,
    private val tokens: TokenManager,
) {
    suspend fun me(): ApiResult<DriverProfileDto> =
        EtaxiApi.wrap {
            http.get("drivers/me") { authorized(tokens) }
        }

    suspend fun goOnline(req: GoOnlineRequest): ApiResult<Unit> =
        EtaxiApi.wrap {
            http.post("drivers/online") {
                authorized(tokens)
                setBody(req)
            }
        }

    suspend fun goOffline(): ApiResult<Unit> =
        EtaxiApi.wrap {
            http.post("drivers/offline") { authorized(tokens) }
        }

    suspend fun updateLocation(req: UpdateLocationRequest): ApiResult<Unit> =
        EtaxiApi.wrap {
            http.post("drivers/location") {
                authorized(tokens)
                setBody(req)
            }
        }

    suspend fun addVehicle(req: AddVehicleRequest): ApiResult<VehicleDto> =
        EtaxiApi.wrap {
            http.post("drivers/vehicles") {
                authorized(tokens)
                setBody(req)
            }
        }

    suspend fun setActiveVehicle(req: SetActiveVehicleRequest): ApiResult<VehicleDto> =
        EtaxiApi.wrap {
            http.post("drivers/vehicles/active") {
                authorized(tokens)
                setBody(req)
            }
        }

    suspend fun uploadDocument(req: UploadDocumentRequest): ApiResult<DocumentDto> =
        EtaxiApi.wrap {
            http.post("drivers/documents") {
                authorized(tokens)
                setBody(req)
            }
        }

    suspend fun getUploadUrl(req: UploadUrlRequest): ApiResult<UploadUrlResponse> =
        EtaxiApi.wrap {
            http.post("storage/upload-url") {
                authorized(tokens)
                setBody(req)
            }
        }
}
