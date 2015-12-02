package se.inera.intyg.webcert.web.auth.authorities;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Magnus Ekstrand on 25/11/15.
 */
public final class AuthoritiesResolverUtil {

    private AuthoritiesResolverUtil() {
    }

    public static Map<String, Role> toMap(Role role) {
        return Collections.unmodifiableMap(Stream
                .of(new AbstractMap.SimpleEntry<>(role.getName(), role))
                .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));
    }

    public static Map<String, Privilege> toMap(Collection<Privilege> privileges) {
        return privileges.stream()
                .filter(p -> p != null)
                .collect(Collectors.toMap(Privilege::getName, Function.identity()));
    }

}
