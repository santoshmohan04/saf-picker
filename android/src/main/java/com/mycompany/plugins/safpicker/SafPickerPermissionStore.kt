package com.mycompany.plugins.safpicker

import android.content.ContentResolver
import android.net.Uri

data class PersistedPermissionInfo(
    val uri: Uri,
    val isRead: Boolean,
    val isWrite: Boolean,
    val persistedTime: Long?
)

object SafPickerPermissionStore {
    fun listPersistedPermissions(contentResolver: ContentResolver): List<PersistedPermissionInfo> {
        return contentResolver.persistedUriPermissions.map { permission ->
            val persistedTime = try {
                permission.persistedTime
            } catch (_: Throwable) {
                null
            }

            PersistedPermissionInfo(
                uri = permission.uri,
                isRead = permission.isReadPermission,
                isWrite = permission.isWritePermission,
                persistedTime = persistedTime
            )
        }
    }
}
