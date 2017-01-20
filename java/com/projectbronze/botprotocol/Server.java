package com.projectbronze.botprotocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.projectbronze.botprotocol.BotProtocol.Command;
import com.projectbronze.pbbot.Core;

public class Server {
	static SocketNoGuiHandler handler;
	private static final List<Function<String, String>> listeners = new ArrayList<>();
	public static void init() {
		new Thread(() -> {
			try {
				Core.log.info("Server launching");
				BotProtocol.init();
				handler = new SocketNoGuiHandler();
				while (true) {
					String line = handler.readLine();
					String[] parts = line.split(BotProtocol.DIVIDER);
					if (parts.length < 1) {
						continue;
					}
					String cmd = parts[0];
					Command cm = BotProtocol.getCommand(cmd);
					if (cm != null) {
						try {
							String ret = cm.action.perform(line, Arrays.stream(parts).skip(1).collect(Collectors.toList()));
							if (ret != null && !ret.isEmpty()) {
								write(ret);
							}
						} catch (IllegalArgumentException e) {
							write(BotProtocol.NEA.form(e.getMessage()));
						}

					} else {
						write(BotProtocol.INVALID_CMD);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	public static void write(String s) throws Exception {
		handler.writeLine(s);
	}

}
