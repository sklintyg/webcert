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

package se.inera.intyg.common.specifications.spec.util

class FitnesseHelper {

    private static final String FITNESSE_WORKING = System.getProperty("fitnesse.working", ".")
    private static final String FITNESSE_ROOT_DIR = "${FITNESSE_WORKING}${File.separatorChar}FitNesseRoot"
    private static final String FILES_DIR = "${File.separatorChar}files${File.separatorChar}"

    private FitnesseHelper() {

    }

    /**
     * Get a File from the files folder.
     *
     * @param fileName the filename relative the <code>files</code> folder.
     * @return the File
     */
    public static File getFile(String fileName) {
        File file;
        // Normalize fileName for current platform
        String platformFileName = fileName.replace('/' as char, File.separatorChar).replace('\\' as char, File.separatorChar);
        if (platformFileName.startsWith(FILES_DIR)) {
            // References a file within the "files" directory
            file = new File(FITNESSE_ROOT_DIR + platformFileName);
        } else if (platformFileName.startsWith(File.separator) ||
                  (platformFileName.length() >= 2 && 
                   Character.isLetter(platformFileName.charAt(0)) &&
                   platformFileName.charAt(1) == ':')) {
            // Interpret as absolute file name
            file = new File(platformFileName);
        } else {
            // Interpret as file name relative to the "files" directory
            file = new File(FITNESSE_ROOT_DIR + FILES_DIR + platformFileName);
        }
        return file;
    }

    /**
     * Get the content of a File from the files folder as a String.
     *
     * @param fileName the fileName relative the <code>files</code> folder.
     * @return the File content as a String
     * @throws FileNotFoundException if file not found of not readable
     */
    public static String getFileAsString(String fileName) throws FileNotFoundException {
        getFile(fileName).text
    }


}
