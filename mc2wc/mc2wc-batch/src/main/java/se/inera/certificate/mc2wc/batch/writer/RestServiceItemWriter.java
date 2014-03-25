package se.inera.certificate.mc2wc.batch.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import se.inera.certificate.mc2wc.exception.AbstractCertificateMigrationException;
import se.inera.certificate.mc2wc.exception.FatalCertificateMigrationException;
import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.MigrationReply;
import se.inera.certificate.mc2wc.message.MigrationResultType;
import se.inera.certificate.mc2wc.message.QuestionType;
import se.inera.certificate.mc2wc.rest.MigrationReceiver;

import java.util.List;

public class RestServiceItemWriter implements ItemWriter<MigrationMessage> {

    static final String CERTIFICATE_WRITE_COUNT = "certificateWriteCount";
    static final String QUESTION_WRITE_COUNT = "questionWriteCount";
    static final String ANSWER_WRITE_COUNT = "answerWriteCount";
    static final String DUPLICATE_COUNT = "duplicateCount";
    private static final long MAX_DUPLICATE_COUNT = 100;
    private static Logger logger = LoggerFactory.getLogger(RestServiceItemWriter.class);
    @Autowired
    @Qualifier("migrationMessageReceiverService")
    private MigrationReceiver migrationReceiver;

    @Value("#{jobParameters['dryRun']}")
    private String dryRunValue;

    private StepExecution stepExecution;

    @Override
    public void write(List<? extends MigrationMessage> messages) throws Exception {

        logger.debug("Got list with {} MigrationMessages", messages.size());

        for (MigrationMessage migrationMessage : messages) {
            send(migrationMessage);
        }
    }

    void send(MigrationMessage migrationMessage) throws AbstractCertificateMigrationException {

        logger.debug("Sending MigrationMessage");

        ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
        long certificateWriteCount = executionContext.getLong(CERTIFICATE_WRITE_COUNT, 0);
        long questionWriteCount = executionContext.getLong(QUESTION_WRITE_COUNT, 0);
        long answerWriteCount = executionContext.getLong(ANSWER_WRITE_COUNT, 0);
        long duplicateCount = executionContext.getLong(DUPLICATE_COUNT, 0);

        MigrationReply reply = null;
        if (Boolean.parseBoolean(dryRunValue)) {
            reply = new MigrationReply();
            reply.setResult(MigrationResultType.OK);
            logger.info("Running in dryRun-mode, not sending to receiver.");
        } else {
            reply = migrationReceiver.receive(migrationMessage);
        }

        switch (reply.getResult()) {
            case DUPLICATE:
                logger.warn("Received duplicate response for CertificateId: {}", migrationMessage.getCertificateId());
                duplicateCount++;
                if (duplicateCount > MAX_DUPLICATE_COUNT) {
                    logger.error("Max duplicates reached, failing batch.");
                    throw new FatalCertificateMigrationException("Too many duplicates!");
                }
                break;
            case ERROR:
                throw new FatalCertificateMigrationException("Received error from receiver application!");
            default:
                if (migrationMessage.getCertificate() != null) {
                    certificateWriteCount++;
                }
                for (QuestionType question : migrationMessage.getQuestions()) {
                    questionWriteCount++;
                    if (question.getAnswer() != null) {
                        answerWriteCount++;
                    }
                }
        }

        executionContext.putLong(CERTIFICATE_WRITE_COUNT, certificateWriteCount);
        executionContext.putLong(QUESTION_WRITE_COUNT, questionWriteCount);
        executionContext.putLong(ANSWER_WRITE_COUNT, answerWriteCount);
        executionContext.putLong(DUPLICATE_COUNT, duplicateCount);
    }

    @BeforeStep
    public void setStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    public void setMigrationReceiver(MigrationReceiver migrationReceiver) {
        this.migrationReceiver = migrationReceiver;
    }

    public void setDryRunValue(String dryRunValue) {
        this.dryRunValue = dryRunValue;
    }
}
