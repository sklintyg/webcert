package se.inera.intyg.webcert.web.event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.event.model.CertificateEventFailedLoad;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventFailedLoadRepository;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventProcessedRepository;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;

@Service
public class CertificateEventLoaderConsumer implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateEventLoaderConsumer.class);

    @Autowired
    CertificateEventService service;

    @Autowired
    UtkastRepository utkastRepository;

    @Autowired
    CertificateEventFailedLoadRepository failedLoadRepository;

    @Autowired
    CertificateEventProcessedRepository processedRepository;


    @Override
    @JmsListener(destination = "${certificateevent.loader.queueName}")
    public void onMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage objMessage = (ObjectMessage) message;
                var list = (ArrayList<String>) objMessage.getObject();
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
                var byCertificateId = processedRepository.findById(id);
                if (byCertificateId.isPresent()) {
                    processId(id);
                }
            } catch (Exception e) {
                processFailedId(id, e, failedCertificates);
            }
        });

        if (failedCertificates.size() < certificateIdList.size()) {
            LOG.info("Certificate Event Loader successfully finished processing {} certificates out of batch with size {}",
                (certificateIdList.size() - failedCertificates.size()), certificateIdList.size());
        }

        if (failedCertificates.size() > 0) {
            LOG.warn("Certificate Event Loader failed during processing of {} certificates out of batch with size {}. These failed: {}",
                failedCertificates.size(), certificateIdList.size(), String.join(", ", failedCertificates));
        }
    }

    @Transactional
    public void processFailedId(String id, Exception e, List<String> failedCertificates) {
        addToFailedCertificatesTable(id, e);
        processedRepository.deleteById(id);
        failedCertificates.add(id);
    }

    @Transactional
    public void processId(String id) {
        service.getCertificateEvents(id);
        processedRepository.deleteById(id);
    }

    private void addToFailedCertificatesTable(String id, Exception e) {
        var certificateEventFailedLoad = new CertificateEventFailedLoad();
        certificateEventFailedLoad.setCertificateId(id);
        certificateEventFailedLoad.setException(e.toString());
        certificateEventFailedLoad.setTimestamp(LocalDateTime.now());
        failedLoadRepository.save(certificateEventFailedLoad);
    }
}
