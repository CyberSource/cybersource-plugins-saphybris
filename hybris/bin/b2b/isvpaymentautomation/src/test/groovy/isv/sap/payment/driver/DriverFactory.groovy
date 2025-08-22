package isv.sap.payment.driver

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver

@SuppressWarnings('UnusedObject')
class DriverFactory {

    WebDriver createDriver(String browser) {
        String nullDevice = System.getProperty("os.name").toLowerCase().contains("win") ? "NUL" : "/dev/null"
        System.setProperty("webdriver.chrome.silentOutput", "true")
        System.setProperty("webdriver.chrome.logfile", nullDevice)

        switch (browser) {
            case 'chrome':
                return new ChromeDriver(createChromeOptions())
            case 'chrome-headless':
                return new ChromeDriver(createChromeOptions(true))
            case 'firefox':
                return new FirefoxDriver()
            default:
                return new ChromeDriver(createChromeOptions())
        }
    }

    private ChromeOptions createChromeOptions(boolean headless = false) {
        ChromeOptions options = new ChromeOptions()
        options.addArguments("--ignore-certificate-errors")         // Ignores SSL certificate errors
        options.addArguments("start-maximized")                     // Starts browser maximized (deprecated; may not work in headless mode)
        options.addArguments("enable-automation")                   // Enables automation-related switches
        options.addArguments("--no-sandbox")                        // Disables sandboxing (required in some CI environments)
        options.addArguments("--disable-infobars")                  // Disables "Chrome is being controlled by automated test software" infobar
        options.addArguments("--disable-dev-shm-usage")             // Uses disk instead of /dev/shm for shared memory (helps in Docker)
        options.addArguments("--disable-browser-side-navigation")   // Disables side navigation (older workaround for bugs)
        options.addArguments("--disable-gpu")                       // Disables GPU hardware acceleration (important for headless in some environments)
        options.addArguments("--window-size=1920,1080")             // Sets a specific window size

        if (headless) {
            // Enables headless mode using the new headless implementation
            options.addArguments("--headless=new")
        }
        // Hides the "Chrome is being controlled by automated test software" message
        options.setExperimentalOption("excludeSwitches", ["enable-automation"])
        // Prevents detection of automation by disabling Blink feature flags related to automation
        options.addArguments("--disable-blink-features=AutomationControlled")
        return options
    }
}
