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

package se.inera.intyg.webcert.specifications.spec.util.screenshot;

import java.io.File;
import java.io.FileOutputStream;

public final class FileUtil {
    private FileUtil() {
    }

    /**
     * Saves byte[] to new file.
     *
     * @param baseName
     *            name for file created (without extension), if a file already
     *            exists with the supplied name an '_index' will be added.
     * @param extension
     *            extension for file.
     * @param content
     *            data to store in file.
     * @return absolute path of created file.
     */
    public static String saveToFile(String baseName, String extension, byte[] content) {
        File output = determineFilename(baseName, extension);
        try (FileOutputStream target = new FileOutputStream(output)) {
            target.write(content);
            return output.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static File determineFilename(String baseName, String extension) {
        File output = new File(baseName + "." + extension);
        // ensure directory exists
        File parent = output.getAbsoluteFile().getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IllegalArgumentException("Unable to create directory: " + parent.getAbsolutePath());
            }
        }
        int i = 0;
        // determine first filename not yet in use.
        while (output.exists()) {
            i++;
            String name = String.format("%s_%s.%s", baseName, i, extension);
            output = new File(name);
        }
        return output;
    }

}
