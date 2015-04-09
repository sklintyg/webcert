package se.inera.webcert.integration.validator;

import iso.v21090.dt.v1.II;
import org.apache.commons.lang.StringUtils;
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.intyg.common.schemas.Constants;
import se.inera.webcert.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;

import java.util.ArrayList;
import java.util.List;

public final class QuestionAnswerValidator {

    private QuestionAnswerValidator() {
    }

    public static List<String> validate(ReceiveMedicalCertificateAnswerType request) {
        List<String> messages = new ArrayList<>();
        if (request.getAnswer().getSvar() == null) {
            messages.add("Missing svar element.");
        }

        LakarutlatandeEnkelType lakarutlatande = request.getAnswer().getLakarutlatande();
        validateLakarutlatande(messages, lakarutlatande);

        HosPersonalType hosPersonal = request.getAnswer().getAdressVard().getHosPersonal();
        validateHosPersonal(messages, hosPersonal);
        validateEnhet(messages, hosPersonal);
        validateVardgivare(messages, hosPersonal);

        PatientType patient = lakarutlatande.getPatient();
        validatePatient(messages, patient);
        return messages;
    }

    public static List<String> validate(ReceiveMedicalCertificateQuestionType request) {
        List<String> messages = new ArrayList<>();
        if (request.getQuestion().getFraga() == null) {
            messages.add("Missing fraga element.");
        }

        LakarutlatandeEnkelType lakarutlatande = request.getQuestion().getLakarutlatande();
        validateLakarutlatande(messages, lakarutlatande);

        HosPersonalType hosPersonal = request.getQuestion().getAdressVard().getHosPersonal();
        validateHosPersonal(messages, hosPersonal);
        validateEnhet(messages, hosPersonal);
        validateVardgivare(messages, hosPersonal);

        PatientType patient = lakarutlatande.getPatient();
        validatePatient(messages, patient);
        return messages;
    }

    private static void validateLakarutlatande(List<String> messages, LakarutlatandeEnkelType lakarutlatande) {
        if (StringUtils.isEmpty(lakarutlatande.getLakarutlatandeId())) {
            messages.add("Intygsid är tom eller saknas");
        }
    }

    private static void validateHosPersonal(List<String> messages, HosPersonalType hosPersonal) {
        II personalId = hosPersonal.getPersonalId();
        if (!Constants.HSA_ID_OID.equals(personalId.getRoot())) {
            messages.add("Felaktig root pa personalid");
        }
        if (StringUtils.isEmpty(personalId.getExtension())) {
            messages.add("Extension på personalid ar tom eller saknas");
        }
        if (StringUtils.isEmpty(hosPersonal.getFullstandigtNamn())) {
            messages.add("Personalnamn ar tom eller saknas");
        }
    }

    private static void validatePatient(List<String> messages, PatientType patient) {
        if (!Constants.PERSON_ID_OID.equals(patient.getPersonId().getRoot())) {
            messages.add("Felaktig root på personid");
        }
        if (StringUtils.isEmpty(patient.getPersonId().getExtension())) {
            messages.add("Extension på personid ar tom eller saknas");
        }
        if (StringUtils.isEmpty(patient.getFullstandigtNamn())) {
            messages.add("Personnamn saknas eller ar tomt");
        }
    }

    private static void validateEnhet(List<String> messages, HosPersonalType hosPersonal) {
        if (!Constants.HSA_ID_OID.equals(hosPersonal.getEnhet().getEnhetsId().getRoot())) {
            messages.add("Felaktig enhetsid root");
        }
        if (StringUtils.isEmpty(hosPersonal.getEnhet().getEnhetsId().getExtension())) {
            messages.add("Enhetsid extension ar tom eller saknas");
        }
        if (StringUtils.isEmpty(hosPersonal.getEnhet().getEnhetsnamn())) {
            messages.add("Enhetsnamn ar tom eller saknas");
        }
    }

    private static void validateVardgivare(List<String> messages, HosPersonalType hosPersonal) {
        if (!Constants.HSA_ID_OID.equals(hosPersonal.getEnhet().getVardgivare().getVardgivareId().getRoot())) {
            messages.add("Felaktig vardgivareid root");
        }
        if (StringUtils.isEmpty(hosPersonal.getEnhet().getVardgivare().getVardgivareId().getExtension())) {
            messages.add("VardgivareId extension ar tom eller saknas");
        }
        if (StringUtils.isEmpty(hosPersonal.getEnhet().getVardgivare().getVardgivarnamn())) {
            messages.add("Vardgivarenamn ar tom eller saknas");
        }
    }
}
