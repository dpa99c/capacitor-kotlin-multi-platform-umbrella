import Foundation
import Capacitor
import KmpUmbrella

/// `KmpDevice` – backed by the shared Kotlin `DeviceModule` (kmp/features/device).
///
/// Both plugins of this package use the same `KmpUmbrella.xcframework`, i.e. one Kotlin runtime.
/// One plugin class per file: the Capacitor CLI registers only the first Objective-C-named class of each
/// file – and its regex also matches that attribute inside comments, so never write it in a comment.
@objc(KmpDevicePlugin)
public class KmpDevicePlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "KmpDevicePlugin"
    public let jsName = "KmpDevice"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "getDeviceInfo", returnType: CAPPluginReturnPromise)
    ]

    private lazy var kmp = KmpBridge(module: DeviceModule())

    deinit {
        kmp.close()
    }

    @objc func getDeviceInfo(_ call: CAPPluginCall) {
        KmpCapacitor.forward(call) { method, args, onSuccess, onError in
            kmp.call(method: method, argsJson: args, onSuccess: onSuccess, onError: onError)
        }
    }
}
