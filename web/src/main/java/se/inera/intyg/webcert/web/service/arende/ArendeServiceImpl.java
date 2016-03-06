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

package se.inera.intyg.webcert.web.service.arende;

import se.inera.intyg.webcert.web.converter.util.TransportToArende;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
@Transactional("jpaTransactionManager")
public class ArendeServiceImpl implements ArendeService {

    @Autowired
    private ArendeRepository repo;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private WebCertUserService webcertUserService;

    @Autowired
    private MonitoringLogService monitoringLog;

    @Autowired
    private TransportToArende transportToArende;

    private static final ArendeTimeStampComparator ARENDE_TIMESTAMP_COMPARATOR = new ArendeTimeStampComparator();

    @Override
    public Arende processIncomingMessage(Arende arende) throws WebCertServiceException {
        Utkast utkast = utkastRepository.findOne(arende.getIntygsId());
        if (utkast == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "Certificate " + arende.getIntygsId() + " not found.");
        } else if (utkast.getSignatur() == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "Certificate " + arende.getIntygsId() + " not signed.");
        }
        decorateArende(arende, utkast);
        arende.setStatus(Status.PENDING_INTERNAL_ACTION);
        arende.setTimestamp(LocalDateTime.now());

        monitoringLog.logArendeReceived(arende.getIntygsId(), utkast.getIntygsTyp(), utkast.getEnhetsId(), arende.getRubrik());

        return repo.save(arende);
    }

    private void decorateArende(Arende arende, Utkast utkast) {
        arende.setIntygTyp(utkast.getIntygsTyp());
        arende.setSigneratAv(utkast.getSignatur().getSigneradAv());
        arende.setEnhet(utkast.getEnhetsId());
    }

    @Override
    public List<String> listSignedByForUnits() throws WebCertServiceException {
        WebCertUser user = webcertUserService.getUser();
        List<String> unitIds = user.getIdsOfSelectedVardenhet();

        return repo.findSigneratAvByEnhet(unitIds);
    }

    @Override
    public List<Arende> listArendeForUnits() throws WebCertServiceException {
        WebCertUser user = webcertUserService.getUser();
        List<String> unitIds = user.getIdsOfSelectedVardenhet();

        return repo.findByEnhet(unitIds);
    }

    @Override
    public List<Arende> getArende(String intygsId) {
        List<Arende> arendeList = repo.findByIntygsId(intygsId);

        List<Arende> resultList = new ArrayList<>();
        Iterator<Arende> iterator = arendeList.iterator();
        while (iterator.hasNext()) {
            Arende arende = iterator.next();
            Arende latestDraft = transportToArende.convert(arende);
            resultList.add(latestDraft);
        }
        Collections.sort(resultList, ARENDE_TIMESTAMP_COMPARATOR);
        return resultList;
    }

    public static class ArendeTimeStampComparator implements Comparator<Arende> {

        @Override
        public int compare(Arende f1, Arende f2) {
            if (f1.getTimestamp() == null && f2.getTimestamp() == null) {
                return 0;
            } else if (f1.getTimestamp() == null) {
                return -1;
            } else if (f2.getTimestamp() == null) {
                return 1;
            } else {
                return f2.getTimestamp().compareTo(f1.getTimestamp());
            }
        }
    }
}
