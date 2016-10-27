package com.projectbronze.pbbot.utils;

import java.util.Optional;

public class Wrapper<T>
{
	public static enum WrapperState
	{
		SINGLE, MULTI, EMPTY;
	}

	public Optional<T> single;
	public Optional<T[]> multipile;
	public WrapperState state;

	public Wrapper(T single)
	{
		if(single == null)
		{
			empty();
		}
		else
		{
			single(single);
		}
	}

	public Wrapper(T[] multipile)
	{
		if (multipile.length == 0)
		{
			empty();
		}
		else if (multipile.length == 1)
		{
			single(multipile[0]);
		}
		else
		{
			multi(multipile);
		}
	}

	public Wrapper()
	{
		empty();
	}
	
	private void single(T usr)
	{
		this.single = Optional.of(usr);
		this.multipile = Optional.empty();
		this.state = WrapperState.SINGLE;
	}
	
	private void multi(T[] usr)
	{
		this.single = Optional.empty();
		this.multipile = Optional.of(usr);
		this.state = WrapperState.MULTI;
	}
	
	private void empty()
	{
		this.single = Optional.empty();
		this.multipile = Optional.empty();
		state = WrapperState.EMPTY;
	}
}
