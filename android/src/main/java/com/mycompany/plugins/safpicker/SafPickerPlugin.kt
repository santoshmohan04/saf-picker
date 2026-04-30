package com.mycompany.plugins.safpicker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.getcapacitor.ActivityCallback
import com.getcapacitor.ActivityResult
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import org.json.JSONArray

@CapacitorPlugin(name = "SafPickerPlugin")
class SafPickerPlugin : Plugin() {

    private object ErrorCode {
        const val CANCELLED = "CANCELLED"
        const val INVALID_URI = "INVALID_URI"
        const val INVALID_OPTIONS = "INVALID_OPTIONS"
        const val NO_PERMISSION = "NO_PERMISSION"
        const val UNKNOWN = "UNKNOWN"
    }

    private companion object {
        const val FALLBACK_DIRECTORY_NAME = "Storage"
        const val FALLBACK_FILE_NAME = "Unnamed"
    }

    private var pendingPermissionFlags: Int =
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    @PluginMethod
    fun pickDirectory(call: PluginCall) {
        val allowRead = call.getBoolean("allowRead", true) ?: true
        val allowWrite = call.getBoolean("allowWrite", true) ?: true
        val intentFlags = buildPermissionFlags(allowRead, allowWrite, includePersistable = true)

        if (intentFlags == 0) {
            call.reject("At least one permission flag must be requested.", ErrorCode.INVALID_OPTIONS)
            return
        }

        pendingPermissionFlags = buildPermissionFlags(allowRead, allowWrite, includePersistable = false)

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(intentFlags)
        }

        val showAdvanced = call.getBoolean("showAdvanced", false) ?: false
        val showStorageRoots = call.getBoolean("showStorageRoots", false) ?: false
        if (showAdvanced || showStorageRoots) {
            setAdvancedExtras(intent)
        }

        val initialUriStr = call.getString("initialUri")
        if (!initialUriStr.isNullOrBlank()) {
            val initialUri = parseUri(initialUriStr)
            if (initialUri == null) {
                call.reject("Invalid initial URI.", ErrorCode.INVALID_URI)
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
            }
        }

        startActivityForResult(call, intent, "pickDirectoryResult")
    }

    @ActivityCallback
    fun pickDirectoryResult(call: PluginCall, result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            call.reject("Directory pick cancelled.", ErrorCode.CANCELLED)
            return
        }

        val uri = result.data?.data
        if (uri == null) {
            call.reject("No URI returned from picker.", ErrorCode.INVALID_URI)
            return
        }

        try {
            if (pendingPermissionFlags != 0) {
                context.contentResolver.takePersistableUriPermission(uri, pendingPermissionFlags)
            }
        } catch (error: SecurityException) {
            call.reject("Permission not granted for selected directory.", ErrorCode.NO_PERMISSION)
            return
        }

        val doc = DocumentFile.fromTreeUri(context, uri)
        if (doc == null) {
            call.reject("Invalid directory URI.", ErrorCode.INVALID_URI)
            return
        }

        val ret = JSObject()
        ret.put("uri", uri.toString())
        val directoryName = doc.name ?: doc.uri.lastPathSegment ?: FALLBACK_DIRECTORY_NAME
        ret.put("name", directoryName)
        putIfNotNull(ret, "mimeType", doc.type)
        putIfNotNull(ret, "lastModified", doc.lastModified())
        ret.put("canRead", doc.canRead())
        ret.put("canWrite", doc.canWrite())
        putIfNotNull(ret, "documentId", documentIdForUri(uri, isTree = true))

        call.resolve(ret)
    }

    @PluginMethod
    fun listFiles(call: PluginCall) {
        val uriStr = call.getString("uri")
        if (uriStr.isNullOrBlank()) {
            call.reject("Missing uri.", ErrorCode.INVALID_URI)
            return
        }

        val uri = parseUri(uriStr)
        if (uri == null) {
            call.reject("Invalid uri.", ErrorCode.INVALID_URI)
            return
        }

        val root = DocumentFile.fromTreeUri(context, uri)
        if (root == null) {
            call.reject("Invalid folder.", ErrorCode.INVALID_URI)
            return
        }

        if (!root.canRead()) {
            call.reject("No permission to read this directory.", ErrorCode.NO_PERMISSION)
            return
        }

        val filter = parseFilter(call.getString("filter"))
        val sortBy = parseSortBy(call.getString("sortBy"))
        val sortOrder = parseSortOrder(call.getString("sortOrder"))
        val offset = call.getInt("offset", 0) ?: 0
        val maxItems = call.getInt("maxItems")

        if (filter == null || sortBy == null || sortOrder == null) {
            call.reject("Invalid list options.", ErrorCode.INVALID_OPTIONS)
            return
        }

        val entries = root.listFiles().map { file ->
            SafPickerListing(
                payload = file,
                name = file.name,
                isDirectory = file.isDirectory,
                size = if (file.isFile) file.length() else 0L,
                lastModified = file.lastModified()
            )
        }

        val page = SafPickerListingProcessor.process(
            items = entries,
            filter = filter,
            sortBy = sortBy,
            sortOrder = sortOrder,
            offset = offset,
            maxItems = maxItems
        )

        val files = JSONArray()
        page.items.forEach { listing ->
            val file = listing.payload
            val obj = JSObject()
            val fileName = listing.name ?: file.uri.lastPathSegment ?: FALLBACK_FILE_NAME
            obj.put("name", fileName)
            obj.put("uri", file.uri.toString())
            obj.put("isFolder", listing.isDirectory)
            if (file.isFile) {
                obj.put("size", file.length())
            }
            putIfNotNull(obj, "mimeType", file.type)
            putIfNotNull(obj, "lastModified", listing.lastModified)
            obj.put("canRead", file.canRead())
            obj.put("canWrite", file.canWrite())
            putIfNotNull(obj, "documentId", documentIdForUri(file.uri, isTree = false))
            files.put(obj)
        }

        val ret = JSObject()
        ret.put("files", files)
        ret.put("totalCount", page.totalCount)
        page.nextOffset?.let { ret.put("nextOffset", it) }
        call.resolve(ret)
    }

    @PluginMethod
    fun persistPermissions(call: PluginCall) {
        val uriStr = call.getString("uri")
        if (uriStr.isNullOrBlank()) {
            call.reject("Missing uri.", ErrorCode.INVALID_URI)
            return
        }

        val uri = parseUri(uriStr)
        if (uri == null) {
            call.reject("Invalid uri.", ErrorCode.INVALID_URI)
            return
        }

        val allowRead = call.getBoolean("allowRead", true) ?: true
        val allowWrite = call.getBoolean("allowWrite", true) ?: true
        val permissionFlags = buildPermissionFlags(allowRead, allowWrite, includePersistable = false)

        if (permissionFlags == 0) {
            call.reject("At least one permission flag must be requested.", ErrorCode.INVALID_OPTIONS)
            return
        }

        try {
            context.contentResolver.takePersistableUriPermission(uri, permissionFlags)
        } catch (error: SecurityException) {
            call.reject("Permission not granted for requested URI.", ErrorCode.NO_PERMISSION)
            return
        }

        val persistedPermission = findPersistedPermission(uri)
        val response = JSObject()
        response.put("uri", uri.toString())
        response.put("isRead", persistedPermission?.isRead ?: allowRead)
        response.put("isWrite", persistedPermission?.isWrite ?: allowWrite)
        putIfNotNull(response, "persistedTime", persistedPermission?.persistedTime)
        call.resolve(response)
    }

    @PluginMethod
    fun releasePermissions(call: PluginCall) {
        val uriStr = call.getString("uri")
        if (uriStr.isNullOrBlank()) {
            call.reject("Missing uri.", ErrorCode.INVALID_URI)
            return
        }

        val uri = parseUri(uriStr)
        if (uri == null) {
            call.reject("Invalid uri.", ErrorCode.INVALID_URI)
            return
        }

        val allowRead = call.getBoolean("allowRead", true) ?: true
        val allowWrite = call.getBoolean("allowWrite", true) ?: true
        val permissionFlags = buildPermissionFlags(allowRead, allowWrite, includePersistable = false)

        if (permissionFlags == 0) {
            call.reject("At least one permission flag must be requested.", ErrorCode.INVALID_OPTIONS)
            return
        }

        try {
            context.contentResolver.releasePersistableUriPermission(uri, permissionFlags)
        } catch (error: SecurityException) {
            call.reject("Permission not granted for requested URI.", ErrorCode.NO_PERMISSION)
            return
        }

        val response = JSObject()
        response.put("released", true)
        call.resolve(response)
    }

    @PluginMethod
    fun listPersistedPermissions(call: PluginCall) {
        val permissions = SafPickerPermissionStore.listPersistedPermissions(context.contentResolver)
        val payload = JSONArray()

        permissions.forEach { permission ->
            val obj = JSObject()
            obj.put("uri", permission.uri.toString())
            obj.put("isRead", permission.isRead)
            obj.put("isWrite", permission.isWrite)
            putIfNotNull(obj, "persistedTime", permission.persistedTime)
            payload.put(obj)
        }

        val ret = JSObject()
        ret.put("permissions", payload)
        call.resolve(ret)
    }

    private fun buildPermissionFlags(
        allowRead: Boolean,
        allowWrite: Boolean,
        includePersistable: Boolean
    ): Int {
        var flags = 0
        if (allowRead) {
            flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        if (allowWrite) {
            flags = flags or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }
        if (includePersistable && flags != 0) {
            flags = flags or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        return flags
    }

    private fun parseUri(value: String): Uri? {
        return try {
            Uri.parse(value)
        } catch (_: Throwable) {
            null
        }
    }

    private fun setAdvancedExtras(intent: Intent) {
        try {
            intent.putExtra(DocumentsContract.EXTRA_SHOW_ADVANCED, true)
        } catch (_: Throwable) {
            // Ignore if not supported on this Android version.
        }
    }

    private fun documentIdForUri(uri: Uri, isTree: Boolean): String? {
        return try {
            if (isTree) {
                DocumentsContract.getTreeDocumentId(uri)
            } else {
                DocumentsContract.getDocumentId(uri)
            }
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun putIfNotNull(target: JSObject, key: String, value: Any?) {
        if (value != null) {
            target.put(key, value)
        }
    }

    private fun parseFilter(value: String?): SafPickerFilter? {
        return when (value?.lowercase()) {
            null, "all" -> SafPickerFilter.ALL
            "files" -> SafPickerFilter.FILES
            "folders" -> SafPickerFilter.FOLDERS
            else -> null
        }
    }

    private fun parseSortBy(value: String?): SafPickerSortBy? {
        return when (value?.lowercase()) {
            null, "name" -> SafPickerSortBy.NAME
            "size" -> SafPickerSortBy.SIZE
            "lastmodified", "last_modified", "last-modified" -> SafPickerSortBy.LAST_MODIFIED
            else -> null
        }
    }

    private fun parseSortOrder(value: String?): SafPickerSortOrder? {
        return when (value?.lowercase()) {
            null, "asc" -> SafPickerSortOrder.ASC
            "desc" -> SafPickerSortOrder.DESC
            else -> null
        }
    }

    private fun findPersistedPermission(uri: Uri): PersistedPermissionInfo? {
        return SafPickerPermissionStore.listPersistedPermissions(context.contentResolver)
            .firstOrNull { it.uri == uri }
    }
}
