/**
 * 
 */
package net.weta.components.communication.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import net.weta.components.communication.tcp.server.CommunicationServer;

import org.apache.log4j.Logger;

/**
 * Pooled Executor singlton. Provides access to a thread pool.
 * 
 * 
 * @author joachim
 * 
 */
public class PooledThreadExecutor {

	private static ThreadPoolExecutor executorService = null;
	
    private static final Logger LOG = Logger.getLogger(PooledThreadExecutor.class);

	private PooledThreadExecutor() {
	};

	public static ThreadPoolExecutor getInstance() {
		if (executorService == null) {
			executorService = (ThreadPoolExecutor)Executors.newCachedThreadPool();
		}
		return executorService;
	}

	public static void execute(Runnable command) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Number of current running tasks: " + PooledThreadExecutor.getInstance().getActiveCount());
			LOG.debug("Thread pool size: " + PooledThreadExecutor.getInstance().getPoolSize());
			LOG.debug("All time number of scheduled tasks: " + PooledThreadExecutor.getInstance().getTaskCount());
		}
		PooledThreadExecutor.getInstance().execute(command);
	}

	public static Future<?> commit(Runnable command) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Number of current running tasks: " + PooledThreadExecutor.getInstance().getActiveCount());
			LOG.debug("Thread pool size: " + PooledThreadExecutor.getInstance().getPoolSize());
			LOG.debug("All time number of scheduled tasks: " + PooledThreadExecutor.getInstance().getTaskCount());
		}
		return PooledThreadExecutor.getInstance().submit(command);
	}
	
	public static void remove(Runnable task) {
		PooledThreadExecutor.getInstance().remove(task);
	}
	
	public static void purge() {
		if (LOG.isInfoEnabled()) {
			LOG.info("Before purge: Number of current running tasks: " + PooledThreadExecutor.getInstance().getActiveCount() + "; Thread pool size: " + PooledThreadExecutor.getInstance().getPoolSize() + "; Working queue size: " + PooledThreadExecutor.getInstance().getQueue().size());
		}
		PooledThreadExecutor.getInstance().purge();
		if (LOG.isInfoEnabled()) {
			LOG.info("After purge: Number of current running tasks: " + PooledThreadExecutor.getInstance().getActiveCount() + "; Thread pool size: " + PooledThreadExecutor.getInstance().getPoolSize() + "; Working queue size: " + PooledThreadExecutor.getInstance().getQueue().size());
		}
	}

}
