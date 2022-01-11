/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.handelse.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;

public interface HandelseRepository extends JpaRepository<Handelse, Long> {

    List<Handelse> findByIntygsId(String intygsId);

    List<Handelse> findByPersonnummer(String personId);

    List<Handelse> findByPersonnummerAndEnhetsIdIn(String personId, List<String> unitId);

    List<Handelse> findByPersonnummerAndEnhetsIdInAndTimestampBetween(String personId, List<String> unitIds, LocalDateTime from,
        LocalDateTime to);

    List<Handelse> findByPersonnummerAndEnhetsIdInAndTimestampAfter(String personId, List<String> unitIds, LocalDateTime from);

    List<Handelse> findByPersonnummerAndEnhetsIdInAndTimestampBefore(String personId, List<String> unitIds, LocalDateTime to);

    List<Handelse> findByPersonnummerAndVardgivarId(String personId, String careProviderId);

    List<Handelse> findByPersonnummerAndVardgivarIdAndTimestampBetween(String personId, String careProviderId, LocalDateTime from,
        LocalDateTime to);

    List<Handelse> findByPersonnummerAndVardgivarIdAndTimestampAfter(String personId, String careProviderId, LocalDateTime from);

    List<Handelse> findByPersonnummerAndVardgivarIdAndTimestampBefore(String personId, String careProviderId, LocalDateTime to);
}
