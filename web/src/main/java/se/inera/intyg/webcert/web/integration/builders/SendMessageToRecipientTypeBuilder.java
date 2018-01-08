/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.builders;

import static se.inera.intyg.common.support.Constants.KV_AMNE_CODE_SYSTEM;

import org.apache.commons.lang3.StringUtils;
import se.inera.intyg.common.support.modules.converter.InternalConverterUtil;
import se.inera.intyg.infra.integration.hsa.model.AbstractVardenhet;
import se.inera.intyg.infra.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.MeddelandeReferens;
import se.riv.clinicalprocess.healthcond.certificate.v3.Vardgivare;

public final class SendMessageToRecipientTypeBuilder {

    private SendMessageToRecipientTypeBuilder() {
    }

    public static SendMessageToRecipientType build(Arende arende, WebCertUser skickatAv, String logiskAdressMottagare) {
        SendMessageToRecipientType request = new SendMessageToRecipientType();

        request.setAmne(buildAmne(arende));
        request.setIntygsId(buildIntygId(arende));
        request.setLogiskAdressMottagare(logiskAdressMottagare);
        request.setMeddelande(arende.getMeddelande());
        request.setMeddelandeId(arende.getMeddelandeId());
        request.setPaminnelseMeddelandeId(arende.getPaminnelseMeddelandeId());
        request.setPatientPersonId(InternalConverterUtil.getPersonId(new Personnummer(arende.getPatientPersonId())));
        request.setReferensId(arende.getReferensId());
        request.setRubrik(arende.getRubrik());
        request.setSistaDatumForSvar(arende.getSistaDatumForSvar());
        request.setSkickatAv(buildHosPersonalFromWebCertUser(skickatAv));
        request.setSkickatTidpunkt(arende.getSkickatTidpunkt());
        decorateWithSvarPa(request, arende);

        return request;
    }

    private static Amneskod buildAmne(Arende arende) {
        Amneskod amneskod = new Amneskod();
        amneskod.setCode(arende.getAmne().name());
        amneskod.setCodeSystem(KV_AMNE_CODE_SYSTEM);
        amneskod.setDisplayName(arende.getAmne().getDescription());
        return amneskod;
    }

    private static IntygId buildIntygId(Arende arende) {
        IntygId intygId = new IntygId();
        intygId.setRoot(arende.getEnhetId());
        intygId.setExtension(arende.getIntygsId());
        return intygId;
    }

    private static HosPersonal buildHosPersonalFromWebCertUser(WebCertUser user) {
        HosPersonal hosPersonal = new HosPersonal();
        hosPersonal.setPersonalId(InternalConverterUtil.getHsaId(user.getHsaId()));
        hosPersonal.setFullstandigtNamn(user.getNamn());
        hosPersonal.setForskrivarkod(user.getForskrivarkod());
        hosPersonal.setEnhet(buildEnhet(user));
        return hosPersonal;
    }

    private static Enhet buildEnhet(WebCertUser user) {
        Enhet enhet = new Enhet();
        SelectableVardenhet sourceVardenhet = user.getValdVardenhet();
        if (sourceVardenhet != null && sourceVardenhet instanceof AbstractVardenhet) {
            AbstractVardenhet valdVardenhet = (AbstractVardenhet) sourceVardenhet;
            enhet.setArbetsplatskod(InternalConverterUtil.getArbetsplatsKod(valdVardenhet.getArbetsplatskod()));
            enhet.setEnhetsId(InternalConverterUtil.getHsaId(valdVardenhet.getId()));
            enhet.setEnhetsnamn(StringUtils.defaultString(valdVardenhet.getNamn()));
            enhet.setEpost(StringUtils.trimToNull(valdVardenhet.getEpost()));
            enhet.setPostadress(StringUtils.defaultString(valdVardenhet.getPostadress()));
            enhet.setPostnummer(StringUtils.defaultString(valdVardenhet.getPostnummer()));
            enhet.setPostort(StringUtils.defaultString(valdVardenhet.getPostort()));
            enhet.setTelefonnummer(StringUtils.defaultString(valdVardenhet.getTelefonnummer()));
        }
        enhet.setVardgivare(buildVardgivare(user.getValdVardgivare()));
        return enhet;
    }

    private static Vardgivare buildVardgivare(SelectableVardenhet valdVardgivare) {
        Vardgivare vardgivare = new Vardgivare();
        if (valdVardgivare != null) {
            vardgivare.setVardgivareId(InternalConverterUtil.getHsaId(valdVardgivare.getId()));
            vardgivare.setVardgivarnamn(StringUtils.defaultString(valdVardgivare.getNamn()));
        }
        return vardgivare;
    }

    private static void decorateWithSvarPa(SendMessageToRecipientType request, Arende arende) {
        if (arende.getSvarPaId() != null) {
            MeddelandeReferens meddelandeReferens = new MeddelandeReferens();
            meddelandeReferens.setMeddelandeId(arende.getSvarPaId());
            if (arende.getSvarPaReferens() != null) {
                meddelandeReferens.setReferensId(arende.getSvarPaReferens());
            }
            request.setSvarPa(meddelandeReferens);
        }
    }

}
