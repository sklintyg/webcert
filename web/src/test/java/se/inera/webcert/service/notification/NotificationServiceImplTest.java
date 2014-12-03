package se.inera.webcert.service.notification;

import org.joda.time.LocalDateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import se.inera.log.messages.IntygReadMessage;
import se.inera.webcert.notifications.message.v1.*;

import javax.jms.Session;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * Created by Magnus Ekstrand on 03/12/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceImplTest {

    String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><NotificationRequest xmlns=\"urn:inera:webcert:notifications:1\"><handelse>INTYGSUTKAST_ANDRAT</handelse><handelseTidpunkt>2001-12-31T12:00:00</handelseTidpunkt><intygsId>22334455</intygsId><intygsTyp>FK7263</intygsTyp><hoSPerson><hsaId>SE1234567-0987654321</hsaId><fullstandigtNamn>Karl Karlsson</fullstandigtNamn><vardenhet><hsaId>SE1234567-E000890</hsaId><enhetsNamn>SuperEnheten</enhetsNamn></vardenhet></hoSPerson></NotificationRequest>";

    @Mock
    private JmsTemplate template = mock(JmsTemplate.class);

    @InjectMocks
    NotificationServiceImpl notificationService = new NotificationServiceImpl();

    @Test
    public void marshalNotificationRequestType() throws Exception {

        NotificationRequestType notificationRequestType = createNotificationRequestType(HandelseType.INTYGSUTKAST_ANDRAT);
        NotificationServiceImpl.NotificationMessageCreator obj = new NotificationServiceImpl.NotificationMessageCreator(notificationRequestType);

        assertEquals(expected, obj.objToString());
    }

    @Test
    public void serviceNotifiesThereIsAChangedCertificateDraft() throws Exception {

        ArgumentCaptor<MessageCreator> messageCreatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        NotificationRequestType notificationRequestType = createNotificationRequestType(HandelseType.INTYGSUTKAST_ANDRAT);
        notificationService.notify(notificationRequestType);

        verify(template, only()).send(messageCreatorCaptor.capture());

        Session session = mock(Session.class);

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(session.createObjectMessage(stringArgumentCaptor.capture())).thenReturn(null);

        MessageCreator messageCreator = messageCreatorCaptor.getValue();
        messageCreator.createMessage(session);

        assertEquals(expected, stringArgumentCaptor.getValue());
    }

    NotificationRequestType createNotificationRequestType(HandelseType handelseType) {

        ObjectFactory of = new ObjectFactory();

        VardenhetType vardenhetType = of.createVardenhetType();
        vardenhetType.setHsaId("SE1234567-E000890");
        vardenhetType.setEnhetsNamn("SuperEnheten");

        HoSPersonType hoSPersonType = of.createHoSPersonType();
        hoSPersonType.setHsaId("SE1234567-0987654321");
        hoSPersonType.setFullstandigtNamn("Karl Karlsson");
        hoSPersonType.setVardenhet(vardenhetType);

        NotificationRequestType notificationRequestType = of.createNotificationRequestType();
        notificationRequestType.setHandelse(handelseType);
        notificationRequestType.setHandelseTidpunkt(new LocalDateTime(2001, 12, 31, 12, 0));
        notificationRequestType.setHoSPerson(hoSPersonType);
        notificationRequestType.setIntygsId("22334455");
        notificationRequestType.setIntygsTyp("FK7263");

        return notificationRequestType;
    }

}
