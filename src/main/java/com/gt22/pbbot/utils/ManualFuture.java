package com.gt22.pbbot.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.*;

public class ManualFuture<T> implements Future<T> {
	private CountDownLatch lock = new CountDownLatch(1);
	private T val;
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		throw new UnsupportedOperationException("Cannot cancel manual future");
	}

	@Override
	public boolean isCancelled() {
		throw new UnsupportedOperationException("Cannot cancel manual future");
	}

	@Override
	public boolean isDone() {
		return val != null;
	}

	@Nullable
	public T get() throws InterruptedException {
		lock.await();
		return val;
	}

	@Nonnull
	@Override
	public T get(long timeout, @Nonnull TimeUnit unit) throws InterruptedException, TimeoutException {
		lock.await(timeout, unit);
		assert val != null; //If lock completed and value is still null then something is really wrong
		return val;
	}

	public void complete(@Nonnull T val) {
		this.val = val;
		lock.countDown();
	}

}