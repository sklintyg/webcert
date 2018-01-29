package se.inera.intyg.webcert.web.service.signatur.nias;

import java.io.StringReader;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.secmaker.netid.nias.v1.AuthenticateResponse;
import com.secmaker.netid.nias.v1.NetiDAccessServerSoap;

@Service
public class NiasSignaturServiceImpl implements NiasSignaturService {

    private  static final Logger LOG = LoggerFactory.getLogger(NiasSignaturServiceImpl.class);

    @Autowired
    private NetiDAccessServerSoap netiDAccessServerSoap;

    @Override
    public AuthenticateResponse authenticate(String personId, String userNonVisibleData, String endUserInfo) {
        String result = netiDAccessServerSoap.authenticate(personId, userNonVisibleData, endUserInfo);
        LOG.info("RESP FROM NETID: " + result);
        AuthenticateResponse response = JAXB.unmarshal(new StringReader(result), AuthenticateResponse.class);
        return response;
    }
}
