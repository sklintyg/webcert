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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterPersonIdValue;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.test.TestIntygFactory;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygSource;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

@ExtendWith(MockitoExtension.class)
class CertificateForPatientServiceImplTest {

    @Mock
    private Cache certificatesForPatientCache;
    @Spy
    private CustomObjectMapper objectMapper;
    @Mock
    private IntygService intygService;
    @Mock
    private UtkastService utkastService;
    @Mock
    private WebCertUserService webCertUserService;
    @InjectMocks
    private CertificateForPatientServiceImpl certificateForPatient;
    private static final Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = Pair.of(TestIntygFactory.createListWithIntygItems(),
        false);

    private static final String PATIENT_ID = "1912121212";
    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer(PATIENT_ID).orElseThrow();
    private static final List<String> UNIT_IDS = List.of("Unit1", "Unit2");
    private static final Set<String> INTYGS_TYPER = Set.of("Intygstyp1", "Intygstyp2", "Intygstyp3", "Intygstyp4");

    @Nested
    class NoCachedData {

        @BeforeEach
        void setUp() {
            doReturn(mock(WebCertUser.class)).when(webCertUserService).getUser();

            final var fromWebcert = List.of(
                TestIntygFactory.createUtkast("4", LocalDateTime.parse("2014-01-03T12:12:18"))
            );
            doReturn(fromWebcert).when(utkastService).findUtkastByPatientAndUnits(notNull(), notNull());

            final var fromIntygstjanst = Pair.of(List.of(
                TestIntygFactory.createIntygItem("3", LocalDateTime.parse("2014-01-02T10:11:23"))
            ), false);
            doReturn(fromIntygstjanst).when(intygService).listIntyg(notNull(), notNull());
        }

        @Test
        void shouldReturnListWithCertificatesFromITAndWC() throws IOException {
            final var expectedResult = List.of(
                IntygDraftsConverter.convertUtkastToListIntygEntry(
                    TestIntygFactory.createUtkast("4", LocalDateTime.parse("2014-01-03T12:12:18"))),
                TestIntygFactory.createIntygItem("3", LocalDateTime.parse("2014-01-02T10:11:23"))
            );

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedResult.size(), actualResult.size());
            for (int i = 0; i < expectedResult.size(); i++) {
                assertEquals(expectedResult.get(i), actualResult.get(i));
            }
        }

        @Test
        void shouldCacheListWithCertificatesFromIT() throws IOException {
            final var expectedResult = List.of(
                TestIntygFactory.createIntygItem("3", LocalDateTime.parse("2014-01-02T10:11:23"))
            );

            final var stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

            certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            verify(certificatesForPatientCache).put(any(String.class), stringArgumentCaptor.capture());

            final var actualResult = (List<ListIntygEntry>) objectMapper.readValue(stringArgumentCaptor.getValue(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ListIntygEntry.class)
            );

            assertEquals(expectedResult.size(), actualResult.size());
            for (int i = 0; i < expectedResult.size(); i++) {
                assertEquals(expectedResult.get(i), actualResult.get(i));
            }
        }
    }

    @Nested
    class CachedDataNoChanges {

        @BeforeEach
        void setUp() throws JsonProcessingException {
            doReturn(mock(WebCertUser.class)).when(webCertUserService).getUser();

            final var fromWebcert = List.of(
                TestIntygFactory.createUtkast("4", LocalDateTime.parse("2014-01-03T12:12:18"))
            );
            doReturn(fromWebcert).when(utkastService).findUtkastByPatientAndUnits(notNull(), notNull());

            final var cachedData = objectMapper.writeValueAsString(List.of(
                TestIntygFactory.createIntygItem("3", LocalDateTime.parse("2014-01-02T10:11:23"), IntygSource.IT)
            ));
            doReturn(cachedData).when(certificatesForPatientCache).get(any(String.class), eq(String.class));
        }

        @Test
        void shouldReturnListWithCertificatesFromITAndWC() throws IOException {
            final var expectedResult = List.of(
                IntygDraftsConverter.convertUtkastToListIntygEntry(
                    TestIntygFactory.createUtkast("4", LocalDateTime.parse("2014-01-03T12:12:18"))),
                TestIntygFactory.createIntygItem("3", LocalDateTime.parse("2014-01-02T10:11:23"), IntygSource.IT)
            );

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedResult.size(), actualResult.size());
            for (int i = 0; i < expectedResult.size(); i++) {
                assertEquals(expectedResult.get(i), actualResult.get(i));
            }
        }
    }

    @Nested
    class CachedDataWithNewCertificateInWebcert {

        @BeforeEach
        void setUp() throws JsonProcessingException {
            doReturn(mock(WebCertUser.class)).when(webCertUserService).getUser();

            final var fromWebcert = List.of(
                TestIntygFactory.createUtkast("4", LocalDateTime.parse("2014-01-03T12:12:18")),
                TestIntygFactory.createUtkast("5", LocalDateTime.parse("2014-01-03T12:13:18"))
            );
            doReturn(fromWebcert).when(utkastService).findUtkastByPatientAndUnits(notNull(), notNull());

            final var cachedData = objectMapper.writeValueAsString(List.of(
                TestIntygFactory.createIntygItem("3", LocalDateTime.parse("2014-01-02T10:11:23"), IntygSource.IT)
            ));
            doReturn(cachedData).when(certificatesForPatientCache).get(any(String.class), eq(String.class));
        }

        @Test
        void shouldReturnListWithCertificatesFromITAndWC() throws IOException {
            final var expectedResult = List.of(
                IntygDraftsConverter.convertUtkastToListIntygEntry(
                    TestIntygFactory.createUtkast("5", LocalDateTime.parse("2014-01-03T12:13:18"))),
                IntygDraftsConverter.convertUtkastToListIntygEntry(
                    TestIntygFactory.createUtkast("4", LocalDateTime.parse("2014-01-03T12:12:18"))),
                TestIntygFactory.createIntygItem("3", LocalDateTime.parse("2014-01-02T10:11:23"), IntygSource.IT)
            );

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedResult.size(), actualResult.size());
            for (int i = 0; i < expectedResult.size(); i++) {
                assertEquals(expectedResult.get(i), actualResult.get(i));
            }
        }
    }

    private ListFilter getTestListFilter() {
        final var listFilter = new ListFilter();
        listFilter.addValue(new ListFilterPersonIdValue("19121212-1212"), "PATIENT_ID");
        return listFilter;
    }
}
