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
/*
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
        assertEquals(0, relations.getLatestChildren().size());
    }

    @Test
    public void testGetWithParentRelation() {
        when(utkastRepositoryCustom.findParentRelation(anyString())).thenReturn(buildParentRelations());
        Relations relations = testee.getRelations(INTYG_ID);
        assertEquals(OTHER_INTYG_ID, relations.getParent().getIntygsId());
        assertEquals(0, relations.getLatestChildren().size());
    }

    @Test
    public void testGetWithChildRelations() {
        when(utkastRepositoryCustom.findChildRelations(anyString())).thenReturn(buildChildRelations());
        Relations relations = testee.getRelations(INTYG_ID);
        assertNull(OTHER_INTYG_ID, relations.getParent());
        assertEquals(2, relations.getLatestChildren().size());
        assertEquals(CHILD_INTYG_ID_2, relations.getLatestChildren().get(0).getIntygsId());
        assertEquals(CHILD_INTYG_ID_1, relations.getLatestChildren().get(1).getIntygsId());
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

    @Test
    public void testFilteringOnStatus() {
        when(utkastRepositoryCustom.findParentRelation(anyString())).thenReturn(buildParentRelations());
        when(utkastRepositoryCustom.findChildRelations(anyString())).thenReturn(buildChildRelations());


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
*/
}
