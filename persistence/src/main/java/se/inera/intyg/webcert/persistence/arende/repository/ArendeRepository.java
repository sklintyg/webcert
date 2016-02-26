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

package se.inera.intyg.webcert.persistence.arende.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.inera.intyg.webcert.persistence.arende.model.Arende;

public interface ArendeRepository extends JpaRepository<Arende, Long> {

    /**
     * List all unique signing doctors for the supplied units.
     *
     * @return a list of names
     */
    @Query("SELECT DISTINCT signeratAv FROM Arende WHERE enhet IN (:idList) ORDER BY signeratAv ASC")
    List<String> findSigneratAvByEnhet(@Param("idList") List<String> enhetsIds);
}
