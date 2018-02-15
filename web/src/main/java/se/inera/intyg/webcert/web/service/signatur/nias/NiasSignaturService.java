/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
