/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Certificate (http://code.google.com/p/inera-certificate).
 *
 * Inera Certificate is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Certificate is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.webcert.fkstub.util;

import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;

/**
 * @author andreaskaltenbach
 */
public final class ResultOfCallUtil {

    private ResultOfCallUtil() {
    }

    public static ResultOfCall okResult() {
        ResultOfCall result = new ResultOfCall();
        result.setResultCode(ResultCodeEnum.OK);
        return result;
    }

    public static ResultOfCall failResult(String errorText) {
        return failResult(ErrorIdEnum.VALIDATION_ERROR, errorText);
    }

    public static ResultOfCall applicationErrorResult(String errorText) {
        return failResult(ErrorIdEnum.APPLICATION_ERROR, errorText);
    }

    private static ResultOfCall failResult(ErrorIdEnum errorType, String errorText) {
        ResultOfCall result = new ResultOfCall();
        result.setResultCode(ResultCodeEnum.ERROR);
        result.setErrorId(errorType);
        result.setErrorText(errorText);
        return result;
    }

    public static ResultOfCall infoResult(String infoText) {
        ResultOfCall result = new ResultOfCall();
        result.setResultCode(ResultCodeEnum.INFO);
        result.setInfoText(infoText);
        return result;
    }
}
