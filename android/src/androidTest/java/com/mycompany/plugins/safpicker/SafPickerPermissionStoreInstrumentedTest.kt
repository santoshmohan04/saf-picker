package com.mycompany.plugins.safpicker

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SafPickerPermissionStoreInstrumentedTest {
    @Test
    fun listPersistedPermissionsReturnsUris() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val permissions = SafPickerPermissionStore.listPersistedPermissions(context.contentResolver)

        permissions.forEach { permission ->
            assertNotNull(permission.uri)
        }
    }
}
