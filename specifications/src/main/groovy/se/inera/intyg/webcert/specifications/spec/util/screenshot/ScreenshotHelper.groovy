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

import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.WebDriver
import org.openqa.selenium.internal.Base64Encoder
import org.openqa.selenium.remote.Augmenter
import org.openqa.selenium.remote.ScreenshotException
import se.inera.intyg.webcert.specifications.spec.Browser

/**
 * Created by PEBE on 2015-11-27.
 *
 * Screenshot code from https://github.com/fhoeben/hsac-fitnesse-fixtures
 */
class ScreenshotHelper {

    private String fitNesseRoot = "src/test/fitnesse/FitNesseRoot";
    protected final String filesDir = getFitNesseFilesSectionDir();

    private String screenshotBase = new File(filesDir, "screenshots").getPath() + "/";
    private String screenshotHeight = "200";

    public String getFitNesseFilesSectionDir() {
        return new File(fitNesseRoot, "files").getAbsolutePath();
    }

    /**
     * @param directory sets base directory where screenshots will be stored.
     */
    public void screenshotBaseDirectory(String directory) {
        if (directory.equals("") || directory.endsWith("/") || directory.endsWith("\\")) {
            screenshotBase = directory;
        } else {
            screenshotBase = directory + "/";
        }
    }

    /**
     * @param height height to use to display screenshot images
     */
    public void screenshotShowHeight(String height) {
        screenshotHeight = height;
    }

    protected String getScreenshotLink(String screenshotFile) {
        String wikiUrl = getWikiUrl(screenshotFile);
        if (wikiUrl != null) {
            // make href to screenshot

            if ("".equals(screenshotHeight)) {
                wikiUrl = String.format("<a href=\"%s\">%s</a>",
                        wikiUrl, screenshotFile);
            } else {
                wikiUrl = String.format("<a href=\"%1\$s\"><img src=\"%1\$s\" title=\"%2\$s\" height=\"%3\$s\"/></a>", wikiUrl, screenshotFile, screenshotHeight);
            }
            screenshotFile = wikiUrl;
        }
        return screenshotFile;
    }

    protected String createScreenshot(String basename) {
        String name = getScreenshotBasename(basename);
        return takeScreenshot2(name);
    }

    protected String createScreenshot(String basename, Throwable t) {
        String screenshotFile;
        byte[] screenshotInException = findScreenshot(t);
        if (screenshotInException == null || screenshotInException.length == 0) {
            screenshotFile = createScreenshot(basename);
        } else {
            String name = getScreenshotBasename(basename);
            screenshotFile = writeScreenshot(name, screenshotInException);
        }
        return screenshotFile;
    }

    private String getScreenshotBasename(String basename) {
        return screenshotBase + basename;
    }

    /**
     * Takes screenshot of current page (as .png).
     * @param baseName name for file created (without extension),
     *                 if a file already exists with the supplied name an
     *                 '_index' will be added.
     * @return absolute path of file created.
     */
    public String takeScreenshot2(String baseName) {
        String result = null;
        WebDriver d = Browser.getDriver();
        if (!(d instanceof TakesScreenshot)) {
            d = new Augmenter().augment(d);
        }
        if (d instanceof TakesScreenshot) {
            TakesScreenshot ts = (TakesScreenshot) d;
            byte[] png = ts.getScreenshotAs(OutputType.BYTES);
            result = writeScreenshot(baseName, png);
        }
        return result;
    }

    /**
     * Finds screenshot embedded in throwable, if any.
     * @param t exception to search in.
     * @return content of screenshot (if any is present), null otherwise.
     */
    public byte[] findScreenshot(Throwable t) {
        byte[] result = null;
        if (t != null) {
            if (t instanceof ScreenshotException) {
                String encodedScreenshot = ((ScreenshotException)t).getBase64EncodedScreenshot();
                result = new Base64Encoder().decode(encodedScreenshot);
            } else {
                result = findScreenshot(t.getCause());
            }
        }
        return result;
    }

    /**
     * Saves screenshot (as .png).
     * @param baseName name for file created (without extension),
     *                 if a file already exists with the supplied name an
     *                 '_index' will be added.
     * @return absolute path of file created.
     */
    private String writeScreenshot(String baseName, byte[] png) {
        return FileUtil.saveToFile(baseName, "png", png);
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


}
