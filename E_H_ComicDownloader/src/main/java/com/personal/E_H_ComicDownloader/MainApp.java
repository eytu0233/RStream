package com.personal.E_H_ComicDownloader;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) throws Exception {
        launch(args);
    }

	private Stage primaryStage;
	private BorderPane rootLayout;
	private ObservableList<ComicDownloadTask> imageDownloadTasks = FXCollections.observableArrayList();

    public void start(Stage stage) throws Exception {

    	this.primaryStage = stage;
        this.primaryStage.setTitle("ImageDownloader");
        
        initRootLayout();
        
        showImageDownloaderOverview();
    }

	private void initRootLayout() {
		// TODO Auto-generated method stub
		try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/rootLayout.fxml"));
            rootLayout = loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private void showImageDownloaderOverview() {
		// TODO Auto-generated method stub
		try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/ComicDownloaderOverview.fxml"));
            AnchorPane personOverview = (AnchorPane) loader.load();

            // Set person overview into the center of root layout.
            rootLayout.setCenter(personOverview);
            
            // Give the controller access to the main app.
            ComicDownloadController controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public Stage getPrimaryStage() {
		// TODO Auto-generated method stub
		return primaryStage;
	}

	public ObservableList<ComicDownloadTask> getComicDownloadTasks() {
		// TODO Auto-generated method stub
		return imageDownloadTasks;
	}
}
