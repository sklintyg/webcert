/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.loggtjanststub;

import org.springframework.beans.factory.annotation.Autowired;
import se.riv.ehr.log.store.storelog.rivtabp21.v1.StoreLogResponderInterface;
import se.riv.ehr.log.store.storelogresponder.v1.StoreLogRequestType;
import se.riv.ehr.log.store.storelogresponder.v1.StoreLogResponseType;
import se.riv.ehr.log.store.v1.ResultType;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.ResultCodeType;

import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author andreaskaltenbach
 */
public class StoreLogStubResponder implements StoreLogResponderInterface {

    @Autowired
    private CopyOnWriteArrayList<LogType> logEntries;

    @Override
    public StoreLogResponseType storeLog(String logicalAddress, StoreLogRequestType request) {
        logEntries.addAll(request.getLog());

        StoreLogResponseType response = new StoreLogResponseType();

        ResultType result = new ResultType();
        result.setResultCode(ResultCodeType.OK);
        result.setResultText("Done");
        response.setResultType(result);
        return response;
    }
}
