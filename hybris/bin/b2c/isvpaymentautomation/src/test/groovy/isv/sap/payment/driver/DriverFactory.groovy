package isv.sap.payment.driver

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver

@SuppressWarnings('UnusedObject')
class DriverFactory
{
    WebDriver createDriver(browser)
    {
        switch (browser)
        {
            case ('chrome'):
                new ChromeDriver(createChromeOptions())
                break
            case ('chrome-headless'):
                new ChromeDriver(createChromeOptions(true))
                break
            case ('firefox'):
                new FirefoxDriver()
                break
            default:
                new ChromeDriver(createChromeOptions())
        }
    }

    private ChromeOptions createChromeOptions(boolean headless = false)
    {
        ChromeOptions options = new ChromeOptions()
        //AGRESSIVE: options.setPageLoadStrategy(PageLoadStrategy.NONE); // https://www.skptricks.com/2018/08/timed-out-receiving-message-from-renderer-selenium.html
        //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc
        options.addArguments('start-maximized') // https://stackoverflow.com/a/26283818/1689770
        options.addArguments('enable-automation') // https://stackoverflow.com/a/43840128/1689770
        options.addArguments('--no-sandbox') //https://stackoverflow.com/a/50725918/1689770
        options.addArguments('--disable-infobars') //https://stackoverflow.com/a/43840128/1689770
        options.addArguments('--disable-dev-shm-usage') //https://stackoverflow.com/a/50725918/1689770
        options.addArguments('--disable-browser-side-navigation') //https://stackoverflow.com/a/49123152/1689770
        options.addArguments('--disable-gpu')
        options.addArguments('--window-size=1920,1080')
        options.setHeadless(headless)
        options
    }
}
