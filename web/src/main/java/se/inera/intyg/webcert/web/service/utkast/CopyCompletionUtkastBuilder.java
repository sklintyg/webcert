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

package se.inera.intyg.webcert.web.service.utkast;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.intygstyper.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyRequest;

@Component
public class CopyCompletionUtkastBuilder extends AbstractUtkastBuilder<CreateCompletionCopyRequest> {

    @Autowired
    ArendeService arendeService;

    @Override
    public Relation createRelation(CreateCompletionCopyRequest copyRequest) {
        return createRelation(copyRequest, RelationKod.KOMPLT);
    }

    private Relation createRelation(CreateCompletionCopyRequest request, RelationKod relationKod) {
        Relation relation = new Relation();
        relation.setRelationIntygsId(request.getOriginalIntygId());
        relation.setRelationKod(relationKod);
        relation.setMeddelandeId(request.getMeddelandeId());
        relation.setReferensId(getArendeReferensId(request.getMeddelandeId(), request.getTyp()));
        return relation;
    }

    private String getArendeReferensId(String meddelandeId, String intygsTyp) {
        if (StringUtils.isNotEmpty(meddelandeId) && !intygsTyp.equals(Fk7263EntryPoint.MODULE_ID)) {
            Arende arende = arendeService.getArende(meddelandeId);
            return arende != null ? arende.getReferensId() : null;
        }
        return null;
    }
}
