package com.gt22.pbbot.discord.misc;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.awt.*;
import java.util.function.Predicate;

public class AdvancedCategory extends Command.Category {
	protected final Color color;
	protected final String img;
	public AdvancedCategory(String name, Color color, String img) {
		super(name);
		this.color = color;
		this.img = img;
	}

	public AdvancedCategory(String name, Predicate<CommandEvent> predicate, Color color, String img) {
		super(name, predicate);
		this.color = color;
		this.img = img;
	}

	public AdvancedCategory(String name, String failResponse, Predicate<CommandEvent> predicate, Color color, String img) {
		super(name, failResponse, predicate);
		this.color = color;
		this.img = img;
	}

	public Color getColor() {
		return color;
	}

	public String getImg() {
		return img;
	}
}
