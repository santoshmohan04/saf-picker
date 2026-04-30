import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(SafPickerPlugin)
public class SafPickerPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "SafPickerPlugin"
    public let jsName = "SafPickerPlugin"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "pickDirectory", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "listFiles", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "persistPermissions", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "releasePermissions", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "listPersistedPermissions", returnType: CAPPluginReturnPromise)
    ]

    private func rejectUnsupported(_ call: CAPPluginCall) {
        call.reject("SAF picker is only available on Android.", "UNSUPPORTED")
    }

    @objc func pickDirectory(_ call: CAPPluginCall) {
        rejectUnsupported(call)
    }

    @objc func listFiles(_ call: CAPPluginCall) {
        rejectUnsupported(call)
    }

    @objc func persistPermissions(_ call: CAPPluginCall) {
        rejectUnsupported(call)
    }

    @objc func releasePermissions(_ call: CAPPluginCall) {
        rejectUnsupported(call)
    }

    @objc func listPersistedPermissions(_ call: CAPPluginCall) {
        rejectUnsupported(call)
    }
}
