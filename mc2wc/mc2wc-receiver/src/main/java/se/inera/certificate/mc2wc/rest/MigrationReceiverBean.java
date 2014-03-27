package se.inera.certificate.mc2wc.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.certificate.mc2wc.message.*;
import se.inera.certificate.mc2wc.service.MigrationService;
import se.inera.certificate.mc2wc.service.MigrationServiceException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response.Status;

public class MigrationReceiverBean implements MigrationReceiver {

    private static Logger logger = LoggerFactory.getLogger(MigrationReceiverBean.class);

    @Autowired
    private MigrationService migrationService;

    @PersistenceContext
    private EntityManager entityManager;

    public PingResponse ping(PingRequest request) {
        return new PingResponse();
    }

    @Override
    public StatisticsResponse getStatistics(StatisticsRequest request) {

        Query certificateQuery = entityManager.createQuery("select count(id) from MigreratMedcertIntyg where migreradFran = :sender");
        certificateQuery.setParameter("sender", request.getSender());
        Long certificateCount = (Long) certificateQuery.getSingleResult();

        Query questionQuery = entityManager.createQuery("select count(id) from FragaSvar where vardperson.vardgivarId in :careGiverIds");
        questionQuery.setParameter("careGiverIds", request.getCareGiverIds());
        Long questionCount = (Long) questionQuery.getSingleResult();

        Query answerQuery = entityManager.createQuery("select count(id) from FragaSvar where vardperson.vardgivarId in :careGiverIds and svarsText is not null");
        answerQuery.setParameter("careGiverIds", request.getCareGiverIds());
        Long answerCount = (Long) answerQuery.getSingleResult();

        StatisticsResponse response = new StatisticsResponse();

        response.setCertificateCount(certificateCount);
        response.setQuestionCount(questionCount);
        response.setAnswerCount(answerCount);

        return response;
    }

    public MigrationReply receive(MigrationMessage message) {
        try {
            MigrationResultType result = migrationService.processMigrationMessage(message);

            MigrationReply reply = new MigrationReply();
            reply.setResult(result);

            return reply;
        } catch (MigrationServiceException e) {
            logger.error("Could not store certificate with id {}" + message.getCertificateId());
            throw new ServerErrorException(Status.INTERNAL_SERVER_ERROR, e);
        }
    }
}