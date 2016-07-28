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

package se.inera.intyg.webcert.web.converter;

import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.ws.WebServiceException;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import se.inera.intyg.common.integration.hsa.services.HsaEmployeeService;
import se.inera.intyg.common.support.common.enumerations.PartKod;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.*;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType.Komplettering;
import se.riv.clinicalprocess.healthcond.certificate.v2.MeddelandeReferens;

public final class ArendeConverter {

    private ArendeConverter() {
    }

    public static Arende convert(SendMessageToCareType request) {
        Arende res = new Arende();
        res.setAmne(ArendeAmne.valueOf(request.getAmne().getCode()));
        res.setIntygsId(request.getIntygsId().getExtension());
        res.getKontaktInfo().addAll(request.getSkickatAv().getKontaktInfo());
        res.getKomplettering().addAll(request.getKomplettering().stream().map(ArendeConverter::convert).collect(Collectors.toList()));
        res.setMeddelande(request.getMeddelande());
        res.setMeddelandeId(request.getMeddelandeId());
        res.setPaminnelseMeddelandeId(request.getPaminnelseMeddelandeId());
        res.setPatientPersonId(request.getPatientPersonId().getExtension());
        res.setReferensId(request.getReferensId());
        res.setRubrik(request.getRubrik());
        res.setSistaDatumForSvar(request.getSistaDatumForSvar());
        res.setSkickatAv(PartKod.valueOf(request.getSkickatAv().getPart().getCode()).getValue());
        res.setSkickatTidpunkt(request.getSkickatTidpunkt());
        if (request.getSvarPa() != null) {
            res.setSvarPaId(request.getSvarPa().getMeddelandeId());
            res.setSvarPaReferens(extractReferensId(request.getSvarPa()));
        }
        return res;
    }

    public static void decorateArendeFromUtkast(Arende arende, Utkast utkast, LocalDateTime now, HsaEmployeeService hsaEmployeeService) {
        arende.setTimestamp(now);
        arende.setSenasteHandelse(now);
        arende.setStatus(arende.getSvarPaId() == null ? Status.PENDING_INTERNAL_ACTION : Status.ANSWERED);
        arende.setVidarebefordrad(Boolean.FALSE);

        arende.setIntygTyp(utkast.getIntygsTyp());
        arende.setSigneratAv(utkast.getSignatur().getSigneradAv());
        arende.setSigneratAvName(getSignedByName(utkast, hsaEmployeeService));
        arende.setEnhetId(utkast.getEnhetsId());
        arende.setEnhetName(utkast.getEnhetsNamn());
        arende.setVardgivareName(utkast.getVardgivarNamn());
    }

    public static Arende createArendeQuestionFromUtkast(ArendeAmne amne, String rubrik, String meddelande, Utkast utkast, LocalDateTime now,
            String vardaktorNamn, HsaEmployeeService hsaEmployeeService) {
        Arende arende = new Arende();
        arende.setStatus(Status.PENDING_EXTERNAL_ACTION);
        arende.setAmne(amne);
        arende.setEnhetId(utkast.getEnhetsId());
        arende.setEnhetName(utkast.getEnhetsNamn());
        arende.setVardgivareName(utkast.getVardgivarNamn());
        arende.setIntygsId(utkast.getIntygsId());
        arende.setIntygTyp(utkast.getIntygsTyp());
        arende.setMeddelande(meddelande);
        arende.setPatientPersonId(utkast.getPatientPersonnummer().getPersonnummerWithoutDash());
        arende.setRubrik(rubrik);
        arende.setSigneratAv(utkast.getSignatur().getSigneradAv());
        arende.setSigneratAvName(getSignedByName(utkast, hsaEmployeeService));
        decorateNewArende(arende, now, vardaktorNamn);
        return arende;
    }

    public static Arende createArendeAnswerFromQuestion(String meddelande, Arende svarPaMeddelande, LocalDateTime now, String vardaktorNamn) {
        Arende arende = new Arende();
        arende.setStatus(Status.CLOSED);
        arende.setSvarPaId(svarPaMeddelande.getMeddelandeId());
        arende.setSvarPaReferens(svarPaMeddelande.getReferensId());
        arende.setAmne(svarPaMeddelande.getAmne());
        arende.setEnhetId(svarPaMeddelande.getEnhetId());
        arende.setEnhetName(svarPaMeddelande.getEnhetName());
        arende.setVardgivareName(svarPaMeddelande.getVardgivareName());
        arende.setIntygsId(svarPaMeddelande.getIntygsId());
        arende.setIntygTyp(svarPaMeddelande.getIntygTyp());
        arende.setMeddelande(meddelande);
        arende.setPatientPersonId(svarPaMeddelande.getPatientPersonId());
        arende.setRubrik(svarPaMeddelande.getRubrik());
        arende.setSigneratAv(svarPaMeddelande.getSigneratAv());
        arende.setSigneratAvName(svarPaMeddelande.getSigneratAvName());
        decorateNewArende(arende, now, vardaktorNamn);
        return arende;
    }

    private static void decorateNewArende(Arende arende, LocalDateTime now, String vardaktorNamn) {
        arende.setMeddelandeId(UUID.randomUUID().toString());
        arende.setSkickatAv(FrageStallare.WEBCERT.getKod());
        arende.setVidarebefordrad(Boolean.FALSE);
        arende.setSenasteHandelse(now);
        arende.setSkickatTidpunkt(now);
        arende.setTimestamp(now);
        arende.setVardaktorName(vardaktorNamn);
    }

    // There are between 0 and 1 referensid in the MeddelandeReferens according to specification 2.0.RC3
    // Because of this we get the first item if there exists one
    private static String extractReferensId(MeddelandeReferens meddelandeReferens) {
        return meddelandeReferens.getReferensId() != null && !meddelandeReferens.getReferensId().isEmpty()
                ? meddelandeReferens.getReferensId().get(0)
                : null;
    }

    private static MedicinsktArende convert(Komplettering k) {
        MedicinsktArende res = new MedicinsktArende();
        res.setFrageId(k.getFrageId());
        res.setText(k.getText());
        res.setInstans(k.getInstans());
        return res;
    }

    // If we already have the signer's name in the information in the certificate we use this. This information could be
    // either in skapadAv or senastSparadAv. If neither of those matches the signer of the certificate we ask HSA.
    private static String getSignedByName(Utkast utkast, HsaEmployeeService hsaEmployeeService) {
        if (utkast.getSkapadAv() != null && utkast.getSkapadAv().getHsaId().equals(utkast.getSignatur().getSigneradAv())) {
            return utkast.getSkapadAv().getNamn();
        } else if (utkast.getSenastSparadAv() != null && utkast.getSenastSparadAv().getHsaId().equals(utkast.getSignatur().getSigneradAv())) {
            return utkast.getSenastSparadAv().getNamn();
        } else {
            try {
                return hsaEmployeeService.getEmployee(utkast.getSignatur().getSigneradAv(), null)
                        .stream()
                        .filter(pit -> StringUtils.isNotEmpty(pit.getMiddleAndSurName()))
                        .map(pit -> StringUtils.isNotEmpty(pit.getGivenName())
                                ? pit.getGivenName() + " " + pit.getMiddleAndSurName()
                                : pit.getMiddleAndSurName())
                        .findFirst()
                        .orElseThrow(() -> new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "No name was found in HSA"));
            } catch (WebServiceException e) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM,
                        "Could not communicate with HSA. Cause: " + e.getMessage());
            }
        }
    }
}
