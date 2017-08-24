package com.gt22.pbbot.interfaces;

public interface ITMBModule {

	void init() throws Exception;
	String name();
	boolean isReloadable();
	void reload() throws Exception;
}
