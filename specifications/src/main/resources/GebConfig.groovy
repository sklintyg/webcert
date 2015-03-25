import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.safari.SafariDriver
import org.openqa.selenium.safari.SafariOptions
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.Platform

waiting {
    timeout = 4 // default wait is two seconds
}
environments {
    saucelabschrome {
        driver = {
            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            capabilities.setCapability("version", "31");
            capabilities.setCapability("platform", Platform.WIN8);
            createDriver(capabilities);
        }
    }
    saucelabsfirefox {
        driver = {
            DesiredCapabilities capabilities = DesiredCapabilities.firefox();
            capabilities.setCapability("version", "23");
            capabilities.setCapability("platform", Platform.WIN8);
            createDriver(capabilities);
        }
    }
    saucelabsie {
        driver = {
            DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
            capabilities.setCapability("version", "10");
            capabilities.setCapability("platform", Platform.WIN8);
            createDriver(capabilities);
        }
    }
    saucelabssafari {
        // Login to saucelabs.com. Name: olofklason  Pwd: MittSauceLabs1
        driver = {
            DesiredCapabilities capabilities = DesiredCapabilities.safari();
            capabilities.setCapability("version", "5");
            capabilities.setCapability("platform", "OS X 10.6");
            createDriver(capabilities);
        }
    }
    chrome {
        driver = { new ChromeDriver() }
    }
    safari {
        driver = {
            SafariOptions options = new SafariOptions(); 
            options.setUseCleanSession(true); 
            new SafariDriver(options)
        }
    }
    firefox {
        driver = { new FirefoxDriver() }
    }
    headless {
        driver = { new HtmlUnitDriver() }
    }
    'win-ie' {
        driver = {
            new RemoteWebDriver(new URL("http://windows.ci-server.local"), DesiredCapabilities.internetExplorer())
        }
    }
}

private RemoteWebDriver createDriver(DesiredCapabilities capabilities) {
    // Login to saucelabs.com. Name: olofklason  Pwd: MittSauceLabs1
    new RemoteWebDriver(
            new URL("http://olofklason:da0ae2dc-0522-4c76-972c-26e4e900ecfe@ondemand.saucelabs.com:80/wd/hub"),
            capabilities)
}