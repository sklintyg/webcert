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
package se.inera.intyg.webcert.notification_sender.notifications.filter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.camel.Message;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;

/**
 * Created by eriklupander on 2016-07-04.
 */
public class NotificationMessageDiscardFilter {

    private ObjectMapper om = new CustomObjectMapper();

    public List<Message> process(List<Message> messageList) {

        Map<String, List<Message>> latestMessage = new HashMap<>();

        for (Message camelMsg : messageList) {
            NotificationMessage msg = getNotificationFromBody(camelMsg);

            // Always start by adding an empty list for intygsId if necessary
            if (!latestMessage.containsKey(msg.getIntygsId())) {
                latestMessage.put(msg.getIntygsId(), new ArrayList<>());
            }

            switch (msg.getHandelse()) {
                case SIGNAT:
                    handleSigneratNotification(latestMessage, camelMsg, msg);
                    break;

                case ANDRAT:
                    handleAndratNotification(latestMessage, camelMsg, msg);
                    break;

                default:
                    // If some unknown type accidently makes its way in here, just forward it.
                    latestMessage.get(msg.getIntygsId()).add(camelMsg);
                    break;
            }
        }

        // Flatten out the hashmap values and return as list.
        return latestMessage.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    private void handleAndratNotification(Map<String, List<Message>> latestMessage, Message camelMsg, NotificationMessage msg) {
        List<Message> existingMessagesForIntygsId = latestMessage.get(msg.getIntygsId());

        // If SIGNERAT entry exists, do nothing
        if (existingMessagesForIntygsId.stream()
                .filter(existingMsg -> getNotificationFromBody(existingMsg).getHandelse() == HandelsekodEnum.SIGNAT)
            .count() > 0) {
            return;
        }

        // Extract the existing msg of ANDRAT type if it exists (Can never be more than one)
        Message andratMsg = existingMessagesForIntygsId.stream()
                .filter(existingMsg -> getNotificationFromBody(existingMsg).getHandelse() == HandelsekodEnum.ANDRAT)
                .findFirst().orElse(null);

        // No existing of type, add.
        if (andratMsg == null) {
            existingMessagesForIntygsId.add(camelMsg);
        } else if (getNotificationFromBody(andratMsg).getHandelseTid().compareTo(msg.getHandelseTid()) < 0) {
            // Exists, but older - replace.
            existingMessagesForIntygsId.remove(andratMsg);
            existingMessagesForIntygsId.add(camelMsg);
        }
    }

    private void handleSigneratNotification(Map<String, List<Message>> latestMessage, Message camelMsg, NotificationMessage msg) {

        // Add it
        latestMessage.get(msg.getIntygsId()).add(camelMsg);

        // Remove any "ANDRAT" messages
        Iterator<Message> i =  latestMessage.get(msg.getIntygsId()).iterator();
        while (i.hasNext()) {
            Message existingMsg = i.next();
            if (getNotificationFromBody(existingMsg).getHandelse() == HandelsekodEnum.ANDRAT) {
                i.remove();
            }
        }
    }

    private NotificationMessage getNotificationFromBody(Message existingMsg) {
        try {
            return om.readValue((String) existingMsg.getBody(), NotificationMessage.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
