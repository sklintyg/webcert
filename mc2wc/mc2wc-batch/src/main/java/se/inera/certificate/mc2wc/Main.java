package se.inera.certificate.mc2wc;

import java.io.Console;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.JOptCommandLinePropertySource;
import org.springframework.core.env.PropertySource;

import se.inera.certificate.mc2wc.batch.MigrationJobExecutor;

public class Main {

	private static final String CONTEXT_LOCATION = "/application-context.xml";
	private static final String CONFIG_PARAM = "configFile";
	
	@Autowired
	private MigrationJobExecutor migrationJobExecutor; 

	public static void main(String[] args) throws Exception {
		Console console = System.console();
		
		//console.printf("%s", "Starting application...");
		
		OptionParser parser = new OptionParser();
		parser.accepts(CONFIG_PARAM).withRequiredArg();
		OptionSet options = parser.parse(args);
		PropertySource ps = new JOptCommandLinePropertySource(options);

		Main main = new Main();
		
//		if (options.has(CONFIG_PARAM)) {
//			//console.printf("Config file: %s", options.valueOf(CONFIG_PARAM));
//		}
		
		ApplicationContextLoader appCxtLoader = new ApplicationContextLoader();
		appCxtLoader.load(main, ps, CONTEXT_LOCATION);
		
		start(main, console);
	}

	private static void start(Main main, Console console) throws Exception {
		//console.printf("%s", "Starting migration job...");
		main.migrationJobExecutor.startMigration();
	}

}
