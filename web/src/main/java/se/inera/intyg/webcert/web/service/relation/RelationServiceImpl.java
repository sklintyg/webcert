/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RelationItem;

@Service
public class RelationServiceImpl implements RelationService {

    @Autowired
    private UtkastRepository utkastRepo;

    @Override
    public List<RelationItem> getParentRelations(String intygsId) {
        List<RelationItem> relationList = new ArrayList<>();
        Utkast reference = utkastRepo.findOne(intygsId);

        // While we have a parent in the reference intyg
        while (reference != null && StringUtils.isNotEmpty(reference.getRelationIntygsId())) {
            reference = utkastRepo.findOne(reference.getRelationIntygsId());
            relationList.add(new RelationItem(reference));
        }
        return relationList;
    }

    @Override
    public List<RelationItem> getChildRelations(String intygsId) {
        return utkastRepo.findAllByRelationIntygsId(intygsId).stream()
                .map(RelationItem::new)
                .sorted(Comparator.comparing(RelationItem::getDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<RelationItem> getRelations(String intygsId) {
        List<RelationItem> res = getChildRelations(intygsId);
        res.add(new RelationItem(utkastRepo.findOne(intygsId)));
        res.addAll(getParentRelations(intygsId));
        return res;
    }
}
