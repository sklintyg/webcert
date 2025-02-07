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
package se.inera.intyg.webcert.web.service.fmb.sjukfall;

import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponseType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitType;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.sjukfall.dto.Formaga;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.fmb.sjukfall.converter.IntygstjanstConverter;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.Period;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsData;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsLista;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class FmbSjukfallServiceImpl implements FmbSjukfallService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String MESSAGE =
        "Fetch intyg data from Intygstjänst to Calculate total sjukskrivningstid for patient and care unit";

    private static final int MAX_GLAPP = 5;
    private static final int MAX_SEDAN_SJUKAVSLUT = 0;

    private final ListActiveSickLeavesForCareUnitResponderInterface sickLeavesForCareUnit;
    private final SjukfallEngineService sjukfallEngineService;
    private final WebCertUserService webCertUserService;

    public FmbSjukfallServiceImpl(
        final ListActiveSickLeavesForCareUnitResponderInterface sickLeavesForCareUnit,
        final SjukfallEngineService sjukfallEngineService,
        final WebCertUserService webCertUserService) {
        this.sickLeavesForCareUnit = sickLeavesForCareUnit;
        this.sjukfallEngineService = sjukfallEngineService;
        this.webCertUserService = webCertUserService;
    }

    // INTYGFV-12314: Sometimes getAktivtSjukfallForPatientAndEnhet() raises a NullPointerException,
    // and therefore more detailed logging has been enabled.
    @Override
    public int totalSjukskrivningstidForPatientAndCareUnit(final Personnummer personnummer, List<Period> periods) {

        LOG.debug("Starting: {}", MESSAGE);

        final Try<List<SjukfallEnhet>> sjukfallUppslag = Try.of(() -> getAktivtSjukfallForPatientAndEnhet(personnummer, periods));

        if (sjukfallUppslag.isFailure()) {
            LOG.error("Unable to get sjukfall: ", sjukfallUppslag.getCause());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, "Failed: " + MESSAGE);
        }

        LOG.debug("Done: {}", MESSAGE);

        if (isThereActiveSickLeaves(sjukfallUppslag)) {
            return getTotaltAntalDagar(sjukfallUppslag.get());
        } else {
            // If there are no active sickleaves, then return the total number of days for the periods passed.
            return getTotalNumberOfDaysFromPeriods(periods);
        }
    }

    private boolean isThereActiveSickLeaves(Try<List<SjukfallEnhet>> sjukfallUppslag) {
        return sjukfallUppslag.get().size() > 0;
    }

    /**
     * Calculate the sum of periods and adding one day to include both start and end date.
     *
     * @param periods Periods to calculate the sum.
     * @return Total number of days.
     */
    private int getTotalNumberOfDaysFromPeriods(List<Period> periods) {
        return periods.stream()
            .mapToInt(period -> Long.valueOf(DAYS.between(period.getFrom(), period.getTom())).intValue() + 1)
            .sum();
    }

    private List<SjukfallEnhet> getAktivtSjukfallForPatientAndEnhet(final Personnummer personnummer, final List<Period> periods) {
        final ListActiveSickLeavesForCareUnitType request = createRequest(personnummer, periods);
        final ListActiveSickLeavesForCareUnitResponseType response = sickLeavesForCareUnit.listActiveSickLeavesForCareUnit("", request);

        final IntygsLista intygsLista = response.getIntygsLista();
        // check for null (might be root-cause to INTYGFV-12314)
        final List<IntygsData> intygsData = Objects.isNull(intygsLista) ? Collections.emptyList() : intygsLista.getIntygsData();
        final List<IntygData> intygData = IntygstjanstConverter.toSjukfallFormat(intygsData);

        final LocalDate aktivtDatum;
        if (intygsData.size() > 0) {
            // If an active sick leave already exists, then add an intygData representing the periods passed.
            // Calculate the active date to use, based on the sick leave.
            intygData.add(createIntygDataFromPeriods(periods,
                intygData.get(0).getVardenhetId(),
                intygData.get(0).getVardgivareId(),
                intygData.get(0).getPatientId()));
            aktivtDatum = calculateWhichActiveDateToUse(intygData);
        } else {
            // If no active sick leave exists, then add an intygData representing the period passed and use the first periods
            // from date as the active date.
            intygData.add(createIntygDataFromPeriods(periods,
                request.getEnhetsId().getExtension(),
                request.getEnhetsId().getExtension(),
                personnummer.getPersonnummer()));
            aktivtDatum = periods.get(0).getFrom();
        }

        final IntygParametrar intygParametrar = new IntygParametrar(MAX_GLAPP, MAX_SEDAN_SJUKAVSLUT, aktivtDatum);

        return sjukfallEngineService.beraknaSjukfallForEnhet(intygData, intygParametrar);
    }

    private IntygData createIntygDataFromPeriods(List<Period> periods, String vardenhetId, String vardgivareId, String patientId) {
        final IntygData intygData = new IntygData();
        intygData.setVardenhetId(vardenhetId);
        intygData.setVardgivareId(vardgivareId);
        intygData.setPatientId(patientId);

        final List<Formaga> formagaList = new ArrayList<>(periods.size());
        for (Period period : periods) {
            formagaList.add(new Formaga(period.getFrom(), period.getTom(), period.getNedsattning()));
        }
        intygData.setFormagor(formagaList);

        // Set signing date time to now. Reason for this is to make sure that the sjukfall-logic can evaluate
        // which certificate is considered active, if multiple certificates span over the same period.
        intygData.setSigneringsTidpunkt(LocalDateTime.now());

        return intygData;
    }

    /**
     * Calculation of Sjukfall is usually based on current date. But in order to make correct calculations when the sjukfall is either
     * in the past or the future, the date intervals of formaga needs to be considered.
     *
     * If the last formagas slutdatum is in the past, then use that date instead of now.
     *
     * @param intygData List of intygData to consider.
     * @return Date to consider as active.
     */
    private LocalDate calculateWhichActiveDateToUse(List<IntygData> intygData) {
        final LocalDate now = LocalDate.now();

        final LocalDate slutdatum = intygData.stream()
            .flatMap(intyg -> intyg.getFormagor().stream())
            .reduce(((formaga, formaga2) -> formaga.getSlutdatum().compareTo(formaga2.getSlutdatum()) >= 0 ? formaga : formaga2))
            .get().getSlutdatum();

        return now.compareTo(slutdatum) >= 0 ? slutdatum : now;
    }

    private ListActiveSickLeavesForCareUnitType createRequest(final Personnummer personnummer, final List<Period> periods) {

        PersonId personId = new PersonId();
        personId.setExtension(personnummer.getOriginalPnr());

        final String hsaQueryEnhet = getEnhetsIdForQueryingIntygstjansten(webCertUserService.getUser());

        HsaId hsaId = new HsaId();
        hsaId.setExtension(hsaQueryEnhet);
        hsaId.setRoot("");

        ListActiveSickLeavesForCareUnitType request = new ListActiveSickLeavesForCareUnitType();
        request.setPersonId(personId);

        request.setMaxDagarSedanAvslut(calculateMaxDaysSinceNow(periods));
        request.setEnhetsId(hsaId);

        return request;
    }

    /**
     * Calculate how many days since now sickleaves should be considered. Normally when checking active sickleaves, it is considered
     * active now. But because list of periods can be in the past, the calculation needs to consider that.
     *
     * Add the difference between the ealiest from date and now to the MAX_GAP to cater for this.
     *
     * @param periods Periods to calculate max days since now.
     * @return Number of max days since now.
     */
    private int calculateMaxDaysSinceNow(List<Period> periods) {
        if (periods.size() == 0) {
            return MAX_GLAPP;
        }

        Collections.sort(periods, Comparator.comparing(Period::getFrom));

        final LocalDate date = periods.get(0).getFrom();
        final int diffToNow = (int) ChronoUnit.DAYS.between(date, LocalDate.now());
        return diffToNow + MAX_GLAPP;
    }

    private String getEnhetsIdForQueryingIntygstjansten(WebCertUser user) {
        if (user.isValdVardenhetMottagning()) {
            // Must return PARENT id if selected unit is an underenhet aka "mottagning".
            for (Vardgivare vg : user.getVardgivare()) {
                for (Vardenhet ve : vg.getVardenheter()) {
                    for (Mottagning m : ve.getMottagningar()) {
                        if (m.getId().equals(user.getValdVardenhet().getId())) {
                            //Return the selected mottagnings parent
                            return ve.getId();
                        }
                    }
                }
            }
            throw new IllegalStateException("User object is in invalid state. "
                + "Current selected enhet is an underenhet, but no ID for the parent enhet was found.");
        } else {
            return user.getValdVardenhet().getId();
        }
    }

    private int getTotaltAntalDagar(final List<SjukfallEnhet> sjukfallForEnhet) {
        return sjukfallForEnhet.stream()
            .map(SjukfallEnhet::getDagar)
            .reduce(0, Integer::sum);
    }
}
