package se.inera.certificate.page

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import se.inera.certificate.spec.Browser
import geb.Page

import java.util.concurrent.TimeUnit

import static org.openqa.selenium.By.id

abstract class AbstractPage extends Page {

    static content = {
        modalBackdrop(required:false) {$('.modal-backdrop')}
    }

    static boolean doneLoading() {
        waitForAngularBootstrap();
        waitForAngularRequestsToFinish();
        true
    }

    /**
     * Executes Javascript in browser and then waits for 'callback' to be invoked.
     * If statementPattern should reference the magic (function) variable 'callback' which should be
     * called to provide this method's result.
     * If the statementPattern contains the magic variable 'arguments'
     * the parameters will also be passed to the statement. In the latter case the parameters
     * must be a number, a boolean, a String, WebElement, or a List of any combination of the above.
     * @link http://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/JavascriptExecutor.html#executeAsyncScript(java.lang.String,%20java.lang.Object...)
     * @param statementPattern javascript to run, possibly with placeholders to be replaced.
     * @param parameters placeholder values that should be replaced before executing the script.
     * @return return value from statement.
     */
    static public Object waitForJavascriptCallback(String statementPattern, Object... parameters) {
        def driver = Browser.getDriver();
        driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
        Object result = "";
        String script = "var callback = arguments[arguments.length - 1];" + String.format(statementPattern, parameters);
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        if (statementPattern.contains("arguments")) {
            result = jse.executeAsyncScript(script, parameters);
        } else {
            result = jse.executeAsyncScript(script);
        }
        return result;
    }

    static protected void waitForAngularBootstrap(String root) {
        if (root == null) {
            root = "body"
        }
        Object result = waitForJavascriptCallback(NgClientSideScripts.TEST_FOR_ANGULAR_TESTABILITY, root, 10);
        if (!result[0]) {
            throw new RuntimeException(result[1].toString());
        }
    }

    static protected void waitForAngularRequestsToFinish(String root) {
        if (root == null) {
            root = "body"
        }
        Object result = waitForJavascriptCallback(NgClientSideScripts.WAIT_FOR_ANGULAR, root);
        if (result != null) {
            throw new RuntimeException(result.toString());
        }
    }

    static void scrollIntoView(elementId, boolean waitUntilClickable = true) {
        def jqScrollToVisible = "jQuery(\'#" + elementId + "\')[0].scrollIntoView();var current=jQuery('body').scrollTop(); jQuery('body').scrollTop(current-400);"
        Browser.drive {
            js.exec(jqScrollToVisible)
        }
        if (waitUntilClickable) {
            WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 5);
            wait.until(ExpectedConditions.elementToBeClickable(By.id(elementId)));
        }
    }

    static boolean isButtonDisabled(button){
        return button.@disabled == 'true';
    }

    def waitForModalBackdropToHide(){
        waitFor {
            return !modalBackdrop.isDisplayed();
        }
    }

    def elementForId(elementId){
        return $("#" + elementId);
    }

    def elementForClass(classId){
        return $("." + classId);
    }

}
