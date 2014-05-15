package se.inera.certificate.mc2wc.web.control;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import se.inera.certificate.mc2wc.message.StatisticsRequest;
import se.inera.certificate.mc2wc.message.StatisticsResponse;
import se.inera.certificate.mc2wc.rest.MigrationReceiver;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 *
 */
@Controller
public class StatisticsController {

	@PersistenceContext(unitName="jpa.migration.medcert")
    private EntityManager entityManager;

    @Autowired
    @Qualifier("migrationMessageReceiverService")
    private MigrationReceiver migrationReceiver;

    @Value("${medcert.sender.id}")
    private String sender;

    @RequestMapping("/statistics")
    public String createStatistics(Model model) {

        Query certificateQuery = entityManager.createQuery("select count(id) from Certificate where document is not null");
        Long certificateCount = (Long) certificateQuery.getSingleResult();

        Query questionQuery = entityManager.createQuery("select count(id) from Question where subject is not null and text is not null");
        Long questionCount = (Long) questionQuery.getSingleResult();

        Query answerQuery = entityManager.createQuery("select count(id) from Answer where text is not null");
        Long answerCount = (Long) answerQuery.getSingleResult();

        model.addAttribute("certificateCountMedcert", certificateCount);
        model.addAttribute("questionCountMedcert", questionCount);
        model.addAttribute("answerCountMedcert", answerCount);

        Query careGiverIdQuery = entityManager.createQuery("select distinct (addressCare.careGiverId) from Question where subject is not null and text is not null");
        @SuppressWarnings("unchecked")
        List<String> careGiverIds = careGiverIdQuery.getResultList();

        StatisticsRequest request = new StatisticsRequest();
        request.setSender(sender);
        request.getCareGiverIds().addAll(careGiverIds);


        StatisticsResponse response = migrationReceiver.getStatistics(request);

        model.addAttribute("certificateCountWebcert", response.getCertificateCount());
        model.addAttribute("questionCountWebcert", response.getQuestionCount());
        model.addAttribute("answerCountWebcert", response.getAnswerCount());

        return "medcertStatistics";
    }

}
