package se.inera.certificate.page

import org.openqa.selenium.JavascriptExecutor
import se.inera.certificate.spec.Browser
import geb.Page

import java.util.concurrent.TimeUnit

abstract class AbstractPage extends Page {

    static content = {
        modalBackdrop(required:false) {$('.modal-backdrop')}
    }

    static boolean doneLoading() {
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

    static protected void waitForAngularRequestsToFinish(String root) {
        if (root == null) {
            root = "body"
        }
        Object result = waitForJavascriptCallback(NgClientSideScripts.WaitForAngular, root);
        if (result != null) {
            throw new RuntimeException(result.toString());
        }
    }

    static void scrollIntoView(elementId){
        def jqScrollToVisible = "jQuery(\'#" + elementId + "\')[0].scrollIntoView();var current=jQuery('body').scrollTop(); jQuery('body').scrollTop(current-400);"
        Browser.drive {
            js.exec(jqScrollToVisible)
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
