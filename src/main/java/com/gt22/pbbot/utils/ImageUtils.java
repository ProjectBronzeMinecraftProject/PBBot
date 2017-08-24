package com.gt22.pbbot.utils;

import com.gt22.pbbot.Core;
import com.gt22.randomutils.Instances;
import org.apache.http.client.methods.RequestBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;

public class ImageUtils {
	private static final HashMap<String, BufferedImage> IMAGE_CACHE = new HashMap<>();
	private static final HashMap<String, ManualFuture<BufferedImage>> SCHEDULED_FUTURES = new HashMap<>();
	public static BufferedImage mergeImages(BufferedImage img1, BufferedImage img2) throws IOException {
		img2 = resize(
				img2,
				img1.getWidth(),
				img1.getHeight()
		);
		BufferedImage ret = new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = ret.createGraphics();
		g.drawImage(img1, 0, 0, null);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f));
		g.drawImage(img2, 0, 0, null);
		g.dispose();

		return ret;
	}

	public static BufferedImage resize(BufferedImage img, int newW, int newH) {
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_AREA_AVERAGING);
		BufferedImage ret = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = ret.createGraphics();
		g.drawImage(tmp, 0, 0, null);
		g.dispose();

		return ret;
	}


	public static ManualFuture<BufferedImage> readImg(String url) {
		ManualFuture<BufferedImage> ret = new ManualFuture<>();

		if (!IMAGE_CACHE.containsKey(url)) {
			Core.getCoreLog().debug("Loading image " + url);
			IMAGE_CACHE.put(url, null); //Placeholder to send only one request
			Instances.getExecutor().execute(() -> {
				try {
					BufferedImage read = ImageIO.read(Instances.getHttpClient().execute(RequestBuilder.get(url).build()).getEntity().getContent());
					IMAGE_CACHE.put(url, read);
					ret.complete(read);
					SCHEDULED_FUTURES.forEach((u, f) -> f.complete(read));
					Core.getCoreLog().debug("Image " + url + " loaded");

				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		} else {
			BufferedImage cached = IMAGE_CACHE.get(url);
			if(cached == null) { //Image loading in progress
				SCHEDULED_FUTURES.put(url, ret);
			} else {
				ret.complete(IMAGE_CACHE.get(url));
			}
		}
		return ret;
	}
}