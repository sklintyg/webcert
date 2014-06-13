package se.inera.certificate.mc2wc.converter;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.certificate.mc2wc.medcert.jpa.model.*;
import se.inera.certificate.mc2wc.message.*;

import java.util.Date;
import java.util.Set;

public class MigrationMessageConverterImpl implements MigrationMessageConverter {

    private static final String INTYGS_TYP = "FK7263";
    private static Logger log = LoggerFactory.getLogger(MigrationMessageConverter.class);

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.certificate.mc2wc.converter.MigrationMessageConverter#
     * toMigrationMessage(se.inera.certificate.mc2wc.jpa.model.Certificate,
     * boolean)
     */
    @Override
    public MigrationMessage toMigrationMessage(Certificate cert, String sender) {
                
        MigrationMessage migrationMessage = new MigrationMessage();
        migrationMessage.setCertificateId(cert.getId());

        if (hasCertAnyContents(cert)) {
            CertificateType wcCert = toWCCertificate(cert, sender);
            migrationMessage.setCertificate(wcCert);
        }
        
        if (hasCertAnyQuestions(cert)) {
            addQuestionsToMigrationMessage(migrationMessage, cert);
        }

        return migrationMessage;
    }

    private void addQuestionsToMigrationMessage(MigrationMessage msg, Certificate mcCert) {
        Set<Question> questions = mcCert.getQuestions();

        log.debug("Certificate {} has {} questions", mcCert.getId(), questions.size());

        for (Question mcQuestion : questions) {
            if (mcQuestion.getState() == State.CREATED || mcQuestion.getSubject() == null || mcQuestion.getText() == null) {
                log.info("Question {}, belonging to certificate {} will not be migrated since it either has state CREATED or lacks subject or text", mcQuestion.getId(), mcCert.getId());
                continue;
            }
            QuestionType wcQuestionAnswer = toWCQuestionAnswer(mcCert.getId(), mcQuestion);
            msg.getQuestions().add(wcQuestionAnswer);
        }
    }
    
    private boolean hasCertAnyContents(Certificate cert) {
        return (cert.getDocument() != null && cert.getDocument().length > 0);
    }
    
    private boolean hasCertAnyQuestions(Certificate cert) {
        return (cert.getQuestions() != null && cert.getQuestions().size() > 0);
    }
    
    private CertificateType toWCCertificate(Certificate mcCert, String sender) {

        log.debug("Converting the contents of Certificate {}", mcCert.getId());

        CertificateType wcCert = new CertificateType();

        wcCert.setCertificateId(mcCert.getId());
        wcCert.setCertificateType(INTYGS_TYP);
        wcCert.setCareUnitId(mcCert.getCareUnitId());
        wcCert.setOrigin(mcCert.getOrigin().toString());

        wcCert.setCreated(toLocalDateTime(mcCert.getCreatedAt()));
        wcCert.setSent(toLocalDateTime(mcCert.getSentAt()));
        wcCert.setStatus(toStatusType(mcCert.getState()));

        wcCert.setContents(mcCert.getDocument());

        PatientType wcPatient = new PatientType();
        wcPatient.setFullName(mcCert.getPatientName());
        wcPatient.setPersonId(mcCert.getPatientSsn());
        wcCert.setPatient(wcPatient);

        wcCert.setMigratedFrom(sender);

        return wcCert;
    }

    private QuestionType toWCQuestionAnswer(String certificateId, Question mcQuestion) {

        log.debug("Converting Question {}, part of Certificate {}", mcQuestion.getId(), certificateId);

        QuestionType qa = new QuestionType();

        qa.setCertificateId(certificateId);
        qa.setCertificateType(INTYGS_TYP);
        qa.setExternalReference(mcQuestion.getFkReferenceId());

        qa.setQuestionLastAnswerDate(toLocalDate(mcQuestion.getLastDateForAnswer()));
        qa.setSent(toLocalDateTime(mcQuestion.getSentAt()));
        qa.setSigned(toLocalDateTime(mcQuestion.getTextSignedAt()));

        qa.setOriginator(toQuestionOriginatorType(mcQuestion.getOriginator()));
        qa.setStatus(toStatusType(mcQuestion.getState()));
        qa.setSubject(toQuestionSubject(mcQuestion.getSubject()));

        qa.setQuestionText(mcQuestion.getText());
        qa.setCaption(mcQuestion.getCaption());

        qa.setPatient(toPatient(mcQuestion.getPatient()));
        qa.setCarePerson(toCarePerson(mcQuestion.getAddressCare()));

        if (mcQuestion.getAnswer() != null) {
            log.debug("Converting Answer for Question {}", mcQuestion.getId());
            qa.setAnswer(toAnswer(mcQuestion.getAnswer()));
        }

        return qa;
    }

    private LocalDate toLocalDate(Date theDate) {
        return (theDate != null) ? LocalDate.fromDateFields(theDate) : null;
    }

    private LocalDateTime toLocalDateTime(Date theDate) {
        return (theDate != null) ? LocalDateTime.fromDateFields(theDate) : null;
    }

    private CarePersonType toCarePerson(AddressCare addressCare) {

        CarePersonType cp = new CarePersonType();

        cp.setFullName(addressCare.getCarePersonName());
        cp.setPersonId(addressCare.getCarePersonId());
        cp.setPrescriptionCode(addressCare.getCarePersonCode());
        cp.setCareUnit(toCareUnit(addressCare));

        return cp;
    }

    private CareUnitType toCareUnit(AddressCare addressCare) {

        CareUnitType cu = new CareUnitType();

        cu.setId(addressCare.getCareUnitId());
        cu.setName(addressCare.getCareUnitName());
        cu.setWorkplaceCode(addressCare.getCareUnitWorkplaceCode());
        cu.setPhone(addressCare.getPhoneNumber());
        cu.setEmail(addressCare.getEmailAddress());
        cu.setPostalAddress(addressCare.getPostalAddress());
        cu.setPostalNumber(addressCare.getPostalNumber());
        cu.setPostalCity(addressCare.getPostalCity());

        CareGiverType cg = new CareGiverType();
        cg.setId(addressCare.getCareGiverId());
        cg.setName(addressCare.getCareGiverName());

        cu.setCareGiver(cg);

        return cu;
    }

    private PatientType toPatient(Patient patient) {

        PatientType pat = new PatientType();

        pat.setFullName(patient.getName());
        pat.setPersonId(patient.getSsn());

        return pat;
    }

    private AnswerType toAnswer(Answer answer) {

        if (answer == null) {
            return null;
        }

        log.debug("Converting Answer {}", answer.getId());

        AnswerType answerType = new AnswerType();

        answerType.setText(answer.getText());
        answerType.setStatus(toStatusType(answer.getState()));
        answerType.setSigned(toLocalDateTime(answer.getTextSignedAt()));
        answerType.setSent(toLocalDateTime(answer.getSentAt()));

        return answerType;
    }

    private QuestionSubjectType toQuestionSubject(Subject subject) {

        if (subject == null) {
            return null;
        }

        switch (subject) {
        case CONTACT:
            return QuestionSubjectType.CONTACT;
        case KOMPLEMENTING:
            return QuestionSubjectType.KOMPLEMENTING;
        case MAKULERING:
            return QuestionSubjectType.MAKULERING;
        case MEETING:
            return QuestionSubjectType.MEETING;
        case REMINDER:
            return QuestionSubjectType.REMINDER;
        case WORK_PROLONGING:
            return QuestionSubjectType.WORK_PROLONGING;
        case OTHER:
            return QuestionSubjectType.OTHER;
        default:
            return null;
        }
    }

    private StatusType toStatusType(State state) {

        switch (state) {
        case CREATED:
            return StatusType.CREATED;
        case EDITED:
            return StatusType.EDITED;
        case PRINTED:
            return StatusType.PRINTED;
        case SENT:
            return StatusType.SENT;
        case SIGNED:
            return StatusType.SIGNED;
        case SIGNED_AND_SENT:
            return StatusType.SIGNED_AND_SENT;
        case SENT_HANDLED:
            return StatusType.SENT_HANDLED;
        case SENT_UNHANDLED:
            return StatusType.SENT_UNHANDLED;
        default:
            return null;
        }
    }

    private QuestionOriginatorType toQuestionOriginatorType(String source) {

        if (source.equalsIgnoreCase("FK")) {
            return QuestionOriginatorType.FK;
        } else if (source.equalsIgnoreCase("CARE")) {
            return QuestionOriginatorType.CARE;
        }

        return null;
    }
}
