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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.util.CandidateDataHelper;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData.Builder;

@ExtendWith(MockitoExtension.class)
class GetCandidateMessageForCertificateFacadeServiceImplTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String PATIENT_ID = "191212121212";
    @Mock
    private GetCertificateFacadeService getCertificateFacadeService;
    @Mock
    private CandidateDataHelper candidateDataHelper;
    @InjectMocks
    private GetCandidateMessageForCertificateFacadeServiceImpl getCandidateUnitFromCertificateFacadeService;

    @Test
    void shallReturnCorrectMessageForDodsbevis() {
        final var expectedMessage = "<p>Det finns ett signerat dödsbevis för detta personnummer på "
            + "<span class='iu-fw-bold'>"
            + createCandidateMetaData(DbModuleEntryPoint.MODULE_ID, "sopra steria").getEnhetName()
            + "</span>. Det är tyvärr inte möjligt att kopiera de svar som givits i det intyget till detta intygsutkast. ";
        final var dbCertificate = getDbCertificate();
        doReturn(dbCertificate)
            .when(getCertificateFacadeService).getCertificate(CERTIFICATE_ID, false, true);

        doReturn(Optional.of(createCandidateMetaData(DbModuleEntryPoint.MODULE_ID, "sopra steria")))
            .when(candidateDataHelper)
            .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

        final var actualString = getCandidateUnitFromCertificateFacadeService.get(CERTIFICATE_ID);

        assertEquals(expectedMessage, actualString.getMessage());
    }

    @Test
    void shallReturnCorrectTitleForDodsbevis() {
        final var expectedTitle = "Information om vårdenhet";
        final var dbCertificate = getDbCertificate();
        doReturn(dbCertificate)
            .when(getCertificateFacadeService).getCertificate(CERTIFICATE_ID, false, true);

        doReturn(Optional.of(createCandidateMetaData(DbModuleEntryPoint.MODULE_ID, "enhet")))
            .when(candidateDataHelper)
            .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

        final var actualString = getCandidateUnitFromCertificateFacadeService.get(CERTIFICATE_ID);

        assertEquals(expectedTitle, actualString.getTitle());
    }

    @Test
    void shallReturnCorrectMessageIfNoCandidateFound() {
        final var expectedMessage = "Saknar meddelande";
        final var dbCertificate = getDbCertificate();

        doReturn(dbCertificate)
            .when(getCertificateFacadeService).getCertificate(CERTIFICATE_ID, false, true);

        doReturn(Optional.empty())
            .when(candidateDataHelper)
            .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

        final var actualString = getCandidateUnitFromCertificateFacadeService.get(CERTIFICATE_ID);

        assertEquals(expectedMessage, actualString.getMessage());
    }

    @Test
    void shallReturnCorrectTitleIfNoCandidateFound() {
        final var expectedTitle = "Saknar titel";

        final var dbCertificate = getDbCertificate();

        doReturn(dbCertificate)
            .when(getCertificateFacadeService).getCertificate(CERTIFICATE_ID, false, true);

        doReturn(Optional.empty())
            .when(candidateDataHelper)
            .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

        final var actualString = getCandidateUnitFromCertificateFacadeService.get(CERTIFICATE_ID);

        assertEquals(expectedTitle, actualString.getTitle());
    }

    private UtkastCandidateMetaData createCandidateMetaData(String intygType, String enhet) {
        return new Builder()
            .with(builder -> {
                builder.intygType = intygType;
                builder.enhetName = enhet;
            })
            .create();
    }

    private Certificate getDbCertificate() {
        return CertificateBuilder.create()
            .metadata(
                CertificateMetadata.builder()
                    .id(CERTIFICATE_ID)
                    .type(DbModuleEntryPoint.MODULE_ID)
                    .typeVersion("test")
                    .patient(
                        Patient.builder()
                            .personId(
                                PersonId.builder()
                                    .id(PATIENT_ID)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();
    }
}
