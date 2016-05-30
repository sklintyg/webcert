/**
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of rehabstod (https://github.com/sklintyg/rehabstod).
 *
 * rehabstod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * rehabstod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.integration.integrationtest;

import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;

/*
 * Detta är en kopia av motsvarande klass i IT. Efter införande av gradle som byggsystem
 * skall dessa testutilklasser flyttas till en test-jar i common som alla applikationer SOAP tjänster kan använda.
 *
 * Se INTYG-2391, INTYG-2536
 */
public class ClasspathResourceResolver implements LSResourceResolver {
    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        try {
            return new DOMInputImpl(publicId, systemId, baseURI, load(baseURI, systemId), null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream load(String baseURI, String name) throws IOException {
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        if (resourceAsStream == null) {
            String localName = name.replaceAll("^((\\.)+/)+", "");
            resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(localName);
        }
        return resourceAsStream;
    }

}
