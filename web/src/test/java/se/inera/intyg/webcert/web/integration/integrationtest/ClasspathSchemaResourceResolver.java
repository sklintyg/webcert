/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.integrationtest;

import java.io.IOException;
import java.io.InputStream;

import org.apache.cxf.common.xmlschema.LSInputImpl;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Resource resolver util that will search the entire classpath for a schema resource. Some schema imports (mainly
 * types) are duplicated across several resource directories.
 * This resolver will take the first found, relying on that we have unique schema systemId names.
 *
 * The wildcard search was added because of problems with how RestAssured.matchesXsd resolves relative imports.
 */
public class ClasspathSchemaResourceResolver implements LSResourceResolver {

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        try {
            return new LSInputImpl(publicId, systemId, load(systemId));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream load(String schemaSystemId) throws IOException {
        // Strip any relative path prefixes
        if (schemaSystemId.contains("/")) {
            schemaSystemId = schemaSystemId.substring(schemaSystemId.lastIndexOf('/') + 1);
        }

        // Do a wildcard classpath search..
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        String searchPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/**/" + schemaSystemId;
        Resource[] resources = resourcePatternResolver.getResources(searchPattern);

        if ((resources == null) || (resources.length == 0)) {
            throw new RuntimeException("Could not find schema [" + schemaSystemId + "]");
        }
        // We found at least 1 matching systemId - they should all be identical.
        return resources[0].getInputStream();
    }

}
