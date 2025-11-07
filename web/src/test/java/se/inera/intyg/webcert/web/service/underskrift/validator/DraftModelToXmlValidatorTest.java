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
package se.inera.intyg.webcert.web.service.underskrift.validator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.intyg.common.db.v1.rest.DbModuleApiV1;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateXmlResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleValidationException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.UtkastModelToXMLConverter;

@RunWith(MockitoJUnitRunner.class)
public class DraftModelToXmlValidatorTest {

    @Mock
    private IntygModuleRegistry intygModuleRegistry;

    @Mock
    private UtkastModelToXMLConverter draftModelToXMLConverter;

    @InjectMocks
    private DraftModelToXmlValidator draftModelToXmlValidator;

    public DraftModelToXmlValidatorTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void validateDraftModelAsXmlValid() throws ModuleNotFoundException, IOException, ModuleException {
        String xml = toString("DraftModelToXmlValidatorTest/db_valid.xml");
        Utkast draft = createDraft();
        DbModuleApiV1 dbModuleApiV1 = new DbModuleApiV1();

        when(draftModelToXMLConverter.utkastToXml(anyString(), anyString())).thenReturn(xml);
        when(intygModuleRegistry.getModuleApi(anyString(), anyString())).thenReturn(dbModuleApiV1);

        ValidateXmlResponse validateXmlResponse = draftModelToXmlValidator.validateDraftModelAsXml(draft);
        assertNotNull(validateXmlResponse);
        draftModelToXmlValidator.assertResponse(draft.getIntygsId(), validateXmlResponse);
    }

    @Test
    public void validateDraftModelAsXmlInvalid() throws ModuleNotFoundException, IOException, ModuleException {
        String xml = toString("DraftModelToXmlValidatorTest/db_invalid.xml");
        Utkast draft = createDraft();
        DbModuleApiV1 dbModuleApiV1 = new DbModuleApiV1();

        when(draftModelToXMLConverter.utkastToXml(anyString(), anyString())).thenReturn(xml);
        when(intygModuleRegistry.getModuleApi(anyString(), anyString())).thenReturn(dbModuleApiV1);

        ValidateXmlResponse validateXmlResponse = draftModelToXmlValidator.validateDraftModelAsXml(draft);
        assertNotNull(validateXmlResponse);
        try {
            draftModelToXmlValidator.assertResponse(draft.getIntygsId(), validateXmlResponse);
            fail("Call to assertResponse did not throw exception as expected.");
        } catch (ModuleValidationException e) {
            assertNotNull(e);
        }
    }

    private String toString(String file) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(file);
        return IOUtils.toString(classPathResource.getInputStream(), StandardCharsets.UTF_8);
    }

    private Utkast createDraft() {
        Utkast draft = new Utkast();
        draft.setIntygsId("id");
        draft.setIntygsTyp("db");
        draft.setIntygTypeVersion("1.0");
        draft.setModel("se/inera/intyg/webcert/integration/privatepractitioner/model");
        return draft;
    }

}
