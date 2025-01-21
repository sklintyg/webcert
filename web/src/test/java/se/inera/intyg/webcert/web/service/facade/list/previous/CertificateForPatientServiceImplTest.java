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

package se.inera.intyg.webcert.web.service.facade.list.previous;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
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
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations.FrontendRelations;

@ExtendWith(MockitoExtension.class)
class CertificateForPatientServiceImplTest {

    private ConcurrentMapCache certificatesForPatientCache;
    private CustomObjectMapper objectMapper;
    @Mock
    private IntygService intygService;
    @Mock
    private UtkastService utkastService;
    @Mock
    private WebCertUserService webCertUserService;
    private CertificateForPatientServiceImpl certificateForPatient;
    public static final String CACHE_KEY = "-2053878503";
    private static final String PATIENT_ID = "1912121212";
    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer(PATIENT_ID).orElseThrow();
    private static final List<String> UNIT_IDS = List.of("Unit1", "Unit2");
    private static final String HSA_ID = "HSAID-11223";

    @BeforeEach
    void setUp() {
        certificatesForPatientCache = new ConcurrentMapCache("test-cache");
        objectMapper = new CustomObjectMapper();
        certificateForPatient = new CertificateForPatientServiceImpl(
            certificatesForPatientCache,
            objectMapper,
            intygService,
            utkastService,
            webCertUserService);

        final var webCertUser = mock(WebCertUser.class);
        doReturn(HSA_ID).when(webCertUser).getHsaId();
        doReturn(webCertUser).when(webCertUserService).getUser();
    }

    @Nested
    class NoCachedData {

        @BeforeEach
        void setUp() {
            final var fromWebcert = List.of(
                utkastFromWC("4", "2014-01-03T12:12:18", CertificateState.SENT.name())
            );
            doReturn(fromWebcert).when(utkastService).findUtkastByPatientAndUnits(notNull(), notNull());

            final var fromIntygstjanst = List.of(
                intygListItemFromIT("3", "2014-01-02T10:11:23", CertificateState.SENT.name())
            );
            doReturn(fromIntygstjanst).when(intygService).listIntygFromIT(notNull(), notNull());
        }

        @Test
        void shouldReturnListWithCertificatesFromITAndWC() {
            final var expectedResult = List.of(
                intygListItemFromWC("4", "2014-01-03T12:12:18", CertificateState.SENT.name()),
                intygListItemFromIT("3", "2014-01-02T10:11:23", CertificateState.SENT.name())
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
                intygListItemFromIT("3", "2014-01-02T10:11:23", CertificateState.SENT.name())
            );

            certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            final var actualResult = (List<ListIntygEntry>) objectMapper.readValue(
                certificatesForPatientCache.get(CACHE_KEY, String.class),
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
            final var fromWebcert = List.of(
                utkastFromWC("4", "2014-01-03T12:12:18", CertificateState.SENT.name())
            );
            doReturn(fromWebcert).when(utkastService).findUtkastByPatientAndUnits(notNull(), notNull());

            final var cachedData = objectMapper.writeValueAsString(List.of(
                intygListItemFromIT("3", "2014-01-02T10:11:23", CertificateState.SENT.name())
            ));
            certificatesForPatientCache.put(CACHE_KEY, cachedData);
        }

        @Test
        void shouldReturnListWithCertificatesFromITAndWC() {
            final var expectedResult = List.of(
                intygListItemFromWC("4", "2014-01-03T12:12:18", CertificateState.SENT.name()),
                intygListItemFromIT("3", "2014-01-02T10:11:23", CertificateState.SENT.name())
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
            final var fromWebcert = List.of(
                utkastFromWC("4", "2014-01-03T12:12:18", CertificateState.SENT.name()),
                utkastFromWC("5", "2014-01-03T12:13:18", CertificateState.SENT.name())
            );
            doReturn(fromWebcert).when(utkastService).findUtkastByPatientAndUnits(notNull(), notNull());

            final var cachedData = objectMapper.writeValueAsString(List.of(
                intygListItemFromIT("3", "2014-01-02T10:11:23", CertificateState.SENT.name())
            ));
            certificatesForPatientCache.put(CACHE_KEY, cachedData);
        }

        @Test
        void shouldReturnListWithCertificatesFromITAndWC() {
            final var expectedResult = List.of(
                intygListItemFromWC("5", "2014-01-03T12:13:18", CertificateState.SENT.name()),
                intygListItemFromWC("4", "2014-01-03T12:12:18", CertificateState.SENT.name()),
                intygListItemFromIT("3", "2014-01-02T10:11:23", CertificateState.SENT.name())
            );

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedResult.size(), actualResult.size());
            for (int i = 0; i < expectedResult.size(); i++) {
                assertEquals(expectedResult.get(i), actualResult.get(i));
            }
        }
    }

    @Nested
    class CachedDataWithUpdatedCertificateInWebcert {

        @BeforeEach
        void setUp() throws JsonProcessingException {
            final var fromWebcert = List.of(
                utkastFromWC("4", "2014-01-03T12:12:18", CertificateState.SENT.name()),
                utkastFromWC("3", "2014-01-03T12:13:18", CertificateState.SENT.name())
            );
            doReturn(fromWebcert).when(utkastService).findUtkastByPatientAndUnits(notNull(), notNull());

            final var cachedData = objectMapper.writeValueAsString(List.of(
                intygListItemFromIT("3", "2014-01-02T10:11:23", CertificateState.SENT.name())
            ));
            certificatesForPatientCache.put(CACHE_KEY, cachedData);
        }

        @Test
        void shouldReturnListWithCertificatesFromITAndWC() {
            final var expectedResult = List.of(
                intygListItemFromWC("3", "2014-01-03T12:13:18", CertificateState.SENT.name()),
                intygListItemFromWC("4", "2014-01-03T12:12:18", CertificateState.SENT.name())
            );

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedResult.size(), actualResult.size());
            for (int i = 0; i < expectedResult.size(); i++) {
                assertEquals(expectedResult.get(i), actualResult.get(i));
            }
        }
    }

    @Nested
    class CachedDataWithCancelledCertificateInIntygstjanst {

        @BeforeEach
        void setUp() throws JsonProcessingException {
            final var fromWebcert = List.of(
                utkastFromWC("4", "2014-01-03T12:12:18", CertificateState.SENT.name()),
                utkastFromWC("3", "2014-01-03T12:13:18", CertificateState.SENT.name())
            );
            doReturn(fromWebcert).when(utkastService).findUtkastByPatientAndUnits(notNull(), notNull());

            final var cachedData = objectMapper.writeValueAsString(List.of(
                intygListItemFromIT("3", "2014-01-02T10:11:23", CertificateState.CANCELLED.name())
            ));
            certificatesForPatientCache.put(CACHE_KEY, cachedData);
        }

        @Test
        void shouldReturnListWithCertificatesFromITAndWC() {
            final var expectedResult = List.of(
                intygListItemFromWC("4", "2014-01-03T12:12:18", CertificateState.SENT.name()),
                intygListItemFromIT("3", "2014-01-02T10:11:23", CertificateState.CANCELLED.name())
            );

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedResult.size(), actualResult.size());
            for (int i = 0; i < expectedResult.size(); i++) {
                assertEquals(expectedResult.get(i), actualResult.get(i));
            }
        }
    }

    @Nested
    class CachedDataWithSentCertificateInIntygstjanst {

        @BeforeEach
        void setUp() throws JsonProcessingException {
            final var fromWebcert = List.of(
                utkastFromWC("4", "2014-01-03T12:12:18", CertificateState.RECEIVED.name()),
                utkastFromWC("3", "2014-01-03T12:13:18", CertificateState.RECEIVED.name())
            );
            doReturn(fromWebcert).when(utkastService).findUtkastByPatientAndUnits(notNull(), notNull());

            final var cachedData = objectMapper.writeValueAsString(List.of(
                intygListItemFromIT("3", "2014-01-02T10:11:23", CertificateState.SENT.name())
            ));
            certificatesForPatientCache.put(CACHE_KEY, cachedData);
        }

        @Test
        void shouldReturnListWithCertificatesFromITAndWC() {
            final var expectedResult = List.of(
                intygListItemFromWC("4", "2014-01-03T12:12:18", CertificateState.RECEIVED.name()),
                intygListItemFromIT("3", "2014-01-02T10:11:23", CertificateState.SENT.name())
            );

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedResult.size(), actualResult.size());
            for (int i = 0; i < expectedResult.size(); i++) {
                assertEquals(expectedResult.get(i), actualResult.get(i));
            }
        }
    }

    @Nested
    class CachedDataWithSentCertificateInIntygstjanstButCancelledInWebcert {

        @BeforeEach
        void setUp() throws JsonProcessingException {
            final var fromWebcert = List.of(
                utkastFromWC("4", "2014-01-03T12:12:18", CertificateState.RECEIVED.name()),
                utkastFromWC("3", "2014-01-03T12:13:18", CertificateState.CANCELLED.name())
            );
            doReturn(fromWebcert).when(utkastService).findUtkastByPatientAndUnits(notNull(), notNull());

            final var cachedData = objectMapper.writeValueAsString(List.of(
                intygListItemFromIT("3", "2014-01-02T10:11:23", CertificateState.SENT.name())
            ));
            certificatesForPatientCache.put(CACHE_KEY, cachedData);
        }

        @Test
        void shouldReturnListWithCertificatesFromITAndWC() {
            final var expectedResult = List.of(
                intygListItemFromWC("3", "2014-01-03T12:13:18", CertificateState.CANCELLED.name()),
                intygListItemFromWC("4", "2014-01-03T12:12:18", CertificateState.RECEIVED.name())
            );

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedResult.size(), actualResult.size());
            for (int i = 0; i < expectedResult.size(); i++) {
                assertEquals(expectedResult.get(i), actualResult.get(i));
            }
        }
    }

    @Nested
    class IncludeRelationsOnListIntygEntry {

        @Test
        void shouldNotAddRelationIfNoRelationExists() {
            final var expectedRelations = prepareCertificates(null, null);

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedRelations, actualResult.get(0).getRelations());
        }

        @Test
        void shouldAddRelationIfReplacedBySignedCertificate() {
            final var expectedRelations = prepareCertificates(List.of(RelationKod.ERSATT), CertificateState.RECEIVED);

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedRelations, actualResult.get(0).getRelations());
        }

        @Test
        void shouldNotAddRelationIfReplacedByUnsignedCertificate() {
            final var expectedRelations = prepareCertificates(List.of(RelationKod.ERSATT), CertificateState.IN_PROGRESS);

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedRelations, actualResult.get(0).getRelations());
        }

        @Test
        void shouldNotAddRelationIfReplacedByRevokedCertificate() {
            final var expectedRelations = prepareCertificates(List.of(RelationKod.ERSATT), CertificateState.CANCELLED);

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedRelations, actualResult.get(0).getRelations());
        }

        @Test
        void shouldAddRelationIfComplementedBySignedCertificate() {
            final var expectedRelations = prepareCertificates(List.of(RelationKod.KOMPLT), CertificateState.RECEIVED);

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedRelations, actualResult.get(0).getRelations());
        }

        @Test
        void shouldNotAddRelationIfComplementedByUnsignedCertificate() {
            final var expectedRelations = prepareCertificates(List.of(RelationKod.KOMPLT), CertificateState.IN_PROGRESS);

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedRelations, actualResult.get(0).getRelations());
        }

        @Test
        void shouldNotAddRelationIfComplementedByRevokedCertificate() {
            final var expectedRelations = prepareCertificates(List.of(RelationKod.KOMPLT), CertificateState.CANCELLED);

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedRelations, actualResult.get(0).getRelations());
        }

        @Test
        void shouldAddRelationsIfComplementedAndReplacedBySignedCertificate() {
            final var expectedRelations = prepareCertificates(List.of(RelationKod.KOMPLT, RelationKod.ERSATT), CertificateState.RECEIVED);

            final var actualResult = certificateForPatient.get(getTestListFilter(), PERSONNUMMER, UNIT_IDS);

            assertEquals(expectedRelations, actualResult.get(0).getRelations());
        }

        private Relations prepareCertificates(List<RelationKod> relationKodList, CertificateState certificateState) {
            if (relationKodList != null) {
                final var fromWebcert = relationKodList.stream()
                    .map(relationKod -> {
                        final var certificateWithRelation = utkastFromWC("4", "2014-01-03T12:12:18", certificateState.name());
                        certificateWithRelation.setRelationKod(relationKod);
                        certificateWithRelation.setRelationIntygsId("3");
                        certificateWithRelation.setSkapad(LocalDateTime.of(2014, 01, 03, 12, 13, 18));
                        return certificateWithRelation;
                    })
                    .collect(Collectors.toList());
                fromWebcert.add(utkastFromWC("3", "2014-01-03T12:13:18", CertificateState.RECEIVED.name()));

                doReturn(fromWebcert).when(utkastService).findUtkastByPatientAndUnits(notNull(), notNull());
            } else {
                final var fromWebcert = List.of(
                    utkastFromWC("4", "2014-01-03T12:13:18", CertificateState.RECEIVED.name()),
                    utkastFromWC("3", "2014-01-03T12:13:18", CertificateState.RECEIVED.name())
                );

                doReturn(fromWebcert).when(utkastService).findUtkastByPatientAndUnits(notNull(), notNull());
            }

            final var relations = new Relations();
            final var frontendRelations = new FrontendRelations();
            relations.setLatestChildRelations(frontendRelations);
            if (relationKodList != null && CertificateState.RECEIVED.equals(certificateState)) {
                relationKodList.forEach(relationKod -> {
                    final var webcertCertificateRelation = new WebcertCertificateRelation(
                        "3",
                        relationKod,
                        LocalDateTime.of(2014, 01, 03, 12, 13, 18),
                        UtkastStatus.SIGNED,
                        false
                    );
                    if (RelationKod.ERSATT.equals(relationKod)) {
                        frontendRelations.setReplacedByIntyg(webcertCertificateRelation);
                    }
                    if (RelationKod.KOMPLT.equals(relationKod)) {
                        frontendRelations.setComplementedByIntyg(webcertCertificateRelation);
                    }
                });
            }
            return relations;
        }
    }

    private Utkast utkastFromWC(String id, String localDateTimeAsStr, String status) {
        final var utkast = TestIntygFactory.createUtkast(id, LocalDateTime.parse(localDateTimeAsStr));
        if (CertificateState.SENT.name().equalsIgnoreCase(status)) {
            utkast.setStatus(UtkastStatus.SIGNED);
            utkast.setSkickadTillMottagare("FK");
            utkast.setSkickadTillMottagareDatum(LocalDateTime.parse(localDateTimeAsStr));
            return utkast;
        }

        if (CertificateState.RECEIVED.name().equalsIgnoreCase(status)) {
            utkast.setSignatur(new Signatur());
            utkast.setStatus(UtkastStatus.SIGNED);
            return utkast;
        }

        if (CertificateState.CANCELLED.name().equalsIgnoreCase(status)) {
            utkast.setSignatur(new Signatur());
            utkast.setStatus(UtkastStatus.SIGNED);
            utkast.setAterkalladDatum(LocalDateTime.parse(localDateTimeAsStr));
            return utkast;
        }

        return utkast;
    }

    private ListIntygEntry intygListItemFromWC(String id, String localDateTimeAsStr, String status) {
        return IntygDraftsConverter.convertUtkastToListIntygEntry(
            utkastFromWC(id, localDateTimeAsStr, status)
        );
    }

    private ListIntygEntry intygListItemFromIT(String id, String localDateTimeAsStr, String status) {
        final var intygItem = TestIntygFactory.createIntygItem(id, LocalDateTime.parse(localDateTimeAsStr), IntygSource.IT);
        intygItem.setStatus(status);
        return intygItem;
    }

    private ListFilter getTestListFilter() {
        final var listFilter = new ListFilter();
        listFilter.addValue(new ListFilterPersonIdValue("19121212-1212"), "PATIENT_ID");
        return listFilter;
    }
}
