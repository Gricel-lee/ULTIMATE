package project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import sharedContext.SharedContext;
import utils.Alerter;
//import utils.Alerter;
import utils.FileUtils;
import verification.NPMCVerification;

public class Project {
	
	private static final Logger logger = LoggerFactory.getLogger(Project.class);

	private Set<Model> models; // set of models in the project
    private ObservableList<Model> observableModels;    // The observable list used for UI binding
    private ObjectProperty<Model> currentModel; // Observable property for current model
	private String projectName;
	private ProjectImporter importer;
	private ProjectExporter exporter;
	private String stormInstall = null;
	private String stormParsInstall = null;
	private String prismInstall = null;
	private ObjectProperty<String> chosenPMC;
	private String saveLocation; // set when a project has been saved as, used for subsequent saves
	private String directory = null;
    private SharedContext sharedContext = SharedContext.getInstance();
	
	public Project(String projectPath) throws IOException {
		this.directory = getDirectory(projectPath);
        sharedContext.setProject(this);
		FileUtils.isUltimateFile(projectPath); // throws IOE if file is not an ultimate project file
		importer = new ProjectImporter(projectPath);
		models = importer.importProject();
        // Initialise currentModel property (first model in the set or null)
        currentModel = new SimpleObjectProperty<>(models.isEmpty() ? null : models.iterator().next());
        // Initialise the observable list with the contents of the set
        observableModels = FXCollections.observableArrayList(models);
		this.projectName = FileUtils.removeUltimateFileExtension(projectPath);
        if (sharedContext.getMainStage() != null) {
    		sharedContext.getMainStage().setTitle("Ultimate Multi-Model Verifier: " + projectName);

        }
		try {
			setupConfigs();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
        chosenPMC = new SimpleObjectProperty<>(prismInstall != null ? "PRISM" : (stormInstall != null ? "STORM" : null));
    }
	
	public Project() {
		this.models = new HashSet<Model>();
        this.observableModels = FXCollections.observableArrayList();
        this.currentModel = new SimpleObjectProperty<>(null);
		this.projectName = "untitled";
        sharedContext.setProject(this);
        if (sharedContext.getMainStage() != null) {
    		sharedContext.getMainStage().setTitle("Ultimate Multi-Model Verifier: " + projectName);

        }
		try {
			setupConfigs();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
        chosenPMC = new SimpleObjectProperty<>(prismInstall != null ? "PRISM" : (stormInstall != null ? "STORM" : null));
   }
	
	public ArrayList<String> getModelIDs() {
		ArrayList<String> modelIDs = new ArrayList<String>();
		models.forEach(model -> {
			modelIDs.add(model.getModelId());
		});
		return modelIDs;
	}
	
    public ObservableList<Model> getObservableModels() {
        return observableModels;
    }
    
    public void addModel(Model addModel) {
        // Check if the model already exists using the set
        for (Model model : models) {
            if (model.getModelId().equals(addModel.getModelId())) {
            	throw new IllegalArgumentException();
            	//Alerter.showErrorAlert("Model Already Exists!", "The model could not be added as it exists in the project.");
            }
        }
        // Add to the set
        models.add(addModel);
        // Update the observable list
        observableModels.add(addModel);
    }
    
    public void removeModel(Model removeModel) {
        // Remove from the set (by id comparison)
        models.removeIf(model -> model.getModelId().equals(removeModel.getModelId()));
        // Remove from the observable list
        observableModels.removeIf(model -> model.getModelId().equals(removeModel.getModelId()));
    }
	
	public String getProjectName() {
		return projectName;
	}
	
	public Set<Model> getModels() {
		return this.models;
	}
	
    // Getter for currentModel property (for binding)
    public ObjectProperty<Model> currentModelProperty() {
        return currentModel;
    }

    public void setCurrentModel(Model model) {
        this.currentModel.set(model);
    }

    public Model getCurrentModel() {
        return currentModel.get();
    }
    
	public ObjectProperty<String> chosenPMCProperty() {
		return chosenPMC;
	}
	
	public void setChosenPMC(String chosenPMC) {
		this.chosenPMC.set(chosenPMC);
	}
	
	public String getChosenPMC() {
		return chosenPMC.get();
	}
    
    public void save(String saveLocation) {
    	exporter = new ProjectExporter(this);
    	exporter.saveExport(saveLocation);
    }
    
    public void save() {
    	exporter = new ProjectExporter(this);
    	exporter.saveExport(saveLocation);
    }
    
    public void setSaveLocation(String location) {
    	this.saveLocation = location;
    }
    
    public String getSaveLocation() {
    	return this.saveLocation;
    }
    
    public void setStormInstall(String stormInstall) {
        if (FileUtils.isFile(stormInstall)) {
        	this.stormInstall = stormInstall;
        }
        else {
        	Alerter.showWarningAlert("No Storm Installation found!", "Please configure the location of the storm install on your system!");
        }
    }
    
    public void setStormParsInstall(String stormParsInstall) {
        if (FileUtils.isFile(stormParsInstall)) {
        	this.stormParsInstall = stormParsInstall;
        }
        else {
        	Alerter.showWarningAlert("No Storm-Pars Installation found!", "Please configure the location of the storm-pars install on your system!");
        }
    }
    
    public String getStormInstall() {
    	return this.stormInstall;
    }
    
    public String getStormParsInstall() {
    	return this.stormParsInstall;
    }
    
	private void setupConfigs() throws IOException {
        File configFile = new File("config.json");
        String content = new String(Files.readAllBytes(Paths.get(configFile.toURI())));
        JSONObject configJSON = new JSONObject(content);
        
        String stormInstall = configJSON.getString("stormInstall");
        if (FileUtils.isFile(stormInstall) && !stormInstall.equals("")) {
        	this.stormInstall = stormInstall;
        }
        else {
        	if (sharedContext.getMainStage() != null) {
            	Alerter.showWarningAlert("No Storm Installation found!", "Please configure the location of the storm install on your system!");
        	}
        }
        
        String stormParsInstall = configJSON.getString("stormParsInstall");
        if (FileUtils.isFile(stormParsInstall) && !stormParsInstall.equals("")) {
        	this.stormParsInstall = stormParsInstall;
        }
        else {
        	if (sharedContext.getMainStage() != null) {
            	Alerter.showWarningAlert("No Storm-Pars Installation found!", "Please configure the location of the storm-pars install on your system!");
        	}
        }
        
        String prismInstall = configJSON.getString("prismInstall");
        if (FileUtils.isFile(prismInstall) && !prismInstall.equals("")) {
        	this.prismInstall = prismInstall;
        }
        else {
        	if (sharedContext.getMainStage() != null) {
            	Alerter.showWarningAlert("No PRISM Installation found!", "Please configure the location of the PRISM install on your system!");
        	}
        }
	}
	
	/*
	 * Gets the directory of the project
	 */
	private String getDirectory(String projectPath) {
        Path path = Paths.get(projectPath);
        return path.toAbsolutePath().getParent().toString(); // Get directory
	}
	
	public String directory() {
		return this.directory;
	}
	
	public void setDirectory(String directory) {
		this.directory = directory;
	}
}
	
