/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.interactions.listcertificatesforcarewithqa;

import static se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTypeConverter.toArenden;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.annotations.SchemaValidation;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.web.csintegration.patient.GetCertificatesWithQAFromCertificateService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsRequest;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsResponse;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.HandelseList;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.List;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListCertificatesForCareWithQAResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListCertificatesForCareWithQAResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListCertificatesForCareWithQAType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListItem;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;

@Slf4j
@SchemaValidation
public class ListCertificatesForCareWithQAResponderImpl implements ListCertificatesForCareWithQAResponderInterface {

    private final IntygService intygService;
    private final GetCertificatesWithQAFromCertificateService getCertificatesWithQAFromCertificateService;
    private final NotificationService notificationService;
    private final HashUtility hashUtility;

    public ListCertificatesForCareWithQAResponderImpl(IntygService intygService,
        GetCertificatesWithQAFromCertificateService getCertificatesWithQAFromCertificateService,
        NotificationService notificationService, HashUtility hashUtility) {
        this.intygService = intygService;
        this.getCertificatesWithQAFromCertificateService = getCertificatesWithQAFromCertificateService;
        this.notificationService = notificationService;
        this.hashUtility = hashUtility;
    }

    @Override
    @PerformanceLogging(eventAction = "list-certificates-for-care-with-qa", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public ListCertificatesForCareWithQAResponseType listCertificatesForCareWithQA(String s, ListCertificatesForCareWithQAType request) {
        Objects.requireNonNull(request.getEnhetsId());
        if (invalidRequest(request)) {
            throw new IllegalArgumentException();
        }

        final var intygWithNotificationsRequest = getIntygWithNotificationsRequest(request);
        final var response = new ListCertificatesForCareWithQAResponseType();
        final var start = System.currentTimeMillis();
        log.info("Started processing request: PersonId: '{}' - CareProviderId '{}' - UnitIds '{}'",
            hashUtility.hash(intygWithNotificationsRequest.getPersonnummer().getPersonnummer()),
            intygWithNotificationsRequest.getVardgivarId(),
            intygWithNotificationsRequest.getEnhetId()
        );
        try {
            final var notifications = notificationService.findNotifications(intygWithNotificationsRequest);
            final var listItemsFromCS = getCertificatesWithQAFromCertificateService.get(notifications);
            final var intygWithNotifications = intygService.listCertificatesForCareWithQA(
                notifications.stream()
                    .filter(removeNotificationsRelatedToCertificatesFromCertificateService(listItemsFromCS))
                    .collect(Collectors.toList())
            );

            List list = new List();
            list.getItem().addAll(buildListItemsForWC(intygWithNotifications));
            list.getItem().addAll(listItemsFromCS);
            response.setList(list);

            return response;
        } finally {
            log.info(
                "Request processing completed. PersonId: '{}' CareProviderId '{}' UnitIds '{}'."
                    + " Returning '{}' number of certificates. Elapsed time: '{}' milliseconds",
                hashUtility.hash(intygWithNotificationsRequest.getPersonnummer().getPersonnummer()),
                intygWithNotificationsRequest.getVardgivarId(),
                intygWithNotificationsRequest.getEnhetId(),
                response.getList() != null ? response.getList().getItem().size() : 0,
                timeElapsed(start)
            );
        }
    }

    private java.util.List<ListItem> buildListItemsForWC(java.util.List<IntygWithNotificationsResponse> intygWithNotifications) {
        final var listItems = new ArrayList<ListItem>();
        for (IntygWithNotificationsResponse intygHolder : intygWithNotifications) {
            ListItem item = new ListItem();
            item.setIntyg(intygHolder.getIntyg());
            HandelseList handelseList = new HandelseList();
            handelseList.getHandelse().addAll(intygHolder.getNotifications().stream()
                .map(HandelseFactory::toHandelse)
                .toList());
            item.setHandelser(handelseList);
            item.setSkickadeFragor(toArenden(intygHolder.getSentQuestions()));
            item.setMottagnaFragor(toArenden(intygHolder.getReceivedQuestions()));
            item.setRef(intygHolder.getRef());
            listItems.add(item);
        }
        return listItems;
    }

    private static IntygWithNotificationsRequest getIntygWithNotificationsRequest(ListCertificatesForCareWithQAType request) {
        IntygWithNotificationsRequest.Builder builder = new IntygWithNotificationsRequest.Builder()
            .setPersonnummer(Personnummer.createPersonnummer(request.getPersonId().getExtension()).get());

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
        return builder.build();
    }

    private long timeElapsed(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    private static Predicate<Handelse> removeNotificationsRelatedToCertificatesFromCertificateService(
        java.util.List<ListItem> listItemsFromCS) {
        return notification -> listItemsFromCS.stream()
            .noneMatch(item -> item.getIntyg().getIntygsId().getExtension().equals(notification.getIntygsId()));
    }

    private boolean invalidRequest(ListCertificatesForCareWithQAType request) {
        if (request.getPersonId() == null) {
            return true;
        }
        return isMissingCareProviderIdAndUnitId(request);
    }

    private boolean isMissingCareProviderIdAndUnitId(ListCertificatesForCareWithQAType request) {
        return request.getEnhetsId().isEmpty() && request.getVardgivarId() == null;
    }
}
