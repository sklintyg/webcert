package se.inera.intyg.webcert.integration.fmb.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxInformation;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Typfall;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class FmbConsumerImpl implements FmbConsumer {

    private String baseUrl;

    public FmbConsumerImpl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Typfall getTypfall() throws FailedToFetchFmbData {
        final String urlString = baseUrl + "/typfall?inlinehtmlmarkup=true";
        return fetchFmbData(urlString, Typfall.class);
    }

    @Override
    public FmdxInformation getForsakringsmedicinskDiagnosinformation() throws FailedToFetchFmbData {
        final String urlString = baseUrl + "/forsakringsmedicinskdiagnosinformation?inlinehtmlmarkup=true";
        return fetchFmbData(urlString, FmdxInformation.class);
    }

    private <T> T fetchFmbData(String urlString, Class<T> valueType) throws FailedToFetchFmbData {
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(con.getInputStream(), valueType);
        } catch (IOException e) {
            throw new FailedToFetchFmbData(baseUrl, e);
        }
    }

}
