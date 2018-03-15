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
package se.inera.intyg.webcert.web.converter;

import org.junit.Test;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.StatusKod;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.test.TestIntygFactory;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Statuskod;
import se.riv.clinicalprocess.healthcond.certificate.v3.IntygsStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IntygDraftsConverterTest {

    @Test
    public void testMergeWithEmptyLists() {
        List<ListIntygEntry> intygList = new ArrayList<>();
        List<Utkast> utkastList = new ArrayList<>();

        List<ListIntygEntry> res = IntygDraftsConverter.merge(intygList, utkastList);

        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testMergeWithBothListsFilled() {
        List<ListIntygEntry> intygList = TestIntygFactory.createListWithIntygItems();
        List<Utkast> utkastList = TestIntygFactory.createListWithUtkast();

        List<ListIntygEntry> res = IntygDraftsConverter.merge(intygList, utkastList);

        assertNotNull(res);
        assertFalse(res.isEmpty());
        assertEquals(4, res.size());
        assertOrder(res, "4321");
    }

    @Test
    public void testConvertIntygToListEntries() {

        LocalDateTime modfied = LocalDateTime.parse("2014-01-01T10:00:00");

        String id = "123";
        String type = "type";
        String updatedSignedBy = "Dr Dengroth";
        String updatedSignedByHsaId = "HSA1234";

        Personnummer patientId = Personnummer.createValidatedPersonnummer("19121212-1212").get();

        List<Utkast> utkastList = Collections.singletonList(TestIntygFactory.createUtkast(id, modfied, type, updatedSignedBy,
                updatedSignedByHsaId, UtkastStatus.DRAFT_COMPLETE, patientId));

        List<ListIntygEntry> res = IntygDraftsConverter.convertUtkastsToListIntygEntries(utkastList);

        assertNotNull(res);
        assertEquals(1, res.size());

        ListIntygEntry ref = res.get(0);
        assertEquals(id, ref.getIntygId());
        assertEquals(type, ref.getIntygType());
        assertEquals("DRAFT_COMPLETE", ref.getStatus());
        assertEquals(updatedSignedBy, ref.getUpdatedSignedBy());
        assertEquals(modfied, ref.getLastUpdatedSigned());
    }

    @Test
    public void testConvertUtkastToListIntygEntrySetsStatusSentIfApplicable() {
        Utkast utkast = createUtkast();
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setSkickadTillMottagareDatum(LocalDateTime.now());

        ListIntygEntry res = IntygDraftsConverter.convertUtkastToListIntygEntry(utkast);

        assertNotNull(res);
        assertEquals(CertificateState.SENT.name(), res.getStatus());
    }

    @Test
    public void testConvertUtkastToListIntygEntrySetsStatusCancelledIfApplicable() {
        Utkast utkast = createUtkast();
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setAterkalladDatum(LocalDateTime.now());

        ListIntygEntry res = IntygDraftsConverter.convertUtkastToListIntygEntry(utkast);

        assertNotNull(res);
        assertEquals(CertificateState.CANCELLED.name(), res.getStatus());
    }

    @Test
    public void testConvertUtkastToListIntygEntrySetsStatusReceivedIfApplicable() {
        Signatur signatur = mock(Signatur.class);

        Utkast utkast = createUtkast();
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setSignatur(signatur);

        when(signatur.getSigneringsDatum()).thenReturn(LocalDateTime.now());

        ListIntygEntry res = IntygDraftsConverter.convertUtkastToListIntygEntry(utkast);

        assertNotNull(res);
        assertEquals(CertificateState.RECEIVED.name(), res.getStatus());
    }

    @Test
    public void testConvertUtkastToListIntygEntrySetsUtkastStatusIfNoOtherAvailable() {
        ListIntygEntry res = IntygDraftsConverter.convertUtkastToListIntygEntry(createUtkast());

        assertNotNull(res);
        assertEquals(UtkastStatus.DRAFT_INCOMPLETE.name(), res.getStatus());
    }

    @Test
    public void testConvertUtkastToListIntygEntrySetsCancelledFirst() {
        Signatur signatur = mock(Signatur.class);

        Utkast utkast = createUtkast();
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setSignatur(signatur);
        utkast.setAterkalladDatum(LocalDateTime.now());
        utkast.setSkickadTillMottagareDatum(LocalDateTime.now());

        when(signatur.getSigneringsDatum()).thenReturn(LocalDateTime.now());

        ListIntygEntry res = IntygDraftsConverter.convertUtkastToListIntygEntry(utkast);

        assertNotNull(res);
        assertEquals(CertificateState.CANCELLED.name(), res.getStatus());
    }

    @Test
    public void testConvertUtkastToListIntygEntrySetsSentBeforeReceived() {
        Signatur signatur = mock(Signatur.class);

        Utkast utkast = createUtkast();
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setSignatur(signatur);
        utkast.setSkickadTillMottagareDatum(LocalDateTime.now());

        when(signatur.getSigneringsDatum()).thenReturn(LocalDateTime.now());

        ListIntygEntry res = IntygDraftsConverter.convertUtkastToListIntygEntry(utkast);

        assertNotNull(res);
        assertEquals(CertificateState.SENT.name(), res.getStatus());
    }

    @Test
    public void testConvertUtkastToListIntygEntryResolvesSignedByNameNoSignatur() {
        final String senastSparadAvName = "Anders Andersson";

        Utkast utkast = createUtkast();
        utkast.getSenastSparadAv().setNamn(senastSparadAvName);

        ListIntygEntry res = IntygDraftsConverter.convertUtkastToListIntygEntry(utkast);

        assertNotNull(res);
        assertEquals(senastSparadAvName, res.getUpdatedSignedBy());
    }

    @Test
    public void testConvertUtkastToListIntygEntryResolvesSignedByNameFromSkapadAv() {
        Signatur signatur = mock(Signatur.class);

        final String skapadAvName = "Bengt Bengtsson";
        final String skapadAvHsaId = "BengtsHsaId";

        Utkast utkast = createUtkast();
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.getSkapadAv().setNamn(skapadAvName);
        utkast.setSignatur(signatur);

        when(signatur.getSigneradAv()).thenReturn(skapadAvHsaId);

        ListIntygEntry res = IntygDraftsConverter.convertUtkastToListIntygEntry(utkast);

        assertNotNull(res);
        assertEquals(skapadAvHsaId, res.getUpdatedSignedBy());
    }

    @Test
    public void testConvertUtkastToListIntygEntryResolvesSignedByNameFromSenastSparadAv() {
        Utkast utkast = createUtkast();
        utkast.setStatus(UtkastStatus.SIGNED);

        ListIntygEntry res = IntygDraftsConverter.convertUtkastToListIntygEntry(utkast);

        assertNotNull(res);
        assertEquals(utkast.getSenastSparadAv().getNamn(), res.getUpdatedSignedBy());
    }

    @Test
    public void testConvertUtkastToListIntygEntryResolvesSignedByNameReturnsSignaturHsaId() {
        Signatur signatur = mock(Signatur.class);

        Utkast utkast = createUtkast();
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setSignatur(signatur);

        when(signatur.getSigneradAv()).thenReturn(utkast.getSenastSparadAv().getNamn());

        ListIntygEntry res = IntygDraftsConverter.convertUtkastToListIntygEntry(utkast);

        assertNotNull(res);
        assertEquals(utkast.getSenastSparadAv().getNamn(), res.getUpdatedSignedBy());
    }

    @Test
    public void testFindLatestStatus() {

        LocalDateTime defaultTime = LocalDateTime.now();
        CertificateState res;
        List<IntygsStatus> statuses;

        // test with empty list
        statuses = new ArrayList<>();
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(CertificateState.UNHANDLED, res);

        // test with just some statuses
        statuses = new ArrayList<>();
        statuses.add(buildIntygsStatus(StatusKod.RECEIV.name(), defaultTime.minusHours(2)));
        statuses.add(buildIntygsStatus(StatusKod.SENTTO.name(), defaultTime.minusHours(1)));
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(CertificateState.SENT, res);

        // test with DELETED in the list, which should be removed
        statuses = new ArrayList<>();
        statuses.add(buildIntygsStatus(StatusKod.CANCEL.name(), defaultTime.minusHours(2)));
        statuses.add(buildIntygsStatus(StatusKod.SENTTO.name(), defaultTime.minusHours(3)));
        statuses.add(buildIntygsStatus(StatusKod.DELETE.name(), defaultTime.minusHours(1)));
        statuses.add(buildIntygsStatus(StatusKod.RECEIV.name(), defaultTime.minusHours(4)));
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(CertificateState.CANCELLED, res);

        // test with DELETED and RESTORED in the list, which should be removed
        statuses = new ArrayList<>();
        statuses.add(buildIntygsStatus(StatusKod.CANCEL.name(), defaultTime.minusHours(2)));
        statuses.add(buildIntygsStatus(StatusKod.SENTTO.name(), defaultTime.minusHours(3)));
        statuses.add(buildIntygsStatus(StatusKod.RESTOR.name(), defaultTime.minusMinutes(30)));
        statuses.add(buildIntygsStatus(StatusKod.DELETE.name(), defaultTime.minusHours(1)));
        statuses.add(buildIntygsStatus(StatusKod.RECEIV.name(), defaultTime.minusHours(4)));
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(CertificateState.CANCELLED, res);

        // test with just DELETED, which will be removed and result in an empty list
        statuses = new ArrayList<>();
        statuses.add(buildIntygsStatus(StatusKod.DELETE.name(), defaultTime.minusHours(1)));
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(CertificateState.UNHANDLED, res);
    }

    private void assertOrder(List<ListIntygEntry> res, String expectedOrder) {

        StringBuilder sb = new StringBuilder();

        for (ListIntygEntry entry : res) {
            sb.append(entry.getIntygId());
        }

        assertEquals(expectedOrder, sb.toString());
    }

    private IntygsStatus buildIntygsStatus(String statuskod, LocalDateTime timestamp) {
        IntygsStatus status = new IntygsStatus();
        status.setStatus(new Statuskod());
        status.getStatus().setCode(statuskod);
        status.setTidpunkt(timestamp);
        return status;
    }

    private Utkast createUtkast() {

        final String sparadAvNamn = "namn efternamn";
        final String sparadAvHsaId = "hsaid";
        final String skapadAvHsaId = "hsaid";

        VardpersonReferens vardpersonReferens = new VardpersonReferens();

        Utkast utkast = new Utkast();
        utkast.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        utkast.setSenastSparadAv(vardpersonReferens);
        utkast.getSenastSparadAv().setNamn(sparadAvNamn);
        utkast.getSenastSparadAv().setHsaId(sparadAvHsaId);
        utkast.setSkapadAv(vardpersonReferens);
        utkast.getSkapadAv().setHsaId(skapadAvHsaId);
        utkast.setPatientPersonnummer(Personnummer.createValidatedPersonnummer("20121212-1211").get());

        Signatur signatur = mock(Signatur.class);
        utkast.setSignatur(signatur);

        when(signatur.getSigneradAv()).thenReturn(sparadAvHsaId);

        return utkast;
    }
}
