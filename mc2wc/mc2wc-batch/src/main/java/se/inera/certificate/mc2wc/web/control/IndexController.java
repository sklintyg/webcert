package se.inera.certificate.mc2wc.web.control;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 */
@Controller
public class IndexController {

    @RequestMapping("/home")
    public String home() {
        return "home";
    }

}
