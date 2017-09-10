package com.gt22.pbbot.discord.utils;

import com.gt22.pbbot.Core;
import com.gt22.pbbot.utils.JavaHttpRequestBuilder;
import com.gt22.randomutils.Instances;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.iharder.Base64;
import org.apache.http.client.methods.RequestBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.function.Supplier;

public class EmbedUtils {

	public static MessageEmbed create(int color, String title, String message, String img) {
		return create(new Color(color), title, message, img);
	}

	public static MessageEmbed create(Color color, String title, String message, String img) {
		return new EmbedBuilder()
				.setTitle(title, null)
				.appendDescription(message)
				.setColor(color)
				.setThumbnail(img)
				.build();
	}

	public static String convertImgToURL(Supplier<BufferedImage> img, String name) throws IOException {
		return imageToDBUrl(name, img);
	}

	private static String imageToDBUrl(String name, Supplier<BufferedImage> img) throws IOException {
		name = URLEncoder.encode(name, "UTF-8");
		BufferedReader check = new BufferedReader(new InputStreamReader(new JavaHttpRequestBuilder(Core.getConfig().IMAGE_DB_MANAGER + "?mode=check&name=" + name).build().getInputStream()));
		String res = check.readLine();
		check.close();
		if (res.equals("false")) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			Base64.OutputStream b64 = new Base64.OutputStream(os);
			ImageIO.write(img.get(), "png", b64);
			b64.close();
			String base64 = os.toString("UTF-8");
			new JavaHttpRequestBuilder(Core.getConfig().IMAGE_DB_MANAGER + "?mode=add&name=" + name)
					.addPostParam("img", base64)
					.build().getInputStream().read(); //Read required to make sure that request started
		} else if (!res.equals("true")) {
			Core.getCoreLog().fatal("Unable to check is image present! First line of response: " + res);
		}
		return String.format("%s?mode=get&name=%s", Core.getConfig().IMAGE_DB_MANAGER, name);
	}

	public static void deleteConvertedImage(String name) throws IOException {
		Instances.getHttpClient().execute(RequestBuilder.get("http://52.48.142.75/imagedecoder.php")
				.addParameter("mode", "remove")
				.addParameter("name", name)
				.build());
	}

}
