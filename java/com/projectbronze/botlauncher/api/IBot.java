package com.projectbronze.botlauncher.api;

import java.io.PrintStream;

import net.dv8tion.jda.JDA;

public interface IBot {
	public void start(PrintStream infoStream);

	public default void stop(boolean free) {
		getBot().shutdown(free);
	}

	public default void restart(PrintStream infoStream) {
		stop(false);
		start(infoStream);
	}

	public JDA getBot();
}
