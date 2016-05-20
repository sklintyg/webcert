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

package se.inera.intyg.webcert.web.integration.builder;

import se.inera.intyg.common.integration.hsa.model.AbstractVardenhet;
import se.inera.intyg.common.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.common.support.modules.converter.InternalConverterUtil;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v1.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v2.*;

public final class SendMessageToRecipientTypeBuilder {

    private static final String AMNE_CODE_SYSTEM = "ffa59d8f-8d7e-46ae-ac9e-31804e8e8499";

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
        amneskod.setCodeSystem(AMNE_CODE_SYSTEM);
        amneskod.setDisplayName(arende.getAmne().getDescription());
        return amneskod;
    }

    private static IntygId buildIntygId(Arende arende) {
        IntygId intygId = new IntygId();
        intygId.setRoot(arende.getEnhet());
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
            enhet.setEnhetsnamn(valdVardenhet.getNamn());
            enhet.setEpost(valdVardenhet.getEpost());
            enhet.setPostadress(valdVardenhet.getPostadress());
            enhet.setPostnummer(valdVardenhet.getPostnummer());
            enhet.setPostort(valdVardenhet.getPostort());
            enhet.setTelefonnummer(valdVardenhet.getTelefonnummer());
        }
        enhet.setVardgivare(buildVardgivare(user.getValdVardgivare()));
        return enhet;
    }

    private static Vardgivare buildVardgivare(SelectableVardenhet valdVardgivare) {
        Vardgivare vardgivare = new Vardgivare();
        if (valdVardgivare != null) {
            vardgivare.setVardgivareId(InternalConverterUtil.getHsaId(valdVardgivare.getId()));
            vardgivare.setVardgivarnamn(valdVardgivare.getNamn());
        }
        return vardgivare;
    }

    private static void decorateWithSvarPa(SendMessageToRecipientType request, Arende arende) {
        if (arende.getSvarPaId() != null) {
            MeddelandeReferens meddelandeReferens = new MeddelandeReferens();
            meddelandeReferens.setMeddelandeId(arende.getSvarPaId());
            if (arende.getSvarPaReferens() != null) {
                meddelandeReferens.getReferensId().add(arende.getSvarPaReferens());
            }
            request.setSvarPa(meddelandeReferens);
        }
    }

}
