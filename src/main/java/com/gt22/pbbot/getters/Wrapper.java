package com.gt22.pbbot.getters;

import java.util.List;
import java.util.Optional;

public class Wrapper<T> {



	public enum WrapperState {
		SINGLE,
		MULTI,
		NONE
	}

	private T single;
	private List<T> multi;
	private WrapperState state;
	public Wrapper() {
		none();
	}

	public Wrapper(T single) {
		if(single == null) {
			none();
		} else {
			single(single);
		}
	}

	public Wrapper(List<T> multi) {
		switch (multi.size()) {
			case 0: {
				none();
				break;
			}
			case 1: {
				single(multi.get(0));
				break;
			}
			default: {
				multi(multi);
			}
		}
	}

	public Optional<T> getSingle() {
		return Optional.ofNullable(single);
	}

	public Optional<List<T>> getMulti() {
		return Optional.of(multi);
	}

	public WrapperState getState() {
		return state;
	}

	private void single(T e) {
		single = e;
		multi = null;
		state = WrapperState.SINGLE;
	}

	private void multi(List<T> e) {
		single = null;
		multi = e;
		state = WrapperState.MULTI;
	}

	private void none() {
		single = null;
		multi = null;
		state = WrapperState.NONE;
	}



}
