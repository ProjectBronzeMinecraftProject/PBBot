package com.gt22.pbbot.router;

import com.gt22.botrouter.api.events.SubscribeEvent;
import com.gt22.botrouter.api.events.message.MessageReceivedEvent;
import com.gt22.botrouter.api.events.state.ReadyEvent;
import com.gt22.botrouter.api.interfaces.IMessage;
import com.gt22.pbbot.Core;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.List;

public class RouterEventHandler {

	@SubscribeEvent
	public void ready(ReadyEvent e) throws IOException {
		RouterCore.getRouterLog().info("Router connector initialized");
	}

	@SubscribeEvent
	public void msg(MessageReceivedEvent e) throws SignatureException, InvalidKeyException {
		IMessage m = e.getMsg();
		List<String> args = m.getArgs();
		String id = args.get(0);
		Pair<String, PublicKey> k = Core.getConfig().PUBLIC_KEYS.get(id);
		if (k != null) {
			if(args.size() != 3) {
				RouterCore.getRouterLog().warn("Got message from " + k.getKey() + ", but it is not in TMB format");
				return;
			}
			String json = args.get(1);
			boolean verified = verifySignature(k.getValue(), json, args.get(2));
			System.out.println("Message from " + k.getKey() + ": " + json + ", Signature: " + (verified ? "Verified" : "UNVERIFIED"));
		} else {
			System.out.println("Message from unknown source: " + m.getSourceString());
		}
	}

	private boolean verifySignature(PublicKey key, String content, String signature) throws SignatureException, InvalidKeyException {
		Signature sig = RouterCore.getSig();
		sig.initVerify(key);
		sig.update((RouterCore.getBot().getId() + "@" + content).getBytes(StandardCharsets.UTF_8));
		return sig.verify(Base64.getDecoder().decode(signature.getBytes(StandardCharsets.UTF_8)));
	}

}
