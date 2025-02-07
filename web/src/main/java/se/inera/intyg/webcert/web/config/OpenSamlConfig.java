/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.config;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OpenSamlConfig implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        initializeOpenSamlConfiguration();
    }

    private void initializeOpenSamlConfiguration() {
        try {
            final var registry = new XMLObjectProviderRegistry();
            ConfigurationService.register(XMLObjectProviderRegistry.class, registry);
            registry.setParserPool(getParserPool());
            InitializationService.initialize();

        } catch (ComponentInitializationException | InitializationException e) {
            throw new IllegalStateException("Failure initializing OpenSaml configuration", e);
        }
    }

    private ParserPool getParserPool() throws ComponentInitializationException {
        final var parserPool = new BasicParserPool();
        parserPool.setMaxPoolSize(100);
        parserPool.setCoalescing(true);
        parserPool.setIgnoreComments(true);
        parserPool.setIgnoreElementContentWhitespace(true);
        parserPool.setNamespaceAware(true);
        parserPool.setExpandEntityReferences(false);
        parserPool.setXincludeAware(false);

        final var features = getOpenSamlBuilderFeatures();
        parserPool.setBuilderFeatures(features);
        parserPool.setBuilderAttributes(new HashMap<>());
        parserPool.initialize();
        return parserPool;
    }

    private static Map<String, Boolean> getOpenSamlBuilderFeatures() {
        final var features = new HashMap<String, Boolean>();
        features.put("http://xml.org/sax/features/external-general-entities", Boolean.FALSE);
        features.put("http://xml.org/sax/features/external-parameter-entities", Boolean.FALSE);
        features.put("http://apache.org/xml/features/disallow-doctype-decl", Boolean.TRUE);
        features.put("http://apache.org/xml/features/validation/schema/normalized-value", Boolean.FALSE);
        features.put("http://javax.xml.XMLConstants/feature/secure-processing", Boolean.TRUE);
        return features;
    }
}
