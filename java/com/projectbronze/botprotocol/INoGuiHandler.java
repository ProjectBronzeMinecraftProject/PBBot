package com.projectbronze.botprotocol;

public interface INoGuiHandler {
	public String readLine() throws Exception;
	public void writeLine(String line) throws Exception;
}
