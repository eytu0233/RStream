package com.personal.E_H_ComicDownloader.model;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.personal.E_H_ComicDownloader.ComicDownloadTask;
import com.personal.E_H_ComicDownloader.MainApp;

import javafx.application.Platform;
import javafx.collections.ObservableList;

public class ComicGallery implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(MainApp.class);

	private String galleryURL;

	private ObservableList<ComicDownloadTask> comicDownloadTasks;
	
	private ComicGalleriesTracker comicGalleriesTracker;
	
	private ExecutorService executor;
	
	public ComicGallery(String galleryURL, ObservableList<ComicDownloadTask> comicDownloadTasks, ComicGalleriesTracker comicGalleriesTracker, ExecutorService executor) {
		super();
		this.galleryURL = galleryURL;
		this.comicDownloadTasks = comicDownloadTasks;
		this.comicGalleriesTracker = comicGalleriesTracker;
		this.executor = executor;
	}

	public void run() {
		// TODO Auto-generated method stub
		log.debug("galleryURL : " + galleryURL);
		try {
			Document doc = Jsoup.connect(galleryURL).get();
			Elements divs = doc.select("div[class=it5]");
			for(Element div : divs){
				ComicDownloadTask comic = new ComicDownloadTask(div.select("a").attr("href"), div.select("a").text(), this);
				Platform.runLater(()->comicDownloadTasks.add(comic));
				executor.submit(comic);
				comicGalleriesTracker.increaseTotal();
//				log.debug(div.select("a").text());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void finish(){
		comicGalleriesTracker.increaseDownloaded();
	}

}
