package se.inera.intyg.webcert.web.service.signatur.nias;

import com.secmaker.netid.nias.v1.ResultCollect;
import com.secmaker.netid.nias.v1.SignResponse;

import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;

/**
 * @author eriklupander
 */
public interface NiasSignaturService {

    String OUTSTANDING_TRANSACTION = "OUTSTANDING_TRANSACTION";
    String USER_SIGN = "USER_SIGN";
    String COMPLETE = "COMPLETE";

    SignaturTicket startNiasAuthentication(String intygId, long version);

    /**
     * Returns the orderRef to use for this interaction.
     *
     * @param personId
     *      personid (or possibly hsaId)
     * @param userNonVisibleData
     *      nullable
     * @param endUserInfo
     *      nullable
     * @return
     *      The orderRef to use in subsequent operations.
     */
    String authenticate(String personId, String userNonVisibleData, String endUserInfo);

    /**
     * Perform a collect request using an orderRef received in an authenticate (or sign) request.
     *
     * @param orderRef
     *      A reference number previously obtained through an authenticate or sign request.
     * @return
     *      A ResultCollect, pay attention to the processStatus.
     */
    ResultCollect collect(String orderRef);

    /**
     *
     * @param personalNumber
     * @param userVisibleData
     * @param userNonVisibleData
     * @param endUserInfo
     * @return
     */
    SignResponse sign(String personalNumber, String userVisibleData, String userNonVisibleData, String endUserInfo);
}
