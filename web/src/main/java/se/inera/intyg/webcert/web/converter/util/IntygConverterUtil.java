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

package se.inera.intyg.webcert.web.converter.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;

import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.VardAdresseringsType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.intyg.common.integration.hsa.model.AbstractVardenhet;
import se.inera.intyg.common.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.converter.ModelConverter;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.common.internal.*;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public final class IntygConverterUtil {

    private IntygConverterUtil() {
    }

    public static SendType buildSendTypeFromUtlatande(Utlatande utlatande) {

        LakarutlatandeEnkelType utlatandeType = ModelConverter.toLakarutlatandeEnkelType(utlatande);

        VardAdresseringsType vardAdressType = ModelConverter.toVardAdresseringsType(utlatande.getGrundData());

        SendType sendType = new SendType();
        sendType.setLakarutlatande(utlatandeType);
        sendType.setAdressVard(vardAdressType);
        sendType.setVardReferensId(buildVardReferensId(utlatande.getId()));
        sendType.setAvsantTidpunkt(LocalDateTime.now());

        return sendType;
    }

    public static String concatPatientName(List<String> fNames, List<String> mNames, String lName) {
        return concatPatientName(StringUtils.join(fNames, " "), StringUtils.join(mNames, " "), lName);
    }

    public static String concatPatientName(String fName, String mName, String lName) {
        StringBuilder sb = new StringBuilder();
        sb.append(fName);

        if (mName != null) {
            sb.append(" ").append(mName);
        }

        sb.append(" ").append(lName);
        return StringUtils.normalizeSpace(sb.toString());
    }

    public static String buildVardReferensId(String intygId) {
        return buildVardReferensId(intygId, LocalDateTime.now());
    }

    public static String buildVardReferensId(String intygId, LocalDateTime ts) {
        String time = ts.toString(ISODateTimeFormat.basicDateTime());
        return StringUtils.join(new Object[] { "SEND", intygId, time }, "-");
    }

    /**
     * Given an Utkast, a List of Statuses is built given:
     *
     * <li>If draft has a skickadTillMottagareDatum, a SENT status is added</li>
     * <li>If draft has a aterkalledDatum, a CANCELLED status is added</li>
     * <li>If there is a signature with a signature date, a RECEIVED status is added.</li>
     */
    public static List<se.inera.intyg.common.support.model.Status> buildStatusesFromUtkast(Utkast draft) {
        List<se.inera.intyg.common.support.model.Status> statuses = new ArrayList<>();

        if (draft.getSkickadTillMottagareDatum() != null) {
            se.inera.intyg.common.support.model.Status status = new se.inera.intyg.common.support.model.Status(CertificateState.SENT,
                    draft.getSkickadTillMottagare(), draft.getSkickadTillMottagareDatum());
            statuses.add(status);
        }
        if (draft.getAterkalladDatum() != null) {
            se.inera.intyg.common.support.model.Status status = new se.inera.intyg.common.support.model.Status(CertificateState.CANCELLED,
                    null, draft.getAterkalladDatum());
            statuses.add(status);
        }
        if (draft.getSignatur() != null && draft.getSignatur().getSigneringsDatum() != null) {
            se.inera.intyg.common.support.model.Status status = new se.inera.intyg.common.support.model.Status(CertificateState.RECEIVED,
                    null, draft.getSignatur().getSigneringsDatum());
            statuses.add(status);
        }
        return statuses;
    }

    public static HoSPersonal buildHosPersonalFromWebCertUser(WebCertUser user, Vardenhet vardenhet) {
        HoSPersonal hosPersonal = new HoSPersonal();
        hosPersonal.setPersonId(user.getHsaId());
        hosPersonal.setFullstandigtNamn(user.getNamn());
        hosPersonal.setForskrivarKod(user.getForskrivarkod());
        if (user.getBefattningar() != null) {
            hosPersonal.getBefattningar().addAll(user.getBefattningar());
        }
        if (user.getSpecialiseringar() != null) {
            hosPersonal.getSpecialiteter().addAll(user.getSpecialiseringar());
        }
        if (vardenhet != null) {
            hosPersonal.setVardenhet(vardenhet);
        } else {
            hosPersonal.setVardenhet(buildVardenhet(user));
        }
        return hosPersonal;
    }

    private static Vardenhet buildVardenhet(WebCertUser user) {
        Vardenhet vardenhet = new Vardenhet();
        SelectableVardenhet sourceVardenhet = user.getValdVardenhet();
        if (sourceVardenhet != null && sourceVardenhet instanceof AbstractVardenhet) {
            AbstractVardenhet valdVardenhet = (AbstractVardenhet) sourceVardenhet;
            vardenhet.setArbetsplatsKod(valdVardenhet.getArbetsplatskod());
            vardenhet.setEnhetsid(valdVardenhet.getId());
            vardenhet.setEnhetsnamn(valdVardenhet.getNamn());
            vardenhet.setEpost(valdVardenhet.getEpost());
            vardenhet.setPostadress(valdVardenhet.getPostadress());
            vardenhet.setPostnummer(valdVardenhet.getPostnummer());
            vardenhet.setPostort(valdVardenhet.getPostort());
            vardenhet.setTelefonnummer(valdVardenhet.getTelefonnummer());
        }
        vardenhet.setVardgivare(buildVardgivare(user.getValdVardgivare()));
        return vardenhet;
    }

    private static Vardgivare buildVardgivare(SelectableVardenhet valdVardgivare) {
        Vardgivare vardgivare = new Vardgivare();
        if (valdVardgivare != null) {
            vardgivare.setVardgivarid(valdVardgivare.getId());
            vardgivare.setVardgivarnamn(valdVardgivare.getNamn());
        }
        return vardgivare;
    }
}
