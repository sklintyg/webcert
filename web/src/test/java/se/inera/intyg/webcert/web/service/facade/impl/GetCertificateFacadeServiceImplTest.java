/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.facade.util.CertificateConverter;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@ExtendWith(MockitoExtension.class)
class GetCertificateFacadeServiceImplTest {

    @Mock
    private UtkastService utkastService;

    @Mock
    private CertificateConverter certificateConverter;

    @InjectMocks
    private GetCertificateFacadeServiceImpl getCertificateService;

    private final Utkast draft = createDraft();

    @BeforeEach
    void setupMocks() {
        doReturn(draft)
            .when(utkastService).getDraft(eq(draft.getIntygsId()), anyBoolean());

        doReturn(createCertificate())
            .when(certificateConverter).convert(draft);
    }

    @Test
    void shallReturnCertificate() {
        final var certificate = getCertificateService.getCertificate(draft.getIntygsId(), false);
        assertNotNull(certificate, "Certificate should not be null!");
    }

    @Nested
    class ValidatePdlLogging {

        @Test
        void shallPdlLogIfRequired() {
            final var actualPdlLogValue = ArgumentCaptor.forClass(Boolean.class);
            getCertificateService.getCertificate(draft.getIntygsId(), true);
            verify(utkastService).getDraft(anyString(), actualPdlLogValue.capture());
            assertTrue(actualPdlLogValue.getValue(), "Expect true because pdl logging is required");
        }

        @Test
        void shallNotPdlLogIfRequired() {
            final var actualPdlLogValue = ArgumentCaptor.forClass(Boolean.class);
            getCertificateService.getCertificate(draft.getIntygsId(), false);
            verify(utkastService).getDraft(anyString(), actualPdlLogValue.capture());
            assertFalse(actualPdlLogValue.getValue(), "Expect false because no pdl logging is required");
        }
    }

    private Utkast createDraft() {
        final var draft = new Utkast();
        draft.setIntygsId("certificateId");
        draft.setIntygsTyp("certificateType");
        draft.setIntygTypeVersion("certificateTypeVersion");
        draft.setModel("draftJson");
        draft.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        draft.setSkapad(LocalDateTime.now());
        draft.setPatientPersonnummer(Personnummer.createPersonnummer("191212121212").orElseThrow());
        draft.setSkapadAv(new VardpersonReferens("personId", "personName"));
        return draft;
    }

    private Certificate createCertificate() {
        return CertificateBuilder.create()
            .metadata(
                CertificateMetadata.builder()
                    .id("certificateId")
                    .type("certificateType")
                    .typeVersion("certificateTypeVersion")
                    .unit(
                        Unit.builder()
                            .unitId("unitId")
                            .unitName("unitName")
                            .address("address")
                            .zipCode("zipCode")
                            .city("city")
                            .email("email")
                            .phoneNumber("phoneNumber")
                            .build()
                    )
                    .build()
            )
            .build();
    }
}