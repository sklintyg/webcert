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
package se.inera.intyg.webcert.web.converter.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.web.controller.api.dto.BesvaratMedIntyg;

@Component
public final class BesvaratMedIntygUtil {

    private BesvaratMedIntygUtil() {
    }

    /**
     * Returns the info of kompletterande intyg associated with the message sent at the given date and time. If no such
     * intyg exist, null is returned.
     *
     * Note: using this method is inefficient if checking multiple messages, due to unnecessary database lookups.
     *
     * @param intygsId
     * @param messageSendDate
     * @param utkastRepository
     * @return
     */
    public static BesvaratMedIntyg findIntygKompletteringForMessage(String intygsId, LocalDateTime messageSendDate,
            UtkastRepository utkastRepository) {
        return returnOldestKompltOlderThan(messageSendDate, findAllKomplementForGivenIntyg(intygsId, utkastRepository));
    }

    public static BesvaratMedIntyg returnOldestKompltOlderThan(LocalDateTime fragaSendDate,
            List<BesvaratMedIntyg> kompltForIntyg) {
        return kompltForIntyg.stream()
                .reduce(null, (saved, current) -> {
                    if (saved == null) {
                        return current;
                    }
                    return isInsideBounds(current.getSigneratDatum(), fragaSendDate, saved.getSkickatDatum()) ? current : saved;
                });
    }

    private static boolean isInsideBounds(LocalDateTime arg, LocalDateTime lowerBound, LocalDateTime upperBound) {
        return (arg.compareTo(lowerBound) > 0) && (arg.compareTo(upperBound) < 0);
    }

    /**
     * Given an existing intyg's id, will return info about all associated supplemental (kompletterande) intyg, and an
     * empty list if no such intyg are found.
     *
     * @param intygsId
     * @param utkastRepository
     * @return
     */
    public static List<BesvaratMedIntyg> findAllKomplementForGivenIntyg(String intygsId, UtkastRepository utkastRepository) {
        return utkastRepository.findAllByRelationIntygsId(intygsId).stream()
                .filter(u -> Objects.equals(u.getRelationKod(), RelationKod.KOMPLT))
                .filter(u -> u.getSignatur() != null)
                .map(BesvaratMedIntyg::create)
                .collect(Collectors.toList());
    }

}
