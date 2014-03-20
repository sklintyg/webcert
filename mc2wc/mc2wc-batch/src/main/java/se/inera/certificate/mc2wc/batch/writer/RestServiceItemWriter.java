package se.inera.certificate.mc2wc.batch.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import se.inera.certificate.mc2wc.exception.AbstractCertificateMigrationException;
import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.MigrationReply;
import se.inera.certificate.mc2wc.message.QuestionType;
import se.inera.certificate.mc2wc.rest.MigrationReceiver;

import java.util.List;

public class RestServiceItemWriter implements ItemWriter<MigrationMessage>, StepExecutionListener {

    private static Logger log = LoggerFactory.getLogger(RestServiceItemWriter.class);

    private StepExecution stepExecution;

    @Autowired
    @Qualifier("migrationMessageReceiverService")
    private MigrationReceiver migrationReceiver;

    @Override
    public void write(List<? extends MigrationMessage> messages) throws Exception {

        log.debug("Got list with {} MigrationMessages", messages.size());

        for (MigrationMessage migrationMessage : messages) {
            send(migrationMessage);
        }

    }

    private void send(MigrationMessage migrationMessage) throws AbstractCertificateMigrationException {

        log.debug("Sending MigrationMessage");

        ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
        long certificateWriteCount = executionContext.getLong("certificateWriteCount", 0);
        long questionWriteCount = executionContext.getLong("questionWriteCount", 0);
        long answerWriteCount = executionContext.getLong("answerWriteCount", 0);

        MigrationReply reply = migrationReceiver.receive(migrationMessage);

        if (migrationMessage.getCertificate() != null) {
            certificateWriteCount++;
        }
        for (QuestionType question : migrationMessage.getQuestions()) {
            questionWriteCount++;
            if (question.getAnswer() != null) {
                answerWriteCount++;
            }
        }

        executionContext.putLong("certificateWriteCount", certificateWriteCount);
        executionContext.putLong("questionWriteCount", questionWriteCount);
        executionContext.putLong("answerWriteCount", answerWriteCount);
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return this.stepExecution.getExitStatus();
    }
}
