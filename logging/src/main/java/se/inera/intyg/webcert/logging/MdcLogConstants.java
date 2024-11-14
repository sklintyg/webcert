/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

    public static final String HTTP_REQUEST_ID = "http.request.id";
    public static final String OS_FAMILY = "os.family";
    public static final String OS_VERSION = "os.version";
    public static final String SERVICE_ADDRESS = "service.address";
    public static final String SERVICE_STATE = "service.state";
    public static final String SESSION_ID_KEY = "session.id";
    public static final String SPAN_ID_KEY = "span.id";
    public static final String TRACE_ID_KEY = "trace.id";
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
    public static final String EVENT_CERTIFICATE_DIAGNOSIS_CODE = "event.certificate.diagnosis.code";
    public static final String EVENT_CERTIFICATE_ID = "event.certificate.id";
    public static final String EVENT_CERTIFICATE_ORIGINAL_ID = "event.certificate.original.id";
    public static final String EVENT_CERTIFICATE_ORIGINAL_TYPE = "event.certificate.original.type";
    public static final String EVENT_CERTIFICATE_PDF_PRINT_TYPE = "event.certificate.pdf.print.type";
    public static final String EVENT_CERTIFICATE_PREFILL_ELEMENT_COUNT = "event.certificate.prefill.element.count";
    public static final String EVENT_CERTIFICATE_RECIPIENT = "event.certificate.recipient";
    public static final String EVENT_CERTIFICATE_RELATION_CODE = "event.certificate.relation.code";
    public static final String EVENT_CERTIFICATE_REVOKE_MESSAGE = "event.certificate.revoke.message";
    public static final String EVENT_CERTIFICATE_REVOKE_REASON = "event.certificate.revoke.reason";
    public static final String EVENT_CERTIFICATE_TYPE = "event.certificate.type";
    public static final String EVENT_CERTIFICATE_UNIT_ID = "event.certificate.unit.id";
    public static final String EVENT_CERTIFICATE_VERSION = "event.certificate.version";
    public static final String EVENT_MESSAGE_ID = "event.message.id";
    public static final String EVENT_MESSAGE_EMAIL_REASON = "event.message.email.reason";
    public static final String EVENT_MESSAGE_QUESTION_ID_LIST = "event.message.question.id.list";
    public static final String EVENT_MESSAGE_QUESTION_ORIGIN = "event.message.question.origin";
    public static final String EVENT_MESSAGE_REFERENCE_EXTERNAL = "event.message.reference.external";
    public static final String EVENT_MESSAGE_REFERENCE_INTERNAL = "event.message.reference.internal";
    public static final String EVENT_MESSAGE_TOPIC = "event.message.topic";
    public static final String EVENT_MESSAGE_TYPE = "event.message.type";
    public static final String EVENT_PERSON_ID = "event.person.id";
    public static final String EVENT_PERSON_ID_LOOKUP_RESULT = "event.person.id.lookup.result";
    public static final String EVENT_SRS_CLIENT_CONTEXT = "event.srs.client.context";
    public static final String EVENT_STATUS_UPDATE_EVENT_ID = "event.status.update.event.id";
    public static final String EVENT_STATUS_UPDATE_CORRELATION_ID = "event.status.update.correlation.id";
    public static final String EVENT_STATUS_UPDATE_LOGICAL_ADDRESS = "event.status.update.logical.address";
    public static final String EVENT_STATUS_UPDATE_SEND_ATTEMPT = "event.status.update.send_attempt";
    public static final String EVENT_STATUS_UPDATE_TIME = "event.status.update.time";
    public static final String EVENT_STATUS_UPDATE_TYPE = "event.status.update.type";

    public static final String ORGANIZATION_CARE_PROVIDER_ID = "organization.care_provider.id";
    public static final String ORGANIZATION_ID = "organization.id";
    public static final String ORGANIZATION_ID_LIST = "organization.id.list";

    public static final String USER_ID = "user.id";
    public static final String USER_ORIGIN = "user.origin";
    public static final String USER_ROLE = "user.role";
    public static final String USER_ROLE_TYPE_NAME = "user.role_type.name";
}
