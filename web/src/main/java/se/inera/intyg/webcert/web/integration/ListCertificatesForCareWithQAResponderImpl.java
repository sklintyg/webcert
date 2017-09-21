/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration;

import static se.inera.intyg.common.support.Constants.KV_AMNE_CODE_SYSTEM;
import static se.inera.intyg.common.support.Constants.KV_HANDELSE_CODE_SYSTEM;
import static se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTypeConverter.toArenden;

import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsRequest;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsResponse;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.HandelseList;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.List;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListCertificatesForCareWithQAResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListCertificatesForCareWithQAResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListCertificatesForCareWithQAType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListItem;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Handelsekod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Handelse;

@SchemaValidation
public class ListCertificatesForCareWithQAResponderImpl implements ListCertificatesForCareWithQAResponderInterface {

    @Autowired
    private IntygService intygService;

    @Override
    public ListCertificatesForCareWithQAResponseType listCertificatesForCareWithQA(String s, ListCertificatesForCareWithQAType request) {
        Objects.requireNonNull(request.getEnhetsId());
        if (!validate(request)) {
            throw new IllegalArgumentException();
        }

        ListCertificatesForCareWithQAResponseType response = new ListCertificatesForCareWithQAResponseType();
        List list = new List();
        IntygWithNotificationsRequest.Builder builder = new IntygWithNotificationsRequest.Builder()
                .setPersonnummer(new Personnummer(request.getPersonId().getExtension()));

        if (!request.getEnhetsId().isEmpty()) {
            builder = builder.setEnhetId(request.getEnhetsId().stream().map(HsaId::getExtension).collect(Collectors.toList()));
        }
        if (request.getVardgivarId() != null) {
            builder = builder.setVardgivarId(request.getVardgivarId().getExtension());
        }
        if (request.getFromTidpunkt() != null) {
            builder = builder.setStartDate(request.getFromTidpunkt());
        }
        if (request.getTomTidpunkt() != null) {
            builder = builder.setEndDate(request.getTomTidpunkt());
        }

        java.util.List<IntygWithNotificationsResponse> intygWithNotifications = intygService.listCertificatesForCareWithQA(builder.build());

        for (IntygWithNotificationsResponse intygHolder : intygWithNotifications) {
            ListItem item = new ListItem();
            item.setIntyg(intygHolder.getIntyg());
            HandelseList handelseList = new HandelseList();
            handelseList.getHandelse().addAll(intygHolder.getNotifications().stream()
                    .map(ListCertificatesForCareWithQAResponderImpl::toHandelse)
                    .collect(Collectors.toList()));
            item.setHandelser(handelseList);
            item.setSkickadeFragor(toArenden(intygHolder.getSentQuestions()));
            item.setMottagnaFragor(toArenden(intygHolder.getReceivedQuestions()));
            item.setRef(intygHolder.getRef());

            list.getItem().add(item);
        }
        response.setList(list);
        return response;
    }

    private boolean validate(ListCertificatesForCareWithQAType request) {
        if (request.getPersonId() == null) {
            return false;
        }
        if (!validateEnhetIdAndVardgivarId(request)) {
            return false;
        }
        return true;
    }

    private boolean validateEnhetIdAndVardgivarId(ListCertificatesForCareWithQAType request) {
        // Giltigt fall: Noll till flera enhetsid:n anges, men inget vårdgivar-id
        if (request.getVardgivarId() == null) {
            return true;
        }
        // Giltigt fall: Ett vårdgivar-id, men inga enhetsid:n
        return request.getEnhetsId().isEmpty();
    }

    private static Handelse toHandelse(se.inera.intyg.webcert.persistence.handelse.model.Handelse e) {
        Handelse res = new Handelse();

        Handelsekod code = new Handelsekod();
        code.setCodeSystem(KV_HANDELSE_CODE_SYSTEM);
        code.setCode(e.getCode().value());
        res.setHandelsekod(code);
        if (e.getAmne() != null) {
            res.setAmne(buildAmne(e.getAmne()));
        }
        res.setSistaDatumForSvar(e.getSistaDatumForSvar());
        res.setTidpunkt(e.getTimestamp());

        return res;
    }

    private static Amneskod buildAmne(ArendeAmne arende) {
        Amneskod amneskod = new Amneskod();
        amneskod.setCode(arende.name());
        amneskod.setCodeSystem(KV_AMNE_CODE_SYSTEM);
        amneskod.setDisplayName(arende.getDescription());
        return amneskod;
    }

}
