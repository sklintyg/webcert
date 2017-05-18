package se.inera.intyg.webcert.web.service.intyg.decorator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by eriklupander on 2017-05-18.
 */
@Service
public class IntygRelationHelperImpl implements IntygRelationHelper {

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;

    @Autowired
    private CertificateRelationService certificateRelationService;

    @Autowired
    private ListRelationsForCertificateResponderInterface listRelationsForCertificateResponderInterface;

    @Override
    public Relations getRelationsForIntyg(String intygId) {
        Relations certificateRelations = new Relations();
        ListRelationsForCertificateResponseType response = getRelationsFromIntygstjanst(intygId);

        // Iterate over all relations fetched from IT, split them up into parent and child relation(s)
        response.getIntygRelation().stream()
                .flatMap(ir -> ir.getRelation().stream())
                .forEach(r -> applyRelation(intygId, certificateRelations, r));

        // Enrich with any relations present only in Webcert (e.g. for utkast etc.)
        Relations webcertRelations = certificateRelationService.getRelations(intygId);
        mergeRelations(certificateRelations, webcertRelations);
        return certificateRelations;
    }

    @Override
    public void decorateIntygListWithRelations(List<ListIntygEntry> fullIntygItemList) {

        ListRelationsForCertificateResponseType response = getRelationsFromIntygstjanst(fullIntygItemList.stream()
                .map(ListIntygEntry::getIntygId)
                .collect(Collectors.toList()));

        if (response != null) {
            // Very ugly, iterate over both lists, find matches and create relation(s) on the ListIntygEntries.
            for (IntygRelations ir : response.getIntygRelation()) {
                for (ListIntygEntry lie : fullIntygItemList) {
                    if (lie.getIntygId().equals(ir.getIntygsId().getExtension())) {

                        // Create a Relations instance to hold relations.
                        lie.setRelations(new Relations());

                        // Iterate over all relations for this particular intyg
                        for (Relation r : ir.getRelation()) {
                            applyRelation(lie.getIntygId(), lie.getRelations(), r);
                        }
                        // Sort
                        sortRelations(lie);
                    }
                }
            }
        }

        // Finally, we need to take any relations present in Webcert (but not yet present in Intygstjänsten) into
        // account.
        for (ListIntygEntry lie : fullIntygItemList) {

            Relations relations = certificateRelationService.getRelations(lie.getIntygId());
            mergeRelations(lie.getRelations(), relations);

            // Sort
            sortRelations(lie);
        }
    }

    private ListRelationsForCertificateResponseType getRelationsFromIntygstjanst(String intygId) {
        return getRelationsFromIntygstjanst(Collections.singletonList(intygId));
    }

    private ListRelationsForCertificateResponseType getRelationsFromIntygstjanst(List<String> intygIds) {
        ListRelationsForCertificateType request = new ListRelationsForCertificateType();
        request.getIntygsId().addAll(intygIds);
        return listRelationsForCertificateResponderInterface.listRelationsForCertificate(logicalAddress, request);
    }

    private void applyRelation(String intygId, Relations certificateRelations, Relation r) {

        // In this context, all statuses are SIGNED.
        if (r.getTillIntygsId().getExtension().equals(intygId)) {
            certificateRelations.getChildren().add(new WebcertCertificateRelation(r.getFranIntygsId().getExtension(),
                    RelationKod.fromValue(r.getTyp().getCode()), r.getSkapad(), UtkastStatus.SIGNED));
        } else if (r.getFranIntygsId().getExtension().equals(intygId)) {
            certificateRelations.setParent(new WebcertCertificateRelation(r.getTillIntygsId().getExtension(),
                    RelationKod.fromValue(r.getTyp().getCode()), r.getSkapad(), UtkastStatus.SIGNED));
        }
    }

    private void mergeRelations(Relations startRelations, Relations augmentWith) {
        // No point merging relations if there aren't any...
        if (startRelations == null || augmentWith == null) {
            return;
        }
        // Since only a single parent relation is possible to have, only add if there is none already present.
        if (augmentWith.getParent() != null && startRelations.getParent() == null) {
            startRelations.setParent(augmentWith.getParent());
        }

        // Perform a matching, any relation NOT already present on the Relations of the ListIntygEntry shall be added.
        List<WebcertCertificateRelation> others = augmentWith.getChildren().stream()
                .filter(cr -> !startRelations.getChildren().contains(cr))
                .collect(Collectors.toList());
        if (others.size() > 0) {
            startRelations.getChildren().addAll(others);
        }
    }

    private void sortRelations(ListIntygEntry lie) {
        lie.getRelations().setChildren(lie.getRelations().getChildren().stream()
                .sorted((cr1, cr2) -> cr2.getSkapad().compareTo(cr1.getSkapad()))
                .collect(Collectors.toList()));
    }

}
