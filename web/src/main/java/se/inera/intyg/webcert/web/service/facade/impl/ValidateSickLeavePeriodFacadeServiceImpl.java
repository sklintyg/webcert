/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.impl;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.ValidateSickLeavePeriodFacadeService;
import se.inera.intyg.webcert.web.service.fmb.FmbDiagnosInformationService;
import se.inera.intyg.webcert.web.web.controller.api.dto.Icd10KoderRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.MaximalSjukskrivningstidRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.MaximalSjukskrivningstidResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.Period;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateSickLeavePeriodRequestDTO;

@Service
public class ValidateSickLeavePeriodFacadeServiceImpl implements ValidateSickLeavePeriodFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateSickLeavePeriodFacadeServiceImpl.class);

    private final FmbDiagnosInformationService fmbDiagnosInformationService;

    @Autowired
    public ValidateSickLeavePeriodFacadeServiceImpl(
        FmbDiagnosInformationService fmbDiagnosInformationService) {
        this.fmbDiagnosInformationService = fmbDiagnosInformationService;
    }

    @Override
    public MaximalSjukskrivningstidResponse validateSickLeavePeriod(ValidateSickLeavePeriodRequestDTO request) {
        MaximalSjukskrivningstidRequest sickLeaveTimeRequest = new MaximalSjukskrivningstidRequest();
        Icd10KoderRequest codesRequest = new Icd10KoderRequest();
        codesRequest.setIcd10Kod1(request.getIcd10Code(0));
        codesRequest.setIcd10Kod2(request.getIcd10Code(1));
        codesRequest.setIcd10Kod3(request.getIcd10Code(2));

        List<Period> periods = new ArrayList<>();
        request.getDateRangeList().getList().forEach((dateRange) -> {
            Period period = new Period();
            period.setFrom(dateRange.getFrom());
            period.setTom(dateRange.getTo());
            period.setNedsattning(Integer.parseInt(dateRange.getId()));
            periods.add(period);
        });

        sickLeaveTimeRequest.setIcd10Koder(codesRequest);
        sickLeaveTimeRequest.setPersonnummer(Personnummer.createPersonnummer(request.getPersonId()).get());
        sickLeaveTimeRequest.setPeriods(periods);
        return fmbDiagnosInformationService.validateSjukskrivningtidForPatient(sickLeaveTimeRequest);
    }
}