package se.inera.intyg.webcert.web.event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.persistence.event.model.CertificateEventFailedLoad;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventFailedLoadRepository;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@Service
public class CertificateEventLoaderConsumer implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateEventLoaderConsumer.class);

    @Autowired
    CertificateEventService service;

    @Autowired
    MonitoringLogService monitoringLogService;

    @Autowired
    UtkastRepository utkastRepository;

    @Autowired
    CertificateEventFailedLoadRepository failedLoadRepository;

//    @Autowired
//    @Qualifier("jmsCertificateEventLoaderTemplate")
//    private JmsTemplate jmsTemplate;


    @Override
    @JmsListener(destination = "${certificateeventloader.queue}")
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                ObjectMessage objMessage = (ObjectMessage) message;
                var list = (ArrayList) objMessage.getObject();
                processIds(list);
            }
        } catch (Exception e) {
            LOG.error("Could not process certificate event loader message: {}", e.getMessage());
        }
    }

    public void processIds(List<String> certificateIdList) {
        List<String> failedCertificates = new ArrayList<>();

        certificateIdList.forEach(id -> {
            try {
                service.getCertificateEvents(id);
            } catch (Exception e) {
                addToFailedCertificatesTable(id, e);
                failedCertificates.add(id);
            }
        });

        if (failedCertificates.size() < certificateIdList.size()) {
            monitoringLogService.logSuccessfulCertificateEventLoaderBatch(certificateIdList, certificateIdList.size());
        }

        if (failedCertificates.size() > 0) {
            monitoringLogService.logFailedCertificateEventLoaderBatch(failedCertificates, certificateIdList.size());
        }
    }

    private void addToFailedCertificatesTable(String id, Exception e) {
        var certificateEventFailedLoad = new CertificateEventFailedLoad();
        certificateEventFailedLoad.setCertificateId(id);
        certificateEventFailedLoad.setException(e.toString());
        certificateEventFailedLoad.setTimestamp(LocalDateTime.now());
        failedLoadRepository.save(new CertificateEventFailedLoad());
    }
}
