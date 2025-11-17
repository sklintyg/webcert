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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;

@ExtendWith(MockitoExtension.class)
class CertificateTypeInfoModalServiceTest {

    private static final String CERTIFICATE_TYPE = "db";
    private static final String PATIENT_ID = "191212121212";
    private static final String INTYG_ID = "intyg-123";
    private static final String CARE_UNIT_NAME = "Vårdenhet Stockholm";
    private static final String CARE_UNIT_HSA_ID = "HSA-VE-STOCK-001";
    private static final String CARE_PROVIDER_NAME = "Stockholms Vårdgivare";
    private static final String INTYG_INDICATOR = "intyg";
    private static final String UTKAST_INDICATOR = "utkast";

    @Mock
    private UtkastService utkastService;

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private CertificateTypeInfoModalService service;

    private Personnummer personnummer;
    private WebCertUser user;

    @BeforeEach
    void setUp() {
        personnummer = Personnummer.createPersonnummer(PATIENT_ID).orElseThrow();
        user = new WebCertUser();
    }

    @Test
    void shouldReturnEmptyWhenNoPreviousCertificates() {
        when(webCertUserService.getUser()).thenReturn(user);
        when(utkastService.checkIfPersonHasExistingIntyg(eq(personnummer), eq(user), any(), eq(false)))
            .thenReturn(Collections.emptyMap());

        final var result = service.get(CERTIFICATE_TYPE, personnummer);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenPreviousCertificatesDoNotContainCertificateType() {
        final var previousCertificates = new HashMap<String, Map<String, PreviousIntyg>>();
        final var intygMap = new HashMap<String, PreviousIntyg>();
        intygMap.put("doi", createPreviousIntyg("doi-123", true, true));
        previousCertificates.put(INTYG_INDICATOR, intygMap);

        when(webCertUserService.getUser()).thenReturn(user);
        when(utkastService.checkIfPersonHasExistingIntyg(eq(personnummer), eq(user), any(), eq(false)))
            .thenReturn(previousCertificates);

        final var result = service.get(CERTIFICATE_TYPE, personnummer);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenCertificateTypeHasNoModalProvider() {
        final var certificateType = "lisjp";
        final var previousCertificates = new HashMap<String, Map<String, PreviousIntyg>>();
        final var intygMap = new HashMap<String, PreviousIntyg>();
        intygMap.put(certificateType, createPreviousIntyg("lisjp-123", true, true));
        previousCertificates.put(INTYG_INDICATOR, intygMap);

        when(webCertUserService.getUser()).thenReturn(user);
        when(utkastService.checkIfPersonHasExistingIntyg(eq(personnummer), eq(user), any(), eq(false)))
            .thenReturn(previousCertificates);

        final var result = service.get(certificateType, personnummer);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnModalWhenPreviousSignedCertificateExists() {
        final var previousCertificates = createPreviousCertificatesMap(CERTIFICATE_TYPE, INTYG_ID, true, false);
        final var utkast = createUtkast(UtkastStatus.SIGNED);

        when(webCertUserService.getUser()).thenReturn(user);
        when(utkastService.checkIfPersonHasExistingIntyg(eq(personnummer), eq(user), any(), eq(false)))
            .thenReturn(previousCertificates);
        when(utkastService.getDraft(eq(INTYG_ID), eq(CERTIFICATE_TYPE), anyBoolean()))
            .thenReturn(utkast);

        final var result = service.get(CERTIFICATE_TYPE, personnummer);

        assertAll(
            () -> assertTrue(result.isPresent()),
            () -> assertEquals("Signerat dödsbevis på annan vårdenhet", result.get().getTitle()),
            () -> assertTrue(result.get().getDescription().contains(CARE_PROVIDER_NAME)),
            () -> assertTrue(result.get().getDescription().contains(CARE_UNIT_NAME)),
            () -> assertTrue(result.get().getDescription().contains(CARE_UNIT_HSA_ID))
        );
    }

    @Test
    void shouldReturnModalWhenPreviousDraftExists() {
        final var previousCertificates = new HashMap<String, Map<String, PreviousIntyg>>();
        final var utkastMap = new HashMap<String, PreviousIntyg>();
        utkastMap.put(CERTIFICATE_TYPE, createPreviousIntyg(INTYG_ID, true, false));
        previousCertificates.put(UTKAST_INDICATOR, utkastMap);

        final var utkast = createUtkast(UtkastStatus.DRAFT_INCOMPLETE);

        when(webCertUserService.getUser()).thenReturn(user);
        when(utkastService.checkIfPersonHasExistingIntyg(eq(personnummer), eq(user), any(), eq(false)))
            .thenReturn(previousCertificates);
        when(utkastService.getDraft(INTYG_ID, CERTIFICATE_TYPE, false))
            .thenReturn(utkast);

        final var result = service.get(CERTIFICATE_TYPE, personnummer);

        assertAll(
            () -> assertTrue(result.isPresent()),
            () -> assertEquals("Utkast på dödsbevis på annan vårdenhet", result.get().getTitle())
        );
    }

    @Test
    void shouldPrioritizeSignedCertificateOverDraft() {
        final var previousCertificates = new HashMap<String, Map<String, PreviousIntyg>>();
        final var intygMap = new HashMap<String, PreviousIntyg>();
        final var utkastMap = new HashMap<String, PreviousIntyg>();

        intygMap.put(CERTIFICATE_TYPE, createPreviousIntyg(INTYG_ID, true, false));
        utkastMap.put(CERTIFICATE_TYPE, createPreviousIntyg("draft-123", true, false));

        previousCertificates.put(INTYG_INDICATOR, intygMap);
        previousCertificates.put(UTKAST_INDICATOR, utkastMap);

        final var utkast = createUtkast(UtkastStatus.SIGNED);

        when(webCertUserService.getUser()).thenReturn(user);
        when(utkastService.checkIfPersonHasExistingIntyg(eq(personnummer), eq(user), any(), eq(false)))
            .thenReturn(previousCertificates);
        when(utkastService.getDraft(eq(INTYG_ID), eq(CERTIFICATE_TYPE), anyBoolean()))
            .thenReturn(utkast);

        final var result = service.get(CERTIFICATE_TYPE, personnummer);

        assertAll(
            () -> assertTrue(result.isPresent()),
            () -> verify(utkastService).getDraft(eq(INTYG_ID), eq(CERTIFICATE_TYPE), eq(false))
        );
    }

    @Test
    void shouldReturnEmptyWhenSameCareProviderAndSameUnit() {
        final var previousCertificates = createPreviousCertificatesMap(CERTIFICATE_TYPE, INTYG_ID, true, true);
        final var utkast = createUtkast(UtkastStatus.SIGNED);

        when(webCertUserService.getUser()).thenReturn(user);
        when(utkastService.checkIfPersonHasExistingIntyg(eq(personnummer), eq(user), any(), eq(false)))
            .thenReturn(previousCertificates);
        when(utkastService.getDraft(eq(INTYG_ID), eq(CERTIFICATE_TYPE), anyBoolean()))
            .thenReturn(utkast);

        final var result = service.get(CERTIFICATE_TYPE, personnummer);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldHandleDoiCertificateType() {
        final var certificateType = "doi";
        final var previousCertificates = createPreviousCertificatesMap(certificateType, INTYG_ID, true, false);
        final var utkast = createUtkast(UtkastStatus.SIGNED);

        when(webCertUserService.getUser()).thenReturn(user);
        when(utkastService.checkIfPersonHasExistingIntyg(eq(personnummer), eq(user), any(), eq(false)))
            .thenReturn(previousCertificates);
        when(utkastService.getDraft(eq(INTYG_ID), eq(certificateType), anyBoolean()))
            .thenReturn(utkast);

        final var result = service.get(certificateType, personnummer);

        assertAll(
            () -> assertTrue(result.isPresent()),
            () -> assertEquals("Signerat dödsorsaksintyg på annan vårdenhet", result.get().getTitle())
        );
    }

    private Map<String, Map<String, PreviousIntyg>> createPreviousCertificatesMap(
        String certificateType, String intygId, boolean sameCareProvider, boolean sameUnit) {

        final var previousCertificates = new HashMap<String, Map<String, PreviousIntyg>>();
        final var intygMap = new HashMap<String, PreviousIntyg>();
        intygMap.put(certificateType, createPreviousIntyg(intygId, sameCareProvider, sameUnit));
        previousCertificates.put(INTYG_INDICATOR, intygMap);

        return previousCertificates;
    }

    private PreviousIntyg createPreviousIntyg(String intygId, boolean sameVardgivare, boolean sameEnhet) {
        return PreviousIntyg.of(sameVardgivare, sameEnhet, false, "Test Enhet", intygId, null);
    }

    private Utkast createUtkast(UtkastStatus status) {
        final var utkast = new Utkast();
        utkast.setEnhetsNamn(CARE_UNIT_NAME);
        utkast.setEnhetsId(CARE_UNIT_HSA_ID);
        utkast.setVardgivarNamn(CARE_PROVIDER_NAME);
        utkast.setStatus(status);
        return utkast;
    }
}

