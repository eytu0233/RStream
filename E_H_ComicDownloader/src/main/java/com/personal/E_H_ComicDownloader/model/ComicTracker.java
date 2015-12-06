package com.personal.E_H_ComicDownloader.model;

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

import com.personal.E_H_ComicDownloader.MainApp;

public class ComicTracker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(MainApp.class);

	private static final String DOWNLOADING_FILE_NAME = "DOWNLOADING";
	private static final String FAIL_LIST_FILE_NAME = "failList.txt";

	private static final int PAGE_IMAGE_MAX = 40;

	private String comicURL;
	private String comicTitle;

	private int finish = 0;
	private int imageNum;

	private LinkedList<String> imageURLs = new LinkedList<String>();
	private LinkedList<String> failList = new LinkedList<String>();

	private static ExecutorService executor = Executors.newScheduledThreadPool(3, new ThreadFactory() {
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			return t;
		}
	});

	public ComicTracker(String comicURL, String comicTitle) {
		super();
		this.comicURL = comicURL;
		this.comicTitle = comicTitle.replaceAll("[/|\\?]", "");
	}

	public void run() {
		// TODO Auto-generated method stub
		Pattern p = Pattern.compile("Showing (\\d+) - (\\d+) of (\\d+) images");
		int currentPageNum = 1, maxPageNum = 1;
		String templateURL = null;

		log.debug(comicTitle);

		try {
			Document doc = Jsoup.connect(comicURL).get();
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
				templateURL = comicURL + "?p=%d";
			}
			// log.debug("templateURL : " + templateURL);

			for (int index = currentPageNum; index <= maxPageNum; index++) {
				comicPageScanner(String.format(templateURL, index - 1));
			}

			writeDownloadingFile();
			
			for (String imageURL : imageURLs) {
				File destination = new File(comicTitle + "/" + String.format("%03d.jpg", ++imageNum));
				if (!destination.exists()) {
					executor.submit(new ImageDownloader(imageURL, destination, this));
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

	private void showProgress() {
		log.debug(comicTitle + " : " + (String.format("%.1f%%", (double) finish / imageNum * 100)));
		if (finish == imageNum) {
			log.debug(comicTitle + " complete...");
			deleteDownloadingFile();
			if (failList.size() > 0) {
				log.debug(comicTitle + " failList : " + failList.size());
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

	private void writeDownloadingFile() {
		try {
			File DownloadingListFile = new File(comicTitle + "/" + DOWNLOADING_FILE_NAME);
			DownloadingListFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void deleteDownloadingFile() {
		File DownloadingListFile = new File(comicTitle + "/" + DOWNLOADING_FILE_NAME);
		DownloadingListFile.delete();
	}

	private void writeFailList() {
		try {
			File failListFile = new File(comicTitle + "/" + FAIL_LIST_FILE_NAME);
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
