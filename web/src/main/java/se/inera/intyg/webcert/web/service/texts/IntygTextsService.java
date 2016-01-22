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

package se.inera.intyg.webcert.web.service.texts;

/**
 * Service used to access the texts in a certificate.
 */
public interface IntygTextsService {
    /**
     * Returns the texts for a given type and version.
     *
     * @param intygsTyp
     *            the type
     * @param version
     *            the version
     * @return the texts as JSON
     */
    String getIntygTexts(String intygsTyp, String version);

    /**
     * Returns the latest version for <code>intygsTyp</code>.
     *
     * @param intygsTyp the type
     * @return the latest version
     */
    String getLatestVersion(String intygsTyp);
}
