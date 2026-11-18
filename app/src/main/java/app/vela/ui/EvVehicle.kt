package app.vela.ui

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import app.vela.core.ev.EvModel
import app.vela.core.ev.EvModels
import app.vela.core.ev.EvSpec

/**
 * The car the driving directions are for: fuel (the default, nothing extra on the sheet) or
 * electric, with its model or a custom battery and consumption, and the battery percent the next
 * drive starts with. The directions sheet shows the battery used and left on arrival from it
 * ([app.vela.core.ev.EvEnergy]).
 */
object EvVehicle {
    val electric = mutableStateOf(false)
    /** An [EvModels] id, or [CUSTOM]. */
    val modelId = mutableStateOf(EvModels.all.first().id)
    val batteryPercent = mutableIntStateOf(80)
    val customKWh = mutableStateOf(DEFAULT_CUSTOM_KWH)
    val customKWhPer100km = mutableStateOf(DEFAULT_CUSTOM_CONSUMPTION)

    const val CUSTOM = "custom"
    const val DEFAULT_CUSTOM_KWH = 60.0
    /** 60 kWh at 15 kWh/100 km is 400 km on a full battery. */
    const val DEFAULT_CUSTOM_CONSUMPTION = 15.0

    val model: EvModel? get() = EvModels.byId(modelId.value)

    /** The battery and consumption the estimate uses. */
    val spec: EvSpec
        get() = model?.spec ?: EvSpec(customKWh.value, customKWhPer100km.value)

    fun init(context: Context) {
        val p = prefs(context)
        electric.value = p.getBoolean(KEY_ELECTRIC, false)
        modelId.value = p.getString(KEY_MODEL, null)?.takeIf { it == CUSTOM || EvModels.byId(it) != null }
            ?: EvModels.all.first().id
        batteryPercent.intValue = p.getInt(KEY_BATTERY, 80).coerceIn(0, 100)
        customKWh.value = p.getFloat(KEY_CUSTOM_KWH, DEFAULT_CUSTOM_KWH.toFloat()).toDouble()
        customKWhPer100km.value = p.getFloat(KEY_CUSTOM_CONSUMPTION, DEFAULT_CUSTOM_CONSUMPTION.toFloat()).toDouble()
    }

    fun setElectric(context: Context, value: Boolean) {
        electric.value = value
        prefs(context).edit().putBoolean(KEY_ELECTRIC, value).apply()
    }

    fun setModel(context: Context, id: String) {
        modelId.value = id
        prefs(context).edit().putString(KEY_MODEL, id).apply()
    }

    fun setBattery(context: Context, percent: Int) {
        batteryPercent.intValue = percent.coerceIn(0, 100)
        prefs(context).edit().putInt(KEY_BATTERY, batteryPercent.intValue).apply()
    }

    fun setCustom(context: Context, kWh: Double, kWhPer100km: Double) {
        customKWh.value = kWh
        customKWhPer100km.value = kWhPer100km
        prefs(context).edit()
            .putFloat(KEY_CUSTOM_KWH, kWh.toFloat())
            .putFloat(KEY_CUSTOM_CONSUMPTION, kWhPer100km.toFloat())
            .apply()
    }

    private fun prefs(c: Context) = c.getSharedPreferences("vela_settings", Context.MODE_PRIVATE)
    private const val KEY_ELECTRIC = "vehicle_electric"
    private const val KEY_MODEL = "ev_model"
    private const val KEY_BATTERY = "ev_battery_percent"
    private const val KEY_CUSTOM_KWH = "ev_custom_kwh"
    private const val KEY_CUSTOM_CONSUMPTION = "ev_custom_kwh_per_100km"
}
