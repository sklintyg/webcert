package se.inera.webcert.service.fragasvar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;


/**
 * Created by pehr on 10/7/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-services-cxf-servlet.xml"})
@ActiveProfiles("dev")
public class SendQuestionToFKTest {


    @Autowired
    SendMedicalCertificateQuestionResponderInterface sendQuestionToFKClient;

    @Before
    public void setup(){

    }

    @Test
    @Ignore
    public void sendQuestionTest(){
        SendMedicalCertificateQuestionType sendType = new SendMedicalCertificateQuestionType();
        QuestionToFkType q2fk = new QuestionToFkType();
        sendType.setQuestion(q2fk);
        SendMedicalCertificateQuestionResponseType resp =  sendQuestionToFKClient.sendMedicalCertificateQuestion(null, sendType);
        Assert.assertTrue(resp.getResult().getResultCode().equals(ResultCodeEnum.OK));
    }

}
