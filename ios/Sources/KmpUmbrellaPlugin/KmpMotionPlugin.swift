import Foundation
import Capacitor
import KmpUmbrella

/// `KmpMotion` – backed by the shared Kotlin `MotionModule` (kmp/features/motion).
@objc(KmpMotionPlugin)
public class KmpMotionPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "KmpMotionPlugin"
    public let jsName = "KmpMotion"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "isMotionAvailable", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "requestMotionPermission", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "startMotionUpdates", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "stopMotionUpdates", returnType: CAPPluginReturnPromise)
    ]

    private lazy var kmp = KmpBridge(module: MotionModule())

    override public func load() {
        KmpCapacitor.connectEvents(of: self) { listener in kmp.setEventListener(listener: listener) }
    }

    deinit {
        kmp.close()
    }

    @objc func isMotionAvailable(_ call: CAPPluginCall) { forward(call) }
    @objc func requestMotionPermission(_ call: CAPPluginCall) { forward(call) }
    @objc func startMotionUpdates(_ call: CAPPluginCall) { forward(call) }
    @objc func stopMotionUpdates(_ call: CAPPluginCall) { forward(call) }

    private func forward(_ call: CAPPluginCall) {
        KmpCapacitor.forward(call) { method, args, onSuccess, onError in
            kmp.call(method: method, argsJson: args, onSuccess: onSuccess, onError: onError)
        }
    }
}
