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

package se.inera.intyg.webcert.web.service.facade.list.previous;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterPersonIdValue;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.test.TestIntygFactory;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygSource;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

@ExtendWith(MockitoExtension.class)
class CertificateForPatientServiceImplTest {

    @Mock
    private Cache certificatesForPatientCache;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private IntygService intygService;
    @InjectMocks
    private CertificateForPatientServiceImpl certificateForPatient;
    private static final Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = Pair.of(TestIntygFactory.createListWithIntygItems(),
        false);

    @Test
    void shouldReturnListStoredInRedis() throws IOException, ModuleNotFoundException {
        final var utkasts = List.of(
            TestIntygFactory.createUtkast("4", LocalDateTime.parse("2014-01-03T12:12:18")));
        final var personnummer = Personnummer.createPersonnummer("1912121212").get();
        final var unitId = List.of("unitId");
        final var key = "key";
        final var webCertUser = new WebCertUser();
        final var listFilter = getTestListFilter();
        final var expectedResult = List.of(
            listIntygEntryFromWc("4", LocalDateTime.parse("2014-01-03T12:12:18")));
        when(certificatesForPatientCache.get(any(String.class), eq(String.class))).thenReturn(key);
        when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        when(objectMapper.readValue(any(String.class), any(CollectionType.class))).thenReturn(expectedResult);
        final var actualResult = certificateForPatient.get(listFilter, webCertUser, utkasts, personnummer, unitId);

        assertIterableEquals(expectedResult, actualResult);
    }

    @Test
    void shouldReturnMergedListOfDraftAndCertificatesFromIntygService() throws IOException, ModuleNotFoundException {
        final var utkasts = TestIntygFactory.createListWithUtkast();
        final var personnummer = Personnummer.createPersonnummer("1912121212").get();
        final var unitId = List.of("unitId");
        final var webCertUser = new WebCertUser();
        final var listFilter = getTestListFilter();
        final var expectedResult = List.of(
            TestIntygFactory.createIntygItem("4", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createIntygItem("3", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createIntygItem("2", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createIntygItem("1", LocalDateTime.parse("2014-01-03T12:12:18")));
        when(intygService.listIntyg(any(), any(Personnummer.class))).thenReturn(intygItemListResponse);
        final var actualResult = certificateForPatient.get(listFilter, webCertUser, utkasts, personnummer, unitId);

        assertIterableEquals(expectedResult, actualResult);
    }

    @Test
    void shouldReturnUpdatedListIfDraftsWereAddedSinceLastUpdate() throws IOException, ModuleNotFoundException {
        final var utkasts = List.of(
            TestIntygFactory.createUtkast("4", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createUtkast("3", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createUtkast("2", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createUtkast("1", LocalDateTime.parse("2014-01-03T12:12:18")));

        final var valuesStoredInRedis = List.of(
            listIntygEntryFromWc("4", LocalDateTime.parse("2014-01-03T12:12:18")),
            listIntygEntryFromWc("3", LocalDateTime.parse("2014-01-03T12:12:18")));

        final var expectedResult = List.of(
            TestIntygFactory.createIntygItem("4", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createIntygItem("3", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createIntygItem("2", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createIntygItem("1", LocalDateTime.parse("2014-01-03T12:12:18")));

        final var personnummer = Personnummer.createPersonnummer("1912121212").get();
        final var unitId = List.of("unitId");
        final var key = "key";
        final var listFilter = getTestListFilter();
        final var webCertUser = new WebCertUser();

        when(certificatesForPatientCache.get(any(String.class), eq(String.class))).thenReturn(key);
        when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        when(objectMapper.readValue(any(String.class), any(CollectionType.class))).thenReturn(valuesStoredInRedis);
        final var actualResult = certificateForPatient.get(listFilter, webCertUser, utkasts, personnummer, unitId);

        assertIterableEquals(expectedResult, actualResult);
    }

    @Test
    void shouldReturnUpdatedListWithUpdatedValues() throws IOException, ModuleNotFoundException {
        final var utkasts = List.of(
            TestIntygFactory.createUtkast("4", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createUtkast("3", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createUtkast("2", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createUtkast("1", LocalDateTime.parse("2014-01-03T12:12:18")));

        final var valuesStoredInRedis = List.of(
            listIntygEntryFromWc("4", LocalDateTime.parse("2014-01-03T12:12:01")),
            listIntygEntryFromWc("3", LocalDateTime.parse("2014-01-03T12:12:02")),
            listIntygEntryFromWc("2", LocalDateTime.parse("2014-01-03T12:12:03")),
            listIntygEntryFromWc("1", LocalDateTime.parse("2014-01-03T12:12:04")));

        final var expectedResult = List.of(
            TestIntygFactory.createIntygItem("4", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createIntygItem("3", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createIntygItem("2", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createIntygItem("1", LocalDateTime.parse("2014-01-03T12:12:18")));

        final var personnummer = Personnummer.createPersonnummer("1912121212").get();
        final var unitId = List.of("unitId");
        final var key = "key";
        final var listFilter = getTestListFilter();
        final var webCertUser = new WebCertUser();

        when(certificatesForPatientCache.get(any(String.class), eq(String.class))).thenReturn(key);
        when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        when(objectMapper.readValue(any(String.class), any(CollectionType.class))).thenReturn(valuesStoredInRedis);
        final var actualResult = certificateForPatient.get(listFilter, webCertUser, utkasts, personnummer, unitId);

        assertAll(
            () -> assertEquals(expectedResult.get(0).getLastUpdatedSigned(), actualResult.get(0).getLastUpdatedSigned()),
            () -> assertEquals(expectedResult.get(1).getLastUpdatedSigned(), actualResult.get(1).getLastUpdatedSigned()),
            () -> assertEquals(expectedResult.get(2).getLastUpdatedSigned(), actualResult.get(2).getLastUpdatedSigned()),
            () -> assertEquals(expectedResult.get(3).getLastUpdatedSigned(), actualResult.get(3).getLastUpdatedSigned())
        );
    }

    @Test
    void shouldRemoveOldDraftsThatAreNoLongerRelevantFromWc() throws IOException, ModuleNotFoundException {
        final var utkasts = List.of(
            TestIntygFactory.createUtkast("3", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createUtkast("2", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createUtkast("1", LocalDateTime.parse("2014-01-03T12:12:18")));

        final var valuesStoredInRedis = List.of(
            listIntygEntryFromWc("4", LocalDateTime.parse("2014-01-03T12:12:18")),
            listIntygEntryFromWc("3", LocalDateTime.parse("2014-01-03T12:12:18")),
            listIntygEntryFromWc("2", LocalDateTime.parse("2014-01-03T12:12:18")),
            listIntygEntryFromWc("1", LocalDateTime.parse("2014-01-03T12:12:18")));

        final var expectedResult = List.of(
            TestIntygFactory.createIntygItem("3", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createIntygItem("2", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createIntygItem("1", LocalDateTime.parse("2014-01-03T12:12:18")));

        final var personnummer = Personnummer.createPersonnummer("1912121212").get();
        final var unitId = List.of("unitId");
        final var key = "key";
        final var listFilter = getTestListFilter();
        final var webCertUser = new WebCertUser();

        when(certificatesForPatientCache.get(any(String.class), eq(String.class))).thenReturn(key);
        when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        when(objectMapper.readValue(any(String.class), any(CollectionType.class))).thenReturn(valuesStoredInRedis);
        final var actualResult = certificateForPatient.get(listFilter, webCertUser, utkasts, personnummer, unitId);

        assertIterableEquals(expectedResult, actualResult);
    }

    @Test
    void shouldAddPreviousCertificatesIfFromIt() throws IOException, ModuleNotFoundException {
        final var utkasts = List.of(
            TestIntygFactory.createUtkast("3", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createUtkast("2", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createUtkast("1", LocalDateTime.parse("2014-01-03T12:12:18")));

        final var valuesStoredInRedis = List.of(
            listIntygEntryFromIt("4", LocalDateTime.parse("2014-01-03T12:12:18")),
            listIntygEntryFromWc("3", LocalDateTime.parse("2014-01-03T12:12:18")),
            listIntygEntryFromWc("2", LocalDateTime.parse("2014-01-03T12:12:18")),
            listIntygEntryFromWc("1", LocalDateTime.parse("2014-01-03T12:12:18")));

        final var expectedResult = List.of(
            TestIntygFactory.createIntygItem("3", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createIntygItem("2", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createIntygItem("1", LocalDateTime.parse("2014-01-03T12:12:18")),
            TestIntygFactory.createIntygItem("4", LocalDateTime.parse("2014-01-03T12:12:18")));

        final var personnummer = Personnummer.createPersonnummer("1912121212").get();
        final var unitId = List.of("unitId");
        final var key = "key";
        final var listFilter = getTestListFilter();
        final var webCertUser = new WebCertUser();

        when(certificatesForPatientCache.get(any(String.class), eq(String.class))).thenReturn(key);
        when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        when(objectMapper.readValue(any(String.class), any(CollectionType.class))).thenReturn(valuesStoredInRedis);
        final var actualResult = certificateForPatient.get(listFilter, webCertUser, utkasts, personnummer, unitId);

        assertIterableEquals(expectedResult, actualResult);
    }

    private ListIntygEntry listIntygEntryFromWc(String id, LocalDateTime signedDate) {
        final var entry = TestIntygFactory.createIntygItem(id, signedDate);
        entry.setSource(IntygSource.WC);
        return entry;
    }

    private ListIntygEntry listIntygEntryFromIt(String id, LocalDateTime signedDate) {
        final var entry = TestIntygFactory.createIntygItem(id, signedDate);
        entry.setSource(IntygSource.IT);
        return entry;
    }

    private ListFilter getTestListFilter() {
        final var listFilter = new ListFilter();
        listFilter.addValue(new ListFilterPersonIdValue("19121212-1212"), "PATIENT_ID");
        return listFilter;
    }
}
