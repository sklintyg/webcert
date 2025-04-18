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
package se.inera.intyg.webcert.persistence.handelse.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Handelse> findByVardgivarId(String careProviderId);

    @Query("select h.id from Handelse h where h.intygsId in :certificateIds")
    List<Long> findHandelseIdsByCertificateIds(@Param("certificateIds") List<String> certificateIds);

    @Query("select h from Handelse h where h.intygsId in :certificateIds")
    List<Handelse> getHandelseByIntygsIds(@Param("certificateIds") List<String> certificateIds);


    @Query(value = """
        SELECT COUNT(h.ID) FROM HANDELSE h
        INNER JOIN HANDELSE_METADATA hm ON h.ID = hm.HANDELSE_ID
        WHERE h.VARDGIVAR_ID = :careGiverId AND hm.DELIVERY_STATUS IN :statuses
        AND h.TIMESTAMP BETWEEN :start AND :end""", nativeQuery = true)
    int countInsertsForCareGiver(@Param("careGiverId") String careGiverId,
        @Param("statuses") List<String> statuses,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    @Query(value = """
        SELECT COUNT(h.ID) FROM HANDELSE h
        INNER JOIN HANDELSE_METADATA hm ON h.ID = hm.HANDELSE_ID
        WHERE h.INTYGS_ID IN :certificateIds AND hm.DELIVERY_STATUS IN :statuses""", nativeQuery = true)
    int countInsertsForCertificates(@Param("certificateIds") List<String> certificateIds,
        @Param("statuses") List<String> statuses);

    @Query(value = """
        SELECT COUNT(h.ID) FROM HANDELSE h
        INNER JOIN HANDELSE_METADATA hm ON h.ID = hm.HANDELSE_ID
        WHERE h.ENHETS_ID IN :unitIds AND hm.DELIVERY_STATUS IN :statuses
        AND h.TIMESTAMP BETWEEN :start AND :end""", nativeQuery = true)
    int countInsertsForUnits(@Param("unitIds") List<String> unitIds,
        @Param("statuses") List<String> statuses,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    @Query(value = """
        SELECT COUNT(h.ID) FROM HANDELSE h
        WHERE h.id = :notificationId""", nativeQuery = true)
    int countNotification(@Param("notificationId") String notificationId);

    @Query(value = """
        SELECT COUNT(h.ID) FROM HANDELSE h
        INNER JOIN HANDELSE_METADATA hm ON h.ID = hm.HANDELSE_ID
        WHERE h.VARDGIVAR_ID = :careGiverId AND hm.DELIVERY_STATUS IN :statuses
        AND h.TIMESTAMP BETWEEN :start AND :end""", nativeQuery = true)
    int countNotificationsForCareGiver(@Param("careGiverId") String careGiverId,
        @Param("statuses") List<String> statuses,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    @Query(value = """
        SELECT COUNT(h.ID) FROM HANDELSE h
        INNER JOIN HANDELSE_METADATA hm ON h.ID = hm.HANDELSE_ID
        WHERE h.INTYGS_ID IN :certificateIds
        AND hm.DELIVERY_STATUS IN :statuses""", nativeQuery = true)
    int countNotificationsForCertificates(
        @Param("certificateIds") List<String> certificateIds,
        @Param("statuses") List<String> statuses);

    @Query(value = """
        SELECT COUNT(h.ID) FROM HANDELSE h
        INNER JOIN HANDELSE_METADATA hm ON h.ID = hm.HANDELSE_ID
        WHERE h.ENHETS_ID IN :unitIds AND hm.DELIVERY_STATUS IN :statuses
        AND h.TIMESTAMP BETWEEN :start AND :end""", nativeQuery = true)
    int countNotificationsForUnits(
        @Param("unitIds") List<String> unitIds,
        @Param("statuses") List<String> statuses,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    default int eraseHandelseByCertificateIds(List<String> certificateIds) {
        final var handelseList = getHandelseByIntygsIds(certificateIds);
        deleteAll(handelseList);
        return handelseList.size();
    }

    int deleteHandelseByVardgivarId(String vardgivarId);
}