package app.vela.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/**
 * Whether the basemap's `building-3d` fill-extrusion layer renders (z16+). Extrusion is
 * the most fragment-shader-expensive thing the map draws, so this is the one-tap lever
 * for "panning stutters when zoomed in" on weaker GPUs. ON by default; the flat
 * footprints (a separate fill layer) always stay.
 */
object Buildings3d {
    val on = mutableStateOf(true)

    fun init(context: Context) {
        on.value = prefs(context).getBoolean(KEY, true)
    }

    fun set(context: Context, value: Boolean) {
        on.value = value
        prefs(context).edit().putBoolean(KEY, value).apply()
    }

    private fun prefs(c: Context) = c.getSharedPreferences("vela_settings", Context.MODE_PRIVATE)
    private const val KEY = "buildings_3d"
}

/**
 * Settings > Navigation "Show 3D buildings while driving" (pref `nav_buildings_3d`, on in this
 * build). The drive camera otherwise hides the `building-3d` extrusions to save battery; on, they
 * draw while the drive camera is tilted, with [Buildings3d] on and satellite imagery off.
 */
object NavBuildings3d {
    val on = mutableStateOf(true)

    fun init(context: Context) {
        on.value = prefs(context).getBoolean(KEY, true)
    }

    fun set(context: Context, value: Boolean) {
        on.value = value
        prefs(context).edit().putBoolean(KEY, value).apply()
    }

    private fun prefs(c: Context) = c.getSharedPreferences("vela_settings", Context.MODE_PRIVATE)
    private const val KEY = "nav_buildings_3d"
}

/**
 * Settings > Navigation "Android Auto: turn and tilt the map while navigating" (pref
 * `car_nav_heading_up`, on by default). On, the car screen's drive map turns with the car and
 * tilts like the phone's drive camera even with Settings > Map "Keep north up" on, which used to
 * leave the car's drive map flat and north-up. Off, the car's drive map stays north-up and flat.
 * The car's overview button still frames the route north-up either way.
 */
object CarHeadingUp {
    val on = mutableStateOf(true)

    fun init(context: Context) {
        on.value = prefs(context).getBoolean(KEY, true)
    }

    fun set(context: Context, value: Boolean) {
        on.value = value
        prefs(context).edit().putBoolean(KEY, value).apply()
    }

    private fun prefs(c: Context) = c.getSharedPreferences("vela_settings", Context.MODE_PRIVATE)
    private const val KEY = "car_nav_heading_up"
}
