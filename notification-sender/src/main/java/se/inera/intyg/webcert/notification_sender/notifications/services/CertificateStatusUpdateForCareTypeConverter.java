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
package se.inera.intyg.webcert.notification_sender.notifications.services;

import static se.inera.intyg.common.support.Constants.KV_HANDELSE_CODE_SYSTEM;

import com.google.common.base.Strings;

import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v2.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Handelsekod;
import se.riv.clinicalprocess.healthcond.certificate.v2.Arenden;
import se.riv.clinicalprocess.healthcond.certificate.v2.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v2.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.v2.Intyg;

public final class CertificateStatusUpdateForCareTypeConverter {

    protected static final String TEMPORARY_ARBETSPLATSKOD = "TEMPORARY ARBETSPLATSKOD";

    private CertificateStatusUpdateForCareTypeConverter() {
    }

    public static CertificateStatusUpdateForCareType convert(NotificationMessage notificationMessage, Intyg intyg) {
        CertificateStatusUpdateForCareType destination = new CertificateStatusUpdateForCareType();
        complementIntyg(intyg);
        destination.setIntyg(intyg);
        decorateWithHandelse(destination, notificationMessage);
        decorateWithArenden(destination, notificationMessage);
        destination.setRef(notificationMessage.getReference());
        return destination;
    }

    /**
     * This method should only be used for CertificateStatusUpdateForCare. DO NOT MOVE THIS METHOD!
     *
     * It is needed because a utkast might not contain the information needed to meet the requirements of the service
     * contract. And we send not yet signed utkast in CertificateStatusUpdateForCare.
     */
    private static void complementIntyg(Intyg intyg) {
        Enhet enhet = intyg.getSkapadAv().getEnhet();
        if (Strings.nullToEmpty(enhet.getArbetsplatskod().getExtension()).trim().isEmpty()) {
            enhet.getArbetsplatskod().setExtension(TEMPORARY_ARBETSPLATSKOD);
        }
        if ("".equals(enhet.getEpost())) {
            enhet.setEpost(null);
        }
    }

    private static void decorateWithHandelse(CertificateStatusUpdateForCareType statusUpdateType, NotificationMessage notificationMessage) {
        Handelsekod handelseKod = new Handelsekod();
        handelseKod.setCodeSystem(KV_HANDELSE_CODE_SYSTEM);

        handelseKod.setCode(notificationMessage.getHandelse().value());
        handelseKod.setDisplayName(notificationMessage.getHandelse().description());

        Handelse handelseType = new Handelse();
        handelseType.setHandelsekod(handelseKod);
        handelseType.setTidpunkt(notificationMessage.getHandelseTid());

        statusUpdateType.setHandelse(handelseType);
    }

    private static void decorateWithArenden(CertificateStatusUpdateForCareType statusUpdateType, NotificationMessage notificationMessage) {
        statusUpdateType.setSkickadeFragor(toArenden(notificationMessage.getSkickadeFragor()));
        statusUpdateType.setMottagnaFragor(toArenden(notificationMessage.getMottagnaFragor()));
    }

    private static Arenden toArenden(ArendeCount source) {
        Arenden target = new Arenden();
        target.setTotalt(source.getTotalt());
        target.setBesvarade(source.getBesvarade());
        target.setEjBesvarade(source.getEjBesvarade());
        target.setHanterade(source.getHanterade());
        return target;
    }
}
