package se.inera.intyg.webcert.web.service.relation;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

import java.util.List;
import java.util.Optional;

/**
 * Defines a service interface to get relations for a given intygsId.
 *
 * Please note that implementations should only returns relations directly associated with the specified intygsId,
 * e.g. no "graphs" or "trees" of (n)-th level associated intyg.
 *
 * Created by eriklupander on 2017-05-15.
 */
public interface CertificateRelationService {

    /**
     * Builds an instance of {@link Relations} where parent and child relations are grouped together.
     *
     * @param intygsId
     *      IntygsId to query for.
     * @return
     *      Relations to/from the specified Intyg / Utkast.
     */
    Relations getRelations(String intygsId);

    /**
     * If present, returns a relation to the parent of this intyg/utkast.
     * @param intygsId
     *      IntygsId to query for.
     * @return
     *      Relation to the parent.
     */
    Optional<WebcertCertificateRelation> findParentRelation(String intygsId);

    /**
     * Fetches relations for intyg having the specified intyg as parent.
     *
     * @param intygsId
     *      IntygsId to query for.
     * @return
     *      A list of 0-n children having the specified intyg as parent.
     */
    List<WebcertCertificateRelation> findChildRelations(String intygsId);

    /**
     * Tries to find a child relation of the specified {@link RelationKod} for a given intygsId.
     *
     * If there for some reason exists more than one relation of a given type, the newest one is returned.
     *
     * @param intygsId
     *      IntygsId to query for.
     * @param relationKod
     *      {@link RelationKod} to query for.
     * @return
     *      0..1 child relation having the specified relationKod.
     */
    Optional<WebcertCertificateRelation> getRelationOfType(String intygsId, RelationKod relationKod);
}
