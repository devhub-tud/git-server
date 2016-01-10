package nl.tudelft.ewi.git.web;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface MockedSingleton {
}
