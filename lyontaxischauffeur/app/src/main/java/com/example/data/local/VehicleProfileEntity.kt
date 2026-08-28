package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_profile")
data class VehicleProfileEntity(
    @PrimaryKey val id: Int = 1,
    val model: String = "Mercedes-Benz Classe E 300de",
    val plate: String = "FR-789-AB",
    val color: String = "Noir Obsidienne Métallisé",
    val category: String = "Aber Berline & Confort",
    val inspectionStatus: String = "Conforme & Validé",
    val lastInspectionDate: String = "12/06/2026",
    val nextInspectionDate: String = "12/06/2027",
    val mileageKm: Int = 64250,
    val technicalNotes: String = "Pneus neufs Michelin CrossClimate 2, révision des 60k km effectuée en concession.",
    val insuranceCompany: String = "AXA Pro Circulation VTC",
    val insuranceExpiry: String = "15/11/2026",
    val isHybridOrElectric: Boolean = true,
    val fuelType: String = "Hybride Rechargeable Diesel / Électrique",
    val passengerCapacity: Int = 4,
    val luggageCapacity: Int = 3
)
