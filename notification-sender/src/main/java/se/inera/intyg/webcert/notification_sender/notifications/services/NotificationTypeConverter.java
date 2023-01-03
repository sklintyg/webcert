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
package se.inera.intyg.webcert.notification_sender.notifications.services;

import static se.inera.intyg.common.support.Constants.KV_HANDELSE_CODE_SYSTEM;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Handelsekod;
import se.riv.clinicalprocess.healthcond.certificate.v3.Arenden;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

public final class NotificationTypeConverter {

    protected static final String TEMPORARY_ARBETSPLATSKOD = "TEMPORARY ARBETSPLATSKOD";

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationTypeConverter.class);

    private NotificationTypeConverter() {
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
    public static void complementIntyg(Intyg intyg) {
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

        Handelse handelse = new Handelse();
        handelse.setHandelsekod(handelseKod);
        handelse.setTidpunkt(notificationMessage.getHandelseTid());

        // JIRA INTYG-3715 föreskriver att ämne och sista svarsdatum endast ska läggas
        // till om händelsen är av typen NYFRFM (ny fråga från mottagare).
        if (HandelsekodEnum.fromValue(handelseKod.getCode()) == HandelsekodEnum.NYFRFM) {
            if (notificationMessage.getAmne() == null) {
                LOGGER.debug("Vid händelsetypen NYFRFM var ämneskod null");
            }
            handelse.setAmne(notificationMessage.getAmne());

            if (notificationMessage.getSistaSvarsDatum() == null) {
                LOGGER.debug("Vid händelsetypen NYFRFM var sista datum för svars null");
            }
            handelse.setSistaDatumForSvar(notificationMessage.getSistaSvarsDatum());
        }

        statusUpdateType.setHandelse(handelse);
    }

    private static void decorateWithArenden(CertificateStatusUpdateForCareType statusUpdateType, NotificationMessage notificationMessage) {
        statusUpdateType.setSkickadeFragor(toArenden(notificationMessage.getSkickadeFragor()));
        statusUpdateType.setMottagnaFragor(toArenden(notificationMessage.getMottagnaFragor()));
    }

    public static Arenden toArenden(ArendeCount source) {
        Arenden target = new Arenden();
        target.setTotalt(source.getTotalt());
        target.setBesvarade(source.getBesvarade());
        target.setEjBesvarade(source.getEjBesvarade());
        target.setHanterade(source.getHanterade());
        return target;
    }
}
