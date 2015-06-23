package se.inera.webcert.intygstjanststub.mode;

import java.lang.annotation.*;

/**
 * Created by erikl on 15-04-09.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface StubModeAware {
}