package app.vela.core

import app.vela.core.ev.EvArrival
import app.vela.core.ev.EvEnergy
import app.vela.core.ev.EvModels
import app.vela.core.ev.EvSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvEnergyTest {
    private val sixty = EvSpec(usableKWh = 60.0, kWhPer100km = 15.0) // 400 km on a full battery

    @Test fun `used share is distance times consumption over the battery`() {
        // 100 km x 15 kWh/100 km = 15 kWh of 60 = 25%.
        assertEquals(25.0, EvEnergy.usedPercent(100_000.0, sixty), 1e-9)
        assertEquals(400.0, sixty.rangeKm, 1e-9)
    }

    @Test fun `arrival is the start minus what the route uses`() {
        val a = EvEnergy.arrival(80, 100_000.0, sixty)
        assertEquals(25, a.usedPercent)
        assertEquals(55, a.arrivalPercent)
        assertEquals(EvArrival.Status.OK, a.status)
    }

    @Test fun `under ten percent on arrival is low`() {
        val a = EvEnergy.arrival(30, 100_000.0, sixty)
        assertEquals(5, a.arrivalPercent)
        assertEquals(EvArrival.Status.LOW, a.status)
    }

    @Test fun `running out on the way cannot reach`() {
        val a = EvEnergy.arrival(20, 100_000.0, sixty)
        assertEquals(-5, a.arrivalPercent)
        assertEquals(EvArrival.Status.CANNOT_REACH, a.status)
        assertEquals(EvArrival.Status.CANNOT_REACH, EvEnergy.arrival(25, 100_000.0, sixty).status)
    }

    @Test fun `the start percent is clamped and a zero route uses nothing`() {
        assertEquals(100, EvEnergy.arrival(130, 0.0, sixty).arrivalPercent)
        assertEquals(0, EvEnergy.arrival(50, 0.0, sixty).usedPercent)
    }

    @Test fun `an unset car uses nothing rather than dividing by zero`() {
        val a = EvEnergy.arrival(50, 100_000.0, EvSpec(0.0, 15.0))
        assertEquals(0, a.usedPercent)
        assertEquals(50, a.arrivalPercent)
    }

    @Test fun `consumption from a stated range`() {
        assertEquals(15.0, EvEnergy.kWhPer100km(60.0, 400.0), 1e-9)
        assertEquals(0.0, EvEnergy.kWhPer100km(60.0, 0.0), 1e-9)
    }

    @Test fun `every catalog car has a sane battery, consumption and a unique id`() {
        val ids = EvModels.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        EvModels.all.forEach {
            assertTrue(it.name, it.spec.usableKWh in 30.0..110.0)
            assertTrue(it.name, it.spec.kWhPer100km in 9.0..25.0)
            assertTrue(it.name, it.spec.rangeKm in 200.0..700.0)
        }
    }

    @Test fun `a catalog car gives the real-world figure`() {
        // BYD Atto 3: 60.5 kWh usable, 17.8 kWh/100 km. 170 km uses 50%.
        val atto = EvModels.byId("byd_atto3_605")!!
        val a = EvEnergy.arrival(90, 170_000.0, atto.spec)
        assertEquals(50, a.usedPercent)
        assertEquals(40, a.arrivalPercent)
    }
}
