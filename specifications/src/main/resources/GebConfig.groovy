import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.safari.SafariDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver

driver = { new FirefoxDriver() } // use firefox by default
//driver = { new ChromeDriver() }
//driver = { new HtmlUnitDriver() }
//baseUrl = "http://localhost:8088"
waiting {
    timeout = 2 // default wait is two seconds
}
environments {
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