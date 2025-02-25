package controllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;
import utils.DialogOpener;

public class MenuBarController {
	
	@FXML private MenuItem loadButton;
	@FXML private MenuItem saveButton;
	@FXML private MenuItem saveAsButton;
	@FXML private MenuItem quitButton;

	private boolean saved = false;
	
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();	
	
    @FXML
	private void initialize() {
		
	}
	
	@FXML
	private void load() throws IOException {
		String file = DialogOpener.openUltimateFileDialog(sharedContext.getMainStage());
		if (file == null) {
			return;
		}
		if (quit()) {
			Stage newMainStage = new Stage();
			sharedContext.setMainStage(newMainStage);
			Project project = new Project(file);
			sharedContext.setProject(project);
	        // Load the FXML file and initialize its associated controller
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_view.fxml")); // Specifies the FXML path
	        
	        // Load the root layout from the FXML file (in this case, a GridPane layout)
	        GridPane root = loader.load(); // The root layout is defined in main_view.fxml

	        // Create a Scene using the root layout and set its dimensions
	        newMainStage.setScene(new Scene(root, 1500, 1000)); // Scene dimensions: 1500x1000 pixels

	        // Set the title of the primary stage (window)
	        //stage.setTitle("Ultimate Stochastic World Model Manager: UNTITLED"); // Customize the window title as needed

	        // Set minimum dimensions for the primary stage
	        newMainStage.setMinWidth(1000); // Ensure the stage cannot be resized smaller than 800px in width
	        newMainStage.setMinHeight(800); // Ensure the stage cannot be resized smaller than 600px in height

	        // Display the stage (window) to the user
	        newMainStage.show(); // Makes the primary stage visible
		}
	}
	
	@FXML
	private void save() {
		if (!saved) {
			saveAs();
		}
		else {
			project.save();
		}
	}
	
	@FXML private void saveAs() {
		String location = DialogOpener.openSaveDialog(sharedContext.getMainStage());
		// deal with cancel neatly
		if (location == null) {
			return;
		}
		project.setSaveLocation(location);
		project.save(location);
		saved = true;
	}
	
	@FXML
	private boolean quit() {
		boolean q = Alerter.showConfirmationAlert("Project Not Saved!", "Are you sure you want to quit without saving?");
		if (q) {
			sharedContext.getMainStage().close();
		}
		return q;
	}

}
