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
package se.inera.intyg.webcert.infra.monitoring.annotation;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import com.google.common.base.Strings;
import io.prometheus.client.Summary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.ControllerAdvice;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;

@Aspect("pertarget(se.inera.intyg.infra.monitoring.annotation.MethodTimer.timeable())")
@Scope("prototype")
@ControllerAdvice
public class MethodTimer {

  private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();
  private static final HashMap<String, Summary> SUMMARIES = new HashMap<>();
  private static final HashSet<String> NAME_SET = new HashSet<>();

  @Pointcut("@annotation(se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod)")
  public void timeable() {}

  @Around("timeable()")
  public Object timeMethod(final ProceedingJoinPoint pjp) throws Throwable {
    final Signature signature = pjp.getSignature();
    // final MethodSignature signature = (MethodSignature) pjp.getSignature();
    final String key = signature.toLongString();

    Summary summary = lockOp(LOCK.readLock(), () -> SUMMARIES.get(key));
    if (summary == null) {
      // summary = registerSummary(signature, key);
      summary = registerSummary(pjp, key);
    }

    final Summary.Timer t = summary.startTimer();
    try {
      return pjp.proceed();
    } finally {
      t.observeDuration();
    }
  }

  PrometheusTimeMethod getAnnotation(final ProceedingJoinPoint pjp) {
    try {
      final Class targetClass = pjp.getTarget().getClass();
      final MethodSignature signature = (MethodSignature) pjp.getSignature();
      return findAnnotation(
          targetClass.getDeclaredMethod(signature.getName(), signature.getParameterTypes()),
          PrometheusTimeMethod.class);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(
          "Annotation could not be found for pjp \"" + pjp.toShortString() + "\"", e);
    }
  }

  // run locked protected lambda expr.
  <T> T lockOp(final Lock lock, final Supplier<T> supplier) {
    lock.lock();
    try {
      return supplier.get();
    } finally {
      lock.unlock();
    }
  }

  //
  Summary registerSummary(final ProceedingJoinPoint pjp, final String key) {

    // final PrometheusTimeMethod annotation = findAnnotation(signature.getMethod(),
    // PrometheusTimeMethod.class);
    final PrometheusTimeMethod annotation = getAnnotation(pjp);
    return lockOp(
        LOCK.writeLock(),
        () -> {
          Summary summary = SUMMARIES.get(key);
          if (summary != null) {
            return summary;
          }
          final String name = annotation.name();
          final String registerName =
              ensureUniqueName(
                  Strings.isNullOrEmpty(name) ? toDisplayName(pjp.getSignature()) : name);

          summary = Summary.build(registerName, annotation.help()).register();

          SUMMARIES.put(key, summary);

          return summary;
        });
  }

  // make sure no duplicates exists
  String ensureUniqueName(final String startName) {
    int n = 1;
    String name = startName;
    while (NAME_SET.contains(name)) {
      name = startName + "_" + n++;
    }
    NAME_SET.add(name);
    return name;
  }

  // Returns a java class dot method name prefixed with api_
  String toDisplayName(final Signature signature) {
    final String cls = signature.getDeclaringTypeName();
    int index = cls.lastIndexOf('.');
    return "api_" + ((index > 0) ? cls.substring(index + 1) : cls) + "_" + signature.getName();
  }
}
