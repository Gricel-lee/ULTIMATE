package main;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import model.Model;
import project.Project;
import sharedContext.SharedContext;
import utils.FileUtils;
import verification.NPMCVerification;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;

public class Headless {
	
	private static Options options = new Options();
	private static String projectFile;
	private static String modelID;
	private static String property;
	private static boolean help = false;
    private static SharedContext sharedContext = SharedContext.getInstance();
    
	private static void setUpCLI() {		
		Option help = new Option("help", "prints usage help inforamtion");
		
		Option projectFile = Option.builder("pf")
							 	   .argName("file")
							 	   .hasArg()
							 	   .desc("The file path to the world model ultimate file")
							 	   .build();
		
		Option modelID = Option.builder("m")
		 					   .argName("modelID")
		 					   .hasArg()
		 					   .desc("The ID of the model as described by the project file")
		 					   .build();
		
		
		Option property = Option.builder("p")
		 					    .argName("definition or file")
		 					    .hasArg()
		 					    .desc("The definition of a property OR a path to a .pctl file")
		 					    .build();
		
		options.addOption(projectFile);
		options.addOption(modelID);
		options.addOption(property);
		options.addOption(help);
	}
	
	private static void getArgs(String[] args) {
		CommandLineParser parser = new DefaultParser();
		try {
	        CommandLine line = parser.parse(options, args);
	        projectFile = line.getOptionValue("pf");
	        modelID = line.getOptionValue("m");
	        property = line.getOptionValue("p");
	        
	        if (line.hasOption("help")) {
	        	help = true;
	        }
		} catch (ParseException e) {
	        System.err.println("Parsing failed.  Reason: " + e.getMessage());
		}
	}
	
	public static void main(String[] args) throws NumberFormatException, IOException {
	    // create the parser
		
		setUpCLI();
	    getArgs(args);
	    
	    if (help) {
		    HelpFormatter formatter = new HelpFormatter();
		    formatter.printHelp("headless", options);
		    return;
	    }
	    Project project = null;
		try {
			project = new Project(projectFile);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		sharedContext.setProject(project);
	    
	    ArrayList<Model> models = new ArrayList<>();
		models.addAll(project.getModels());
		// update the mode files here
		for (Model m : models) {
			FileUtils.writeParametersToFile(m.getVerificationFilePath(), m.getHashExternalParameters());
		}
		NPMCVerification verifier = new NPMCVerification(models);
	    double result = verifier.verify(modelID, property);
	    System.out.println("Result: " + result);
	}
}
