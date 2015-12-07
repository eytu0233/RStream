package com.personal.E_H_ComicDownloader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.personal.E_H_ComicDownloader.model.ComicGallery;
import com.personal.E_H_ComicDownloader.model.ImageDownloader;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

public class ComicDownloadTask extends Task<Void>{
	
	private static final Logger log = LoggerFactory.getLogger(MainApp.class);
	
	private static final String FAIL_LIST_FILE_NAME = "failList.txt";

	private static final int PAGE_IMAGE_MAX = 40;
	
	private static ExecutorService executor = Executors.newScheduledThreadPool(3, new ThreadFactory() {
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			return t;
		}
	});
	
	private ComicGallery comicGallery;

	private StringProperty comicName;
	private StringProperty url;
	
	private int finish = 0;
	private int imageNum;
	
	private LinkedList<String> imageURLs = new LinkedList<String>();
	private LinkedList<String> failList = new LinkedList<String>();

	
	public ComicDownloadTask(String url, String comicName, ComicGallery comicGallery){
		this.comicName = new SimpleStringProperty(comicName);
		this.url = new SimpleStringProperty(url);
		this.comicGallery = comicGallery;
		
		updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, 1);
		updateMessage("等待排程");
	}
	
	@Override
	protected Void call() throws Exception {
		// TODO Auto-generated method stub
		
		this.updateMessage("下載中");
		
		Pattern p = Pattern.compile("Showing (\\d+) - (\\d+) of (\\d+) images");
		int currentPageNum = 1, maxPageNum = 1;
		String templateURL = null;

		try {
			Document doc = Jsoup.connect(url.get()).get();
			/* 從頁面找出漫畫當前頁數跟最大頁數 */
			Elements itemNum = doc.select("p[class=gpc]");
			for (Element e : itemNum) {
				Matcher m = p.matcher(e.text().replace(",", ""));
				if (m.matches()) {
					currentPageNum = (int) Math.ceil(Double.valueOf(m.group(2)) / PAGE_IMAGE_MAX);
					maxPageNum = (int) Math.ceil(Double.valueOf(m.group(3)) / PAGE_IMAGE_MAX);
					log.debug("currentPageNum : " + currentPageNum);
					log.debug("maxPageNum : " + maxPageNum);
					break;
				}
			}

			/* 在頁數切換列找出頁數間的共有URL模板 */
			Elements tds = doc.select("table[class=ptt]").select("td");
			for (Element e : tds) {
				if (">".equals(e.text())) {
					templateURL = e.select("a").attr("href").replaceFirst("p=" + Integer.toString(currentPageNum),
							"p=%d");
					break;
				}
			}

			if (templateURL == null || templateURL.isEmpty()) {
				templateURL = url.get() + "?p=%d";
			}
			// log.debug("templateURL : " + templateURL);

			for (int index = currentPageNum; index <= maxPageNum; index++) {
				comicPageScanner(String.format(templateURL, index - 1));
			}
			
			for (String imageURL : imageURLs) {
				File destination = new File(comicName.get() + "/" + String.format("%03d.jpg", ++imageNum));
				if (!destination.exists()) {
					executor.submit(new ImageDownloader(url.get(), destination, this));
				} else {
					succeed();
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public StringProperty getComicNameProperty() {
		// TODO Auto-generated method stub
		if (comicName == null)
			this.comicName = new SimpleStringProperty(this, "comicName");
		return comicName;
	}

	public StringProperty getUrlProperty() {
		// TODO Auto-generated method stub
		if (url == null)
			this.url = new SimpleStringProperty(this, "url");
		return url;
	}
	

	public synchronized void succeed() {
		finish++;
		showProgress();
	}

	public synchronized void fail(String failURL) {
		finish++;
		failList.add(failURL);
		showProgress();
	}

	private synchronized void showProgress() {
		log.debug(comicName.get() + " : " + (String.format("%.1f%%", (double) finish / imageNum * 100)));
		Platform.runLater(()->updateProgress((double) finish / imageNum, 1));
		if (finish == imageNum) {
			log.debug(comicName.get() + " complete...");
			comicGallery.finish();
			Platform.runLater(()->this.updateMessage("下載完成"));
			if (failList.size() > 0) {
				log.debug(comicName.get() + " failList : " + failList.size());
				writeFailList();
			}
		}
	}

	private void comicPageScanner(String url) {
		log.debug("comicPageScanner : " + url);

		try {
			Document doc = Jsoup.connect(url).get();
			Elements divs = doc.select("div[class=gdtm]");
			for (Element div : divs) {
				imageURLs.add(div.select("a").attr("href"));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeFailList() {
		try {
			File failListFile = new File(comicName.get() + "/" + FAIL_LIST_FILE_NAME);
			FileWriter fw = new FileWriter(failListFile);
			for (String failURL : failList) {
				fw.write(failURL + "\r\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
