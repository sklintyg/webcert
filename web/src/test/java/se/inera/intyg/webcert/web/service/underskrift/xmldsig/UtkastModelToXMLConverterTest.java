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
package se.inera.intyg.webcert.web.service.underskrift.xmldsig;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.db.rest.DbModuleApi;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.ts_bas.rest.TsBasModuleApi;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtkastModelToXMLConverterTest {

    @Mock
    private IntygModuleRegistry intygModuleRegistry;

    @InjectMocks
    private UtkastModelToXMLConverter testee;

    private String jsonModel;


    @Test
    public void testConvertDb() throws ModuleNotFoundException, IOException {
        loadJsonModel("UtkastModelToXMLConverter/db.json");

        DbModuleApi dbModuleApi = new DbModuleApi();
        ReflectionTestUtils.setField(dbModuleApi, "objectMapper", new CustomObjectMapper());

        when(intygModuleRegistry.getModuleApi(anyString(), anyString())).thenReturn(dbModuleApi);
        when(intygModuleRegistry.resolveVersionFromUtlatandeJson(anyString())).thenReturn("1.0");
        String xml = testee.utkastToXml(jsonModel, "DB");
        assertNotNull(xml);
    }

    @Test
    public void testConvertTsBasConcrete() throws IOException, ModuleNotFoundException {
        loadJsonModel("UtkastModelToXMLConverter/ts-bas.json");

        TsBasModuleApi tsBaModuleApi = new TsBasModuleApi();
        ReflectionTestUtils.setField(tsBaModuleApi, "objectMapper", new CustomObjectMapper());

        when(intygModuleRegistry.getModuleApi(anyString(), anyString())).thenReturn(tsBaModuleApi);
        when(intygModuleRegistry.resolveVersionFromUtlatandeJson(anyString())).thenReturn("1.0");
        String xml = testee.utkastToXml(jsonModel, "ts-bas");
        assertNotNull(xml);
    }

    private void loadJsonModel(String s) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(s);
        jsonModel = IOUtils.toString(classPathResource.getInputStream(), Charset.forName("UTF-8"));
    }
}
