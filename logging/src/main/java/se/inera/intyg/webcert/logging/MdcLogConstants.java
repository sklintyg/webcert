/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.logging;

public class MdcLogConstants {

    private MdcLogConstants() {
    }

    public static final String OS_FAMILY = "os.family";
    public static final String OS_VERSION = "os.version";
    public static final String SESSION_ID_KEY = "session.id";
    public static final String SPAN_ID_KEY = "span.id";
    public static final String TRACE_ID_KEY = "trace.id";
    public static final String TRANSACTION_ID = "transaction.id";
    public static final String USER_AGENT_NAME = "user_agent.name";
    public static final String USER_AGENT_VERSION = "user_agent.version";

    public static final String ERROR_ID = "error.id";
    public static final String ERROR_CODE = "error.code";
    public static final String ERROR_MESSAGE = "error.message";
    public static final String ERROR_STACKTRACE = "error.stack_trace";

    public static final String EVENT_CATEGORY = "event.category";
    public static final String EVENT_CATEGORY_API = "[api]";
    public static final String EVENT_CATEGORY_PROCESS = "[process]";
    public static final String EVENT_ACTION = "event.action";
    public static final String EVENT_START = "event.start";
    public static final String EVENT_END = "event.end";
    public static final String EVENT_DURATION = "event.duration";
    public static final String EVENT_CLASS = "event.class";
    public static final String EVENT_METHOD = "event.method";
    public static final String EVENT_OUTCOME = "event.outcome";
    public static final String EVENT_OUTCOME_FAILURE = "failure";
    public static final String EVENT_OUTCOME_SUCCESS = "success";
    public static final String EVENT_TYPE = "event.type";
    public static final String EVENT_TYPE_ACCESS = "access";
    public static final String EVENT_TYPE_CHANGE = "change";
    public static final String EVENT_TYPE_CREATION = "creation";
    public static final String EVENT_TYPE_DELETION = "deletion";
    public static final String EVENT_TYPE_DENIED = "denied";
    public static final String EVENT_TYPE_ERROR = "error";
    public static final String EVENT_TYPE_INFO = "info";
    public static final String EVENT_TYPE_USER = "user";

    public static final String EVENT_AUTHENTICATION_SCHEME = "event.authentication.scheme";
    public static final String EVENT_AUTHENTICATION_METHOD = "event.authentication.method";
    public static final String EVENT_CERTIFICATE_CARE_PROVIDER_ID = "event.certificate.care_provider.id";
    public static final String EVENT_CERTIFICATE_ID = "event.certificate.id";
    public static final String EVENT_CERTIFICATE_PARENT_CODE = "event.certificate.parent.code";
    public static final String EVENT_CERTIFICATE_PARENT_ID = "event.certificate.parent.id";
    public static final String EVENT_CERTIFICATE_PARENT_TYPE = "event.certificate.parent.type";
    public static final String EVENT_CERTIFICATE_TYPE = "event.certificate.type";
    public static final String EVENT_CERTIFICATE_UNIT_ID = "event.certificate.unit.id";
    public static final String EVENT_CERTIFICATE_VERSION = "event.certificate.version";
    public static final String EVENT_LOGICAL_ADDRESS = "event.logical_address";
    public static final String EVENT_MESSAGE_ID = "event.message.id";
    public static final String EVENT_MESSAGE_SUBJECT = "event.message.subject";
    public static final String EVENT_PREFILL_COUNT = "event.prefill_count";
    public static final String EVENT_PRINT_TYPE = "event.print_type";
    public static final String EVENT_PU_LOOKUP_ID = "event.pu_lookup.id";
    public static final String EVENT_PU_LOOKUP_RESULT = "event.pu_lookup.result";
    public static final String EVENT_RECIPIENT = "event.recipient";
    public static final String EVENT_REVOKE_REASON = "event.revoke_reason";
    public static final String EVENT_SRS_CLIENT_CONTEXT = "event.srs.client_context";
    public static final String EVENT_SRS_DIAGNOSIS_CODE = "event.srs.diagnosis_code";
    public static final String EVENT_STATUS_UPDATE_CORRELATION_ID = "event.status_update.correlation_id";
    public static final String EVENT_STATUS_UPDATE_EVENT_ID = "event.status_update.event_id";
    public static final String EVENT_STATUS_UPDATE_SEND_ATTEMPT = "event.status_update.send_attempt";
    public static final String EVENT_STATUS_UPDATE_TYPE = "event.status_update.type";

    public static final String ORGANIZATION_CARE_PROVIDER_ID = "organization.care_provider.id";
    public static final String ORGANIZATION_ID = "organization.id";

    public static final String USER_ID = "user.id";
    public static final String USER_ORIGIN = "user.origin";
    public static final String USER_ROLE = "user.role";
}
