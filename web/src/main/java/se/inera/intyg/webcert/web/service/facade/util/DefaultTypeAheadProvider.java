/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.util;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.modules.support.facade.TypeAheadEnum;
import se.inera.intyg.common.support.modules.support.facade.TypeAheadProvider;
import se.inera.intyg.infra.integration.postnummer.service.PostnummerService;

@Component
public class DefaultTypeAheadProvider implements TypeAheadProvider {

    private final PostnummerService postnummerService;

    @Autowired
    public DefaultTypeAheadProvider(PostnummerService postnummerService) {
        this.postnummerService = postnummerService;
    }

    @Override
    public List<String> getValues(TypeAheadEnum typeAheadEnum) {
        if (typeAheadEnum == null) {
            throw new IllegalArgumentException("TypeAheadEnum was null! Must specify what TypeAhead to get values for!");
        }
        return postnummerService.getKommunList();
    }
}
