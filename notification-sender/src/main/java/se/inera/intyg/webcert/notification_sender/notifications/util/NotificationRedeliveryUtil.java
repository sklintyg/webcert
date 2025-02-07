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
package se.inera.intyg.webcert.notification_sender.notifications.util;


import static se.inera.intyg.common.support.Constants.ARBETSPLATS_KOD_OID;
import static se.inera.intyg.common.support.Constants.HSA_ID_OID;
import static se.inera.intyg.common.support.Constants.KV_HANDELSE_CODE_SYSTEM;
import static se.inera.intyg.common.support.Constants.KV_INTYGSTYP_CODE_SYSTEM;
import static se.inera.intyg.common.support.Constants.PERSON_ID_OID;
import static se.inera.intyg.common.support.Constants.SAMORDNING_ID_OID;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.validate.SamordningsnummerValidator;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.notification.AmneskodCreator;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.ArbetsplatsKod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Handelsekod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IIType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

public final class NotificationRedeliveryUtil {

    private static final String TEMPORARY_ARBETSPLATSKOD = "TEMPORARY ARBETSPLATSKOD";

    private NotificationRedeliveryUtil() {
    }

    public static <T extends IIType> T getIIType(T iiType, String extension, String root) {
        if (extension == null) {
            return null;
        } else {
            iiType.setExtension(extension);
            iiType.setRoot(root);
            return iiType;
        }
    }

    public static Handelse getEventV3(HandelsekodEnum eventType, LocalDateTime eventTime, ArendeAmne topic, LocalDate lastDateForReply) {
        Handelsekod eventCodeV3 = new Handelsekod();
        eventCodeV3.setCode(eventType.value());
        eventCodeV3.setCodeSystem(KV_HANDELSE_CODE_SYSTEM);
        eventCodeV3.setDisplayName(eventType.description());

        se.riv.clinicalprocess.healthcond.certificate.v3.Handelse eventV3 =
            new se.riv.clinicalprocess.healthcond.certificate.v3.Handelse();
        eventV3.setHandelsekod(eventCodeV3);
        eventV3.setTidpunkt(eventTime);

        if (HandelsekodEnum.NYFRFM == eventType) {
            eventV3.setAmne(topic != null ? AmneskodCreator.create(topic.name(), topic.getDescription()) : null);
            eventV3.setSistaDatumForSvar(lastDateForReply);
        }

        return eventV3;
    }

    public static HosPersonal getHosPersonal(Vardgivare careProvider, Vardenhet careUnit, PersonInformation personInfo) {
        se.riv.clinicalprocess.healthcond.certificate.v3.Vardgivare careProviderV3 =
            new se.riv.clinicalprocess.healthcond.certificate.v3.Vardgivare();
        careProviderV3.setVardgivareId(getIIType(new HsaId(), careProvider.getId(), HSA_ID_OID));
        careProviderV3.setVardgivarnamn(careProvider.getNamn());

        Enhet unitV3 = new Enhet();
        unitV3.setEnhetsId(getIIType(new HsaId(), careUnit.getId(), HSA_ID_OID));
        unitV3.setEnhetsnamn(careUnit.getNamn());
        unitV3.setArbetsplatskod(getIIType(new ArbetsplatsKod(), Strings.nullToEmpty(careUnit.getArbetsplatskod()).trim().isEmpty()
            ? TEMPORARY_ARBETSPLATSKOD : careUnit.getArbetsplatskod(), ARBETSPLATS_KOD_OID));
        unitV3.setVardgivare(careProviderV3);
        unitV3.setPostadress(careUnit.getPostadress());
        unitV3.setPostnummer(careUnit.getPostnummer());
        unitV3.setPostort(careUnit.getPostort());
        unitV3.setEpost("".equals(careUnit.getEpost()) ? null : careUnit.getEpost());
        unitV3.setTelefonnummer(careUnit.getTelefonnummer());

        HosPersonal hosPersonal = new HosPersonal();
        hosPersonal.setPersonalId(getIIType(new HsaId(), personInfo.getPersonHsaId(), HSA_ID_OID));
        hosPersonal.setFullstandigtNamn(personInfo.getGivenName() + " " + personInfo.getMiddleAndSurName());
        hosPersonal.setEnhet(unitV3);
        return hosPersonal;
    }

    public static TypAvIntyg getCertificateType(ModuleEntryPoint moduleEntryPoint) {
        TypAvIntyg certificateTypeV3 = new TypAvIntyg();
        certificateTypeV3.setCode(moduleEntryPoint.getExternalId());
        certificateTypeV3.setCodeSystem(KV_INTYGSTYP_CODE_SYSTEM);
        certificateTypeV3.setDisplayName(moduleEntryPoint.getModuleName());
        return certificateTypeV3;
    }

    public static Patient getPatient(String personnummer) {
        Patient patientV3 = new Patient();
        Optional<Personnummer> personalId = Personnummer.createPersonnummer(personnummer);
        patientV3.setPersonId(getIIType(new PersonId(), personnummer,
            SamordningsnummerValidator.isSamordningsNummer(personalId) ? SAMORDNING_ID_OID : PERSON_ID_OID));
        patientV3.setFornamn("");
        patientV3.setEfternamn("");
        patientV3.setPostadress("");
        patientV3.setPostnummer("");
        patientV3.setPostort("");
        return patientV3;
    }
}
