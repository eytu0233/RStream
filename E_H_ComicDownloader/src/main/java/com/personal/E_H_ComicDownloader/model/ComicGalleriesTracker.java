package com.personal.E_H_ComicDownloader.model;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class ComicGalleriesTracker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(MainApp.class);

	private String orignalGalleryURL;
	private String dirPath;
	private int trackerNum;
	private ProgressBar totalProgressBar;
	private Label totalProgressBarPercentage;
	private ObservableList<ComicDownloadTask> comicDownloadTasks;

	private ExecutorService executor;

	private double total = 0, downloaded = 0;

	public ComicGalleriesTracker(String orignalGalleryURL, int trackerNum, String dirPath, ProgressBar totalProgressBar,
			Label totalProgressBarPercentage, ObservableList<ComicDownloadTask> comicDownloadTasks,
			ExecutorService executor) {
		super();
		this.orignalGalleryURL = orignalGalleryURL;
		this.trackerNum = trackerNum;
		this.dirPath = dirPath;
		this.totalProgressBar = totalProgressBar;
		this.totalProgressBarPercentage = totalProgressBarPercentage;
		this.comicDownloadTasks = comicDownloadTasks;
		this.executor = executor;
	}

	public static void main(String[] args) {
		ExecutorService executor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		String url1 = "http://g.e-hentai.org/?f_doujinshi=0&f_manga=1&f_artistcg=0&f_gamecg=0&f_western=0&f_non-h=0&f_imageset=0&f_cosplay=0&f_asianporn=0&f_misc=0&f_search=sea&f_apply=Apply+Filter";
		String url2 = "http://g.e-hentai.org/?page=1&f_manga=on&f_search=%E6%BC%A2%E5%8C%96&f_apply=Apply+Filter";
		String url3 = "http://g.e-hentai.org/manga/0";
//		executor.submit(new ComicGalleriesTracker(url2, 1, "", executor));
		try {
			executor.awaitTermination(1000, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		final int PAGE_COMIC_MAX = 25;
		Pattern p = Pattern.compile("Showing (\\d+)-(\\d+) of (\\d+)");

		String currentURL = "", templateURL = "";
		Document doc = null;
		int currentPageNum = 0, maxPageNum = 0;

		currentURL = orignalGalleryURL;
		try {
			doc = Jsoup.connect(currentURL).get();
			/* 從頁面找出漫畫的最大數量並算出當前頁數跟最大頁數 */
			Elements itemNum = doc.select("p[class=ip]");
			for (Element e : itemNum) {
				Matcher m = p.matcher(e.text().replace(",", ""));
				if (m.matches()) {
					currentPageNum = Math.round(Integer.valueOf(m.group(2)) / PAGE_COMIC_MAX);
					maxPageNum = Math.round(Integer.valueOf(m.group(3)) / PAGE_COMIC_MAX);
					log.debug("currentPageNum : " + currentPageNum);
					log.debug("maxPageNum : " + maxPageNum);
					maxPageNum = (currentPageNum + trackerNum - 1 < maxPageNum) ? currentPageNum + trackerNum - 1
							: maxPageNum;
					break;
				}
			}

			/* 在頁數切換列找出頁數間的共有URL模板 */
			Elements tds = doc.select("table[class=ptt]").select("td");
			for (Element e : tds) {
				if (">".equals(e.text())) {
					// 避免URL模板因為Unicode的'%'而導致format發生錯誤會先將'%'取代成'\\u'之後會取代回來
					templateURL = e.select("a").attr("href").replace("%", "\\u")
							.replaceFirst(Integer.toString(currentPageNum), "%d");

					log.debug("templateURL : " + templateURL);
					break;
				}
			}
			if (templateURL.isEmpty())
				return;

			for (int index = currentPageNum; index <= maxPageNum; index++) {
				executor.submit(new ComicGallery(String.format(templateURL, index - 1).replace("\\u", "%"), dirPath,
						comicDownloadTasks, this, executor));
			}

			// executor.shutdown();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public synchronized void increaseTotal() {
		total++;
		updateTotalProgressBar();
	}

	public synchronized void increaseDownloaded() {
		downloaded++;
		updateTotalProgressBar();
	}

	public synchronized void updateTotalProgressBar() {
		double percentage = downloaded / total;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				totalProgressBar.setProgress(percentage);
				totalProgressBarPercentage.setText(String.format("%.1f%%", percentage * 100));
			}
		});
	}

}
