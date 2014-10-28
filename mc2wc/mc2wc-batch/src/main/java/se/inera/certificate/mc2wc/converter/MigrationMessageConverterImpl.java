package se.inera.certificate.mc2wc.converter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.certificate.mc2wc.medcert.jpa.model.AddressCare;
import se.inera.certificate.mc2wc.medcert.jpa.model.Answer;
import se.inera.certificate.mc2wc.medcert.jpa.model.Certificate;
import se.inera.certificate.mc2wc.medcert.jpa.model.Complement;
import se.inera.certificate.mc2wc.medcert.jpa.model.Patient;
import se.inera.certificate.mc2wc.medcert.jpa.model.Question;
import se.inera.certificate.mc2wc.medcert.jpa.model.State;
import se.inera.certificate.mc2wc.medcert.jpa.model.Subject;
import se.inera.certificate.mc2wc.message.AnswerType;
import se.inera.certificate.mc2wc.message.CareGiverType;
import se.inera.certificate.mc2wc.message.CarePersonType;
import se.inera.certificate.mc2wc.message.CareUnitType;
import se.inera.certificate.mc2wc.message.CertificateType;
import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.PatientType;
import se.inera.certificate.mc2wc.message.QuestionOriginatorType;
import se.inera.certificate.mc2wc.message.QuestionSubjectType;
import se.inera.certificate.mc2wc.message.QuestionType;
import se.inera.certificate.mc2wc.message.StatusType;
import se.inera.certificate.mc2wc.message.SupplementType;

public class MigrationMessageConverterImpl implements MigrationMessageConverter {

    private static final String INTYGS_TYP = "fk7263";

    private static Logger log = LoggerFactory.getLogger(MigrationMessageConverter.class);

    private static final List<State> UNMIGRATABLE_QUESTION_STATES = Arrays.asList(State.CREATED, State.EDITED, State.PRINTED);

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.certificate.mc2wc.converter.MigrationMessageConverter#
     * toMigrationMessage(se.inera.certificate.mc2wc.jpa.model.Certificate,
     * boolean)
     */
    @Override
    public MigrationMessage toMigrationMessage(Certificate cert, String sender) {

        log.info("Converting Certificate {} to MigrationMessage", cert.getId());

        MigrationMessage migrationMessage = new MigrationMessage();
        migrationMessage.setCertificateId(cert.getId());
        migrationMessage.setCertificateOrigin(cert.getOrigin().toString());
        migrationMessage.setCertificateState(cert.getState().toString());

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
            if (!isQuestionToBeMigrated(mcQuestion)) {
                log.info("Question {}, belonging to certificate {} will not be migrated since it either has state {} or lacks subject or text",
                        new Object[] { mcQuestion.getId(), mcCert.getId(), mcQuestion.getState() });
                continue;
            }
            QuestionType wcQuestionAnswer = toWCQuestionAnswer(mcCert.getId(), mcCert.getSignedAt(), mcQuestion);
            msg.getQuestions().add(wcQuestionAnswer);
        }

        log.info("Added {} questions of {} to certificate {}", new Object[] { msg.getQuestions().size(), questions.size(), msg.getCertificateId() });
    }

    private boolean isQuestionToBeMigrated(Question q) {

        if (UNMIGRATABLE_QUESTION_STATES.contains(q.getState())) {
            log.debug("Question {} has state {}", q.getId(), q.getState());
            return false;
        }

        if (q.getSubject() == null || StringUtils.isBlank(q.getText())) {
            log.debug("Question {} is missing subject or text", q.getId());
            return false;
        }

        return true;
    }

    private boolean hasCertAnyContents(Certificate cert) {
        return ArrayUtils.isNotEmpty(cert.getDocument());
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

    private QuestionType toWCQuestionAnswer(String certificateId, Date certificateSignedDate, Question mcQuestion) {

        log.debug("Converting Question {}, part of Certificate {}", mcQuestion.getId(), certificateId);

        QuestionType qa = new QuestionType();

        qa.setCertificateId(certificateId);
        qa.setCertificateType(INTYGS_TYP);
        qa.setExternalReference(mcQuestion.getFkReferenceId());
        qa.setCertificateSigned(toLocalDateTime(certificateSignedDate));

        qa.setQuestionLastAnswerDate(toLocalDate(mcQuestion.getLastDateForAnswer()));
        qa.setSent(toLocalDateTime(mcQuestion.getSentAt()));
        qa.setSigned(toLocalDateTime(mcQuestion.getTextSignedAt()));

        qa.setOriginator(toQuestionOriginatorType(mcQuestion.getOriginator()));
        qa.setStatus(toStatusType(mcQuestion.getState()));
        qa.setSubject(toQuestionSubject(mcQuestion.getSubject()));

        qa.setQuestionText(mcQuestion.getText());
        qa.setCaption(limitLength(mcQuestion.getCaption(), 254));

        qa.setPatient(toPatient(mcQuestion.getPatient()));
        qa.setCarePerson(toCarePerson(mcQuestion.getAddressCare()));
        
        if (mcQuestion.getAddressFk() != null) {
            qa.setExternalContacts(mcQuestion.getAddressFk().getContact());
        }
        
        if (mcQuestion.getAnswer() != null) {
            qa.setAnswer(toAnswer(mcQuestion.getAnswer()));
        }

        addComplements(qa, mcQuestion.getComplements());
        
        return qa;
    }

    private void addComplements(QuestionType qa, Set<Complement> complements) {

        if (complements == null || complements.isEmpty()) {
            return;
        }

        for (Complement mcComplement : complements) {
            SupplementType st = new SupplementType();
            st.setField(mcComplement.getFalt());
            st.setText(mcComplement.getText());
            qa.getSupplements().add(st);
        }

        log.debug("Added {} supplements for question", qa.getSupplements().size());
    }

    private String limitLength(String in, int limit) {
        return StringUtils.left(in, limit);
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
