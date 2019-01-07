/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.fmb.repository;

/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import se.inera.intyg.webcert.persistence.fmb.model.dto.MaximalSjukskrivningstidDagar;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation;

public interface DiagnosInformationRepository extends JpaRepository<DiagnosInformation, Long> {

    // CHECKSTYLE:OFF MethodName
    Optional<DiagnosInformation> findFirstByIcd10KodList_kod(final String icd10Kod);
    // CHECKSTYLE:ON MethodName

    // CHECKSTYLE:OFF OperatorWrap
    // CHECKSTYLE:OFF LineLength
    @Query("SELECT new se.inera.intyg.webcert.persistence.fmb.model.dto.MaximalSjukskrivningstidDagar(icd10Kod.kod, max(typfall.maximalSjukrivningstidDagar)) FROM DiagnosInformation diagnosInfo " +
           "JOIN diagnosInfo.icd10KodList icd10Kod " +
           "JOIN icd10Kod.typFallList typfall " +
           "WHERE typfall.maximalSjukrivningstidDagar IS NOT NULL " +
           "AND icd10Kod.kod IN :koder " +
           "GROUP BY icd10Kod.kod, typfall.maximalSjukrivningstidDagar " +
           "ORDER BY typfall.maximalSjukrivningstidDagar DESC"
    )
    List<MaximalSjukskrivningstidDagar> findMaximalSjukrivningstidDagarByIcd10Koder(@Param("koder") final Set<String> koder);
    // CHECKSTYLE:ON OperatorWrap
    // CHECKSTYLE:ON LineLength
}

