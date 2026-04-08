/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.infra.security.filter;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.SerializationUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * This filter checks if the user Principal has changed (new vald vardenhet, some consent given
 * etc). If true, the wrapped RedisSession is "touched" using
 * session.setAttribute("SPRING_SECURITY_CONTEXT", context) which then will trigger a diff in the
 * springSessionRepositoryFilter forcing an update of the Principal in the redis store.
 *
 * <p>ONLY use this filter if you're using Spring Session with Redis!
 *
 * <p>This filter should run directly AFTER the spring security filters so it has access to the
 * Spring Security SecurityContextHolder.getContext().
 *
 * @author eriklupander
 */
public class PrincipalUpdatedFilter extends OncePerRequestFilter {

  static final int HF_BITS = 128;

  HashFunction hf = Hashing.goodFastHash(HF_BITS);

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final SecurityContext context = authContext();

    final HashCode beforeHash =
        Objects.nonNull(context) ? hashCode(context.getAuthentication().getPrincipal()) : null;

    // Invoke next filter in chain.
    filterChain.doFilter(request, response);

    // If we were authenticated and calculated a hash before invoke...
    if (Objects.nonNull(context) && Objects.nonNull(context.getAuthentication())) {
      final HashCode afterHash = hashCode(context.getAuthentication().getPrincipal());
      // Check if principal hash has changed
      if (!beforeHash.equals(afterHash)) {
        request.getSession(false).setAttribute(SPRING_SECURITY_CONTEXT_KEY, context);
      }
    }
  }

  /**
   * Returns context if authentication exists.
   *
   * @return context if context and it's authentication exists, otherwise null.
   */
  private SecurityContext authContext() {
    final SecurityContext sc = SecurityContextHolder.getContext();
    return Objects.nonNull(sc.getAuthentication()) ? sc : null;
  }

  /**
   * Returns a 128 bit hash code of the object if it's a Serializable (which is strongly
   * recommended).
   *
   * @param object the object.
   * @return the hash code for the objects state if it's serializable, otherwise is hashCode() used.
   */
  HashCode hashCode(final Object object) {
    return (object instanceof Serializable)
        ? hf.hashBytes(SerializationUtils.serialize(object))
        : hf.hashInt(object.hashCode());
  }
}
