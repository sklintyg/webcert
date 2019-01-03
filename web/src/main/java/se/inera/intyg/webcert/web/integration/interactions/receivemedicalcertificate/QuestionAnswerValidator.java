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
package se.inera.intyg.webcert.web.integration.interactions.receivemedicalcertificate;

import com.google.common.base.Strings;
import iso.v21090.dt.v1.II;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.Amnetyp;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.intyg.common.support.Constants;

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
        validateAmne(messages, request.getAnswer().getAmne());

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
        validateAmne(messages, request.getQuestion().getAmne());

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
        if (Strings.isNullOrEmpty(lakarutlatande.getLakarutlatandeId())) {
            messages.add("Intygsid är tom eller saknas");
        }
    }

    private static void validateAmne(List<String> messages, Amnetyp amne) {
        if (amne == null) {
            messages.add("Amne är felaktigt");
        }
    }

    private static void validateHosPersonal(List<String> messages, HosPersonalType hosPersonal) {
        II personalId = hosPersonal.getPersonalId();
        if (!Constants.HSA_ID_OID.equals(personalId.getRoot())) {
            messages.add("Felaktig root pa personalid");
        }
        if (Strings.isNullOrEmpty(personalId.getExtension())) {
            messages.add("Extension på personalid ar tom eller saknas");
        }
        if (Strings.isNullOrEmpty(hosPersonal.getFullstandigtNamn())) {
            messages.add("Personalnamn ar tom eller saknas");
        }
    }

    private static void validatePatient(List<String> messages, PatientType patient) {
        if (!Constants.SAMORDNING_ID_OID.equals(patient.getPersonId().getRoot())
                && !Constants.PERSON_ID_OID.equals(patient.getPersonId().getRoot())) {
            messages.add("Felaktig root på personid");
        }
        if (Strings.isNullOrEmpty(patient.getPersonId().getExtension())) {
            messages.add("Extension på personid ar tom eller saknas");
        }
        if (Strings.isNullOrEmpty(patient.getFullstandigtNamn())) {
            messages.add("Personnamn saknas eller ar tomt");
        }
    }

    private static void validateEnhet(List<String> messages, HosPersonalType hosPersonal) {
        if (!Constants.HSA_ID_OID.equals(hosPersonal.getEnhet().getEnhetsId().getRoot())) {
            messages.add("Felaktig enhetsid root");
        }
        if (Strings.isNullOrEmpty(hosPersonal.getEnhet().getEnhetsId().getExtension())) {
            messages.add("Enhetsid extension ar tom eller saknas");
        }
        if (Strings.isNullOrEmpty(hosPersonal.getEnhet().getEnhetsnamn())) {
            messages.add("Enhetsnamn ar tom eller saknas");
        }
    }

    private static void validateVardgivare(List<String> messages, HosPersonalType hosPersonal) {
        if (!Constants.HSA_ID_OID.equals(hosPersonal.getEnhet().getVardgivare().getVardgivareId().getRoot())) {
            messages.add("Felaktig vardgivareid root");
        }
        if (Strings.isNullOrEmpty(hosPersonal.getEnhet().getVardgivare().getVardgivareId().getExtension())) {
            messages.add("VardgivareId extension ar tom eller saknas");
        }
        if (Strings.isNullOrEmpty(hosPersonal.getEnhet().getVardgivare().getVardgivarnamn())) {
            messages.add("Vardgivarenamn ar tom eller saknas");
        }
    }
}
