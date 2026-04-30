import XCTest
@testable import SafPickerPlugin

class SafPickerPluginTests: XCTestCase {
    func testPluginRegistrationMetadata() {
        let plugin = SafPickerPlugin()
        XCTAssertEqual(plugin.jsName, "SafPickerPlugin")
        XCTAssertEqual(plugin.identifier, "SafPickerPlugin")
    }
}
