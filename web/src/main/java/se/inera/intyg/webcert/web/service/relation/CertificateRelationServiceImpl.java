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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepositoryCustom;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

/**
 * Created by eriklupander on 2017-05-15.
 */
@Service
public class CertificateRelationServiceImpl implements CertificateRelationService {

    @Autowired
    private UtkastRepositoryCustom utkastRepoCustom;

    @Override
    public Relations getRelations(String intygsId) {
        Relations relations = new Relations();
        Optional<WebcertCertificateRelation> parentRelation = findParentRelation(intygsId);
        parentRelation.ifPresent(relations::setParent);

        List<WebcertCertificateRelation> childRelations = findChildRelations(intygsId);
        relations.setLatestChildRelations(prepareChildRelationDataForFrontend(childRelations));

        return relations;
    }

    private Relations.FrontendRelations prepareChildRelationDataForFrontend(List<WebcertCertificateRelation> childRelations) {
        Collections.sort(childRelations, (r1, r2) -> r1.getSkapad().compareTo(r2.getSkapad()) * -1); // Descending
                                                                                                     // order.
        Relations.FrontendRelations latestChildRelations = new Relations.FrontendRelations();
        latestChildRelations.setReplacedByIntyg(findRelationOfType(childRelations, RelationKod.ERSATT, true));
        latestChildRelations.setReplacedByUtkast(findRelationOfType(childRelations, RelationKod.ERSATT, false));
        latestChildRelations.setComplementedByIntyg(findRelationOfType(childRelations, RelationKod.KOMPLT, true));
        latestChildRelations.setComplementedByUtkast(findRelationOfType(childRelations, RelationKod.KOMPLT, false));
        latestChildRelations.setUtkastCopy(findRelationOfType(childRelations, RelationKod.KOPIA, false));
        return latestChildRelations;
    }

    private WebcertCertificateRelation findRelationOfType(List<WebcertCertificateRelation> relations, RelationKod relationKod,
            boolean signed) {
        for (WebcertCertificateRelation wcr : relations) {
            if (wcr.getRelationKod() == relationKod) {
                if ((signed && wcr.getStatus() == UtkastStatus.SIGNED)
                        || (!signed
                                && (wcr.getStatus() == UtkastStatus.DRAFT_INCOMPLETE || wcr.getStatus() == UtkastStatus.DRAFT_COMPLETE))) {
                    return wcr;
                }
            }
        }
        return null;
    }

    /**
     * Implementation detail: Spring JPA Repository @Query doesn't really work with getSingleResult()-style queries,
     * thus this method gets a list of "parentRelations" even though there can never be more than one parent.
     */
    @Override
    public Optional<WebcertCertificateRelation> findParentRelation(String intygsId) {
        List<WebcertCertificateRelation> parentRelations = utkastRepoCustom.findParentRelation(intygsId);
        return parentRelations.stream().findFirst();
    }

    @Override
    public List<WebcertCertificateRelation> findChildRelations(String intygsId) {
        return utkastRepoCustom.findChildRelations(intygsId)
                .stream()
                .sorted((r1, r2) -> r2.getSkapad().compareTo(r1.getSkapad()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WebcertCertificateRelation> getNewestRelationOfType(String intygsId, RelationKod relationKod,
            List<UtkastStatus> allowedStatuses) {
        return findChildRelations(intygsId).stream()
                .filter(cr -> cr.getRelationKod() == relationKod)
                .filter(cr -> allowedStatuses.contains(cr.getStatus()))
                .sorted((cr1, cr2) -> cr2.getSkapad().compareTo(cr1.getSkapad()))
                .findFirst();
    }
}
