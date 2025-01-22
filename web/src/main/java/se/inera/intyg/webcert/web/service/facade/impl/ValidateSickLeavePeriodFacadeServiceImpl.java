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
package se.inera.intyg.webcert.web.service.facade.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.lisjp.model.internal.Sjukskrivning.SjukskrivningsGrad;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRange;
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

    private final FmbDiagnosInformationService fmbDiagnosInformationService;

    @Autowired
    public ValidateSickLeavePeriodFacadeServiceImpl(
        FmbDiagnosInformationService fmbDiagnosInformationService) {
        this.fmbDiagnosInformationService = fmbDiagnosInformationService;
    }

    @Override
    public String validateSickLeavePeriod(ValidateSickLeavePeriodRequestDTO request) {
        final var sickLeaveTimeRequest = new MaximalSjukskrivningstidRequest();
        final var codesRequest = getIcd10CodesRequest(request);
        final var periods = getPeriods(request);
        final var totalDays = getTotalDays(request);

        sickLeaveTimeRequest.setIcd10Koder(codesRequest);
        sickLeaveTimeRequest.setPersonnummer(Personnummer.createPersonnummer(request.getPersonId()).get());
        sickLeaveTimeRequest.setPeriods(periods);

        try {
            final var response = fmbDiagnosInformationService.validateSjukskrivningtidForPatient(sickLeaveTimeRequest);
            return getResponseText(response, totalDays.get());
        } catch (Exception e) {
            return "På grund av ett tekniskt fel kan vi just nu inte räkna"
                + " ut om patienten överskrider FMB:s rekommenderade sjukskrivningslängd.";
        }
    }

    private AtomicLong getTotalDays(ValidateSickLeavePeriodRequestDTO request) {
        final var totalDays = new AtomicLong();
        request.getDateRangeList().getList().forEach((dateRange) ->
            totalDays.addAndGet(getTotalDays(dateRange))
        );
        return totalDays;
    }

    private List<Period> getPeriods(ValidateSickLeavePeriodRequestDTO request) {
        final List<Period> periods = new ArrayList<>();
        request.getDateRangeList().getList().forEach((dateRange) -> {
            final var period = new Period();
            period.setFrom(dateRange.getFrom());
            period.setTom(dateRange.getTo());
            final var sjukskrivningsgrad = SjukskrivningsGrad.fromId(dateRange.getId());
            period.setNedsattning(Integer.parseInt(sjukskrivningsgrad.getLabel().replace("%", "")));
            periods.add(period);
        });
        return periods;
    }

    private Icd10KoderRequest getIcd10CodesRequest(ValidateSickLeavePeriodRequestDTO request) {
        final var codesRequest = new Icd10KoderRequest();
        codesRequest.setIcd10Kod1(request.getIcd10Code(0));
        codesRequest.setIcd10Kod2(request.getIcd10Code(1));
        codesRequest.setIcd10Kod3(request.getIcd10Code(2));
        return codesRequest;
    }

    private long getTotalDays(CertificateDataValueDateRange dateRange) {
        if (dateRange.getFrom() == null || dateRange.getTo() == null) {
            return 0;
        }
        return Duration.between(dateRange.getFrom().atStartOfDay(), dateRange.getTo().atStartOfDay().plusDays(1)).toDays();
    }

    private String getResponseText(MaximalSjukskrivningstidResponse response, long daysFromCurrentCertificate) {
        if (response.isOverskriderRekommenderadSjukskrivningstid()) {
            if (response.getTotalSjukskrivningstid() == daysFromCurrentCertificate) {
                return "Den föreslagna sjukskrivningsperioden är längre än FMBs rekommendation på "
                    + response.getMaximaltRekommenderadSjukskrivningstid() + " dagar ("
                    + response.getMaximaltRekommenderadSjukskrivningstidSource() + ") för diagnosen "
                    + response.getAktuellIcd10Kod() + ". Ange en motivering för att underlätta Försäkringskassans handläggning.";
            } else {
                return "Den totala sjukskrivningsperioden är "
                    + response.getTotalSjukskrivningstid()
                    + " dagar och därmed längre än FMBs rekommendation på "
                    + response.getMaximaltRekommenderadSjukskrivningstid()
                    + " dagar (" + response.getMaximaltRekommenderadSjukskrivningstidSource() + ") för diagnosen "
                    + response.getAktuellIcd10Kod()
                    + ". Ange en motivering för att underlätta Försäkringskassans handläggning. Sjukskrivningsperioden"
                    + " är baserad på patientens sammanhängande intyg på denna vårdenhet.";
            }
        }
        return "";
    }
}
