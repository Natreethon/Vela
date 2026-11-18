package app.vela.ui.place

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.vela.R
import app.vela.core.ev.EvArrival
import app.vela.core.ev.EvEnergy
import app.vela.core.ev.EvModel
import app.vela.core.ev.EvModels
import app.vela.core.ev.EvSpec
import app.vela.ui.EvVehicle
import app.vela.ui.SheetPalette
import app.vela.ui.VelaDialog
import app.vela.ui.dpadHighlight
import app.vela.ui.formatDistance
import app.vela.ui.icons.Sym
import java.util.Locale

/** The English query the "EV charging" category chip searches with (QuickCategories). */
private const val CHARGER_QUERY = "EV charging station"

/**
 * The vehicle line on the driving directions: a pill naming the car (fuel, or the electric model
 * and its battery percent) that opens [EvVehicleDialog], and for an electric car the battery used
 * on [routeMeters] and left on arrival, with a warning and a charger search when it runs low.
 */
@Composable
internal fun EvTripRow(
    routeMeters: Double?,
    ink: Color,
    dim: Color,
    onSearchAlongRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf(false) }
    val electric = EvVehicle.electric.value
    val battery = EvVehicle.batteryPercent.intValue
    Column(modifier) {
        Row(
            Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .dpadHighlight(CircleShape)
                .clickable { editing = true }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (electric) Sym.EvStation else Sym.LocalGasStation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (electric) stringResource(R.string.ev_pill, evModelName(), battery) else stringResource(R.string.vehicle_fuel),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                Sym.ExpandMore,
                contentDescription = stringResource(R.string.vehicle_title),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(16.dp),
            )
        }
        if (electric && routeMeters != null) {
            val a = EvEnergy.arrival(battery, routeMeters, EvVehicle.spec)
            val color = when (a.status) {
                EvArrival.Status.OK -> ink
                EvArrival.Status.LOW -> SheetPalette.TrafficAmber
                EvArrival.Status.CANNOT_REACH -> MaterialTheme.colorScheme.error
            }
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.ev_arrival, a.arrivalPercent.coerceAtLeast(0), a.usedPercent),
                style = MaterialTheme.typography.bodyMedium,
                color = color,
            )
            if (a.status != EvArrival.Status.OK) {
                Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.Top) {
                    Icon(Sym.Warning, contentDescription = null, tint = color, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (a.status == EvArrival.Status.LOW) stringResource(R.string.ev_low)
                        else stringResource(R.string.ev_cannot_reach, a.usedPercent),
                        style = MaterialTheme.typography.bodyMedium,
                        color = color,
                    )
                }
                FilterChip(
                    selected = false,
                    onClick = { onSearchAlongRoute(CHARGER_QUERY) },
                    label = { Text(stringResource(R.string.ev_find_chargers)) },
                    leadingIcon = { Icon(Sym.EvStation, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    shape = CircleShape,
                    modifier = Modifier.padding(top = 4.dp).dpadHighlight(CircleShape),
                )
            } else {
                Text(stringResource(R.string.ev_estimate_hint), style = MaterialTheme.typography.bodySmall, color = dim)
            }
        }
    }
    if (editing) EvVehicleDialog(onClose = { editing = false })
}

@Composable
private fun evModelName(): String = EvVehicle.model?.name ?: stringResource(R.string.ev_custom)

private fun fmt1(v: Double) = String.format(Locale.getDefault(), "%.1f", v)

/** Parses a typed number, accepting a comma as the decimal mark. */
private fun parsePositive(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 && it < 1000.0 }

/**
 * Fuel or electric, the battery percent, and the car: a model from [EvModels] or a custom battery
 * and consumption. Nothing is kept until Save.
 */
@Composable
internal fun EvVehicleDialog(onClose: () -> Unit) {
    val context = LocalContext.current
    var electric by remember { mutableStateOf(EvVehicle.electric.value) }
    var battery by remember { mutableStateOf(EvVehicle.batteryPercent.intValue.toFloat()) }
    var modelId by remember { mutableStateOf(EvVehicle.modelId.value) }
    var kWhText by remember { mutableStateOf(fmt1(EvVehicle.customKWh.value)) }
    var consText by remember { mutableStateOf(fmt1(EvVehicle.customKWhPer100km.value)) }
    var picking by remember { mutableStateOf(false) }
    VelaDialog(
        onDismissRequest = onClose,
        title = stringResource(R.string.vehicle_title),
        confirmText = stringResource(R.string.ev_dialog_save),
        onConfirm = {
            EvVehicle.setElectric(context, electric)
            EvVehicle.setBattery(context, battery.toInt())
            EvVehicle.setModel(context, modelId)
            val kWh = parsePositive(kWhText)
            val cons = parsePositive(consText)
            if (kWh != null && cons != null) EvVehicle.setCustom(context, kWh, cons)
            onClose()
        },
        dismissText = stringResource(R.string.ev_dialog_cancel),
        onDismiss = onClose,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !electric,
                    onClick = { electric = false },
                    label = { Text(stringResource(R.string.vehicle_fuel)) },
                    leadingIcon = { Icon(Sym.LocalGasStation, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    shape = CircleShape,
                    modifier = Modifier.dpadHighlight(CircleShape),
                )
                FilterChip(
                    selected = electric,
                    onClick = { electric = true },
                    label = { Text(stringResource(R.string.vehicle_electric)) },
                    leadingIcon = { Icon(Sym.EvStation, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    shape = CircleShape,
                    modifier = Modifier.dpadHighlight(CircleShape),
                )
            }
            if (!electric) return@Column
            // Battery now: a slider for touch, minus and plus for the D-pad and fine steps.
            Text(stringResource(R.string.ev_battery_now, battery.toInt()), style = MaterialTheme.typography.titleSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { battery = (battery - 1f).coerceAtLeast(0f) }, modifier = Modifier.dpadHighlight(CircleShape)) {
                    Icon(Sym.Remove, contentDescription = stringResource(R.string.ev_battery_less))
                }
                Slider(
                    value = battery,
                    onValueChange = { battery = it.toInt().toFloat() },
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { battery = (battery + 1f).coerceAtMost(100f) }, modifier = Modifier.dpadHighlight(CircleShape)) {
                    Icon(Sym.Add, contentDescription = stringResource(R.string.ev_battery_more))
                }
            }
            // The car: the picked one, tapped to open the list.
            Text(stringResource(R.string.ev_car_model), style = MaterialTheme.typography.titleSmall)
            val picked = EvModels.byId(modelId)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .dpadHighlight(RoundedCornerShape(12.dp))
                    .clickable { picking = !picking }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(picked?.name ?: stringResource(R.string.ev_custom), style = MaterialTheme.typography.bodyLarge)
                    picked?.let { ModelSpecLine(it.spec) }
                }
                Icon(if (picking) Sym.ExpandLess else Sym.ExpandMore, contentDescription = null)
            }
            if (picking) {
                Column {
                    EvModels.all.forEach { m ->
                        ModelRow(m.name, m.spec, modelId == m.id) { modelId = m.id; picking = false }
                    }
                    ModelRow(stringResource(R.string.ev_custom), null, modelId == EvVehicle.CUSTOM) {
                        modelId = EvVehicle.CUSTOM; picking = false
                    }
                }
            }
            if (picked == null) {
                OutlinedTextField(
                    value = kWhText,
                    onValueChange = { kWhText = it },
                    label = { Text(stringResource(R.string.ev_custom_kwh)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = consText,
                    onValueChange = { consText = it },
                    label = { Text(stringResource(R.string.ev_custom_consumption)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                val kWh = parsePositive(kWhText)
                val cons = parsePositive(consText)
                if (kWh != null && cons != null) {
                    Text(
                        stringResource(R.string.ev_range_full, formatDistance(EvSpec(kWh, cons).rangeKm * 1000.0)),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                Text(
                    if (picked.basis == EvModel.Basis.REAL_WORLD) stringResource(R.string.ev_basis_real)
                    else stringResource(R.string.ev_basis_claimed, picked.ratedStandard),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ModelSpecLine(spec: EvSpec) {
    Text(
        stringResource(R.string.ev_model_spec, fmt1(spec.usableKWh), fmt1(spec.kWhPer100km), formatDistance(spec.rangeKm * 1000.0)),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ModelRow(name: String, spec: EvSpec?, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .dpadHighlight(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(name, style = MaterialTheme.typography.bodyLarge)
            spec?.let { ModelSpecLine(it) }
        }
    }
}
