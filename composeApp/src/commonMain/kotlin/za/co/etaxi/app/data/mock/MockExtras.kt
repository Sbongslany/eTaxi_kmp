package za.co.etaxi.app.data.mock

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import za.co.etaxi.app.domain.model.CardBrand
import za.co.etaxi.app.domain.model.DocumentStatus
import za.co.etaxi.app.domain.model.DocumentType
import za.co.etaxi.app.domain.model.DriverDocument
import za.co.etaxi.app.domain.model.DriverVehicle
import za.co.etaxi.app.domain.model.PaymentMethod
import za.co.etaxi.app.domain.model.VehicleClass
import za.co.etaxi.app.domain.model.VehicleStatus
import za.co.etaxi.app.domain.model.WalletTransaction
import za.co.etaxi.app.domain.model.WalletTxReason
import za.co.etaxi.app.domain.model.WalletTxType

object MockExtras {

    private val now get() = Clock.System.now()

    // ===== Rider: payment methods =====
    val paymentMethods = listOf(
        PaymentMethod("pm-1", CardBrand.VISA, "4242", 12, 2028, isDefault = true),
        PaymentMethod("pm-2", CardBrand.MASTERCARD, "4444", 6, 2027),
    )

    // ===== Driver: vehicles =====
    val driverVehicles = listOf(
        DriverVehicle(
            id = "v-1",
            make = "VW", model = "Polo Vivo",
            year = 2022, color = "White",
            plateNumber = "GP 483 284",
            vehicleClass = VehicleClass.STANDARD,
            status = VehicleStatus.APPROVED,
            isActive = true,
        ),
        DriverVehicle(
            id = "v-2",
            make = "Toyota", model = "Corolla Quest",
            year = 2021, color = "Silver",
            plateNumber = "GP 115 779",
            vehicleClass = VehicleClass.STANDARD,
            status = VehicleStatus.APPROVED,
        ),
        DriverVehicle(
            id = "v-3",
            make = "Mercedes-Benz", model = "E-Class",
            year = 2023, color = "Black",
            plateNumber = "GP 901 442",
            vehicleClass = VehicleClass.LUX,
            status = VehicleStatus.PENDING,
        ),
    )

    // ===== Driver: documents =====
    val driverDocuments = listOf(
        DriverDocument(
            type = DocumentType.ID_DOCUMENT,
            status = DocumentStatus.APPROVED,
            uploadedAt = now,
        ),
        DriverDocument(
            type = DocumentType.DRIVERS_LICENSE,
            status = DocumentStatus.APPROVED,
            uploadedAt = now,
            expiresAt = now.plus((18 * 30 * 24).toLong(), DateTimeUnit.HOUR),
        ),
        DriverDocument(
            type = DocumentType.PRDP,
            status = DocumentStatus.APPROVED,
            uploadedAt = now,
            expiresAt = now.plus((6 * 30 * 24).toLong(), DateTimeUnit.HOUR),
        ),
        DriverDocument(
            type = DocumentType.VEHICLE_REGISTRATION,
            status = DocumentStatus.APPROVED,
            uploadedAt = now,
        ),
        DriverDocument(
            type = DocumentType.ROADWORTHY,
            status = DocumentStatus.PENDING,
            uploadedAt = now,
        ),
        DriverDocument(
            type = DocumentType.OPERATING_LICENSE,
            status = DocumentStatus.REJECTED,
            uploadedAt = now,
            rejectionReason = "Document is not clear — please upload a higher-resolution scan.",
        ),
        DriverDocument(
            type = DocumentType.INSURANCE,
            status = DocumentStatus.MISSING,
        ),
        DriverDocument(
            type = DocumentType.PROFILE_PHOTO,
            status = DocumentStatus.APPROVED,
            uploadedAt = now,
        ),
    )

    // ===== Driver: wallet transactions =====
    val walletTransactions = listOf(
        WalletTransaction(
            id = "wt-1",
            amountCents = 6_760,
            type = WalletTxType.CREDIT,
            reason = WalletTxReason.TRIP_EARNING,
            description = "Trip R84.50 · Sandton → Rosebank",
            createdAt = now,
        ),
        WalletTransaction(
            id = "wt-2",
            amountCents = 12_160,
            type = WalletTxType.CREDIT,
            reason = WalletTxReason.TRIP_EARNING,
            description = "Trip R152.00 · Melrose → Maboneng",
            createdAt = now,
        ),
        WalletTransaction(
            id = "wt-3",
            amountCents = 5_000,
            type = WalletTxType.CREDIT,
            reason = WalletTxReason.BONUS,
            description = "Weekend incentive bonus",
            createdAt = now,
        ),
        WalletTransaction(
            id = "wt-4",
            amountCents = 150_000,
            type = WalletTxType.DEBIT,
            reason = WalletTxReason.PAYOUT,
            description = "Payout to bank •• 8842",
            createdAt = now,
        ),
        WalletTransaction(
            id = "wt-5",
            amountCents = 18_240,
            type = WalletTxType.CREDIT,
            reason = WalletTxReason.TRIP_EARNING,
            description = "Trip R228.00 · Sandton → OR Tambo",
            createdAt = now,
        ),
    )
}
