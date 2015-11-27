package se.inera.intyg.webcert.web.service.maillink;

import java.net.URI;

/**
 * Created by pebe on 2015-10-05.
 */
public interface MailLinkService {
    URI intygRedirect(String typ, String intygId);
}
