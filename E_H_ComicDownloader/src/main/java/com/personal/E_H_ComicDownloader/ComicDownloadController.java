package com.personal.E_H_ComicDownloader;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.personal.E_H_ComicDownloader.model.ComicGalleriesTracker;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;

public class ComicDownloadController {

	private static ExecutorService executor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			return t;
		}
	});
	
	private MainApp mainApp;

	@FXML
	private TableView<ComicDownloadTask> ComicDownloaderTable;
	@FXML
	private TableColumn<ComicDownloadTask, String> ComicNameColumn;
	@FXML
	private TableColumn<ComicDownloadTask, String> statusColumn;
	@FXML
	private TableColumn<ComicDownloadTask, Double> progressColumn;
	@FXML
	private TableColumn<ComicDownloadTask, String> urlColumn;
	@FXML
	private TextField urlTextField;
	@FXML
	private ProgressBar totalProgressBar;
	@FXML
	private Label totalProgressBarPercentage;

	@FXML
	private void initialize() {
		ComicNameColumn.setCellValueFactory(cellData -> cellData.getValue().getComicNameProperty());
		statusColumn.setCellValueFactory(new PropertyValueFactory<ComicDownloadTask, String>("message"));
		urlColumn.setCellValueFactory(cellData -> cellData.getValue().getUrlProperty());
		progressColumn.setCellValueFactory(new PropertyValueFactory<ComicDownloadTask, Double>("progress"));
		progressColumn.setCellFactory(ProgressBarTableCell.<ComicDownloadTask> forTableColumn());
	
	}
	
	@FXML
	private void chooseDirectoryAndScan() {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		final File selectedDirectory = directoryChooser.showDialog(mainApp
				.getPrimaryStage());
		if (selectedDirectory != null && !urlTextField.getText().isEmpty()) {
			mainApp.getComicDownloadTasks().clear();
			totalProgressBar.setProgress(0);
			
			String dirPath = selectedDirectory.getAbsolutePath();
			
			executor.submit(new ComicGalleriesTracker(urlTextField.getText(), 1, dirPath, totalProgressBar, totalProgressBarPercentage, mainApp.getComicDownloadTasks(), executor));
		}
	}

	public void setMainApp(MainApp mainApp) {
		// TODO Auto-generated method stub
		this.mainApp = mainApp;
		
		ComicDownloaderTable.setItems(mainApp.getComicDownloadTasks());
	}

}
