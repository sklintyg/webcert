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
package se.inera.intyg.webcert.web.service.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * Helper class for taking a list of arbitrary "id", "unitId", "personnummer", filter out any personnummer being
 * sekretessmarkerad and returning the result as a map of unitId -> count(id).
 *
 * Used for getting counters of fråga/svar, ärenden and ej signerade utkast correct.
 *
 * Created by eriklupander on 2017-08-21.
 */
@Component
public class StatisticsGroupByUtil {

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    @Autowired
    private WebCertUserService webCertUserService;

    private AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    /**
     * Takes a list of object[] where each object[] is one of utkast, fraga/svar or arende represented as:
     *
     * [0] id (unique, this is what we want to count per enhetsId)
     * [1] enhetsId
     * [2] personnummer
     *
     * This method will filter out any items belonging to a patient having sekretessmarkering and return the result as a
     * map: EnhetsId -> number of id for that unit.
     *
     * @param groupableItems Each item is an array of: id, enhetsId, personnummer, intygsTyp.
     * @return Map with enhetsId -> count, with personummer being sekretessmarkerade has been removed.
     */
    public Map<String, Long> toSekretessFilteredMap(List<GroupableItem> groupableItems) {
        if (groupableItems == null || groupableItems.size() == 0) {
            return new HashMap<>();
        }

        List<GroupableItem> filteredGroupableItems = getFilteredGroupableItemList(groupableItems);

        WebCertUser user = webCertUserService.getUser();
        Map<Personnummer, SekretessStatus> sekretessStatusMap
            = patientDetailsResolver.getSekretessStatusForList(getPersonummerList(filteredGroupableItems));

        // update sekretess status
        filteredGroupableItems.forEach(item -> item.setSekretessStatus(sekretessStatusMap.get(createPnr(item.getPersonnummer()))));

        return filteredGroupableItems.stream()
            .filter(item -> item.getSekretessStatus() != SekretessStatus.UNDEFINED)
            .filter(item -> authoritiesValidator.given(user, item.getIntygsTyp())
                .privilegeIf(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                    item.getSekretessStatus() == SekretessStatus.TRUE)
                .isVerified())
            .collect(Collectors.groupingBy(GroupableItem::getEnhetsId, Collectors.counting()));
    }

    /*
     * Get a list of Personnummer where all objects with an
     * invalid personnummer are filtered out.
     *
     * Se INTYG-5094: Gör Webcert mindre känslig för felformaterade personnummer.
     */
    List<Personnummer> getPersonummerList(List<GroupableItem> results) {
        return results.stream()
            .map(item -> createPnr(item.getPersonnummer()))
            .filter(pnr -> pnr != null)     // filter out invalid personnummer
            .collect(Collectors.toList());
    }

    /*
     * Get a list of GroupableItems where all objects with an
     * invalid personnummer has been filtered out.
     *
     * Se INTYG-5094: Gör Webcert mindre känslig för felformaterade personnummer.
     */
    List<GroupableItem> getFilteredGroupableItemList(List<GroupableItem> results) {
        return results.stream()
            .filter(item -> createPnr(item.getPersonnummer()) != null)
            .collect(Collectors.toList());
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr).orElse(null);
    }

}
