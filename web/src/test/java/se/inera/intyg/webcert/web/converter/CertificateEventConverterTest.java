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
package se.inera.intyg.webcert.web.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.event.model.CertificateEvent;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.CertificateEventDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygTypeInfo;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

@RunWith(MockitoJUnitRunner.class)
public class CertificateEventConverterTest {

    @Mock
    IntygService intygService;

    @InjectMocks
    CertificateEventConverter converter;

    private final String CERTIFICATE_ID = "certId";
    private final String PARENT_CERTIFICATE_ID = "parentId";

    @Test
    public void testConvertToCertificateEventDTO() {
        CertificateEvent event = new CertificateEvent();
        event.setCertificateId(CERTIFICATE_ID);
        event.setEventCode(EventCode.SKAPAT);

        CertificateEventDTO dto = converter.convertToCertificateEventDTO(event);

        assertNotNull(dto);
        assertEquals(CERTIFICATE_ID, dto.getCertificateId());
        assertEquals(EventCode.SKAPAT, dto.getEventCode());
    }

    @Test
    public void testConvertToCertificateEventDTOWithExtendedMessage() {
        IntygContentHolder intygContentHolder = getIntygContentHolder();
        when(intygService.fetchIntygDataForInternalUse(anyString(), anyBoolean())).thenReturn(intygContentHolder);
        when(intygService.getIntygTypeInfo(PARENT_CERTIFICATE_ID)).thenReturn(new IntygTypeInfo(PARENT_CERTIFICATE_ID, "type", "version"));

        CertificateEvent event = new CertificateEvent();
        event.setCertificateId(CERTIFICATE_ID);
        event.setEventCode(EventCode.ERSATTER);

        CertificateEventDTO dto = converter.convertToCertificateEventDTO(event);

        verify(intygService).fetchIntygDataForInternalUse(anyString(), anyBoolean());
        verify(intygService).getIntygTypeInfo(anyString());
        assertNotNull(dto);
        assertEquals(CERTIFICATE_ID, dto.getCertificateId());
        assertEquals(EventCode.ERSATTER, dto.getEventCode());
        assertNotNull(dto.getExtendedMessage());
        assertNotNull(dto.getExtendedMessage().getOriginalCertificateType());
    }

    private IntygContentHolder getIntygContentHolder() {
        WebcertCertificateRelation complementedByIntyg = new WebcertCertificateRelation(PARENT_CERTIFICATE_ID, RelationKod.KOMPLT,
            LocalDateTime.now(), null,
            false);
        Relations parent = new Relations();
        parent.setParent(complementedByIntyg);

        return IntygContentHolder.builder()
            .relations(parent)
            .revoked(false)
            .deceased(false)
            .sekretessmarkering(false)
            .patientNameChangedInPU(false)
            .patientAddressChangedInPU(false)
            .testIntyg(false)
            .relations(parent)
            .latestMajorTextVersion(true)
            .build();
    }
}
