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
package se.inera.intyg.webcert.persistence.utkast.repository;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

public interface UtkastRepository extends JpaRepository<Utkast, String>, UtkastRepositoryCustom {

    List<Utkast> findAllByRelationIntygsId(String relationIntygsId);

    Utkast findByIntygsIdAndIntygsTyp(String intygsId, String intygsTyp);

    List<Utkast> findAllByPatientPersonnummerAndIntygsTypIn(String personnummer, Set<String> intygstyp);

    @Query("select u.intygsId from Utkast u where u.vardgivarId = :careProviderId")
    Page<String> findCertificateIdsForCareProvider(@Param("careProviderId") String careProviderId, Pageable pageable);

    @Query("select u from Utkast u where u.intygsId in :certificateIds")
    List<Utkast> getCertificatesByIntygsId(@Param("certificateIds") List<String> certificateIds);

    default int eraseCertificatesByCertificateIds(List<String> certificateIds) {
        final var utkastList = getCertificatesByIntygsId(certificateIds);
        deleteAll(utkastList);
        return utkastList.size();
    }

}
