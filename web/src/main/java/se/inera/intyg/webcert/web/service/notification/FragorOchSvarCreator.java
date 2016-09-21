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

package se.inera.intyg.webcert.web.service.notification;

import org.apache.commons.lang3.tuple.Pair;

import se.inera.intyg.common.support.modules.support.api.notification.Arenden;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;

public interface FragorOchSvarCreator {

    FragorOchSvar createFragorOchSvar(String intygsId);

    Pair<Arenden, Arenden> createArenden(String intygsId, String intygstyp);

}
