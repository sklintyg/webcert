/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.webcert.integration.fmb.services.FmbService;
import se.inera.intyg.webcert.persistence.fmb.model.FmbType;
import se.inera.intyg.webcert.persistence.fmb.repository.FmbRepository;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;
import se.inera.intyg.webcert.web.service.fmb.FmbDiagnosInformationService;

public class FmbApiControllerTest {

    @InjectMocks
    private FmbApiController controller;

    @Mock
    private FmbRepository fmbRepository;

    @Mock
    private FmbDiagnosInformationService fmbDiagnosInformationService;

    @Mock
    private FmbService fmbService;

    @Mock
    private DiagnosService diagnosService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(diagnosService.getDiagnosisByCode(anyString(), any(Diagnoskodverk.class)))
            .thenReturn(DiagnosResponse.ok(makeDiagnoser(), false));
    }

    private List<Diagnos> makeDiagnoser() {
        Diagnos diagnos = new Diagnos();
        diagnos.setBeskrivning("Diagnosbeskrivning");
        diagnos.setKod("Diagnoskod");
        return Arrays.asList(diagnos);
    }

    @Test
    public void testGetFmbForIcd10HandlesNull() throws Exception {
        Response response = controller.getFmbForIcd10(null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetFmbForIcd10HandlesEmptyInput() throws Exception {
        Response response = controller.getFmbForIcd10("");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetFmbForIcd10HandlesNullResponseFromRepositoryCorrectAndTriesToUpdateFmbData() throws Exception {
        // Given
        doReturn(null).when(fmbRepository).findByIcd10AndTyp(anyString(), any(FmbType.class));

        // When
        Response response = controller.getFmbForIcd10("A10");

        // Then
        assertEquals(204, response.getStatus());
    }
}
