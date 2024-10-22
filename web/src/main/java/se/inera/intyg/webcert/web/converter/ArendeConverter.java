/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import com.google.common.base.Strings;
import jakarta.xml.ws.WebServiceException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.infra.integration.hsatk.services.HsatkEmployeeService;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.converter.util.FragestallareConverterUtil;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType.Komplettering;

public final class ArendeConverter {

    private static final Logger LOG = LoggerFactory.getLogger(ArendeConverter.class);

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
        res.setSkickatAv(FragestallareConverterUtil.partToFrageStallarKod(request.getSkickatAv().getPart().getCode()));
        res.setSkickatTidpunkt(request.getSkickatTidpunkt());
        if (request.getSvarPa() != null) {
            res.setSvarPaId(request.getSvarPa().getMeddelandeId());
            res.setSvarPaReferens(request.getSvarPa().getReferensId());
        }
        return res;
    }

    public static void decorateArendeFromUtkast(Arende arende, Utkast utkast, LocalDateTime now, HsatkEmployeeService hsaEmployeeService) {
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

    /**
     * Decorate a message ({@link Arende}) with metadata from the certificate. This method can be used when there is no {@link Utkast}
     * available, but the {@link Utlatande} is.
     *
     * @param arende Message to decorate
     * @param certificate Certificate to decorate with
     * @param timestamp DateTime of the message
     */
    public static void decorateMessageFromCertificate(Arende arende, Utlatande certificate, LocalDateTime timestamp) {
        arende.setTimestamp(timestamp);
        arende.setSenasteHandelse(timestamp);
        arende.setStatus(arende.getSvarPaId() == null ? Status.PENDING_INTERNAL_ACTION : Status.ANSWERED);
        arende.setVidarebefordrad(Boolean.FALSE);

        arende.setIntygTyp(certificate.getTyp());
        arende.setSigneratAv(certificate.getGrundData().getSkapadAv().getPersonId());
        arende.setSigneratAvName(certificate.getGrundData().getSkapadAv().getFullstandigtNamn());
        arende.setEnhetId(certificate.getGrundData().getSkapadAv().getVardenhet().getEnhetsid());
        arende.setEnhetName(certificate.getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());
        arende.setVardgivareName(certificate.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarnamn());
    }

    public static Arende createArendeFromUtkast(ArendeAmne amne, String rubrik, String meddelande, Utkast utkast, LocalDateTime now,
        String vardaktorNamn, HsatkEmployeeService hsaEmployeeService) {
        Arende arende = new Arende();
        arende.setStatus(Status.PENDING_EXTERNAL_ACTION);
        arende.setAmne(amne);
        arende.setEnhetId(utkast.getEnhetsId());
        arende.setEnhetName(utkast.getEnhetsNamn());
        arende.setVardgivareName(utkast.getVardgivarNamn());
        arende.setIntygsId(utkast.getIntygsId());
        arende.setIntygTyp(utkast.getIntygsTyp());
        arende.setMeddelande(meddelande);
        arende.setPatientPersonId(utkast.getPatientPersonnummer().getPersonnummer());
        arende.setRubrik(rubrik);
        arende.setSigneratAv(utkast.getSignatur().getSigneradAv());
        arende.setSigneratAvName(getSignedByName(utkast, hsaEmployeeService));
        decorateOutgoingArende(arende, now, vardaktorNamn);
        return arende;
    }

    /**
     * Creates a message ({@link Arende}) with metadata from the certificate. This method can be used when there is no {@link Utkast}
     * available, but the {@link Utlatande} is.
     *
     * @param subject Subject of the message
     * @param header Header of the message
     * @param messageText The actual message
     * @param certificate Certificate that the message is related to
     * @param timestamp Timestamp of the message
     * @param nameOfCareGiver Name of the care giver
     * @param hsaEmployeeService {@link HsatkEmployeeService} implementation to use
     * @return The created message
     */
    public static Arende createMessageFromCertificate(ArendeAmne subject, String header, String messageText, Utlatande certificate,
        LocalDateTime timestamp, String nameOfCareGiver,
        HsatkEmployeeService hsaEmployeeService) {
        final var message = new Arende();
        message.setStatus(Status.PENDING_EXTERNAL_ACTION);
        message.setAmne(subject);
        message.setEnhetId(certificate.getGrundData().getSkapadAv().getVardenhet().getEnhetsid());
        message.setEnhetName(certificate.getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());
        message.setVardgivareName(certificate.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarnamn());
        message.setIntygsId(certificate.getId());
        message.setIntygTyp(certificate.getTyp());
        message.setMeddelande(messageText);
        message.setPatientPersonId(certificate.getGrundData().getPatient().getPersonId().getPersonnummer());
        message.setRubrik(header);
        message.setSigneratAv(certificate.getGrundData().getSkapadAv().getPersonId());
        message.setSigneratAvName(getNameByHsaId(message.getSigneratAv(), hsaEmployeeService));
        decorateOutgoingArende(message, timestamp, nameOfCareGiver);
        return message;
    }

    public static Arende createAnswerFromArende(String meddelande, Arende svarPaMeddelande, LocalDateTime now, String vardaktorNamn) {
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
        decorateOutgoingArende(arende, now, vardaktorNamn);
        return arende;
    }

    private static void decorateOutgoingArende(Arende arende, LocalDateTime now, String vardaktorNamn) {
        arende.setMeddelandeId(UUID.randomUUID().toString());
        arende.setSkickatAv(FrageStallare.WEBCERT.getKod());
        arende.setVidarebefordrad(Boolean.FALSE);
        arende.setSenasteHandelse(now);
        arende.setSkickatTidpunkt(now);
        arende.setTimestamp(now);
        arende.setVardaktorName(vardaktorNamn);
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
    private static String getSignedByName(Utkast utkast, HsatkEmployeeService hsaEmployeeService) {
        if (utkast.getSkapadAv() != null && utkast.getSkapadAv().getHsaId().equals(utkast.getSignatur().getSigneradAv())) {
            return utkast.getSkapadAv().getNamn();
        } else if (utkast.getSenastSparadAv() != null
            && utkast.getSenastSparadAv().getHsaId().equals(utkast.getSignatur().getSigneradAv())) {
            return utkast.getSenastSparadAv().getNamn();
        } else {
            return getNameByHsaId(utkast.getSignatur().getSigneradAv(), hsaEmployeeService);
        }
    }

    private static String getNameByHsaId(String hsaId, HsatkEmployeeService hsaEmployeeService) {
        try {
            return hsaEmployeeService.getEmployee(null, hsaId)
                .stream()
                .filter(pit -> !Strings.isNullOrEmpty(pit.getMiddleAndSurName()))
                .map(pit -> !Strings.isNullOrEmpty(pit.getGivenName())
                    ? pit.getGivenName() + " " + pit.getMiddleAndSurName()
                    : pit.getMiddleAndSurName())
                .findFirst()
                .orElseThrow(
                    () -> new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "No name was found in HSA"));
        } catch (WebServiceException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM,
                "Could not communicate with HSA. Cause: " + e.getMessage());
        }
    }

    public static Map<String, String> getNamesByHsaIds(Collection<String> hsaIds, HsatkEmployeeService hsaEmployeeService) {
        Map<String, String> hsaIdNameMap = new HashMap<>();

        hsaIds.forEach(hsaId -> {
            Optional<String> name = getNameByHsaIdNullIfNotFound(hsaId, hsaEmployeeService);

            name.ifPresent(s -> hsaIdNameMap.put(hsaId, s));
        });

        return hsaIdNameMap;
    }

    private static Optional<String> getNameByHsaIdNullIfNotFound(String hsaId, HsatkEmployeeService hsaEmployeeService) {
        try {
            return Optional.of(getNameByHsaId(hsaId, hsaEmployeeService));
        } catch (Exception e) {
            LOG.debug("Name not found for hsaId " + hsaId, e);
            return Optional.empty();
        }
    }
}
