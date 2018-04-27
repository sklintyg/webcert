/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.relation;

import java.util.List;
import java.util.Optional;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

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
     *            IntygsId to query for.
     * @return
     *         Relations to/from the specified Intyg / Utkast.
     */
    Relations getRelations(String intygsId);

    /**
     * If present, returns a relation to the parent of this intyg/utkast.
     *
     * @param intygsId
     *            IntygsId to query for.
     * @return
     *         Relation to the parent.
     */
    Optional<WebcertCertificateRelation> findParentRelation(String intygsId);

    /**
     * Fetches relations for intyg having the specified intyg as parent.
     *
     * @param intygsId
     *            IntygsId to query for.
     * @return
     *         A list of 0-n children having the specified intyg as parent.
     */
    List<WebcertCertificateRelation> findChildRelations(String intygsId);

    /**
     * Tries to find a child relation of the specified {@link RelationKod} for a given intygsId.
     *
     * If there for some reason exists more than one relation of a given type (possible for some relations, not for
     * others), the newest one is returned.
     *
     * @param intygsId
     *            IntygsId to query for.
     * @param relationKod
     *            {@link RelationKod} to query for.
     * @return
     *         0..1 child relation having the specified relationKod.
     */
    Optional<WebcertCertificateRelation> getNewestRelationOfType(String intygsId, RelationKod relationKod,
            List<UtkastStatus> allowedStatuses);
}
