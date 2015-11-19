package se.inera.webcert.logging;

import ch.qos.logback.classic.PatternLayout;

/**
 * Logback {@link PatternLayout} PatternLayout implementation that exposes
 * user and session information.
 *
 * @author nikpet
 */
public class PatternLayoutWithUserContext extends PatternLayout {
    static {
        PatternLayout.defaultConverterMap.put(
                "user", UserConverter.class.getName());
        PatternLayout.defaultConverterMap.put(
                "session", SessionConverter.class.getName());
        PatternLayout.defaultConverterMap.put(
                "selectedCareUnit", UserSelectedCareUnitConverter.class.getName());
    }
}
