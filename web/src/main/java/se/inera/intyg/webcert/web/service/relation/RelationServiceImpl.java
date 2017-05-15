/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RelationItem;

@Service
public class RelationServiceImpl implements RelationService {

    @Autowired
    private UtkastRepository utkastRepo;

    @Autowired
    private WebCertUserService userService;

    @Override
    public List<RelationItem> getParentRelations(String intygsId) {
        List<RelationItem> relationList = new ArrayList<>();
        Utkast reference = utkastRepo.findOne(intygsId);

        // While we have a parent in the reference intyg
        while (reference != null && !Strings.isNullOrEmpty(reference.getRelationIntygsId())) {
            reference = utkastRepo.findOne(reference.getRelationIntygsId());
            if (reference == null || !isAuthorized(reference.getEnhetsId())) {
                break;
            }
            relationList.add(new RelationItem(reference));
        }
        return relationList;

    }

    @Override
    public List<RelationItem> getChildRelations(String intygsId) {
        return utkastRepo.findAllByRelationIntygsId(intygsId).stream()
                .filter(utkast -> userService.getUser().getIdsOfSelectedVardenhet().contains(utkast.getEnhetsId()))
                .map(RelationItem::new)
                .sorted(Comparator.comparing(RelationItem::getDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<RelationItem> getRelations(String intygsId) {
        Utkast baseCertificate = utkastRepo.findOne(intygsId);
        if (baseCertificate != null && isAuthorized(baseCertificate.getEnhetsId())) {
            List<RelationItem> res = getChildRelations(intygsId);
            res.add(new RelationItem(baseCertificate));
            res.addAll(getParentRelations(intygsId));
            return res;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<RelationItem> findNewestReplacingIntyg(String intygId) {
        return findNewestRelatedIntyg(intygId, RelationKod.ERSATT);
    }

    @Override
    public Optional<RelationItem> findNewestComplementingIntyg(String intygId) {
        return findNewestRelatedIntyg(intygId, RelationKod.KOMPLT);
    }

    @VisibleForTesting
    Optional<RelationItem> findNewestRelatedIntyg(String intygId, RelationKod relationKod) {
        return getChildRelations(intygId).stream()
                .filter(r -> r.getKod().equals(relationKod.name()))
                // All other possible values for status other than DRAFT_INCOMPLETE and DRAFT_COMPLETE implicitly means
                // SIGNED.
                .filter(r -> !(r.getStatus().equals(UtkastStatus.DRAFT_INCOMPLETE.name())
                        || r.getStatus().equals(UtkastStatus.DRAFT_COMPLETE.name())))
                .sorted((r1, r2) -> r1.getDate().compareTo(r2.getDate()))
                .findFirst();
    }

    private boolean isAuthorized(String enhetsId) {
        return userService.getUser().getIdsOfSelectedVardenhet().contains(enhetsId);
    }
}
