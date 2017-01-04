/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.specifications.spec.util.screenshot

import fitnesse.slim.fixtureInteraction.FixtureInteraction
import fitnesse.slim.fixtureInteraction.InteractionAwareFixture
import fitnesse.slim.test.StopTestException
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.commons.lang3.StringEscapeUtils
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.UnhandledAlertException
import org.openqa.selenium.WebDriverException
import se.inera.intyg.webcert.specifications.spec.Browser

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Created by PEBE on 2015-11-27.
 */
class ExceptionHandlingFixture implements InteractionAwareFixture {

    private ScreenshotHelper screenshotHelper = new ScreenshotHelper();

    protected final String filesDir = screenshotHelper.getFitNesseFilesSectionDir();
    private String pageSourceBase = new File(filesDir, "pagesources").getPath() + "/";

    public ExceptionHandlingFixture() {
    }

    /**
     * Takes screenshot from current page
     * @param basename filename (below screenshot base directory).
     * @return location of screenshot.
     */
    public String taSkarmbild(String basename) {
        String screenshotFile = screenshotHelper.createScreenshot(basename);
        if (screenshotFile == null) {
            throw new RuntimeException(false, "Unable to take screenshot: does the webdriver support it?");
        } else {
            screenshotFile = screenshotHelper.getScreenshotLink(screenshotFile);
        }
        return screenshotFile;
    }

    @Override
    public Object aroundSlimInvoke(FixtureInteraction interaction, Method method, Object... arguments)
            throws InvocationTargetException, IllegalAccessException {
        Object result;
        try {
            result = interaction.methodInvoke(method, this, arguments);
        } catch (Throwable t) {
            Throwable realEx = ExceptionHelper.stripReflectionException(t);
            Throwable toThrow = handleException(method, arguments, realEx);
            throw toThrow;
            if (toThrow instanceof RuntimeException) {
                throw (RuntimeException) toThrow;
            } else if (toThrow instanceof Error) {
                throw (Error) toThrow;
            }
            throw ExceptionHelper.wrapInReflectionException(toThrow);
        }
        return result;
    }

    protected Throwable handleException(Method method, Object[] arguments, Throwable t) {
        Throwable result;
        if (t instanceof UnhandledAlertException) {
            UnhandledAlertException e = (UnhandledAlertException) t;
            String alertText = e.getAlertText();
            if (alertText == null) {
                alertText = alertText();
            }
            String msgBase = "Unhandled alert: alert must be confirmed or dismissed before test can continue. Alert text: " + alertText;
            String msg = getSlimFixtureExceptionMessage("alertException", msgBase, e);
            result = new StopTestException(false, msg, t);
        } else {
            String msg = getSlimFixtureExceptionMessage("exception", null, t);
            result = new SlimFixtureException(false, msg, t);
        }
        return result;
    }

    protected String getSlimFixtureExceptionMessage(String screenshotBaseName, String messageBase, Throwable t) {
        // take a screenshot of what was on screen
        String screenShotFile = null;
        try {
            screenShotFile = screenshotHelper.createScreenshot(screenshotBaseName, t);
        } catch (UnhandledAlertException e) {
            // https://code.google.com/p/selenium/issues/detail?id=4412
            System.err.println("Unable to take screenshot while alert is present for exception: " + messageBase);
        } catch (Exception sse) {
            System.err.println("Unable to take screenshot for exception: " + messageBase);
            sse.printStackTrace();
        }
        String message = messageBase;
        if (message == null) {
            if (t == null) {
                message = "";
            } else {
                message = ExceptionUtils.getStackTrace(t);
            }
        }
        if (screenShotFile != null) {
            String label = "Page content";
            try {
                String fileName;
                if (t != null) {
                    fileName = t.getClass().getName();
                } else if (screenshotBaseName != null) {
                    fileName = screenshotBaseName;
                } else {
                    fileName = "exception";
                }
                label = savePageSource(fileName, label);
            } catch (UnhandledAlertException e) {
                // https://code.google.com/p/selenium/issues/detail?id=4412
                System.err.println("Unable to capture page source while alert is present for exception: " + messageBase);
            } catch (Exception e) {
                System.err.println("Unable to capture page source for exception: " + messageBase);
                e.printStackTrace();
            }

            String exceptionMsg = formatExceptionMsg(message);
            message = String.format("<div><table><tr class=\"exception closed\"><td>Exception details</td></tr><tr class=\"exception-detail closed-detail\"><td>%s.</td></tr><tr><td>%s:%s</td></tr></table></div>",
                    exceptionMsg, label, screenshotHelper.getScreenshotLink(screenShotFile));
        }
        return message;
    }

    private String formatExceptionMsg(String value) {
        return StringEscapeUtils.escapeHtml4(value);
    }

    /**
     * Saves current page's source to the wiki'f files section and returns a link to the
     * created file.
     * @return hyperlink to the file containing the page source.
     */
    private String savePageSource() {
        String fileName = "pageSource";
        try {
            String location = location();
            URL u = new URL(location);
            String file = FilenameUtils.getName(u.getPath());
            file = file.replaceAll("^(.*?)(\\.html?)?\$", "\$1");
            if (!"".equals(file)) {
                fileName = file;
            }
        } catch (MalformedURLException e) {
            // ignore
        }

        return savePageSource(fileName, fileName + ".html");
    }

    private String savePageSource(String fileName, String linkText) {
        String result = null;
        String html = getHtml();
        if (html != null) {
            try {
                String file = FileUtil.saveToFile(getPageSourceName(fileName), "html", html.getBytes("utf-8"));
                String wikiUrl = getWikiUrl(file);
                if (wikiUrl != null) {
                    // make href to file
                    result = String.format("<a href=\"%s\">%s</a>", wikiUrl, linkText);
                } else {
                    result = file;
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Unable to save source", e);
            }
        }
        return result;
    }

    private String getPageSourceName(String fileName) {
        return pageSourceBase + fileName;
    }

    /**
     * Converts a file path into a relative wiki path, if the path is insides the wiki's 'files' section.
     * @param filePath path to file.
     * @return relative URL pointing to the file (so a hyperlink to it can be created).
     */
    private String getWikiUrl(String filePath) {
        String wikiUrl = null;
        if (filePath.startsWith(filesDir)) {
            String relativeFile = filePath.substring(filesDir.length());
            relativeFile = relativeFile.replace('\\', '/');
            wikiUrl = "files" + relativeFile;
        }
        return wikiUrl;
    }

    /**
     * @return HTML content of current page.
     */
    private String getHtml() {
        String html;
        try {
            html = (String) executeJavascript(
                    "var node = document.doctype;\n" +
                            "var docType = '';\n" +
                            "if (node) {\n" +
                            "  docType = \"<!DOCTYPE \"\n" +
                            "+ node.name\n" +
                            "+ (node.publicId ? ' PUBLIC \"' + node.publicId + '\"' : '')\n" +
                            "+ (!node.publicId && node.systemId ? ' SYSTEM' : '') \n" +
                            "+ (node.systemId ? ' \"' + node.systemId + '\"' : '')\n" +
                            "+ '>'; }\n" +
                            "var html = document.documentElement.outerHTML " +
                            "|| '<html>' + document.documentElement.innerHTML + '</html>';\n" +
                            "return docType + html;"
            );
        } catch (RuntimeException e) {
            // unable to get via Javascript
            try {
                // this is very WebDriver implementation dependent, so we only use as fallback
                html = Browser.getDriver().getPageSource();
            } catch (Exception ex) {
                ex.printStackTrace();
                // throw original exception
                throw e;
            }
        }
        return html;
    }

    /**
     * Executes Javascript in browser. If statementPattern contains the magic variable 'arguments'
     * the parameters will also be passed to the statement. In the latter case the parameters
     * must be a number, a boolean, a String, WebElement, or a List of any combination of the above.
     * @link http://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/JavascriptExecutor.html#executeScript(java.lang.String,%20java.lang.Object...)
     * @param statementPattern javascript to run, possibly with placeholders to be replaced.
     * @param parameters placeholder values that should be replaced before executing the script.
     * @return return value from statement.
     */
    public Object executeJavascript(String statementPattern, Object... parameters) {
        Object result;
        String script = String.format(statementPattern, parameters);
        if (statementPattern.contains("arguments")) {
            result = executeScript(script, parameters);
        } else {
            result = executeScript(script);
        }
        return result;
    }

    protected Object executeScript(String script, Object... parameters) {
        Object result;
        JavascriptExecutor jse = (JavascriptExecutor) Browser.getDriver();
        try {
            result = jse.executeScript(script, parameters);
        } catch (WebDriverException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Detected a page unload event; script execution does not work across page loads.")) {
                // page reloaded while script ran, retry it once
                result = jse.executeScript(script, parameters);
            } else {
                throw e;
            }
        }
        return result;
    }

}
