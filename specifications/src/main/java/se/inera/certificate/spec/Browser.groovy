package se.inera.certificate.spec

import geb.driver.CachingDriverFactory

import org.openqa.selenium.Cookie

import se.inera.certificate.page.AbstractPage

public class Browser {

    private static geb.Browser browser

    static void öppna() {
        if (browser) throw new IllegalStateException("Browser already initialized")
        browser = new geb.Browser()
    }

    public void stäng() {
        if (!browser) throw new IllegalStateException("Browser not initialized")
        browser.quit()
        browser = null
        CachingDriverFactory.clearCache()
    }

    public void laddaOm() {
        if (!browser) throw new IllegalStateException("Browser not initialized")
        browser.driver.navigate().refresh()
        browser.drive {
            waitFor {
                js.doneLoading && js.dialogDoneLoading
            }
        }
    }

    static geb.Browser drive(Closure script) {
        if (!browser) throw new IllegalStateException("Browser not initialized")
        script.delegate = browser
        script()
        browser
    }

    static String getJSession() {
        browser.getDriver().manage().getCookieNamed("JSESSIONID").getValue()
    }

    static String deleteCookie(cookieName) {
        Cookie cookie = new Cookie(cookieName, "")
        browser.getDriver().manage().deleteCookie(cookie)
    }

    static String setCookie(cookieName, cookieValue) {
        Cookie cookie = new Cookie(cookieName, cookieValue)
        browser.getDriver().manage().addCookie(cookie)
    }
}
