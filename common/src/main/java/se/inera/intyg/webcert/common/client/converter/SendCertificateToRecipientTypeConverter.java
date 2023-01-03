/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.common.support.Constants.KV_PART_CODE_SYSTEM;

import java.time.LocalDateTime;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.modules.converter.InternalConverterUtil;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientType.SkickatAv;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Part;

public final class SendCertificateToRecipientTypeConverter {

    private SendCertificateToRecipientTypeConverter() {
    }

    public static SendCertificateToRecipientType convert(String intygsId, String personnummer, HoSPersonal skickatAv, String recipient) {
        SendCertificateToRecipientType request = new SendCertificateToRecipientType();
        request.setSkickatTidpunkt(LocalDateTime.now());
        request.setIntygsId(buildIntygId(intygsId));
        request.setPatientPersonId(InternalConverterUtil.getPersonId(createPnr(personnummer)));
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

    private static Part buildPart(String recipient) {
        Part part = new Part();
        part.setCode(recipient);
        part.setCodeSystem(KV_PART_CODE_SYSTEM);
        return part;
    }

    private static SkickatAv buildSkickatAv(HoSPersonal hosPersonal) {
        SkickatAv skickatAv = new SkickatAv();
        skickatAv.setHosPersonal(InternalConverterUtil.getSkapadAv(hosPersonal));
        return skickatAv;
    }

    private static Personnummer createPnr(String personId) {
        return Personnummer.createPersonnummer(personId)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer: " + personId));
    }
}
