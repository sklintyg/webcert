package se.inera.webcert.converter;

import org.junit.Assert;
import org.junit.Test;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.springframework.core.io.ClassPathResource;

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 * This test makes use of Equals and HashCode from JAXB basics. All types must implement
 * this. 
 */
public class FKAnswerConverterTest {
	
	private FragaSvarConverter fragaSvarConverter = new FragaSvarConverter();

    private QuestionFromFkType inflateQuestionFromFK() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(QuestionFromFkType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource("FragaSvarConverterTest/question.xml").getInputStream()),
                QuestionFromFkType.class).getValue();
    }

    private AnswerToFkType inflateAnswerToFK() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(AnswerToFkType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource("FragaSvarConverterTest/answer_to_fk.xml").getInputStream()),
                AnswerToFkType.class).getValue();
    }

    @Test
    public void testConvertQuestion() throws Exception {

        QuestionFromFkType questionFromFK = inflateQuestionFromFK();
        AnswerToFkType referenceAnswerToFK = inflateAnswerToFK();
        
        // convert QuestionFromFK to FragaSvar entity
        FragaSvar fragaSvar = fragaSvarConverter.convert(questionFromFK);
        
        // add some data
        fragaSvar.setInternReferens(321L);
        fragaSvar.setSvarsText(fragaSvar.getFrageText());
        fragaSvar.setSvarSigneringsDatum(fragaSvar.getFrageSigneringsDatum());
        
        // convert FragaSvar entity to AnswerToFK
        AnswerToFkType convertedAnswerToFK = FKAnswerConverter.convert(fragaSvar);
        
        // compare converted answerToFK to reference answerToFK
        Assert.assertTrue(referenceAnswerToFK.equals(null,null,convertedAnswerToFK, JAXBEqualsStrategy.INSTANCE));
    }

}
