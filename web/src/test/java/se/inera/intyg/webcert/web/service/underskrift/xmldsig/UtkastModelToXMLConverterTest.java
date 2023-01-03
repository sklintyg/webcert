/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.db.v1.rest.DbModuleApiV1;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.services.BefattningService;
import se.inera.intyg.common.ts_bas.v6.rest.TsBasModuleApiV6;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BefattningService.class})
public class UtkastModelToXMLConverterTest {

    @Mock
    private IntygModuleRegistry intygModuleRegistry;

    @InjectMocks
    private UtkastModelToXMLConverter testee;

    private String jsonModel;


    public UtkastModelToXMLConverterTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConvertDb() throws ModuleNotFoundException, IOException {
        loadJsonModel("UtkastModelToXMLConverter/db.json");

        DbModuleApiV1 dbModuleApiV1 = new DbModuleApiV1();
        ReflectionTestUtils.setField(dbModuleApiV1, "objectMapper", new CustomObjectMapper());

        when(intygModuleRegistry.getModuleApi(anyString(), anyString())).thenReturn(dbModuleApiV1);
        when(intygModuleRegistry.resolveVersionFromUtlatandeJson(anyString(), anyString())).thenReturn("1.0");
        String xml = testee.utkastToXml(jsonModel, "DB");
        assertNotNull(xml);
    }

    @Test
    public void testConvertTsBasConcrete() throws IOException, ModuleNotFoundException {
        loadJsonModel("UtkastModelToXMLConverter/ts-bas.json");

        TsBasModuleApiV6 tsBaModuleApi = new TsBasModuleApiV6();
        ReflectionTestUtils.setField(tsBaModuleApi, "objectMapper", new CustomObjectMapper());

        when(intygModuleRegistry.getModuleApi(anyString(), anyString())).thenReturn(tsBaModuleApi);
        when(intygModuleRegistry.resolveVersionFromUtlatandeJson(anyString(), anyString())).thenReturn("1.0");
        String xml = testee.utkastToXml(jsonModel, "ts-bas");
        assertNotNull(xml);
    }

    private void loadJsonModel(String s) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(s);
        jsonModel = IOUtils.toString(classPathResource.getInputStream(), Charset.forName("UTF-8"));
    }
}
