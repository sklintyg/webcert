/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.facade.util.DefaultTypeAheadProvider;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.service.utkast.util.CreateIntygsIdStrategy;

@Component
public class CreateUtkastCopyBuilder
    extends AbstractUtkastBuilder<CreateUtkastFromTemplateRequest> {

  public CreateUtkastCopyBuilder(
      IntygModuleRegistry moduleRegistry,
      @Lazy IntygService intygService,
      CreateIntygsIdStrategy intygsIdStrategy,
      UtkastRepository utkastRepository,
      IntygTextsService intygTextsService,
      DefaultTypeAheadProvider defaultTypeAheadProvider) {
    super(
        moduleRegistry,
        intygService,
        intygsIdStrategy,
        utkastRepository,
        intygTextsService,
        defaultTypeAheadProvider);
  }

  @Override
  public Relation createRelation(CreateUtkastFromTemplateRequest request) {
    Relation relation = new Relation();
    relation.setRelationIntygsId(request.getOriginalIntygId());
    relation.setRelationKod(RelationKod.KOPIA);
    return relation;
  }
}
