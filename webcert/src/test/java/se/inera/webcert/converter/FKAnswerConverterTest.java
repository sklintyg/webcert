package se.inera.webcert.converter;

import org.junit.Assert;
import org.junit.Test;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.springframework.core.io.ClassPathResource;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.AnswerFromFkType;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 * Created by pehr on 10/3/13.
 */
public class FKAnswerConverterTest {

    private QuestionFromFkType question() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(QuestionFromFkType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource("FragaSvarConverterTest/question.xml").getInputStream()),
                QuestionFromFkType.class).getValue();
    }

    private AnswerToFkType answerToFK() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(AnswerToFkType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource("FragaSvarConverterTest/answer_to_fk.xml").getInputStream()),
                AnswerToFkType.class).getValue();
    }

    @Test
    public void testConvertQuestion() throws Exception {

        QuestionFromFkType fkQuestion = question();
        AnswerToFkType answer2 = answerToFK();

        FragaSvar fragaSvar = new FragaSvarConverter().convert(fkQuestion);
        fragaSvar.setInternReferens(321L);
        fragaSvar.setSvarsText(fragaSvar.getFrageText());
        fragaSvar.setSvarSigneringsDatum(fragaSvar.getFrageSigneringsDatum());
        AnswerToFkType convertedAnswer = FKAnswerConverter.convert(fragaSvar);

        Assert.assertTrue(answer2.equals(null,null,convertedAnswer, JAXBEqualsStrategy.INSTANCE));
    }

}
