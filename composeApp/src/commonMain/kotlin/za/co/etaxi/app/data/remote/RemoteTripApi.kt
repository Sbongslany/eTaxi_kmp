package za.co.etaxi.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import za.co.etaxi.app.core.network.AddStopBody
import za.co.etaxi.app.core.network.ApiResult
import za.co.etaxi.app.core.network.CancelTripBody
import za.co.etaxi.app.core.network.EtaxiApi
import za.co.etaxi.app.core.network.LocationPingBody
import za.co.etaxi.app.core.network.RatingBody
import za.co.etaxi.app.core.network.RequestStandardTripBody
import za.co.etaxi.app.core.network.RequestTripResponse
import za.co.etaxi.app.core.network.TokenManager
import za.co.etaxi.app.core.network.TripDto
import za.co.etaxi.app.core.network.authorized

// HTTP client for the /trips/* endpoints. Used by both the rider and the
// driver repository because the same backend module serves both sides
// (request/cancel/addStop are rider-flavoured but shared with driver,
// accept/arriving/arrived/start/end are driver-only).
//
// Endpoints (all require Bearer token; backend enforces role):
//   POST trips/request                    -> RequestTripResponse  (rider)
//   POST trips/:id/cancel                 -> TripDto              (either)
//   POST trips/:id/stops                  -> { trip, stop, fare } (either)
//   POST trips/:id/accept                 -> TripDto              (driver)
//   POST trips/:id/arriving               -> TripDto              (driver)
//   POST trips/:id/arrived                -> TripDto              (driver)
//   POST trips/:id/start                  -> TripDto              (driver)
//   POST trips/:id/end                    -> { trip, fare }       (driver)
//   POST trips/:id/location               -> 204                  (driver)
//   GET  trips/:id                        -> TripDto              (either)
//   GET  trips                            -> { items, total }     (either)
//   POST ratings                          -> 204                  (either)
class RemoteTripApi(
    private val http: HttpClient,
    private val tokens: TokenManager,
) {
    suspend fun requestStandard(req: RequestStandardTripBody): ApiResult<RequestTripResponse> =
        EtaxiApi.wrap {
            http.post("trips/request") {
                authorized(tokens)
                setBody(req)
            }
        }

    suspend fun cancel(tripId: String, reason: String? = null): ApiResult<TripDto> =
        EtaxiApi.wrap {
            http.post("trips/$tripId/cancel") {
                authorized(tokens)
                setBody(CancelTripBody(reason))
            }
        }

    suspend fun addStop(tripId: String, body: AddStopBody): ApiResult<TripDto> =
        EtaxiApi.wrap {
            http.post("trips/$tripId/stops") {
                authorized(tokens)
                setBody(body)
            }
        }

    suspend fun accept(tripId: String): ApiResult<TripDto> =
        EtaxiApi.wrap {
            http.post("trips/$tripId/accept") { authorized(tokens) }
        }

    suspend fun arriving(tripId: String): ApiResult<TripDto> =
        EtaxiApi.wrap {
            http.post("trips/$tripId/arriving") { authorized(tokens) }
        }

    suspend fun arrived(tripId: String): ApiResult<TripDto> =
        EtaxiApi.wrap {
            http.post("trips/$tripId/arrived") { authorized(tokens) }
        }

    suspend fun start(tripId: String): ApiResult<TripDto> =
        EtaxiApi.wrap {
            http.post("trips/$tripId/start") { authorized(tokens) }
        }

    suspend fun end(tripId: String): ApiResult<TripDto> =
        EtaxiApi.wrap {
            http.post("trips/$tripId/end") { authorized(tokens) }
        }

    suspend fun location(tripId: String, body: LocationPingBody): ApiResult<Unit> =
        EtaxiApi.wrap {
            http.post("trips/$tripId/location") {
                authorized(tokens)
                setBody(body)
            }
        }

    suspend fun get(tripId: String): ApiResult<TripDto> =
        EtaxiApi.wrap {
            http.get("trips/$tripId") { authorized(tokens) }
        }

    suspend fun rate(body: RatingBody): ApiResult<Unit> =
        EtaxiApi.wrap {
            http.post("ratings") {
                authorized(tokens)
                setBody(body)
            }
        }
}
