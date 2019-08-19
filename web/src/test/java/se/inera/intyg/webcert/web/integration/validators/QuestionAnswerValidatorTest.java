/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.validators;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBElement;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.AnswerFromFkType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.webcert.web.integration.interactions.receivemedicalcertificate.QuestionAnswerValidator;

/**
 * Created by marced on 05/08/16.
 */
public class QuestionAnswerValidatorTest {

    @Test
    public void testQVPassesValidRequest() throws Exception {
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        assertEquals(0, QuestionAnswerValidator.validate(request).size());
    }

    @Test
    public void testQVCatchesMissingFraga() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().setFraga(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingAmne() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().setAmne(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingLakarutlatandeId() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getLakarutlatande().setLakarutlatandeId(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingHsaPersonIdRoot() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getPersonalId().setRoot(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesInvalidHsaPersonIdRoot() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getPersonalId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingHsaPersonIdExtension() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getPersonalId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingHsaPersonFullstandigtNamn() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().setFullstandigtNamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesInvalidPatientPersonIdRoot() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getLakarutlatande().getPatient().getPersonId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingPatientPersonIdExtension() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getLakarutlatande().getPatient().getPersonId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingPatientPersonFullstandigtNamn() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getLakarutlatande().getPatient().setFullstandigtNamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesInvalidHsaEnhetIdRoot() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingHsaEnhetIdExtension() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingHsaEnhetsNamn() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getEnhet().setEnhetsnamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesInvalidVardgivarIdRoot() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingVardgivarIdExtension() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingVardgivarNamn() throws Exception {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getEnhet().getVardgivare().setVardgivarnamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    // --------- Answer tests
    @Test
    public void testAVPassesValidRequest() throws Exception {
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        assertEquals(0, QuestionAnswerValidator.validate(request).size());
    }

    @Test
    public void testAVCatchesMissingFraga() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().setSvar(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingAmne() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().setAmne(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingLakarutlatandeId() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getLakarutlatande().setLakarutlatandeId(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingHsaPersonIdRoot() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getPersonalId().setRoot(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesInvalidHsaPersonIdRoot() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getPersonalId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingHsaPersonIdExtension() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getPersonalId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingHsaPersonFullstandigtNamn() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().setFullstandigtNamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesInvalidPatientPersonIdRoot() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getLakarutlatande().getPatient().getPersonId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingPatientPersonIdExtension() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getLakarutlatande().getPatient().getPersonId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingPatientPersonFullstandigtNamn() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getLakarutlatande().getPatient().setFullstandigtNamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesInvalidHsaEnhetIdRoot() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingHsaEnhetIdExtension() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingHsaEnhetsNamn() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getEnhet().setEnhetsnamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesInvalidVardgivarIdRoot() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingVardgivarIdExtension() throws Exception {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingVardgivarNamn() throws Exception  {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getEnhet().getVardgivare().setVardgivarnamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    private ReceiveMedicalCertificateAnswerType createValidAnswerRequest() throws IOException {
        ClassPathResource resource = new ClassPathResource("QuestionAnswerValidator/valid-answer-from-fk.xml");
        JAXBElement<AnswerFromFkType> jaxbElement = XmlMarshallerHelper.unmarshal(resource.getInputStream());

        ReceiveMedicalCertificateAnswerType request = new ReceiveMedicalCertificateAnswerType();
        request.setAnswer(jaxbElement.getValue());

        return request;
    }

    private ReceiveMedicalCertificateQuestionType createValidQuestionRequest() throws IOException {
        ClassPathResource resource = new ClassPathResource("QuestionAnswerValidator/valid-question-from-fk.xml");
        JAXBElement<QuestionFromFkType> jaxbElement = XmlMarshallerHelper.unmarshal(resource.getInputStream());

        ReceiveMedicalCertificateQuestionType request = new ReceiveMedicalCertificateQuestionType();
        request.setQuestion(jaxbElement.getValue());

        return request;
    }

}
