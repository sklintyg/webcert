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
package se.inera.intyg.webcert.web.service.fmb.sjukfall;

import io.vavr.control.Try;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponseType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitType;
import se.inera.intyg.infra.integration.hsa.model.Mottagning;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
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
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsData;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsLista;

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
    public int totalSjukskrivningstidForPatientAndCareUnit(final Personnummer personnummer) {

        LOG.debug("Starting: {}", MESSAGE);

        final Try<List<SjukfallEnhet>> sjukfallUppslag = Try.of(() -> getAktivtSjukfallForPatientAndEnhet(personnummer));

        if (sjukfallUppslag.isFailure()) {
            LOG.error("Unable to get sjukfall: ", sjukfallUppslag.getCause());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, "Failed: " + MESSAGE);
        }

        LOG.debug("Done: {}",  MESSAGE);

        return getTotaltAntalDagar(sjukfallUppslag.get());
    }

    private List<SjukfallEnhet> getAktivtSjukfallForPatientAndEnhet(final Personnummer personnummer) {
        final ListActiveSickLeavesForCareUnitType request = createRequest(personnummer);
        final ListActiveSickLeavesForCareUnitResponseType response = sickLeavesForCareUnit.listActiveSickLeavesForCareUnit("", request);

        final IntygsLista intygsLista = response.getIntygsLista();
        // check for null (might be root-cause to INTYGFV-12314)
        final List<IntygsData> intygsData = Objects.isNull(intygsLista) ? Collections.emptyList() : intygsLista.getIntygsData();
        final List<IntygData> intygData = IntygstjanstConverter.toSjukfallFormat(intygsData);

        final LocalDate now = LocalDate.now();
        final IntygParametrar intygParametrar = new IntygParametrar(MAX_GLAPP, MAX_SEDAN_SJUKAVSLUT, now);

        return sjukfallEngineService.beraknaSjukfallForEnhet(intygData, intygParametrar);
    }


    private ListActiveSickLeavesForCareUnitType createRequest(final Personnummer personnummer) {

        PersonId personId = new PersonId();
        personId.setExtension(personnummer.getOriginalPnr());

        final String hsaQueryEnhet = getEnhetsIdForQueryingIntygstjansten(webCertUserService.getUser());

        HsaId hsaId = new HsaId();
        hsaId.setExtension(hsaQueryEnhet);
        hsaId.setRoot("");

        ListActiveSickLeavesForCareUnitType request = new ListActiveSickLeavesForCareUnitType();
        request.setPersonId(personId);
        request.setMaxDagarSedanAvslut(MAX_SEDAN_SJUKAVSLUT);
        request.setEnhetsId(hsaId);

        return request;
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
