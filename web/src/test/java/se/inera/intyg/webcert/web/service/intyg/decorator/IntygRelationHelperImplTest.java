package se.inera.intyg.webcert.web.service.intyg.decorator;

/**
 * Created by eriklupander on 2017-05-18.
 */

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.IntygRelations;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.ListRelationsForCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.ListRelationsForCertificateResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.ListRelationsForCertificateType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.Relation;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvRelation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
        when(listRelationsForCertificateResponderInterface.listRelationsForCertificate(anyString(), any(ListRelationsForCertificateType.class))).thenReturn(new ListRelationsForCertificateResponseType());

        Relations relationsForIntyg = testee.getRelationsForIntyg(INTYG_ID);
        assertNotNull(relationsForIntyg);
    }

    @Test
    public void testGetRelationsForIntygOneInIT() {
        when(listRelationsForCertificateResponderInterface.listRelationsForCertificate(anyString(), any(ListRelationsForCertificateType.class))).thenReturn(buildResponse());

        Relations relationsForIntyg = testee.getRelationsForIntyg(INTYG_ID);
        assertNotNull(relationsForIntyg);
        assertEquals(1, relationsForIntyg.getChildren().size());
        assertEquals(OTHER_INTYG_ID, relationsForIntyg.getChildren().get(0).getIntygsId());
    }

    @Test
    public void testGetRelationsForIntygOneParentInIT() {
        when(listRelationsForCertificateResponderInterface.listRelationsForCertificate(anyString(), any(ListRelationsForCertificateType.class))).thenReturn(buildResponseWithParent());

        Relations relationsForIntyg = testee.getRelationsForIntyg(INTYG_ID);
        assertNotNull(relationsForIntyg);
        assertEquals(PARENT_INTYG_1, relationsForIntyg.getParent().getIntygsId());
        assertEquals(0, relationsForIntyg.getChildren().size());
    }

    @Test
    public void testGetRelationsForIntygOneInITAndTwoFromWebcert() {
        when(listRelationsForCertificateResponderInterface.listRelationsForCertificate(anyString(), any(ListRelationsForCertificateType.class))).thenReturn(buildResponse());
        when(certificateRelationService.getRelations(INTYG_ID)).thenReturn(buildWebcertRelations());

        Relations relationsForIntyg = testee.getRelationsForIntyg(INTYG_ID);
        assertNotNull(relationsForIntyg);
        assertEquals(3, relationsForIntyg.getChildren().size());
    }

    @Test
    public void testGetRelationsForIntygOneInITAndThreeFromWebcertIncludingParent() {
        when(listRelationsForCertificateResponderInterface.listRelationsForCertificate(anyString(), any(ListRelationsForCertificateType.class))).thenReturn(buildResponse());
        when(certificateRelationService.getRelations(INTYG_ID)).thenReturn(buildWebcertRelationsWithParent());

        Relations relationsForIntyg = testee.getRelationsForIntyg(INTYG_ID);
        assertNotNull(relationsForIntyg);
        assertEquals(3, relationsForIntyg.getChildren().size());
    }

    @Test
    public void testDecorate() {
        when(listRelationsForCertificateResponderInterface.listRelationsForCertificate(anyString(), any(ListRelationsForCertificateType.class))).thenReturn(buildResponse());
        when(certificateRelationService.getRelations(INTYG_ID)).thenReturn(buildWebcertRelationsWithParent());
        List<ListIntygEntry> listIntygEntries = buildList();
        testee.decorateIntygListWithRelations(listIntygEntries);
        ListIntygEntry lie = listIntygEntries.get(0);
        assertEquals(3, lie.getRelations().getChildren().size());
    }

    private List<ListIntygEntry> buildList() {
        ListIntygEntry listIntygEntry = new ListIntygEntry();
        listIntygEntry.setIntygId(INTYG_ID);
        return Stream.of(listIntygEntry).collect(Collectors.toList());
    }

    private Relations buildWebcertRelations() {
        Relations relations = new Relations();
        relations.getChildren().add(new WebcertCertificateRelation(OTHER_INTYG_ID_2, RelationKod.ERSATT, LocalDateTime.now().minusDays(1), UtkastStatus.DRAFT_COMPLETE));
        relations.getChildren().add(new WebcertCertificateRelation(OTHER_INTYG_ID_3, RelationKod.KOMPLT, LocalDateTime.now().minusDays(2), UtkastStatus.SIGNED));
        return relations;
    }

    private Relations buildWebcertRelationsWithParent() {
        Relations relations = new Relations();
        relations.getChildren().add(new WebcertCertificateRelation(OTHER_INTYG_ID_2, RelationKod.ERSATT, LocalDateTime.now().minusDays(1), UtkastStatus.DRAFT_COMPLETE));
        relations.getChildren().add(new WebcertCertificateRelation(OTHER_INTYG_ID_3, RelationKod.KOMPLT, LocalDateTime.now().minusDays(2), UtkastStatus.SIGNED));
        relations.setParent(new WebcertCertificateRelation(PARENT_INTYG_1, RelationKod.KOMPLT, LocalDateTime.now().minusDays(3), UtkastStatus.SIGNED));
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
}
