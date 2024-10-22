/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.message;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.infra.message.dto.MessageFromIT;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.ArendeConverter;
import se.inera.intyg.webcert.web.integration.ITIntegrationService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;

@Service
public class MessageImportServiceImpl implements MessageImportService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageImportServiceImpl.class);

    @Autowired
    private UtkastRepository draftRepository;

    @Autowired
    private ITIntegrationService itIntegrationService;

    @Autowired
    private IntygService certificateService;

    @Autowired
    private ArendeRepository messageRepository;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public boolean isImportNeeded(String certificateId) {
        return draftRepository.findById(certificateId).isEmpty();
    }

    @Override
    @Transactional
    public void importMessages(String certificateId) {
        importMessages(certificateId, null);
    }

    @Override
    @Transactional
    public void importMessages(String certificateId, String excludeMessageId) {
        final var messagesToImport = findMessagesToImport(certificateId, excludeMessageId);
        if (messagesToImport.isEmpty()) {
            return;
        }

        final var certificate = getCertificate(certificateId);
        if (certificate != null) {
            importMessages(certificate, messagesToImport);
        }
    }

    private List<MessageFromIT> findMessagesToImport(String certificateId, String excludeMessageId) {
        try {
            final var messagesFromIT = itIntegrationService.findMessagesByCertificateId(certificateId);
            if (messagesFromIT.isEmpty()) {
                return Collections.emptyList();
            }

            final var messagesToConsider = removeMessageToExclude(messagesFromIT, excludeMessageId);

            return removeAlreadyImportedMessages(messagesToConsider);
        } catch (Exception ex) {
            LOG.error("Could not find messages to import for certificateId: " + certificateId, ex);
            return Collections.emptyList();
        }
    }

    private IntygContentHolder getCertificate(String certificateId) {
        try {
            return certificateService.fetchIntygDataForInternalUse(certificateId, true);
        } catch (Exception ex) {
            LOG.error("Could not find the certificate to import messages for. CertificateId: " + certificateId, ex);
            return null;
        }
    }

    private void importMessages(IntygContentHolder certificate, List<MessageFromIT> messages) {
        final var sortedMessages = sortInChronologicalOrder(messages);

        for (var messageFromIT : sortedMessages) {
            try {
                processMessage(messageFromIT, certificate);
            } catch (Exception ex) {
                LOG.error("Could not import message with id: " + messageFromIT.getMessageId(), ex);
            }
        }
    }

    private List<MessageFromIT> removeMessageToExclude(List<MessageFromIT> messages, String excludeMessageId) {
        if (excludeMessageId == null) {
            return messages;
        }

        return messages.stream()
            .filter(message -> !message.getMessageId().equals(excludeMessageId))
            .collect(Collectors.toList());
    }

    private List<MessageFromIT> removeAlreadyImportedMessages(List<MessageFromIT> messages) {
        if (messages.isEmpty()) {
            return messages;
        }

        final var messageIds = new ArrayList<String>();
        for (var message : messages) {
            messageIds.add(message.getMessageId());
        }

        final var existingMessageIds = messageRepository.findMeddelandeIdByMeddelandeId(messageIds);

        final var messagesToImport = new ArrayList<MessageFromIT>(messages.size());
        for (var message : messages) {
            if (existingMessageIds.contains(message.getMessageId())) {
                continue;
            }

            messagesToImport.add(message);
        }

        return messagesToImport;
    }

    private void processMessage(MessageFromIT messageFromIT, IntygContentHolder certificate) {
        if (isSendMessageToCare(messageFromIT.getMessageContent())) {
            processIncomingMessage(messageFromIT, certificate);
        } else if (isSendMessageToRecipient(messageFromIT.getMessageContent())) {
            processOutgoingMessage(messageFromIT, certificate);
        } else {
            LOG.error("Couldn't process message. Unknown message format for messageId: " + messageFromIT.getMessageId());
        }
    }

    private void processOutgoingMessage(MessageFromIT messageFromIT, IntygContentHolder certificate) {
        final var messageToRecipient = convertXmlToSendMessageToRecipient(messageFromIT.getMessageContent(), messageFromIT.getMessageId());
        if (messageToRecipient == null) {
            return;
        }

        final var convertedMessage = convertMessageToRecipient(messageToRecipient, certificate);
        final var savedMessage = messageRepository.save(convertedMessage);

        if (isMessageAnAnswer(savedMessage)) {
            updateAnsweredMessage(savedMessage, Status.CLOSED);
        }

        monitoringLogService.logMessageImported(savedMessage.getIntygsId(), savedMessage.getMeddelandeId(),
            certificate.getUtlatande().getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid(),
            savedMessage.getEnhetId(), isMessageAnAnswer(savedMessage) ? "SendMessageToRecipient - Answer" : "SendMessageToRecipient");
    }

    private void processIncomingMessage(MessageFromIT messageFromIT, IntygContentHolder certificate) {
        final var messageToCare = convertXmlToSendMessageToCare(messageFromIT.getMessageContent(), messageFromIT.getMessageId());
        if (messageToCare == null) {
            return;
        }

        final var convertedMessage = convertMessageToCare(messageToCare, certificate);
        final var savedMessage = messageRepository.save(convertedMessage);

        if (isMessageAnAnswer(savedMessage)) {
            updateAnsweredMessage(savedMessage, Status.CLOSED);
        }

        if (isMessageReminder(savedMessage)) {
            updatedRemindedMessage(savedMessage);
        }

        if (savedMessage.getStatus() == Status.PENDING_INTERNAL_ACTION && savedMessage.getAmne() == ArendeAmne.KOMPLT) {
            if (isComplemented(certificate)) {
                savedMessage.setSenasteHandelse(certificate.getRelations().getLatestChildRelations().getComplementedByIntyg().getSkapad());
                savedMessage.setStatus(Status.CLOSED);
            }
        }

        monitoringLogService.logMessageImported(savedMessage.getIntygsId(), savedMessage.getMeddelandeId(),
            certificate.getUtlatande().getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid(),
            savedMessage.getEnhetId(), isMessageAnAnswer(savedMessage) ? "SendMessageToCare - Answer" : "SendMessageToCare");
    }

    private void updateAnsweredMessage(Arende message, Status answeredMessageStatus) {
        final var answeredMessage = getAnsweredMessage(message);
        if (answeredMessage == null) {
            return;
        }

        answeredMessage.setSenasteHandelse(message.getSkickatTidpunkt());
        answeredMessage.setStatus(answeredMessageStatus);
    }

    private void updatedRemindedMessage(Arende message) {
        final var remindedMessage = getReminderMessage(message);
        if (remindedMessage == null) {
            return;
        }

        remindedMessage.setSenasteHandelse(message.getSkickatTidpunkt());
    }

    private Arende getAnsweredMessage(Arende message) {
        if (isAnsweredOnMessageId(message)) {
            return messageRepository.findOneByMeddelandeId(message.getSvarPaId());
        }

        if (isAnsweredOnReferenceId(message)) {
            return messageRepository.findOneByReferensId(message.getSvarPaReferens());
        }

        return null;
    }

    private Arende getReminderMessage(Arende message) {
        if (isMessageReminder(message)) {
            return messageRepository.findOneByMeddelandeId(message.getPaminnelseMeddelandeId());
        }

        return null;
    }

    private boolean isMessageReminder(Arende message) {
        return message.getPaminnelseMeddelandeId() != null && !message.getPaminnelseMeddelandeId().isEmpty();
    }

    private boolean isMessageAnAnswer(Arende message) {
        return isAnsweredOnMessageId(message) || isAnsweredOnReferenceId(message);
    }

    private boolean isAnsweredOnMessageId(Arende message) {
        return message.getSvarPaId() != null && !message.getSvarPaId().isEmpty();
    }

    private boolean isAnsweredOnReferenceId(Arende message) {
        return message.getSvarPaReferens() != null && !message.getSvarPaReferens().isEmpty();
    }

    private boolean isComplemented(IntygContentHolder certificate) {
        return certificate.getRelations().getLatestChildRelations().getComplementedByIntyg() != null
            && !certificate.getRelations().getLatestChildRelations().getComplementedByIntyg().isMakulerat();
    }

    private List<MessageFromIT> sortInChronologicalOrder(List<MessageFromIT> messagesFromITList) {
        return messagesFromITList.stream()
            .sorted((Comparator.comparing(MessageFromIT::getTimestamp)))
            .collect(Collectors.toList());
    }

    private boolean isSendMessageToCare(String messageXml) {
        return messageXml.contains("SendMessageToCare");
    }

    private boolean isSendMessageToRecipient(String messageXml) {
        return messageXml.contains("SendMessageToRecipient");
    }

    private Arende convertMessageToRecipient(SendMessageToRecipientType messageToRecipientType, IntygContentHolder certificate) {
        final var message = new Arende();

        message.setAmne(ArendeAmne.valueOf(messageToRecipientType.getAmne().getCode()));
        message.setEnhetId(messageToRecipientType.getSkickatAv().getEnhet().getEnhetsId().getExtension());
        message.setIntygsId(messageToRecipientType.getIntygsId().getExtension());
        message.setEnhetName(messageToRecipientType.getSkickatAv().getEnhet().getEnhetsnamn());
        message.setIntygTyp(certificate.getUtlatande().getTyp());
        message.setMeddelande(messageToRecipientType.getMeddelande());
        message.setMeddelandeId(messageToRecipientType.getMeddelandeId());
        message.setPatientPersonId(messageToRecipientType.getPatientPersonId().getExtension());
        message.setRubrik(messageToRecipientType.getRubrik());
        message.setSigneratAv(messageToRecipientType.getSkickatAv().getPersonalId().getExtension());
        message.setSigneratAvName(messageToRecipientType.getSkickatAv().getFullstandigtNamn());
        message.setSenasteHandelse(messageToRecipientType.getSkickatTidpunkt());
        message.setSkickatTidpunkt(messageToRecipientType.getSkickatTidpunkt());
        message.setTimestamp(messageToRecipientType.getSkickatTidpunkt());
        message.setVardgivareName(messageToRecipientType.getSkickatAv().getEnhet().getVardgivare().getVardgivarnamn());
        message.setSkickatAv(FrageStallare.WEBCERT.getKod());
        message.setStatus(Status.PENDING_EXTERNAL_ACTION);
        message.setVardaktorName(messageToRecipientType.getSkickatAv().getFullstandigtNamn());

        if (messageToRecipientType.getSvarPa() != null) {
            message.setSvarPaId(messageToRecipientType.getSvarPa().getMeddelandeId());
            message.setSvarPaReferens(messageToRecipientType.getSvarPa().getReferensId());
            message.setStatus(Status.CLOSED);
        }

        return message;
    }

    private Arende convertMessageToCare(SendMessageToCareType messageToCare, IntygContentHolder certificate) {
        final var message = ArendeConverter.convert(messageToCare);

        ArendeConverter.decorateMessageFromCertificate(message, certificate.getUtlatande(), messageToCare.getSkickatTidpunkt());

        alwaysSetImportedAnswersAsClosed(message);

        return message;
    }

    private void alwaysSetImportedAnswersAsClosed(Arende message) {
        if (isMessageAnAnswer(message)) {
            message.setStatus(Status.CLOSED);
        }
    }

    private SendMessageToCareType convertXmlToSendMessageToCare(String messageXml, String messageId) {
        try {
            final var jaxbContext = JAXBContext.newInstance(SendMessageToCareType.class);
            final var unmarshaller = jaxbContext.createUnmarshaller();
            return (SendMessageToCareType) ((JAXBElement) unmarshaller.unmarshal(new StringReader(messageXml))).getValue();
        } catch (Exception ex) {
            LOG.error("Couldn´t unmarshall SendMessageToCareType with messageId: " + messageId, ex);
            return null;
        }
    }

    private SendMessageToRecipientType convertXmlToSendMessageToRecipient(String messageXml, String messageId) {
        try {
            final var jaxbContext = JAXBContext.newInstance(SendMessageToRecipientType.class);
            final var unmarshaller = jaxbContext.createUnmarshaller();
            return (SendMessageToRecipientType) ((JAXBElement) unmarshaller.unmarshal(new StringReader(messageXml))).getValue();
        } catch (Exception ex) {
            LOG.error("Couldn´t unmarshall SendMessageToRecipient with messageId: " + messageId, ex);
            return null;
        }
    }
}
