/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.notifications.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.NotificationRedeliveryDTO;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@Component
public class NotificationRedeliveryJob {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryJob.class);
    private static final String JOB_NAME = "NotificationRedeliveryJob.run";
    private static final String LOCK_AT_MOST = "PT2M";
    private static final String LOCK_AT_LEAST = "PT1M";

    @Autowired
    private HandelseRepository handelseRepository;

    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtkastRepository draftRepository;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

   @Autowired
    @Qualifier("jmsTemplateNotificationWSSender")
    private JmsTemplate jmsTemplate;

    private static final String LOCAL_PART = "CertificateStatusUpdateForCare";
    private static final String NAMESPACE_URL = "urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:3";

    @Scheduled(cron = "${job.notification.resend.cron}")
    @SchedulerLock(name = JOB_NAME, lockAtLeastFor = LOCK_AT_LEAST, lockAtMostFor = LOCK_AT_MOST)
    public void run() {
        LOG.info("Running job for notificaition redelivery...");

        List<NotificationRedelivery> redeliveryList = notificationRedeliveryRepository.findByRedeliveryTimeLessThan(LocalDateTime.now());
        redeliveryList.sort(Comparator.comparing(NotificationRedelivery::getRedeliveryTime));

        for (NotificationRedelivery notification : redeliveryList) {
            Handelse event = handelseRepository.findById(notification.getEventId()).orElse(null);

            try {
                NotificationRedeliveryDTO redeliveryDTO = objectMapper.readValue(notification.getMessage(), NotificationRedeliveryDTO.class);

                CertificateStatusUpdateForCareType statusUpdate = redeliveryDTO.get();

                if (redeliveryDTO.isCertificate()) {
                    Intyg certificate = getCertificate(redeliveryDTO.getCertId(), redeliveryDTO.getCertType());
                    certificate.setPatient(redeliveryDTO.getPatient());
                    statusUpdate.setIntyg(certificate);
                }

                QName qName = new QName(NAMESPACE_URL, LOCAL_PART);
                JAXBElement<CertificateStatusUpdateForCareType> jaxbElement =
                    new JAXBElement<>(qName, CertificateStatusUpdateForCareType.class, JAXBElement.GlobalScope.class, statusUpdate);
                String statusUpdateXml = XmlMarshallerHelper.marshal(jaxbElement);

                jmsTemplate.convertAndSend(statusUpdateXml.getBytes(), message -> {
                    message.setStringProperty(NotificationRouteHeaders.CORRELATION_ID, notification.getCorrelationId());
                    message.setStringProperty(NotificationRouteHeaders.INTYGS_ID, redeliveryDTO.getCertId());
                    message.setStringProperty(NotificationRouteHeaders.LOGISK_ADRESS, event.getEnhetsId());
                    message.setStringProperty(NotificationRouteHeaders.USER_ID, redeliveryDTO.getHandler().getExtension());
                    message.setLongProperty(Constants.JMS_TIMESTAMP, Instant.now().getEpochSecond());
                    return message;
                });
            } catch (IOException | ModuleException | ModuleNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private Intyg getCertificate(String certificateId, String certificateType) throws IOException, ModuleException,
        ModuleNotFoundException {

        Utkast draft = draftRepository.findByIntygsIdAndIntygsTyp(certificateId, certificateType);
        ModuleApi moduleApi = moduleRegistry.getModuleApi(draft.getIntygsTyp(), draft.getIntygTypeVersion());
        Utlatande utlatande = moduleApi.getUtlatandeFromJson(draft.getModel());
        return moduleApi.getIntygFromUtlatande(utlatande);
    }
}
