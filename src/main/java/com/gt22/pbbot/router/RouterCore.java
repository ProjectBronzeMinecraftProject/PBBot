package com.gt22.pbbot.router;

import com.google.gson.JsonObject;
import com.gt22.botrouter.api.ApiBinder;
import com.gt22.botrouter.api.interfaces.IBotDescriptor;
import com.gt22.botrouter.api.misc.RsaKeyLoaders;
import com.gt22.pbbot.Core;
import com.gt22.pbbot.interfaces.ITMBModule;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.UUID;

public class RouterCore implements ITMBModule {

	private static IBotDescriptor bot;
	private static PrivateKey sigKey;
	private static Signature sig;
	private static SimpleLog LOG = SimpleLog.getLog("TMBot#Router");
	public void init() throws Exception {
		sig  = Signature.getInstance("SHA256withRSA");
		sigKey = RsaKeyLoaders.loadPrivate(Core.getConfig().PRIVATE_KEY);
		bot = ApiBinder.getBinder().connector().addListener(new RouterEventHandler()).connect(UUID.fromString(Core.getConfig().ROUTER_ID), Core.getConfig().ROUTER_IP);
	}

	@Override
	public String name() {
		return "RouterConnector";
	}

	@Override
	public boolean isReloadable() {
		return true;
	}

	@Override
	public void reload() throws Exception {
		bot.close();
		init();
	}

	public static void send(String to, JsonObject o) throws IOException {
		String json = o.toString();
		byte[] signature;
		try {
			sig.initSign(sigKey);
			sig.update((to + "@" + json).getBytes(StandardCharsets.UTF_8));
			signature = sig.sign();

		} catch (InvalidKeyException | SignatureException e) {
			throw new RuntimeException(e);
		}
		bot.writeMessage(ApiBinder.getBinder().messages().msg("CMD", "TO", to, json, new String(Base64.getEncoder().encode(signature), StandardCharsets.UTF_8)));
	}

	public static IBotDescriptor getBot() {
		return bot;
	}

	public static Signature getSig() {
		return sig;
	}

	public static SimpleLog getRouterLog() {
		return LOG;
	}
}
