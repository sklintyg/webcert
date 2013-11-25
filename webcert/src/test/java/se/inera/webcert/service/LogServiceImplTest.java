package se.inera.webcert.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.*;

import se.inera.log.messages.IntygReadMessage;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.web.service.WebCertUserService;
import se.inera.webcert.web.service.WebCertUserServiceImpl;

import javax.jms.*;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

/**
 * Created by pehr on 13/11/13.
 */
@RunWith(MockitoJUnitRunner.class)
public class LogServiceImplTest {

    @Mock
    private JmsTemplate template = mock(JmsTemplate.class);


    @Mock
    protected WebCertUserService webCertUserService = new WebCertUserServiceImpl();

    @InjectMocks
    LogServiceImpl logService = new LogServiceImpl();



    @Test
    public void serviceSendsDocumentAndIdForCreate() throws JMSException {
        ArgumentCaptor<MessageCreator> captor = ArgumentCaptor.forClass(MessageCreator.class);

        //Certificate certificate = mock(Certificate.class);
        //when(certificate.getDocument()).thenReturn("The document");
        //when(certificate.getId()).thenReturn("The id");

        when(webCertUserService.getWebCertUser()).thenReturn(createWcUser()) ;

        GetCertificateForCareResponseType intyg = mock(GetCertificateForCareResponseType.class);
        UtlatandeType utlatande = mock(UtlatandeType.class);
        HosPersonalType persType = mock(HosPersonalType.class);
        EnhetType enhetTyp = mock(EnhetType.class);
        VardgivareType vgType = mock(VardgivareType.class);
        HsaId enhetHsaId = mock(HsaId.class);
        HsaId vgHsaId = mock(HsaId.class);



        when(vgHsaId.getExtension()).thenReturn("VARDGIVARE ID");
        when(vgType.getVardgivareId()).thenReturn(vgHsaId);
        when(enhetTyp.getVardgivare()).thenReturn(vgType);

        when(enhetHsaId.getExtension()).thenReturn("ENHETS ID");
        when(enhetTyp.getEnhetsId()).thenReturn(enhetHsaId);
        when(persType.getEnhet()).thenReturn(enhetTyp);
        when(utlatande.getSkapadAv()).thenReturn(persType);
        when(intyg.getCertificate()).thenReturn(utlatande);

        IntygReadMessage logthis = mock(IntygReadMessage.class);

        ObjectMessage message = mock(ObjectMessage.class);
        Session session = mock(Session.class);
        //when(session.createObjectMessage(any(IntygReadMessage.class))).thenReturn(message);


        logService.logReadOfIntyg(intyg);

        verify(template, only()).send(captor.capture());
        captor.getValue().createMessage(session);

        IntygReadMessage msg = (IntygReadMessage)message.getObject();
    }




    protected WebCertUser createWcUser(){
        WebCertUser wcu = new WebCertUser();
        wcu.setHsaId("HSAID");
        wcu.setNamn("TESTUSER TESTSSSON");


        return wcu;
    }
}
