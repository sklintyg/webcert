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
package se.inera.intyg.webcert.web.service.utkast;

import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CreateDraftCopyHolder;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyRequest;

@Component
public class CreateRenewalCopyUtkastBuilder extends AbstractUtkastBuilder<CreateRenewalCopyRequest> {

    @Override
    public Relation createRelation(CreateRenewalCopyRequest copyRequest) {
        return createRelation(copyRequest, RelationKod.FRLANG);
    }

    private Relation createRelation(CreateRenewalCopyRequest request, RelationKod relationKod) {
        Relation relation = new Relation();
        relation.setRelationIntygsId(request.getOriginalIntygId());
        relation.setRelationKod(relationKod);
        return relation;
    }

    @Override
    protected String getInternalModel(Utlatande template, ModuleApi moduleApi, CreateDraftCopyHolder draftCopyHolder)
            throws ModuleException {
        return moduleApi.createRenewalFromTemplate(draftCopyHolder, template);
    }

}
