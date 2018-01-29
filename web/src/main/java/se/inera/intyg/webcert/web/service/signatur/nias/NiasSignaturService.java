package se.inera.intyg.webcert.web.service.signatur.nias;

import com.secmaker.netid.nias.v1.AuthenticateResponse;

public interface NiasSignaturService {
    AuthenticateResponse authenticate(String personId, String userNonVisibleData, String endUserInfo);
}
