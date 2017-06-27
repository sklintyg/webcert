package se.inera.intyg.webcert.web.service.relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepositoryCustom;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

/**
 * Created by eriklupander on 2017-05-15.
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateRelationServiceImplTest {
    private static final String INTYG_ID = "123";
    private static final String OTHER_INTYG_ID = "456";
    private static final String CHILD_INTYG_ID_1 = "789-1";
    private static final String CHILD_INTYG_ID_2 = "789-2";

    @Mock
    private UtkastRepositoryCustom utkastRepositoryCustom;

    @InjectMocks
    private CertificateRelationServiceImpl testee;

    @Before
    public void init() {
        when(utkastRepositoryCustom.findParentRelation(anyString())).thenReturn(new ArrayList<>());
        when(utkastRepositoryCustom.findChildRelations(anyString())).thenReturn(new ArrayList<>());
    }

    @Test
    public void testGetWithNoRelations() {
        Relations relations = testee.getRelations(INTYG_ID);
        assertNull(relations.getParent());
        assertFrontendRelations(relations.getLatestChildRelations(), null, null, null, null);
    }

    @Test
    public void testGetWithParentRelation() {
        when(utkastRepositoryCustom.findParentRelation(anyString())).thenReturn(buildParentRelations());
        Relations relations = testee.getRelations(INTYG_ID);
        assertEquals(OTHER_INTYG_ID, relations.getParent().getIntygsId());
        assertFrontendRelations(relations.getLatestChildRelations(), null, null, null, null);
    }

    @Test
    public void testGetWithChildRelations() {
        when(utkastRepositoryCustom.findChildRelations(anyString())).thenReturn(buildChildRelations());
        Relations relations = testee.getRelations(INTYG_ID);
        assertNull(OTHER_INTYG_ID, relations.getParent());
        assertFrontendRelationsIntygsIds(relations.getLatestChildRelations(), CHILD_INTYG_ID_2, null, null, CHILD_INTYG_ID_1);
    }

    @Test
    public void testGetRelationOfPresentType() {
        when(utkastRepositoryCustom.findChildRelations(anyString())).thenReturn(buildChildRelations());
        Optional<WebcertCertificateRelation> relationOfType = testee.getNewestRelationOfType(INTYG_ID, RelationKod.ERSATT,
                Arrays.asList(UtkastStatus.values()));
        assertTrue(relationOfType.isPresent());
        assertEquals(CHILD_INTYG_ID_1, relationOfType.get().getIntygsId());
    }

    @Test
    public void testGetRelationOfNonPresentType() {
        when(utkastRepositoryCustom.findChildRelations(anyString())).thenReturn(buildChildRelations());
        Optional<WebcertCertificateRelation> relationOfType = testee.getNewestRelationOfType(INTYG_ID, RelationKod.FRLANG,
                Arrays.asList(UtkastStatus.values()));
        assertFalse(relationOfType.isPresent());
    }

    private List<WebcertCertificateRelation> buildParentRelations() {
        return Stream.of(new WebcertCertificateRelation(OTHER_INTYG_ID, RelationKod.ERSATT, LocalDateTime.now(), UtkastStatus.SIGNED))
                .collect(Collectors.toList());
    }

    private List<WebcertCertificateRelation> buildChildRelations() {
        return Stream.of(
                new WebcertCertificateRelation(CHILD_INTYG_ID_1, RelationKod.ERSATT, LocalDateTime.now().minusDays(5),
                        UtkastStatus.DRAFT_INCOMPLETE),
                new WebcertCertificateRelation(CHILD_INTYG_ID_2, RelationKod.KOMPLT, LocalDateTime.now(), UtkastStatus.SIGNED))
                .collect(Collectors.toList());
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
