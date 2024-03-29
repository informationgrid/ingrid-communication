/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
/**
 * 
 */
package net.weta.components.communication.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Pooled Executor singlton. Provides access to a thread pool.
 * 
 * 
 * @author joachim
 * 
 */
public class PooledThreadExecutor {

    private static ThreadPoolExecutor executorService = null;

    private static final Logger LOG = LogManager.getLogger( PooledThreadExecutor.class );

    private PooledThreadExecutor() {};

    public static ThreadPoolExecutor getInstance() {
        if (executorService == null) {
            executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        }
        return executorService;
    }

    public static void execute(Runnable command) {
        if (LOG.isDebugEnabled()) {
            LOG.debug( "Number of current running tasks: " + PooledThreadExecutor.getInstance().getActiveCount() );
            LOG.debug( "Thread pool size: " + PooledThreadExecutor.getInstance().getPoolSize() );
            LOG.debug( "All time number of scheduled tasks: " + PooledThreadExecutor.getInstance().getTaskCount() );
        }
        PooledThreadExecutor.getInstance().execute( command );
    }

    public static Future<?> submit(Runnable command) {
        if (LOG.isDebugEnabled()) {
            LOG.debug( "Number of current running tasks: " + PooledThreadExecutor.getInstance().getActiveCount() );
            LOG.debug( "Thread pool size: " + PooledThreadExecutor.getInstance().getPoolSize() );
            LOG.debug( "All time number of scheduled tasks: " + PooledThreadExecutor.getInstance().getTaskCount() );
        }
        return PooledThreadExecutor.getInstance().submit( command );
    }

    public static void purge() {
        if (LOG.isInfoEnabled()) {
            LOG.info( "Before purge: Number of current running tasks: " + PooledThreadExecutor.getInstance().getActiveCount() + "; Thread pool size: "
                    + PooledThreadExecutor.getInstance().getPoolSize() + "; Working queue size: " + PooledThreadExecutor.getInstance().getQueue().size() );
        }
        PooledThreadExecutor.getInstance().purge();
        if (LOG.isInfoEnabled()) {
            LOG.info( "After purge: Number of current running tasks: " + PooledThreadExecutor.getInstance().getActiveCount() + "; Thread pool size: "
                    + PooledThreadExecutor.getInstance().getPoolSize() + "; Working queue size: " + PooledThreadExecutor.getInstance().getQueue().size() );
        }
    }

}
