import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.safari.SafariDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.Platform

//driver = { new FirefoxDriver() } // use firefox by default
//driver = { new ChromeDriver() }
//driver = { new HtmlUnitDriver() }
//baseUrl = "http://localhost:8088"
waiting {
    timeout = 2 // default wait is two seconds
}
environments {
	saucelabschrome {
		// Login to saucelabs.com. Name: olofklason  Pwd: MittSauceLabs1
		driver = {
			DesiredCapabilities capabilities = DesiredCapabilities.chrome();
			capabilities.setCapability("version", "31");
			capabilities.setCapability("platform", Platform.WIN8);
			new RemoteWebDriver(
						  new URL("http://olofklason:da0ae2dc-0522-4c76-972c-26e4e900ecfe@ondemand.saucelabs.com:80/wd/hub"),
						  capabilities);
		}
	}
	saucelabsfirefox {
        // Login to saucelabs.com. Name: olofklason  Pwd: MittSauceLabs1
        driver = {
            DesiredCapabilities capabilities = DesiredCapabilities.firefox();
            capabilities.setCapability("version", "23");
            capabilities.setCapability("platform", Platform.WIN8);
            new RemoteWebDriver(
                          new URL("http://olofklason:da0ae2dc-0522-4c76-972c-26e4e900ecfe@ondemand.saucelabs.com:80/wd/hub"),
                          capabilities);
        }
    }
	saucelabsie {
		// Login to saucelabs.com. Name: olofklason  Pwd: MittSauceLabs1
		driver = {
			DesiredCapabilities capabilities = DesiredCapabilities.internetExplore();
			capabilities.setCapability("version", "10");
			capabilities.setCapability("platform", Platform.WIN8);
			new RemoteWebDriver(
						  new URL("http://olofklason:da0ae2dc-0522-4c76-972c-26e4e900ecfe@ondemand.saucelabs.com:80/wd/hub"),
						  capabilities);
		}
	}
    saucelabssafari {
        // Login to saucelabs.com. Name: olofklason  Pwd: MittSauceLabs1
        driver = {
            DesiredCapabilities capabilities = DesiredCapabilities.safari();
            capabilities.setCapability("version", "5");
            capabilities.setCapability("platform", "OS X 10.6");
            new RemoteWebDriver(
                          new URL("http://olofklason:da0ae2dc-0522-4c76-972c-26e4e900ecfe@ondemand.saucelabs.com:80/wd/hub"),
                          capabilities);
        }
    }
    chrome {
        driver = { new ChromeDriver() }
    }
    safari {
        driver = { new SafariDriver() }
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