import XCTest
import KmpUmbrella
@testable import KmpUmbrellaPlugin

class KmpUmbrellaTests: XCTestCase {
    private func call(_ bridge: KmpBridge, _ method: String, _ args: String? = nil) -> [String: Any] {
        let done = expectation(description: method)
        var result: [String: Any] = [:]
        bridge.call(method: method, argsJson: args, onSuccess: { json in
            result = (try? JSONSerialization.jsonObject(with: Data(json.utf8)) as? [String: Any]) ?? [:]
            done.fulfill()
        }, onError: { code, message in
            XCTFail("\(code): \(message)")
            done.fulfill()
        })
        wait(for: [done], timeout: 5)
        return result
    }

    func testBothFeaturesFromOneFramework() {
        let device = KmpBridge(module: DeviceModule())
        let motion = KmpBridge(module: MotionModule())
        defer { device.close(); motion.close() }

        XCTAssertEqual(call(device, "getDeviceInfo")["platform"] as? String, "ios")

        // The motion feature asks the device feature (plain Kotlin call) whether this is a simulator.
        let availability = call(motion, "isMotionAvailable")
        XCTAssertEqual(availability["isVirtualDevice"] as? Bool, true)
        XCTAssertEqual(availability["recommendedSource"] as? String, "simulated")
    }

    func testSimulatedMotionEmitsEvents() {
        let motion = KmpBridge(module: MotionModule())
        defer { motion.close() }
        let event = expectation(description: "motion event")
        event.assertForOverFulfill = false
        motion.setEventListener { name, _ in
            if name == "motion" { event.fulfill() }
        }
        _ = call(motion, "startMotionUpdates", "{\"source\":\"simulated\",\"intervalMs\":20}")
        wait(for: [event], timeout: 5)
        XCTAssertGreaterThan(call(motion, "stopMotionUpdates")["samples"] as? Int ?? 0, 0)
    }
}
