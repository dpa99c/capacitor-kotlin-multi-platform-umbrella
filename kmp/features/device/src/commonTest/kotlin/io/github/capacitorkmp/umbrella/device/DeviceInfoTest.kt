package io.github.capacitorkmp.umbrella.device

import io.github.capacitorkmp.umbrella.device.DeviceInfoService
import io.github.capacitorkmp.umbrella.device.FormFactor
import io.github.capacitorkmp.umbrella.device.OsVersion
import io.github.capacitorkmp.umbrella.device.RawDeviceInfo
import io.github.capacitorkmp.umbrella.device.UserAgentParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OsVersionTest {
    @Test
    fun parsesLenientFormats() {
        assertEquals(OsVersion(16), OsVersion.parse("16"))
        assertEquals(OsVersion(17, 4, 1), OsVersion.parse("17.4.1"))
        assertEquals(OsVersion(10, 15, 7), OsVersion.parse("10_15_7"))
        assertEquals(OsVersion(14), OsVersion.parse("14 (API 34)"))
        assertNull(OsVersion.parse("unknown"))
    }

    @Test
    fun comparesComponentWise() {
        assertTrue(OsVersion(15, 0) < OsVersion(15, 0, 1))
        assertTrue(OsVersion(9, 9) < OsVersion(10))
        assertEquals("17.4.1", OsVersion(17, 4, 1).toString())
        assertEquals("16.0", OsVersion(16).toString())
    }
}

class UserAgentParserTest {
    @Test
    fun iPhoneSafari() {
        val ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Mobile/15E148 Safari/604.1"
        val info = UserAgentParser.parse(ua, maxTouchPoints = 5)
        assertEquals("iOS", info.osName)
        assertEquals("17.4", info.osVersion)
        assertEquals("Safari", info.browserName)
        assertEquals("iPhone", info.deviceModel)
        assertTrue(info.isMobile)
    }

    @Test
    fun androidChrome() {
        val ua = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
        val info = UserAgentParser.parse(ua)
        assertEquals("Android", info.osName)
        assertEquals("14", info.osVersion)
        assertEquals("Pixel 8", info.deviceModel)
        assertEquals("Chrome", info.browserName)
        assertEquals("124.0.0.0", info.browserVersion)
    }

    @Test
    fun desktopBrowsers() {
        val mac = UserAgentParser.parse("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.1 Safari/605.1.15")
        assertEquals("macOS", mac.osName)
        assertEquals("Safari", mac.browserName)
        assertFalse(mac.isMobile)

        val edge = UserAgentParser.parse("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36 Edg/124.0.2478.80")
        assertEquals("Windows", edge.osName)
        assertEquals("Edge", edge.browserName)

        val firefox = UserAgentParser.parse("Mozilla/5.0 (X11; Linux x86_64; rv:125.0) Gecko/20100101 Firefox/125.0")
        assertEquals("Linux", firefox.osName)
        assertEquals("Firefox", firefox.browserName)
    }

    @Test
    fun iPadDesktopModeIsDetectedViaTouch() {
        val ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15"
        val info = UserAgentParser.parse(ua, maxTouchPoints = 5)
        assertEquals("iPadOS", info.osName)
        assertEquals("17.4", info.osVersion)
    }
}

class DeviceInfoServiceTest {
    private val pixel = RawDeviceInfo(
        platform = "android", manufacturer = "google", model = "Pixel 9 Pro", osName = "Android", osVersion = "16",
        locale = "en_GB", screenWidth = 411.4, screenHeight = 914.3, screenScale = 3.5, isVirtual = true,
    )

    @Test
    fun normalisesAndroidDevice() {
        val info = DeviceInfoService.normalize(pixel)
        assertEquals("Google", info.manufacturer)
        assertEquals("en-GB", info.locale)
        assertEquals("GB", info.region)
        assertEquals(FormFactor.PHONE, info.formFactor)
        assertEquals(1440, info.screen.physicalWidth)
        assertTrue(info.isOsSupported)
        assertEquals("7.0", info.minimumOsVersion)
        assertEquals("Google Pixel 9 Pro · Android 16.0 · en-GB · virtual", info.summary)
    }

    @Test
    fun appliesMinimumOsPolicy() {
        val old = DeviceInfoService.normalize(pixel.copy(osVersion = "6.0.1"))
        assertFalse(old.isOsSupported)
        val oldIos = DeviceInfoService.normalize(RawDeviceInfo(platform = "ios", osName = "iOS", osVersion = "14.8", model = "iPhone"))
        assertFalse(oldIos.isOsSupported)
        assertEquals("Apple", oldIos.manufacturer)
    }

    @Test
    fun classifiesFormFactors() {
        assertEquals(FormFactor.TABLET, DeviceInfoService.normalize(pixel.copy(screenWidth = 800.0, screenHeight = 1280.0)).formFactor)
        assertEquals(FormFactor.TABLET, DeviceInfoService.normalize(RawDeviceInfo(platform = "ios", osName = "iPadOS", model = "iPad")).formFactor)
        val desktop = RawDeviceInfo(
            platform = "web", screenWidth = 1920.0, screenHeight = 1080.0, locale = "de-DE",
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        )
        val info = DeviceInfoService.normalize(desktop)
        assertEquals(FormFactor.DESKTOP, info.formFactor)
        assertEquals("Windows", info.operatingSystem)
        assertEquals("Chrome 124", info.browser)
    }

    @Test
    fun normalisesLocales() {
        assertEquals("en-GB", DeviceInfoService.normalizeLocale("en_GB").tag)
        assertEquals("zh-Hant-TW", DeviceInfoService.normalizeLocale("zh_Hant_TW").tag)
        assertEquals("fr", DeviceInfoService.normalizeLocale("fr").tag)
        assertEquals("en-US", DeviceInfoService.normalizeLocale("en_US@calendar=gregorian").tag)
        assertEquals("und", DeviceInfoService.normalizeLocale(null).tag)
    }
}
