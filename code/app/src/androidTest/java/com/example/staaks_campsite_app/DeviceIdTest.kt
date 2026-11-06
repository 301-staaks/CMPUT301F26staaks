package com.example.staaks_campsite_app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.staaks_campsite_app.data.getDeviceId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * US 01.09.01: the user is identified by their device.
 */
@RunWith(AndroidJUnit4::class)
class DeviceIdTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun deviceIdIsNotBlank() {
        assertTrue(getDeviceId(context).isNotBlank())
    }

    @Test
    fun deviceIdIsSameEveryTime() {
        assertEquals(getDeviceId(context), getDeviceId(context))
    }
}
