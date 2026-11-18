package app.vela.core.ev

/**
 * Electric cars sold in Thailand, for the battery estimate on the directions sheet.
 *
 * Where ev-database.org lists the car, [EvModel.spec] is its usable battery and its "EVDB Real
 * Range" vehicle consumption (a mixed real-world average, read 2026-11-18), and [EvModel.ratedKm]
 * is its WLTP range. A car it does not list takes the maker's Thai battery size and claimed range,
 * and its consumption is that range divided into the battery ([EvModel.Basis.CLAIMED]); a claimed
 * NEDC range is optimistic, so the estimate for those cars runs low. Each entry names its source.
 *
 * [EvModel.id] is stored in preferences. Never reuse or rename one; drop a car by removing it.
 */
data class EvModel(
    val id: String,
    val name: String,
    val spec: EvSpec,
    val ratedKm: Int,
    val ratedStandard: String,
    val basis: Basis,
) {
    enum class Basis { REAL_WORLD, CLAIMED }
}

object EvModels {
    private fun real(id: String, name: String, kWh: Double, wh: Int, wltp: Int) =
        EvModel(id, name, EvSpec(kWh, wh / 10.0), wltp, "WLTP", EvModel.Basis.REAL_WORLD)

    private fun claimed(id: String, name: String, kWh: Double, km: Int, standard: String) =
        EvModel(id, name, EvSpec(kWh, EvEnergy.kWhPer100km(kWh, km.toDouble())), km, standard, EvModel.Basis.CLAIMED)

    val all: List<EvModel> = listOf(
        // https://ev-database.org/car/3192/BYD-ATTO-3
        real("byd_atto3_605", "BYD Atto 3 (60.5 kWh)", 60.5, 178, 420),
        // https://ev-database.org/car/1917/BYD-DOLPHIN-449-kWh-Active
        real("byd_dolphin_449", "BYD Dolphin (44.9 kWh)", 44.9, 169, 340),
        // https://ev-database.org/car/1919/BYD-DOLPHIN-604-kWh
        real("byd_dolphin_604", "BYD Dolphin (60.4 kWh)", 60.5, 173, 427),
        // https://ev-database.org/car/3517/BYD-SEAL-614-kWh-RWD-Comfort
        real("byd_seal_614_rwd", "BYD Seal 61.4 kWh RWD", 61.4, 168, 460),
        // https://ev-database.org/car/3515/BYD-SEAL-825-kWh-RWD-Design
        real("byd_seal_825_rwd", "BYD Seal 82.5 kWh RWD", 82.5, 172, 570),
        // https://ev-database.org/car/3516/BYD-SEAL-825-kWh-AWD-Excellence
        real("byd_seal_825_awd", "BYD Seal 82.5 kWh AWD", 82.5, 185, 520),
        // https://ev-database.org/car/3070/BYD-SEALION-7-825-kWh-RWD-Comfort
        real("byd_sealion7_825_rwd", "BYD Sealion 7 82.5 kWh RWD", 82.5, 196, 482),
        // https://ev-database.org/car/3071/BYD-SEALION-7-825-kWh-AWD-Design
        real("byd_sealion7_825_awd", "BYD Sealion 7 82.5 kWh AWD", 82.5, 204, 456),
        // https://ev-database.org/car/3072/BYD-SEALION-7-913-kWh-AWD-Excellence
        real("byd_sealion7_913_awd", "BYD Sealion 7 91.3 kWh AWD", 91.3, 208, 502),
        // https://ev-database.org/car/1707/MG-MG4-Electric-51-kWh
        real("mg4_51", "MG4 Electric 51 kWh", 50.8, 169, 353),
        // https://ev-database.org/car/1708/MG-MG4-Electric-64-kWh
        real("mg4_64", "MG4 Electric 64 kWh", 61.7, 171, 465),
        // https://ev-database.org/car/1944/MG-MG4-Electric-XPOWER
        real("mg4_xpower", "MG4 XPOWER", 61.7, 193, 400),
        // https://ev-database.org/car/1541/MG-ZS-EV-Long-Range
        real("mg_zs_ev_lr", "MG ZS EV Long Range", 68.3, 185, 440),
        // https://ev-database.org/car/1781/ORA-Funky-Cat-48-kWh (sold in Thailand as ORA Good Cat)
        real("ora_good_cat_48", "GWM ORA Good Cat 48 kWh", 45.4, 175, 310),
        // https://ev-database.org/car/1814/ORA-Funky-Cat-63-kWh
        real("ora_good_cat_63", "GWM ORA Good Cat 63 kWh", 59.3, 174, 420),
        // https://ev-database.org/car/1815/ORA-Funky-Cat-GT
        real("ora_good_cat_gt", "GWM ORA Good Cat GT", 59.3, 180, 439),
        // https://ev-database.org/car/3403/Tesla-Model-3-RWD
        real("tesla_m3_rwd", "Tesla Model 3 RWD", 60.0, 133, 534),
        // https://ev-database.org/car/3331/Tesla-Model-3-Long-Range-AWD
        real("tesla_m3_lr_awd", "Tesla Model 3 Long Range AWD", 79.0, 142, 716),
        // https://ev-database.org/car/3332/Tesla-Model-3-Performance
        real("tesla_m3_perf", "Tesla Model 3 Performance", 79.0, 153, 571),
        // https://ev-database.org/car/3741/Tesla-Model-Y-RWD
        real("tesla_my_rwd", "Tesla Model Y RWD", 60.0, 158, 525),
        // https://ev-database.org/car/3104/Tesla-Model-Y-Long-Range-AWD
        real("tesla_my_lr_awd", "Tesla Model Y Long Range AWD", 75.0, 165, 586),
        // https://ev-database.org/car/3269/Tesla-Model-Y-Performance
        real("tesla_my_perf", "Tesla Model Y Performance", 79.0, 172, 580),
        // https://ev-database.org/car/3369/Changan-Deepal-S07-Standard
        real("deepal_s07", "Deepal S07", 78.0, 190, 475),
        // https://ev-database.org/car/1910/Volvo-EX30-Single-Motor-ER
        real("volvo_ex30_single_er", "Volvo EX30 Single Motor Extended Range", 65.0, 178, 476),
        // https://ev-database.org/car/1911/Volvo-EX30-Twin-Motor-Performance
        real("volvo_ex30_twin", "Volvo EX30 Twin Motor Performance", 65.0, 191, 450),
        // https://www.neta.co.th/th/news/brands-pr-news/Neta_ready_to_the_EV_in_Thailand
        // (38.5 kWh, 384 km NEDC)
        claimed("neta_v", "NETA V", 38.5, 384, "NEDC"),
        // https://www.gacgroup.com/th-th/configuration/aion-y-plus/2024 (63.2 kWh) and
        // https://www.aionthai.com/all-models/aion-y-plus/ (490 km per charge, NEDC)
        claimed("aion_y_plus_490", "AION Y Plus 490", 63.2, 490, "NEDC"),
    )

    fun byId(id: String?): EvModel? = all.firstOrNull { it.id == id }
}
