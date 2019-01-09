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
package se.inera.intyg.webcert.web.service.intyg.decorator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.IntygRelations;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.ListRelationsForCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.ListRelationsForCertificateResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.ListRelationsForCertificateType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.Relation;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvRelation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntygRelationHelperImplTest {
    private static final String INTYG_ID = "intyg-123";
    private static final String OTHER_INTYG_ID = "intyg-456";

    private static final String OTHER_INTYG_ID_2 = "intyg-2";
    private static final String OTHER_INTYG_ID_3 = "intyg-3";

    private static final String PARENT_INTYG_1 = "intyg-4";

    @Mock
    private CertificateRelationService certificateRelationService;

    @Mock
    private ListRelationsForCertificateResponderInterface listRelationsForCertificateResponderInterface;

    @InjectMocks
    private IntygRelationHelperImpl testee;

    @Before
    public void init() {
        when(certificateRelationService.getRelations(INTYG_ID)).thenReturn(new Relations());
    }

    @Test
    public void testGetRelationsForIntygNothingInIT() {
        when(listRelationsForCertificateResponderInterface.listRelationsForCertificate(isNull(),
                any(ListRelationsForCertificateType.class))).thenReturn(new ListRelationsForCertificateResponseType());

        Relations relationsForIntyg = testee.getRelationsForIntyg(INTYG_ID);
        assertNotNull(relationsForIntyg);
    }

    @Test
    public void testGetRelationsForIntygNothingInITWithMergeFromWebcert() {
        when(listRelationsForCertificateResponderInterface.listRelationsForCertificate(isNull(),
                any(ListRelationsForCertificateType.class))).thenReturn(new ListRelationsForCertificateResponseType());

        Relations webcertRelations = new Relations();
        Relations.FrontendRelations fr = webcertRelations.getLatestChildRelations();
        fr.setReplacedByIntyg(new WebcertCertificateRelation(OTHER_INTYG_ID_2, RelationKod.ERSATT, LocalDateTime.now().minusDays(1),
                UtkastStatus.SIGNED, false));
        fr.setReplacedByUtkast(new WebcertCertificateRelation(OTHER_INTYG_ID_2, RelationKod.ERSATT, LocalDateTime.now().minusDays(1),
                UtkastStatus.DRAFT_COMPLETE, false));
        fr.setComplementedByIntyg(new WebcertCertificateRelation(OTHER_INTYG_ID_2, RelationKod.KOMPLT, LocalDateTime.now().minusDays(2),
                UtkastStatus.SIGNED, false));
        fr.setComplementedByUtkast(new WebcertCertificateRelation(OTHER_INTYG_ID_2, RelationKod.KOMPLT, LocalDateTime.now().minusDays(2),
                UtkastStatus.DRAFT_INCOMPLETE, false));
        webcertRelations.setParent(
                new WebcertCertificateRelation(PARENT_INTYG_1, RelationKod.KOMPLT, LocalDateTime.now().minusDays(3), UtkastStatus.SIGNED, false));

        when(certificateRelationService.getRelations(INTYG_ID)).thenReturn(webcertRelations);

        Relations relationsForIntyg = testee.getRelationsForIntyg(INTYG_ID);
        assertNotNull(relationsForIntyg);
        assertFrontendRelations(relationsForIntyg.getLatestChildRelations(),
                webcertRelations.getLatestChildRelations().getComplementedByIntyg(),
                webcertRelations.getLatestChildRelations().getComplementedByUtkast(),
                webcertRelations.getLatestChildRelations().getReplacedByIntyg(),
                webcertRelations.getLatestChildRelations().getReplacedByUtkast());
    }

    @Test
    public void testGetRelationsForIntygOneInIT() {
        when(listRelationsForCertificateResponderInterface.listRelationsForCertificate(isNull(),
                any(ListRelationsForCertificateType.class))).thenReturn(buildResponse());

        Relations relationsForIntyg = testee.getRelationsForIntyg(INTYG_ID);
        assertNotNull(relationsForIntyg);
        assertFrontendRelationsIntygsIds(relationsForIntyg.getLatestChildRelations(), null, null, OTHER_INTYG_ID, null);
    }

    @Test
    public void testGetRelationsForIntygOneParentInIT() {
        when(listRelationsForCertificateResponderInterface.listRelationsForCertificate(isNull(),
                any(ListRelationsForCertificateType.class))).thenReturn(buildResponseWithParent());

        Relations relationsForIntyg = testee.getRelationsForIntyg(INTYG_ID);
        assertNotNull(relationsForIntyg);
        assertEquals(PARENT_INTYG_1, relationsForIntyg.getParent().getIntygsId());
        assertFrontendRelations(relationsForIntyg.getLatestChildRelations(), null, null, null, null);
    }

    @Test
    public void testGetRelationsForIntygOneInITAndTwoFromWebcert() {
        when(listRelationsForCertificateResponderInterface.listRelationsForCertificate(isNull(),
                any(ListRelationsForCertificateType.class))).thenReturn(buildResponse());
        when(certificateRelationService.getRelations(INTYG_ID)).thenReturn(buildWebcertRelations());

        Relations relationsForIntyg = testee.getRelationsForIntyg(INTYG_ID);
        assertNotNull(relationsForIntyg);
        assertFrontendRelationsIntygsIds(relationsForIntyg.getLatestChildRelations(), OTHER_INTYG_ID_3, null, OTHER_INTYG_ID,
                OTHER_INTYG_ID_2);
    }

    @Test
    public void testGetRelationsForIntygOneInITAndThreeFromWebcertIncludingParent() {
        when(listRelationsForCertificateResponderInterface.listRelationsForCertificate(isNull(),
                any(ListRelationsForCertificateType.class))).thenReturn(buildResponse());
        when(certificateRelationService.getRelations(INTYG_ID)).thenReturn(buildWebcertRelationsWithParent());

        Relations relationsForIntyg = testee.getRelationsForIntyg(INTYG_ID);
        assertNotNull(relationsForIntyg);
        assertFrontendRelationsIntygsIds(relationsForIntyg.getLatestChildRelations(), OTHER_INTYG_ID_3, null, OTHER_INTYG_ID,
                OTHER_INTYG_ID_2);
    }

    @Test
    public void testDecorate() {
        when(listRelationsForCertificateResponderInterface.listRelationsForCertificate(isNull(),
                any(ListRelationsForCertificateType.class))).thenReturn(buildResponse());
        when(certificateRelationService.getRelations(INTYG_ID)).thenReturn(buildWebcertRelationsWithParent());
        List<ListIntygEntry> listIntygEntries = buildList();
        testee.decorateIntygListWithRelations(listIntygEntries);
        ListIntygEntry lie = listIntygEntries.get(0);
        assertFrontendRelationsIntygsIds(lie.getRelations().getLatestChildRelations(), OTHER_INTYG_ID_3, null, OTHER_INTYG_ID,
                OTHER_INTYG_ID_2);
        assertEquals(lie.getRelations().getParent().getIntygsId(), PARENT_INTYG_1);
    }

    @Test
    public void testDecorateWithEmptyList() {
        List<ListIntygEntry> listIntygEntries = new ArrayList<>();
        testee.decorateIntygListWithRelations(listIntygEntries);
        verifyZeroInteractions(listRelationsForCertificateResponderInterface);
    }

    @Test
    public void testErsattandeRevoked() {
        ListRelationsForCertificateResponseType response = buildResponse();
        response.getIntygRelation().get(0).getRelation().get(0).setFranIntygMakulerat(true);
        when(listRelationsForCertificateResponderInterface.listRelationsForCertificate(isNull(),
                any(ListRelationsForCertificateType.class))).thenReturn(response);

        Relations relationsForIntyg = testee.getRelationsForIntyg(INTYG_ID);
        assertNotNull(relationsForIntyg);
        assertFrontendRelationsIntygsIds(relationsForIntyg.getLatestChildRelations(), null, null, null, null);
    }

    private List<ListIntygEntry> buildList() {
        ListIntygEntry listIntygEntry = new ListIntygEntry();
        listIntygEntry.setIntygId(INTYG_ID);
        return Stream.of(listIntygEntry).collect(Collectors.toList());
    }

    private Relations buildWebcertRelations() {
        Relations relations = new Relations();
        Relations.FrontendRelations fr = relations.getLatestChildRelations();
        fr.setReplacedByUtkast(new WebcertCertificateRelation(OTHER_INTYG_ID_2, RelationKod.ERSATT, LocalDateTime.now().minusDays(1),
                UtkastStatus.DRAFT_COMPLETE, false));
        fr.setComplementedByIntyg(new WebcertCertificateRelation(OTHER_INTYG_ID_3, RelationKod.KOMPLT, LocalDateTime.now().minusDays(2),
                UtkastStatus.SIGNED, false));
        return relations;
    }

    private Relations buildWebcertRelationsWithParent() {
        Relations relations = new Relations();
        Relations.FrontendRelations fr = relations.getLatestChildRelations();
        fr.setReplacedByUtkast(new WebcertCertificateRelation(OTHER_INTYG_ID_2, RelationKod.ERSATT, LocalDateTime.now().minusDays(1),
                UtkastStatus.DRAFT_COMPLETE, false));
        fr.setComplementedByIntyg(new WebcertCertificateRelation(OTHER_INTYG_ID_3, RelationKod.KOMPLT, LocalDateTime.now().minusDays(2),
                UtkastStatus.SIGNED, false));
        relations.setParent(
                new WebcertCertificateRelation(PARENT_INTYG_1, RelationKod.KOMPLT, LocalDateTime.now().minusDays(3), UtkastStatus.SIGNED, false));
        return relations;
    }

    private ListRelationsForCertificateResponseType buildResponse() {
        ListRelationsForCertificateResponseType resp = new ListRelationsForCertificateResponseType();
        IntygRelations intygRelations = new IntygRelations();
        IntygId intygId = new IntygId();
        intygId.setExtension(INTYG_ID);
        intygRelations.setIntygsId(intygId);
        intygRelations.getRelation().add(buildRelation(OTHER_INTYG_ID, INTYG_ID));
        resp.getIntygRelation().add(intygRelations);
        return resp;
    }

    private ListRelationsForCertificateResponseType buildResponseWithParent() {
        ListRelationsForCertificateResponseType resp = new ListRelationsForCertificateResponseType();
        IntygRelations intygRelations = new IntygRelations();
        intygRelations.getRelation().add(buildRelation(INTYG_ID, PARENT_INTYG_1));
        resp.getIntygRelation().add(intygRelations);
        return resp;
    }

    private Relation buildRelation(String fromIntygId, String toIntygId) {
        Relation r = new Relation();

        IntygId from = new IntygId();
        from.setExtension(fromIntygId);
        IntygId to = new IntygId();
        to.setExtension(toIntygId);

        r.setFranIntygsId(from);
        r.setTillIntygsId(to);

        TypAvRelation typ = new TypAvRelation();
        typ.setCode(RelationKod.ERSATT.value());
        r.setTyp(typ);
        r.setSkapad(LocalDateTime.now());
        return r;
    }

    private void assertFrontendRelations(Relations.FrontendRelations fr, WebcertCertificateRelation complementedByIntyg,
            WebcertCertificateRelation complementedByUtkast, WebcertCertificateRelation replacedByIntyg,
            WebcertCertificateRelation replacedByUtkast) {
        assertEquals(complementedByIntyg, fr.getComplementedByIntyg());
        assertEquals(complementedByUtkast, fr.getComplementedByUtkast());
        assertEquals(replacedByIntyg, fr.getReplacedByIntyg());
        assertEquals(replacedByUtkast, fr.getReplacedByUtkast());
    }

    private void assertFrontendRelationsIntygsIds(Relations.FrontendRelations fr, String complementedByIntygIntygsId,
            String complementedByUtkastIntygsId, String replacedByIntygIntygsId, String replacedByUtkastIntygsId) {
        if (fr.getComplementedByIntyg() != null) {
            assertEquals(complementedByIntygIntygsId, fr.getComplementedByIntyg().getIntygsId());
        } else {
            assertNull(complementedByIntygIntygsId);
        }

        if (fr.getComplementedByUtkast() != null) {
            assertEquals(complementedByUtkastIntygsId, fr.getComplementedByUtkast().getIntygsId());
        } else {
            assertNull(complementedByUtkastIntygsId);
        }

        if (fr.getReplacedByIntyg() != null) {
            assertEquals(replacedByIntygIntygsId, fr.getReplacedByIntyg().getIntygsId());
        } else {
            assertNull(replacedByIntygIntygsId);
        }

        if (fr.getReplacedByUtkast() != null) {
            assertEquals(replacedByUtkastIntygsId, fr.getReplacedByUtkast().getIntygsId());
        } else {
            assertNull(replacedByUtkastIntygsId);
        }
    }
}
