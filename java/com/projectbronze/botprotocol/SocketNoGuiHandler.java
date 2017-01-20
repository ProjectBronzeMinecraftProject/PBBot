package com.projectbronze.botprotocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.projectbronze.pbbot.Core;


public class SocketNoGuiHandler implements INoGuiHandler, AutoCloseable {
	private Socket sc;
	private final ServerSocket ssc;
	private DataInputStream r;
	private DataOutputStream w;
	private boolean gotStop = false;

	public SocketNoGuiHandler() throws IOException {
		ssc = new ServerSocket(21025);
		Core.log.info("Socket started");
		awaitConnect();
	}

	@Override
	public String readLine() throws Exception {
		try {
			return r.readUTF();
		} catch (EOFException | SocketException e) {
			Core.log.info("Connection lost");
			awaitConnect();
			return readLine();
		}
	}

	@Override
	public void writeLine(String line) throws Exception {
		w.writeUTF(line);
		w.flush();
	}

	@Override
	public void close() throws Exception {
		sc.close();
		ssc.close();
	}
	
	private void awaitConnect() throws IOException
	{
		Core.log.info("Waiting for connection");
		sc = ssc.accept();
		Core.log.info("Got connection from %s", sc.getInetAddress().getHostAddress());
		r = new DataInputStream(sc.getInputStream());
		w = new DataOutputStream(sc.getOutputStream());
	}

}
