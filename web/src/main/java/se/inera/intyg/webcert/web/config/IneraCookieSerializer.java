/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.config;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.session.web.http.CookieSerializer;

/**
 * Custom CookieSerializer to add samesite=none attribute to session cookie.
 * This is based on the DefaultCookieSerializer from Spring 5 (Spring Session Core 2.2.1)
 *
 * It needed to be re-implemented since the current version of Spring Session (1.3.3)
 * doesn't have the required implementation to be extended.
 */
public class IneraCookieSerializer implements CookieSerializer {

    private static final Log LOG = LogFactory.getLog(IneraCookieSerializer.class);

    private static final BitSet DOMAIN_VALID = new BitSet(128);

    static {
        for (char c = '0'; c <= '9'; c++) {
            DOMAIN_VALID.set(c);
        }
        for (char c = 'a'; c <= 'z'; c++) {
            DOMAIN_VALID.set(c);
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            DOMAIN_VALID.set(c);
        }
        DOMAIN_VALID.set('.');
        DOMAIN_VALID.set('-');
    }

    private Clock clock = Clock.systemUTC();

    private String cookieName = "SESSION";

    private Boolean useSecureCookie;

    private boolean useHttpOnlyCookie = isServlet3();

    private String cookiePath;

    private int cookieMaxAge = -1;

    private String domainName;

    private Pattern domainNamePattern;

    private String jvmRoute;

    private boolean useBase64Encoding;

    private String rememberMeRequestAttribute;

    @Override
    public List<String> readCookieValues(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        List<String> matchingCookieValues = new ArrayList<String>();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (this.cookieName.equals(cookie.getName())) {
                    String sessionId = this.useBase64Encoding
                        ? base64Decode(cookie.getValue()) : cookie.getValue();
                    if (sessionId == null) {
                        continue;
                    }
                    if (this.jvmRoute != null && sessionId.endsWith(this.jvmRoute)) {
                        sessionId = sessionId.substring(0,
                            sessionId.length() - this.jvmRoute.length());
                    }
                    matchingCookieValues.add(sessionId);
                }
            }
        }
        return matchingCookieValues;
    }

    @Override
    public void writeCookieValue(CookieValue cookieValue) {
        HttpServletRequest request = cookieValue.getRequest();
        HttpServletResponse response = cookieValue.getResponse();
        String requestedCookieValue = cookieValue.getCookieValue();
        StringBuilder sb = new StringBuilder();
        sb.append(this.cookieName).append('=');
        String value = getValue(cookieValue);
        if (value != null && value.length() > 0) {
            validateValue(value);
            sb.append(value);
        }
        int maxAge = getMaxAge(requestedCookieValue, request);

        if (maxAge > -1) {
            sb.append("; Max-Age=").append(maxAge);
            ZonedDateTime expires = (maxAge != 0) ? ZonedDateTime.now(this.clock).plusSeconds(maxAge)
                : Instant.EPOCH.atZone(ZoneOffset.UTC);
            sb.append("; Expires=").append(expires.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        }
        String domain = getDomainName(request);
        if (domain != null && domain.length() > 0) {
            validateDomain(domain);
            sb.append("; Domain=").append(domain);
        }
        String path = getCookiePath(request);
        if (path.length() > 0) {
            validatePath(path);
            sb.append("; Path=").append(path);
        }
        if (isSecureCookie(request)) {
            sb.append("; Secure");
        }
        if (this.useHttpOnlyCookie) {
            sb.append("; HttpOnly");
        }

        // Check if connection is secure since "none" isn't a valid option when using http (as used in DEV).
        if (request.isSecure()) {
            sb.append("; SameSite=").append("none");
        }

        response.addHeader("Set-Cookie", sb.toString());
    }

    private int getMaxAge(String requestedCookieValue, HttpServletRequest request) {
        int maxAge;
        if ("".equals(requestedCookieValue)) {
            maxAge = 0;
        } else if (this.rememberMeRequestAttribute != null
            && request.getAttribute(this.rememberMeRequestAttribute) != null) {
            // the cookie is only written at time of session creation, so we rely on
            // session expiration rather than cookie expiration if remember me is
            // enabled
            maxAge = Integer.MAX_VALUE;
        } else {
            maxAge = this.cookieMaxAge;
        }
        return maxAge;
    }

    private void validateDomain(String domain) {
        int i = 0;
        int cur = -1;
        int prev;
        char[] chars = domain.toCharArray();
        while (i < chars.length) {
            prev = cur;
            cur = chars[i];
            if (!DOMAIN_VALID.get(cur) || ((prev == '.' || prev == -1) && (cur == '.' || cur == '-'))
                || (prev == '-' && cur == '.')) {
                throw new IllegalArgumentException("Invalid cookie domain: " + domain);
            }
            i++;
        }
        if (cur == '.' || cur == '-') {
            throw new IllegalArgumentException("Invalid cookie domain: " + domain);
        }
    }

    // CHECKSTYLE:OFF MagicNumber
    private void validatePath(String path) {
        for (char ch : path.toCharArray()) {
            if (ch < 0x20 || ch > 0x7E || ch == ';') {
                throw new IllegalArgumentException("Invalid cookie path: " + path);
            }
        }
    }
    // CHECKSTYLE:ON MagicNumber

    private String getValue(CookieValue cookieValue) {
        String requestedCookieValue = cookieValue.getCookieValue();
        String actualCookieValue = requestedCookieValue;
        if (this.jvmRoute != null) {
            actualCookieValue = requestedCookieValue + this.jvmRoute;
        }
        if (this.useBase64Encoding) {
            actualCookieValue = base64Encode(actualCookieValue);
        }
        return actualCookieValue;
    }

    // CHECKSTYLE:OFF MagicNumber
    private void validateValue(String value) {
        int start = 0;
        int end = value.length();
        if ((end > 1) && (value.charAt(0) == '"') && (value.charAt(end - 1) == '"')) {
            start = 1;
            end--;
        }
        char[] chars = value.toCharArray();
        for (int i = start; i < end; i++) {
            char c = chars[i];
            if (c < 0x21 || c == 0x22 || c == 0x2c || c == 0x3b || c == 0x5c || c == 0x7f) {
                throw new IllegalArgumentException("Invalid character in cookie value: " + c);
            }
        }
    }
    // CHECKSTYLE:ON MagicNumber

    @SuppressWarnings("DefaultCharset")
    private String base64Decode(String base64Value) {
        try {
            byte[] decodedCookieBytes = java.util.Base64.getDecoder().decode(base64Value);
            return new String(decodedCookieBytes);
        } catch (Exception ex) {
            LOG.debug("Unable to Base64 decode value: " + base64Value);
            return null;
        }
    }

    @SuppressWarnings("DefaultCharset")
    private String base64Encode(String value) {
        byte[] encodedCookieBytes = java.util.Base64.getEncoder().encode(value.getBytes());
        return new String(encodedCookieBytes);
    }

    public void setUseSecureCookie(boolean useSecureCookie) {
        this.useSecureCookie = useSecureCookie;
    }

    public void setUseHttpOnlyCookie(boolean useHttpOnlyCookie) {
        if (useHttpOnlyCookie && !isServlet3()) {
            throw new IllegalArgumentException(
                "You cannot set useHttpOnlyCookie to true in pre Servlet 3 environment");
        }
        this.useHttpOnlyCookie = useHttpOnlyCookie;
    }

    private boolean isSecureCookie(HttpServletRequest request) {
        if (this.useSecureCookie == null) {
            return request.isSecure();
        }
        return this.useSecureCookie;
    }

    public void setCookiePath(String cookiePath) {
        this.cookiePath = cookiePath;
    }

    public void setCookieName(String cookieName) {
        if (cookieName == null) {
            throw new IllegalArgumentException("cookieName cannot be null");
        }
        this.cookieName = cookieName;
    }

    public void setCookieMaxAge(int cookieMaxAge) {
        this.cookieMaxAge = cookieMaxAge;
    }

    public void setDomainName(String domainName) {
        if (this.domainNamePattern != null) {
            throw new IllegalStateException(
                "Cannot set both domainName and domainNamePattern");
        }
        this.domainName = domainName;
    }

    public void setDomainNamePattern(String domainNamePattern) {
        if (this.domainName != null) {
            throw new IllegalStateException(
                "Cannot set both domainName and domainNamePattern");
        }
        this.domainNamePattern = Pattern.compile(domainNamePattern,
            Pattern.CASE_INSENSITIVE);
    }

    public void setJvmRoute(String jvmRoute) {
        this.jvmRoute = "." + jvmRoute;
    }

    public void setUseBase64Encoding(boolean useBase64Encoding) {
        this.useBase64Encoding = useBase64Encoding;
    }

    public void setRememberMeRequestAttribute(String rememberMeRequestAttribute) {
        if (rememberMeRequestAttribute == null) {
            throw new IllegalArgumentException(
                "rememberMeRequestAttribute cannot be null");
        }
        this.rememberMeRequestAttribute = rememberMeRequestAttribute;
    }

    private String getDomainName(HttpServletRequest request) {
        if (this.domainName != null) {
            return this.domainName;
        }
        if (this.domainNamePattern != null) {
            Matcher matcher = this.domainNamePattern.matcher(request.getServerName());
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private String getCookiePath(HttpServletRequest request) {
        if (this.cookiePath == null) {
            return request.getContextPath() + "/";
        }
        return this.cookiePath;
    }

    private boolean isServlet3() {
        try {
            ServletRequest.class.getMethod("startAsync");
            return true;
        } catch (NoSuchMethodException e) {
        }
        return false;
    }
}
