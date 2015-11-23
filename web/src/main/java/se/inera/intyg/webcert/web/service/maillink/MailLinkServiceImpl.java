package se.inera.intyg.webcert.web.service.maillink;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pebe on 2015-10-05.
 */
@Service
public class MailLinkServiceImpl implements MailLinkService {

    private static final Logger LOG = LoggerFactory.getLogger(MailLinkServiceImpl.class);

    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_CERT_ID = "certId";
    private static final String PARAM_HOSP_NAME = "hospName";
    private static final String PARAM_PATIENT_SSN = "patientId";

    @Value("${certificate.view.url.base}")
    private String urlBaseTemplate;

    @Value("${certificate.view.url.intyg.integration.template}")
    private String urlIntygFragmentTemplate;

    @Value("${certificate.view.url.utkast.integration.template}")
    private String urlUtkastFragmentTemplate;

    @Override
    public URI intygRedirect(String typ, String intygId) {
        if (StringUtils.isBlank(intygId)) {
            LOG.error("Path parameter 'intygId' was either whitespace, empty (\"\") or null");
            return null;
        }
        if (StringUtils.isBlank(typ)) {
            LOG.error("Path parameter 'typ' was either whitespace, empty (\"\") or null");
            return null;
        }

        LOG.debug("Redirecting to view intyg {} of type {}", intygId, typ);

        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put(PARAM_CERT_TYPE, typ);
        urlParams.put(PARAM_CERT_ID, intygId);
        urlParams.put(PARAM_HOSP_NAME, "");
        urlParams.put(PARAM_PATIENT_SSN, "");

        URI location = UriBuilder.fromPath(urlBaseTemplate).fragment(urlUtkastFragmentTemplate).buildFromMap(urlParams);

        return location;
    }
}
