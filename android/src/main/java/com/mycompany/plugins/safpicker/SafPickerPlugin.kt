package com.mycompany.plugins.safpicker

import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.getcapacitor.*
import org.json.JSONArray

@CapacitorPlugin(name = "SafPickerPlugin")
class SafPickerPlugin : Plugin() {

    private var call: PluginCall? = null

    @PluginMethod
    fun pickDirectory(call: PluginCall) {
        this.call = call

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }

        startActivityForResult(call, intent, "pickDirectoryResult")
    }

    @ActivityCallback
    fun pickDirectoryResult(call: PluginCall, result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            call.reject("Directory pick cancelled")
            return
        }

        val uri = result.data?.data ?: return call.reject("No URI")

        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        val doc = DocumentFile.fromTreeUri(context, uri)

        val ret = JSObject().apply {
            put("uri", uri.toString())
            put("name", doc?.name ?: "Storage")
        }

        call.resolve(ret)
    }

    @PluginMethod
    fun listFiles(call: PluginCall) {
        val uriStr = call.getString("uri") ?: return call.reject("Missing uri")
        val uri = Uri.parse(uriStr)

        val root = DocumentFile.fromTreeUri(context, uri)
            ?: return call.reject("Invalid folder")

        val files = JSONArray()

        for (file in root.listFiles()) {
            val obj = JSObject()
            obj.put("name", file.name)
            obj.put("uri", file.uri.toString())
            obj.put("isFolder", file.isDirectory)
            if (file.isFile) obj.put("size", file.length())
            files.put(obj)
        }

        val ret = JSObject()
        ret.put("files", files)
        call.resolve(ret)
    }
}
