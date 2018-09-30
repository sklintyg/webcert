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
package se.inera.intyg.webcert.notificationstub.v3;

import org.springframework.stereotype.Component;

@Component
public class NotificationStubStateBean {

    private String errorCode = "0";

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        switch (errorCode) {
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
                this.errorCode = errorCode;
                break;
            default:
                this.errorCode = "0";
        }
    }
}
