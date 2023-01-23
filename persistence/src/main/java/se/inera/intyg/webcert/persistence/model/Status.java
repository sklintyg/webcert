/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.model;

/**
 * Possible statuses for a FragaSvar Entity.
 *
 * @author marced
 */
public enum Status {

    /**
     * The FragaSvar or Arende has been received from an external entity and needs to be answered.
     */
    PENDING_INTERNAL_ACTION,

    /**
     * The FragaSvar or Arende has been sent to an external entity and awaits an answer.
     */
    PENDING_EXTERNAL_ACTION,

    /**
     * The FragaSvar or Arende has received an answer from the external entity.
     */
    ANSWERED,

    /**
     * The FragaSvar or Arende has been handled.
     */
    CLOSED;
}
