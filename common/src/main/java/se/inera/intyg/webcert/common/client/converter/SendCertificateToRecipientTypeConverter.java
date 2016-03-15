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

package se.inera.intyg.webcert.common.client.converter;

import org.joda.time.LocalDateTime;

import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v1.SendCertificateToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v1.SendCertificateToRecipientType.SkickatAv;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.*;

public final class SendCertificateToRecipientTypeConverter {

    private static final String PERSON_ID_ROOT = "1.2.752.129.2.1.3.1";
    private static final String HSA_ID_ROOT = "1.2.752.129.2.1.4.1";
    public static final String ARBETSPLATSKOD_ROOT = "1.2.752.29.4.71";
    private static final String MOTTAGARE_CODE_SYSTEM = "769bb12b-bd9f-4203-a5cd-fd14f2eb3b80";

    private SendCertificateToRecipientTypeConverter() {
    }

    public static SendCertificateToRecipientType convert(String intygsId, String personnummer, HoSPersonal skickatAv, String recipient) {
        SendCertificateToRecipientType request = new SendCertificateToRecipientType();
        request.setSkickatTidpunkt(LocalDateTime.now());
        request.setIntygsId(buildIntygId(intygsId));
        request.setPatientPersonId(buildPersonId(personnummer));
        request.setMottagare(buildPart(recipient));
        request.setSkickatAv(buildSkickatAv(skickatAv));
        return request;
    }

    private static IntygId buildIntygId(String intygsId) {
        IntygId intygId = new IntygId();
        intygId.setRoot("SE5565594230-B31"); // IT:s root since unit hsaId is not available
        intygId.setExtension(intygsId);
        return intygId;
    }

    private static PersonId buildPersonId(String personnummer) {
        PersonId personId = new PersonId();
        personId.setRoot(PERSON_ID_ROOT);
        personId.setExtension(personnummer);
        return personId;
    }

    private static Part buildPart(String recipient) {
        Part part = new Part();
        part.setCode(recipient);
        part.setCodeSystem(MOTTAGARE_CODE_SYSTEM);
        return part;
    }

    private static SkickatAv buildSkickatAv(HoSPersonal hosPersonal) {
        SkickatAv skickatAv = new SkickatAv();
        skickatAv.setHosPersonal(buildHosPersonal(hosPersonal));
        return skickatAv;
    }

    private static HosPersonal buildHosPersonal(HoSPersonal source) {
        HosPersonal hosPersonal = new HosPersonal();
        hosPersonal.setPersonalId(buildHsaId(source.getPersonId()));
        hosPersonal.setFullstandigtNamn(source.getFullstandigtNamn());
        hosPersonal.setForskrivarkod(source.getForskrivarKod());
        hosPersonal.setEnhet(buildEnhet(source.getVardenhet()));
        return hosPersonal;
    }

    private static Enhet buildEnhet(Vardenhet sourceVardenhet) {
        Enhet vardenhet = new Enhet();
        vardenhet.setEnhetsId(buildHsaId(sourceVardenhet.getEnhetsid()));
        vardenhet.setEnhetsnamn(sourceVardenhet.getEnhetsnamn());
        vardenhet.setPostnummer(sourceVardenhet.getPostnummer());
        vardenhet.setPostadress(sourceVardenhet.getPostadress());
        vardenhet.setPostort(sourceVardenhet.getPostort());
        vardenhet.setTelefonnummer(sourceVardenhet.getTelefonnummer());
        vardenhet.setEpost(sourceVardenhet.getEpost());
        vardenhet.setVardgivare(buildVardgivare(sourceVardenhet.getVardgivare()));
        vardenhet.setArbetsplatskod(buildArbetsplatsKod(sourceVardenhet.getArbetsplatsKod()));
        return vardenhet;
    }

    private static ArbetsplatsKod buildArbetsplatsKod(String sourceArbetsplatsKod) {
        ArbetsplatsKod arbetsplatsKod = new ArbetsplatsKod();
        arbetsplatsKod.setRoot(ARBETSPLATSKOD_ROOT);
        arbetsplatsKod.setExtension(sourceArbetsplatsKod);
        return arbetsplatsKod;
    }

    private static Vardgivare buildVardgivare(se.inera.intyg.common.support.model.common.internal.Vardgivare sourceVardgivare) {
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivareId(buildHsaId(sourceVardgivare.getVardgivarid()));
        vardgivare.setVardgivarnamn(sourceVardgivare.getVardgivarnamn());
        return vardgivare;
    }

    private static HsaId buildHsaId(String id) {
        HsaId hsaId = new HsaId();
        hsaId.setRoot(HSA_ID_ROOT);
        hsaId.setExtension(id);
        return hsaId;
    }
}
