package app.vela.core.ev

import kotlin.math.roundToInt

/** A car's battery and how much of it a kilometer takes. [usableKWh] is the energy the driver can
 *  use; [kWhPer100km] is the consumption the estimate scales the route by. */
data class EvSpec(val usableKWh: Double, val kWhPer100km: Double) {
    /** Kilometers on a full battery at [kWhPer100km]. */
    val rangeKm: Double get() = if (kWhPer100km > 0.0) usableKWh / kWhPer100km * 100.0 else 0.0

    val valid: Boolean get() = usableKWh > 0.0 && kWhPer100km > 0.0
}

/** What the battery looks like at the end of a route. */
data class EvArrival(
    /** Share of a full battery the route takes, in percent, rounded. */
    val usedPercent: Int,
    /** Battery left on arrival, in percent, rounded. Negative when the car runs out on the way. */
    val arrivalPercent: Int,
    val status: Status,
) {
    enum class Status { OK, LOW, CANNOT_REACH }
}

/**
 * The battery estimate for a drive: used = km x kWh/100 km / usable kWh x 100, and arrival =
 * start % - used. Flat consumption, no elevation, speed, heat or air conditioning; the figures in
 * [EvModels] are real-world averages, so the result is a guide, not a promise.
 */
object EvEnergy {
    /** Below this on arrival the sheet warns. */
    const val LOW_PERCENT = 10

    fun usedPercent(routeMeters: Double, spec: EvSpec): Double {
        if (!spec.valid || routeMeters <= 0.0) return 0.0
        val kWh = routeMeters / 1000.0 * spec.kWhPer100km / 100.0
        return kWh / spec.usableKWh * 100.0
    }

    fun arrival(batteryPercent: Int, routeMeters: Double, spec: EvSpec): EvArrival {
        val start = batteryPercent.coerceIn(0, 100)
        val used = usedPercent(routeMeters, spec)
        val left = start - used
        val status = when {
            left <= 0.0 -> EvArrival.Status.CANNOT_REACH
            left < LOW_PERCENT -> EvArrival.Status.LOW
            else -> EvArrival.Status.OK
        }
        return EvArrival(used.roundToInt(), left.roundToInt(), status)
    }

    /** Consumption from a stated range: what a custom car is given when only the range is known. */
    fun kWhPer100km(usableKWh: Double, rangeKm: Double): Double =
        if (usableKWh > 0.0 && rangeKm > 0.0) usableKWh / rangeKm * 100.0 else 0.0
}
