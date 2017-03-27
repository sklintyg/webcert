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

import java.util.List;
import java.util.Optional;

import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RelationItem;

public interface RelationService {

    /**
     * Returns a list of {@link RelationItem} going backwards in time.
     *
     * @param intygsId
     *            the child
     * @return a list of all the utkast/intyg which has a parent relation to <code>intygsId</code>
     */
    List<RelationItem> getParentRelations(String intygsId);

    /**
     * Returns a list of children to <code>intygsId</code> represented as {@link RelationItem}.
     *
     * @param intygsId
     *            the parent
     * @return a list of all the utkast/intyg which has a child relation to <code>intygsId</code>
     */
    List<RelationItem> getChildRelations(String intygsId);

    /**
     * Returns a list of {@link RelationItem} for the utkast/intyg specified by <code>intygsId</code>.
     *
     * This returns the concatenated list of both child and parent relations
     *
     * @param intygsId
     *            the utkast/intyg to get the relations for
     * @return a list of all the utkast/intyg which has a relation to <code>intygsId</code>
     */
    Optional<List<RelationItem>> getRelations(String intygsId);

    /**
     * Optionally return the latest replacement relation for the given intygsId for the supplied relations.
     * @param intygId
     * @return
     */
    Optional<RelationItem> getReplacedByRelation(String intygId);

}
