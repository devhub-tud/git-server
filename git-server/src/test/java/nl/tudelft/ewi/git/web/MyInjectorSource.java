package nl.tudelft.ewi.git.web;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import cucumber.api.guice.CucumberModules;
import cucumber.runtime.java.guice.InjectorSource;
import nl.tudelft.ewi.git.Config;
import nl.tudelft.ewi.git.GitServerModule;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class MyInjectorSource implements InjectorSource {

	@Override
	public Injector getInjector() {
		return Guice.createInjector(
			Stage.PRODUCTION,
			CucumberModules.SCENARIO,
			Modules.override(new GitServerModule(new Config())).with(new CucumberModule())
		);
	}

}
