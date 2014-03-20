package se.inera.certificate.mc2wc.web.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import se.inera.certificate.mc2wc.message.PingRequest;
import se.inera.certificate.mc2wc.rest.MigrationReceiver;

/**
 *
 */
@Controller
public class PingController {

    private Logger logger = LoggerFactory.getLogger(PingController.class);

    @Autowired
    @Qualifier("migrationMessageReceiverService")
    private MigrationReceiver migrationReceiver;

    @Value("${migration.service.url}")
    private String receiverUrl;

    @RequestMapping("/sendPing")
    public String sendPing(Model model) {
        model.addAttribute("receiverUrl", receiverUrl);
        try {
            migrationReceiver.ping(new PingRequest());

            model.addAttribute("connected", true);
        } catch (Exception e) {
            logger.error("Could not connect to receiver at {}", receiverUrl, e);
            model.addAttribute("connected", false);
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "pingResult";
    }

}
