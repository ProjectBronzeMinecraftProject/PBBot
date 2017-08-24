package com.gt22.pbbot.user;

import java.awt.Color;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Classification {
	public static final Classification IRRELEVANT = new Classification("Irrelevant", "https://vignette3.wikia.nocookie.net/pediaofinterest/images/a/a1/S03-WhiteSquare.svg/revision/latest/scale-to-width-down/", Color.WHITE);
	public static final Classification ASSET = new Classification("Asset", "https://vignette1.wikia.nocookie.net/pediaofinterest/images/a/a4/S03-YellowSquare.svg/revision/latest/scale-to-width-down/", Color.YELLOW);
	public static final Classification ANALOG_INTERFACE = new Classification("Analog Interface", "https://vignette1.wikia.nocookie.net/pediaofinterest/images/2/2e/S03-BlackSquareYellowCorners.svg/revision/latest/scale-to-width-down/", Color.YELLOW);
	public static final Classification IRRELEVANT_THREAT = new Classification("Irrelevant Threat", "https://vignette3.wikia.nocookie.net/pediaofinterest/images/d/d2/S03-WhiteSquareRedCorners.svg/revision/latest/scale-to-width-down/", Color.RED);
	public static final Classification RELEVANT_THREAT = new Classification("Relevant Threat", "https://vignette4.wikia.nocookie.net/pediaofinterest/images/4/4c/S03-RedSquare.svg/revision/latest/scale-to-width-down/", Color.RED);
	public static final Classification CATALYST = new Classification("Catalyst", "https://vignette2.wikia.nocookie.net/pediaofinterest/images/2/2e/S03-BlueSquare.svg/revision/latest/scale-to-width-down/", Color.BLUE);
	public static final Classification RELEVANT_ONE = new Classification("Relevant-One", "https://vignette3.wikia.nocookie.net/pediaofinterest/images/a/a3/S05-BlueSquareWhiteCorners.svg/revision/latest/scale-to-width-down/", Color.BLUE);
	public static final Classification UNKNOWN = new Classification("Unknown", "https://cdn.discordapp.com/attachments/197699632841752576/338403812576329728/classes.png", Color.GRAY);
	private static final Map<String, Classification> CLASS_MAP = new HashMap<>();
	private static final Map<Integer, BufferedImage> UNKNOWN_IMAGE_CACHE = new HashMap<>();

	static {
		registerClass(IRRELEVANT);
		registerClass(ASSET);
		registerClass(ANALOG_INTERFACE);
		registerClass(IRRELEVANT_THREAT);
		registerClass(RELEVANT_THREAT);
		registerClass(CATALYST);
		registerClass(RELEVANT_ONE);
		registerClass(UNKNOWN);
		CLASS_MAP.put("admin", ASSET);
		CLASS_MAP.put("threat", IRRELEVANT_THREAT);
		CLASS_MAP.put("primary threat", RELEVANT_THREAT);
		CLASS_MAP.put("competing system", RELEVANT_THREAT);
		CLASS_MAP.put("relevant one", RELEVANT_ONE);
		CLASS_MAP.put("thesystem", UNKNOWN);

	}

	private final String name;
	private final String img;
	private final Color color;
	private Classification(String name, String img, Color color) {
		this.name = name;
		this.img = img;
		this.color = color;
	}

	private static void registerClass(Classification classification) {
		CLASS_MAP.put(classification.getName().toLowerCase(), classification);
	}

	public static Classification getClassification(String name) {
		return getClassification(name, false);
	}

	public static Classification getClassification(String name, boolean strict) {
		name = name.toLowerCase();
		if (CLASS_MAP.containsKey(name)) {
			return CLASS_MAP.get(name);
		}
		for (Map.Entry<String, Classification> e : CLASS_MAP.entrySet()) {
			if (name.contains(e.getKey())) {
				return e.getValue();
			}
		}
		return strict ? null : IRRELEVANT;
	}


	public String getName() {
		return name;
	}

	public String getImg() {
		return getImg(200);
	}

	public String getImg(int size) {
		if (this == UNKNOWN) {
			return img; //NO SVG for Unknown class
		}
		return img + size;
	}

	public Color getColor() {
		return color;
	}
}
