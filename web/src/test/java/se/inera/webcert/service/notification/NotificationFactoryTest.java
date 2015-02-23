package se.inera.webcert.service.notification;

import static org.junit.Assert.*;

import org.joda.time.LocalDateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.webcert.notifications.message.v1.HoSPersonType;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;

/**
 * Created by Magnus Ekstrand on 03/12/14.
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class NotificationFactoryTest {

    @Test
    public void testNotificationRequestTypeContainsMillis() {
        LocalDateTime handelseTidpunkt = new LocalDateTime(2012, 12, 12, 12, 12, 12, 123);
        System.out.println(LocalDateTime.now());
        HoSPersonType hoSPersonType = new HoSPersonType();
        String intygsId = "intyg-1";
        String intygsTyp = "fk7263";
        
        // TODO implement tests
        //NotificationRequestType request = NotificationMessageFactory.getNotificationRequestType(handelseTidpunkt, hoSPersonType, intygsId, intygsTyp);
        NotificationRequestType request =  null;
        assertNotNull(request);
        assertEquals(request.getHandelseTidpunkt().getMillisOfSecond(), 123);
    }

}
