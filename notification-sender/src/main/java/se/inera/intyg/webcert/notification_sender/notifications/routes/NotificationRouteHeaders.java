/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notification_sender.notifications.routes;

public final class NotificationRouteHeaders {

    public static final String LOGISK_ADRESS = "logiskAdress";
    public static final String INTYGS_ID = "intygsId";
    public static final String INTYGS_TYP = "intygsTyp";
    public static final String INTYG_TYPE_VERSION = "intygTypeVersion";
    public static final String HANDELSE = "handelse";
    public static final String VERSION = "version";
    public static final String USER_ID = "userId";
    public static final String CORRELATION_ID = "correlationId";

    private NotificationRouteHeaders() {
    }
}
