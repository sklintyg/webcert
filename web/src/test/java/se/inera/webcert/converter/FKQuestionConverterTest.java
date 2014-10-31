package se.inera.webcert.converter;

import org.junit.Assert;
import org.junit.Test;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.springframework.core.io.ClassPathResource;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.QuestionToFkType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 * Created by pehr on 10/3/13.
 */
public class FKQuestionConverterTest {

    private QuestionFromFkType question() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(QuestionFromFkType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource("FragaSvarConverterTest/question.xml").getInputStream()),
                QuestionFromFkType.class).getValue();
    }

    private QuestionToFkType questionToFK() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(QuestionToFkType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource("FragaSvarConverterTest/question_to_fk.xml").getInputStream()),
                QuestionToFkType.class).getValue();
    }

    @Test
    public void testConvertQuestion() throws Exception {

        QuestionFromFkType fkQuestion = question();
        QuestionToFkType q2 = questionToFK();

        FragaSvar fragaSvar = new FragaSvarConverter().convert(fkQuestion);
        fragaSvar.setAmne(Amne.KONTAKT);

        fragaSvar.setInternReferens(321L);
        QuestionToFkType convertedAnswer = FKQuestionConverter.convert(fragaSvar);

        Assert.assertTrue(q2.equals(null,null,convertedAnswer, JAXBEqualsStrategy.INSTANCE));
        //Assert.assertSame(q2, convertedAnswer);
    }

}
