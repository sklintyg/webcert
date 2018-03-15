/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.privatlakaravtal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.intyg.webcert.persistence.privatlakaravtal.repository.AvtalRepository;
import se.inera.intyg.webcert.persistence.privatlakaravtal.repository.GodkantAvtalRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

/**
 * Created by eriklupander on 2015-08-05.
 */
@Service("AvtalService")
public class AvtalServiceImpl implements AvtalService {

    @Autowired
    private AvtalRepository avtalRepository;

    @Autowired
    private GodkantAvtalRepository godkantAvtalRepository;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public boolean userHasApprovedLatestAvtal(String userId) {
        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();
        return godkantAvtalRepository.userHasApprovedAvtal(userId, latestAvtalVersion);
    }

    @Override
    public Avtal getLatestAvtal() {
        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();
        return avtalRepository.findOne(latestAvtalVersion);
    }

    @Override
    public void approveLatestAvtal(String userId, String personId) {
        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();
        if (latestAvtalVersion == null || latestAvtalVersion == -1) {
            throw new IllegalStateException("Cannot approve private practitioner avtal, no avtal exists in the database.");
        }
        godkantAvtalRepository.approveAvtal(userId, latestAvtalVersion);
        monitoringLogService.logPrivatePractitionerTermsApproved(userId,
                Personnummer.createValidatedPersonnummer(personId).orElse(null), latestAvtalVersion);
    }

    @Override
    public void removeApproval(String userId) {
        godkantAvtalRepository.removeAllUserApprovments(userId);
    }
}
