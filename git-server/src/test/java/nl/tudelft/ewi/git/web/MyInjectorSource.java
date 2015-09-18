package nl.tudelft.ewi.git.web;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import cucumber.api.guice.CucumberModules;
import cucumber.runtime.java.guice.InjectorSource;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class MyInjectorSource implements InjectorSource {

	@Override
	public Injector getInjector() {
		return Guice.createInjector(Stage.PRODUCTION, CucumberModules.SCENARIO, new CucumberModule());
	}

}
