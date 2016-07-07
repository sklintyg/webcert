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
package se.inera.intyg.webcert.notification_sender.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import se.inera.intyg.common.support.modules.support.api.notification.HandelseType;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.notification_sender.notifications.filter.NotificationMessageDiscardFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by eriklupander on 2016-07-04.
 */
public class NotificationMessageDiscardFilterTest {

    private ObjectMapper om = new CustomObjectMapper();
    private NotificationMessageDiscardFilter testee = new NotificationMessageDiscardFilter();

    @Test
    public void testReturnsSignedWhenBothSignedAndSavedExists() throws IOException {
        List<Message> processed = testee.process(buildMsgList(HandelseType.INTYGSUTKAST_SIGNERAT, HandelseType.INTYGSUTKAST_ANDRAT));
        assertEquals(1, processed.size());
        NotificationMessage notificationMessage = om.readValue( (String) processed.get(0).getBody(), NotificationMessage.class);
        assertEquals(HandelseType.INTYGSUTKAST_SIGNERAT, notificationMessage.getHandelse());
    }

    @Test
    public void testReturnsSignedWhenBothSignedAndSavedExistsAndratBeforeSignerat() throws IOException {
        List<Message> processed = testee.process(buildMsgList(HandelseType.INTYGSUTKAST_ANDRAT, HandelseType.INTYGSUTKAST_SIGNERAT));
        assertEquals(1, processed.size());
        NotificationMessage notificationMessage = om.readValue( (String) processed.get(0).getBody(), NotificationMessage.class);

        assertEquals(HandelseType.INTYGSUTKAST_SIGNERAT, notificationMessage.getHandelse());
    }


    @Test
    public void testFiltersOutAndratButRetainsOthers() throws JsonProcessingException {
        List<Message> processed = testee.process(buildMsgList(HandelseType.INTYGSUTKAST_SKAPAT, HandelseType.INTYGSUTKAST_SIGNERAT, HandelseType.INTYGSUTKAST_ANDRAT, HandelseType.INTYGSUTKAST_ANDRAT, HandelseType.INTYGSUTKAST_RADERAT));
        assertEquals(3, processed.size());
    }

    @Test
    public void testReturnsLatestSaved() throws IOException {
        String intygsId = UUID.randomUUID().toString();
        LocalDateTime first = LocalDateTime.now().minusSeconds(5);
        NotificationMessage nm2 = buildNotificationMessage(intygsId, HandelseType.INTYGSUTKAST_ANDRAT, LocalDateTime.now().minusSeconds(10));
        NotificationMessage nm1 = buildNotificationMessage(intygsId, HandelseType.INTYGSUTKAST_ANDRAT, first);
        NotificationMessage nm3 = buildNotificationMessage(intygsId, HandelseType.INTYGSUTKAST_ANDRAT, LocalDateTime.now().minusSeconds(15));

        List<Message> processed = testee.process(Arrays.asList(to(nm2), to(nm1), to(nm3)));
        assertEquals(1, processed.size());
        NotificationMessage notificationMessage = om.readValue( (String) processed.get(0).getBody(), NotificationMessage.class);
        assertEquals(first, notificationMessage.getHandelseTid());
    }

    private Message to(NotificationMessage nm) throws JsonProcessingException {
        DefaultMessage df = new DefaultMessage();
        df.setBody(om.writeValueAsString(nm));
        return df;
    }

    private List<Message> buildMsgList(HandelseType...typer) throws JsonProcessingException {
        List<Message> msgList = new ArrayList<>();
        String intygsId = UUID.randomUUID().toString();
        for (HandelseType ht : typer) {
            DefaultMessage df = new DefaultMessage();
            df.setBody(om.writeValueAsString(buildNotificationMessage(intygsId, ht)));
            msgList.add(df);
        }
        return msgList;
    }

    private NotificationMessage buildNotificationMessage(String intygsId, HandelseType ht, LocalDateTime tid) {
        NotificationMessage nf = new NotificationMessage();
        nf.setIntygsId(intygsId);
        nf.setHandelse(ht);
        nf.setHandelseTid(tid);
        return nf;
    }

    private NotificationMessage buildNotificationMessage(String intygsId, HandelseType ht) {
         return buildNotificationMessage(intygsId, ht, LocalDateTime.now());
    }

    private void sleep() {
        try {
            Thread.sleep(2L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
