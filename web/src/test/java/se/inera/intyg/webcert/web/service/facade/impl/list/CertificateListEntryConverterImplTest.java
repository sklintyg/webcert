/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.impl.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.infra.certificate.dto.CertificateListEntry;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.CertificateForwardFunction;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.ResourceLinkFactory;
import se.inera.intyg.webcert.web.service.facade.list.CertificateListItemConverterImpl;
import se.inera.intyg.webcert.web.service.facade.list.ResourceLinkListHelper;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItemStatus;
import se.inera.intyg.webcert.web.service.facade.list.dto.ForwardedListInfo;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListType;
import se.inera.intyg.webcert.web.service.facade.list.dto.PatientListInfo;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;

@ExtendWith(MockitoExtension.class)
class CertificateListEntryConverterImplTest {

  final String UNIT_NAME = "UNIT_NAME";
  final String CARE_PROVIDER_NAME = "CARE_PROVIDER_NAME";
  final List<ResourceLinkDTO> LINKS = List.of(ResourceLinkFactory.read());
  final String PATIENT_ID = "191212121212";

  @Mock private HsaOrganizationsService hsaOrganizationsService;
  @Mock private ResourceLinkListHelper resourceLinkListHelper;
  @InjectMocks private CertificateListItemConverterImpl certificateListItemConverter;

  @Nested
  class ListDrafts {

    @BeforeEach
    void setup() {
      when(resourceLinkListHelper.get(
              any(ListIntygEntry.class), any(CertificateListItemStatus.class)))
          .thenReturn(LINKS);
    }

    private final ListType LIST_TYPE = ListType.DRAFTS;

    @Test
    void shouldSetCertificateId() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);

      assertEquals(listIntygEntry.getIntygId(), result.getValue(ListColumnType.CERTIFICATE_ID));
    }

    @Test
    void shouldSetCertificateTypeName() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);

      assertEquals(
          listIntygEntry.getIntygTypeName(), result.getValue(ListColumnType.CERTIFICATE_TYPE_NAME));
    }

    @Test
    void shouldSetPatientId() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertEquals(
          listIntygEntry.getPatientId().getPersonnummerWithDash(), patientListInfo.getId());
    }

    @Test
    void shouldSetPatientIsProtectedPerson() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertTrue(patientListInfo.isProtectedPerson());
    }

    @Test
    void shouldSetPatientIsDeceased() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertTrue(patientListInfo.isDeceased());
    }

    @Test
    void shouldSetPatientIsTestIndicated() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertTrue(patientListInfo.isTestIndicated());
    }

    @Test
    void shouldSetPatientIsNotProtectedPerson() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), false, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertFalse(patientListInfo.isProtectedPerson());
    }

    @Test
    void shouldSetPatientIsNotDeceased() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), false, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertFalse(patientListInfo.isDeceased());
    }

    @Test
    void shouldSetPatientIsNotTestIndicated() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), false, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertFalse(patientListInfo.isTestIndicated());
    }

    @Test
    void shouldSetDraftStatusComplete() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);

      assertEquals(
          CertificateListItemStatus.COMPLETE.getName(), result.getValue(ListColumnType.STATUS));
    }

    @Test
    void shouldSetDraftStatusIncomplete() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_INCOMPLETE.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);

      assertEquals(
          CertificateListItemStatus.INCOMPLETE.getName(), result.getValue(ListColumnType.STATUS));
    }

    @Test
    void shouldSetDraftStatusLocked() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_LOCKED.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);

      assertEquals(
          CertificateListItemStatus.LOCKED.getName(), result.getValue(ListColumnType.STATUS));
    }

    @Test
    void shouldSetSaved() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);

      assertEquals(listIntygEntry.getLastUpdated(), result.getValue(ListColumnType.SAVED));
    }

    @Test
    void shouldSetSavedBy() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);

      assertEquals(listIntygEntry.getUpdatedSignedBy(), result.getValue(ListColumnType.SAVED_BY));
    }

    @Test
    void shouldSetLinks() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
      final var links = (List<ResourceLinkDTO>) result.getValue(ListColumnType.LINKS);

      assertTrue(links.size() > 0);
      assertEquals(LINKS.get(0), links.get(0));
    }

    @Test
    void shouldNotSetForwardedInfoIfLinkDoesNotExist() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, false);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
      final var forwarded = result.getValue(ListColumnType.FORWARD_CERTIFICATE);

      assertNull(forwarded);
    }
  }

  @Nested
  class Forwarded {

    private final ListType LIST_TYPE = ListType.DRAFTS;
    final List<ResourceLinkDTO> linksWithForwarded =
        List.of(ResourceLinkFactory.read(), CertificateForwardFunction.createResourceLink());

    @BeforeEach
    void setup() {
      final var unit = new Vardenhet();
      final var careProvider = new Vardgivare();

      unit.setNamn(UNIT_NAME);
      careProvider.setNamn(CARE_PROVIDER_NAME);

      when(resourceLinkListHelper.get(
              any(ListIntygEntry.class), any(CertificateListItemStatus.class)))
          .thenReturn(linksWithForwarded);
      when(hsaOrganizationsService.getVardenhet(anyString())).thenReturn(unit);
      when(hsaOrganizationsService.getVardgivareInfo(anyString())).thenReturn(careProvider);
    }

    @Test
    void shouldSetIsForwarded() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
      final var forwarded = (boolean) result.getValue(ListColumnType.FORWARDED);

      assertTrue(forwarded);
    }

    @Test
    void shouldSetUnitName() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
      final var forwardedListInfo =
          (ForwardedListInfo) result.getValue(ListColumnType.FORWARD_CERTIFICATE);

      assertEquals(UNIT_NAME, forwardedListInfo.getUnitName());
    }

    @Test
    void shouldSetCareProviderName() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
      final var forwardedListInfo =
          (ForwardedListInfo) result.getValue(ListColumnType.FORWARD_CERTIFICATE);

      assertEquals(CARE_PROVIDER_NAME, forwardedListInfo.getCareProviderName());
    }

    @Test
    void shouldSetIsNotForwarded() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, false);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
      final var forwarded = (boolean) result.getValue(ListColumnType.FORWARDED);

      assertFalse(forwarded);
    }

    @Test
    void shouldSetCertificateType() {
      final var listIntygEntry =
          ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, false);
      final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
      final var forwardedListInfo =
          (ForwardedListInfo) result.getValue(ListColumnType.FORWARD_CERTIFICATE);

      assertEquals(listIntygEntry.getIntygType(), forwardedListInfo.getCertificateType());
    }
  }

  @Nested
  class Statuses {

    private final ListType LIST_TYPE = ListType.DRAFTS;
    WebcertCertificateRelation certificateRelation;
    Relations relations;
    Relations.FrontendRelations frontendRelations;

    @BeforeEach
    void setup() {
      relations = mock(Relations.class);
      frontendRelations = mock(Relations.FrontendRelations.class);
      certificateRelation = mock(WebcertCertificateRelation.class);

      doReturn(frontendRelations).when(relations).getLatestChildRelations();
    }

    @Nested
    class Complemented {

      @Test
      void shouldSetStatusSentWhenComplementedByDraftRelation() {
        final var entry = ListTestHelper.createListIntygEntry("SIGNED", false, false);
        entry.setRelations(relations);
        doReturn(certificateRelation).when(frontendRelations).getComplementedByUtkast();

        final var result = certificateListItemConverter.convert(entry, LIST_TYPE);

        assertEquals(CertificateListItemStatus.SENT.getName(), result.getValue("STATUS"));
      }

      @Test
      void shouldSetStatusComplementedWhenComplementedByCertificateRelation() {
        final var entry = ListTestHelper.createListIntygEntry("SIGNED", false, false);
        entry.setRelations(relations);
        doReturn(certificateRelation).when(frontendRelations).getComplementedByIntyg();

        final var result = certificateListItemConverter.convert(entry, LIST_TYPE);

        assertEquals(CertificateListItemStatus.COMPLEMENTED.getName(), result.getValue("STATUS"));
      }

      @Test
      void shouldNotSetStatusComplementedWhenComplementedByRevokedCertificateRelation() {
        final var entry = ListTestHelper.createListIntygEntry("SIGNED", false, false);
        entry.setRelations(relations);
        doReturn(certificateRelation).when(frontendRelations).getComplementedByIntyg();

        doReturn(true).when(certificateRelation).isMakulerat();

        final var result = certificateListItemConverter.convert(entry, LIST_TYPE);

        assertNotEquals(
            CertificateListItemStatus.COMPLEMENTED.getName(), result.getValue("STATUS"));
      }
    }

    @Nested
    class Replaced {

      @Test
      void shouldNotSetStatusReplacedWhenReplacedByDraftRelation() {
        final var entry = ListTestHelper.createListIntygEntry("SENT", false, false);
        entry.setRelations(relations);

        final var result = certificateListItemConverter.convert(entry, LIST_TYPE);

        assertNotEquals(CertificateListItemStatus.REPLACED.getName(), result.getValue("STATUS"));
      }

      @Test
      void shouldSetStatusReplacedWhenReplacedByCertificateRelation() {
        final var entry = ListTestHelper.createListIntygEntry("SENT", false, false);
        entry.setRelations(relations);
        doReturn(certificateRelation).when(frontendRelations).getReplacedByIntyg();

        final var result = certificateListItemConverter.convert(entry, LIST_TYPE);

        assertEquals(CertificateListItemStatus.REPLACED.getName(), result.getValue("STATUS"));
      }

      @Test
      void shouldNotSetStatusReplacedWhenReplacedByRevokedCertificateRelation() {
        final var entry = ListTestHelper.createListIntygEntry("SENT", false, false);
        entry.setRelations(relations);
        doReturn(certificateRelation).when(frontendRelations).getComplementedByIntyg();

        doReturn(true).when(certificateRelation).isMakulerat();

        final var result = certificateListItemConverter.convert(entry, LIST_TYPE);

        assertNotEquals(CertificateListItemStatus.REPLACED.getName(), result.getValue("STATUS"));
      }
    }

    @Nested
    class Revoked {

      @Test
      void shouldTranslateCancelledIntoRevokedStatus() {
        final var entry = ListTestHelper.createListIntygEntry("CANCELLED", false, false);
        entry.setRelations(relations);

        final var result = certificateListItemConverter.convert(entry, LIST_TYPE);

        assertEquals(CertificateListItemStatus.REVOKED.getName(), result.getValue("STATUS"));
      }

      @Test
      void shouldTranslateDraftLockedCancelledIntoRevokedStatus() {
        final var entry =
            ListTestHelper.createListIntygEntry("DRAFT_LOCKED_CANCELLED", false, false);
        entry.setRelations(relations);

        final var result = certificateListItemConverter.convert(entry, LIST_TYPE);

        assertEquals(CertificateListItemStatus.REVOKED.getName(), result.getValue("STATUS"));
      }
    }
  }

  @Nested
  class ListSignedCertificates {

    @BeforeEach
    void setup() {
      when(resourceLinkListHelper.get(
              any(CertificateListEntry.class), any(CertificateListItemStatus.class)))
          .thenReturn(LINKS);
    }

    @Test
    void shouldSetLinks() {
      final var entry = ListTestHelper.createCertificateListEntry();
      final var result = certificateListItemConverter.convert(entry);
      final var links = (List<ResourceLinkDTO>) result.getValue(ListColumnType.LINKS);

      assertTrue(links.size() > 0);
      assertEquals(LINKS.get(0), links.get(0));
    }

    @Test
    void shouldSetCertificateId() {
      final var entry = ListTestHelper.createCertificateListEntry();
      final var result = certificateListItemConverter.convert(entry);

      assertEquals(entry.getCertificateId(), result.getValue(ListColumnType.CERTIFICATE_ID));
    }

    @Test
    void shouldSetCertificateTypeName() {
      final var entry = ListTestHelper.createCertificateListEntry();
      final var result = certificateListItemConverter.convert(entry);

      assertEquals(
          entry.getCertificateTypeName(), result.getValue(ListColumnType.CERTIFICATE_TYPE_NAME));
    }

    @Test
    void shouldSetPatientId() {
      final var entry = ListTestHelper.createCertificateListEntry();
      final var result = certificateListItemConverter.convert(entry);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertEquals(entry.getCivicRegistrationNumber(), patientListInfo.getId());
    }

    @Test
    void shouldSetPatientIsProtectedPerson() {
      final var entry = ListTestHelper.createCertificateListEntry(false, true, "191212121212");
      final var result = certificateListItemConverter.convert(entry);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertTrue(patientListInfo.isProtectedPerson());
    }

    @Test
    void shouldSetPatientIsDeceased() {
      final var entry = ListTestHelper.createCertificateListEntry(false, true, "191212121212");
      final var result = certificateListItemConverter.convert(entry);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertTrue(patientListInfo.isDeceased());
    }

    @Test
    void shouldSetPatientIsTestIndicated() {
      final var entry = ListTestHelper.createCertificateListEntry(false, true, "191212121212");
      final var result = certificateListItemConverter.convert(entry);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertTrue(patientListInfo.isTestIndicated());
    }

    @Test
    void shouldSetIsSent() {
      final var entry = ListTestHelper.createCertificateListEntry(true, true, "191212121212");
      final var result = certificateListItemConverter.convert(entry);

      assertEquals(result.getValue("STATUS"), "Skickat");
    }

    @Test
    void shouldSetIsNotSent() {
      final var entry = ListTestHelper.createCertificateListEntry(false, true, "191212121212");
      final var result = certificateListItemConverter.convert(entry);

      assertEquals(result.getValue("STATUS"), "Ej skickat");
    }

    @Test
    void shouldSetPatientIsNotProtectedPerson() {
      final var entry = ListTestHelper.createCertificateListEntry(false, false, "191212121212");
      final var result = certificateListItemConverter.convert(entry);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertFalse(patientListInfo.isProtectedPerson());
    }

    @Test
    void shouldSetPatientIsNotDeceased() {
      final var entry = ListTestHelper.createCertificateListEntry(false, false, "191212121212");
      final var result = certificateListItemConverter.convert(entry);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertFalse(patientListInfo.isDeceased());
    }

    @Test
    void shouldSetPatientIsNotTestIndicated() {
      final var entry = ListTestHelper.createCertificateListEntry(false, false, "191212121212");
      final var result = certificateListItemConverter.convert(entry);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertFalse(patientListInfo.isTestIndicated());
    }

    @Test
    void shouldSetSigned() {
      final var entry = ListTestHelper.createCertificateListEntry();
      final var result = certificateListItemConverter.convert(entry);

      assertEquals(entry.getSignedDate(), result.getValue(ListColumnType.SIGNED));
    }
  }

  @Nested
  class ListQuestions {

    @BeforeEach
    void setup() {
      when(resourceLinkListHelper.get(
              any(ArendeListItem.class), any(CertificateListItemStatus.class)))
          .thenReturn(LINKS);
    }

    @Test
    void shouldSetLinks() {
      final var entry = ListTestHelper.createQuestionListEntry(true, PATIENT_ID);
      final var result = certificateListItemConverter.convert(entry);
      final var links = (List<ResourceLinkDTO>) result.getValue(ListColumnType.LINKS);

      assertTrue(links.size() > 0);
      assertEquals(LINKS.get(0), links.get(0));
    }

    @Test
    void shouldSetCertificateId() {
      final var entry = ListTestHelper.createQuestionListEntry(true, PATIENT_ID);
      final var result = certificateListItemConverter.convert(entry);

      assertEquals(entry.getIntygId(), result.getValue(ListColumnType.CERTIFICATE_ID));
    }

    @Test
    void shouldSetPatientId() {
      final var entry = ListTestHelper.createQuestionListEntry(true, PATIENT_ID);
      final var result = certificateListItemConverter.convert(entry);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertEquals(entry.getPatientId(), patientListInfo.getId());
    }

    @Test
    void shouldSetPatientIsProtectedPerson() {
      final var entry = ListTestHelper.createQuestionListEntry(true, PATIENT_ID);
      final var result = certificateListItemConverter.convert(entry);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertTrue(patientListInfo.isProtectedPerson());
    }

    @Test
    void shouldSetPatientIsDeceased() {
      final var entry = ListTestHelper.createQuestionListEntry(true, PATIENT_ID);
      final var result = certificateListItemConverter.convert(entry);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertTrue(patientListInfo.isDeceased());
    }

    @Test
    void shouldSetPatientIsTestIndicated() {
      final var entry = ListTestHelper.createQuestionListEntry(true, PATIENT_ID);
      final var result = certificateListItemConverter.convert(entry);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertTrue(patientListInfo.isTestIndicated());
    }

    @Test
    void shouldSetPatientIsNotProtectedPerson() {
      final var entry = ListTestHelper.createQuestionListEntry(false, PATIENT_ID);
      final var result = certificateListItemConverter.convert(entry);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertFalse(patientListInfo.isProtectedPerson());
    }

    @Test
    void shouldSetPatientIsNotDeceased() {
      final var entry = ListTestHelper.createQuestionListEntry(false, PATIENT_ID);
      final var result = certificateListItemConverter.convert(entry);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertFalse(patientListInfo.isDeceased());
    }

    @Test
    void shouldSetPatientIsNotTestIndicated() {
      final var entry = ListTestHelper.createQuestionListEntry(false, PATIENT_ID);
      final var result = certificateListItemConverter.convert(entry);
      final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

      assertFalse(patientListInfo.isTestIndicated());
    }

    @Test
    void shouldSetSignedBy() {
      final var entry = ListTestHelper.createQuestionListEntry(true, PATIENT_ID);
      final var result = certificateListItemConverter.convert(entry);

      assertEquals(entry.getSigneratAvNamn(), result.getValue(ListColumnType.SIGNED_BY));
    }

    @Test
    void shouldSetQuestionAction() {
      final var entry = ListTestHelper.createQuestionListEntry(true, PATIENT_ID);
      final var result = certificateListItemConverter.convert(entry);

      assertTrue(result.getValue(ListColumnType.QUESTION_ACTION).toString().length() > 0);
    }

    @Test
    void shouldSetSentReceived() {
      final var entry = ListTestHelper.createQuestionListEntry(true, PATIENT_ID);
      final var result = certificateListItemConverter.convert(entry);

      assertEquals(entry.getReceivedDate(), result.getValue(ListColumnType.SENT_RECEIVED));
    }

    @Test
    void shouldSetSenderFK() {
      final var entry = ListTestHelper.createQuestionListEntry(true, PATIENT_ID);
      entry.setFragestallare("FK");
      final var result = certificateListItemConverter.convert(entry);

      assertEquals(result.getValue(ListColumnType.SENDER), "Försäkringskassan");
    }

    @Test
    void shouldSetSenderWC() {
      final var entry = ListTestHelper.createQuestionListEntry(true, PATIENT_ID);
      entry.setFragestallare("WC");
      final var result = certificateListItemConverter.convert(entry);

      assertEquals(result.getValue(ListColumnType.SENDER), "Vårdenheten");
    }

    @Test
    void shouldSetForwarded() {
      final var entry = ListTestHelper.createQuestionListEntry(true, PATIENT_ID);
      entry.setVidarebefordrad(true);
      final var result = certificateListItemConverter.convert(entry);

      assertTrue((boolean) result.getValue(ListColumnType.FORWARDED));
    }
  }
}
