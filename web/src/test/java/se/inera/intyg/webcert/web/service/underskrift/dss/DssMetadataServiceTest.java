/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.underskrift.dss;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;

public class DssMetadataServiceTest {

    @BeforeClass
    public static void init() throws ConfigurationException {
        DefaultBootstrap.bootstrap();
    }

    @Test
    public void getDssActionUrl() {

        DssMetadataService service = new DssMetadataService(Configuration.getParserPool());
        service.setDssServiceMetadataEntityId("https://localhost:19088");
        service.setDssServiceMetadataPath("src/test/resources/dss/dss-sp-valid.xml");

        service.initialize();

        assertEquals("https://localhost:19088/saml/SSO/alias/defaultAlias", service.getDssActionUrl());

    }

    @Test
    public void getDssCertificate() {

    }
}