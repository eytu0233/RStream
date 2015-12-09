package com.personal.E_H_ComicDownloader.model;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.personal.E_H_ComicDownloader.ComicDownloadTask;
import com.personal.E_H_ComicDownloader.MainApp;

public class ImageDownloader implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(MainApp.class);

	private ComicDownloadTask comic;

	private String pageURL;
	private File destination;

	public ImageDownloader(String pageURL, File destination, ComicDownloadTask comic) {
		// TODO Auto-generated constructor stub
		this.pageURL = pageURL;
		this.destination = destination;
		this.comic = comic;
	}

	public void run() {
		// TODO Auto-generated method stub		
		int tryTry = 3;
		int timeout = 2000;

		try {
			Document doc = Jsoup.connect(pageURL).get();
			Elements divs = doc.select("div[id=i3]");
			for (Element div : divs) {
				String imageURL = div.select("img").attr("src");
				tryTry = 3;

				while (tryTry-- > 0) {
					try {
						FileUtils.copyURLToFile(new URL(imageURL), destination, timeout, timeout);
						break;
					} catch (IOException ioe) {
						timeout <<= 1;
						// log.debug(ioe.getMessage() + " : " + imageURL);
					}
				}

				if (tryTry > 0) {
					comic.succeed();
				} else {
					comic.fail(imageURL);
				}
				break;
			}

		} catch (SocketTimeoutException e) {
			log.debug(pageURL + "time out");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
