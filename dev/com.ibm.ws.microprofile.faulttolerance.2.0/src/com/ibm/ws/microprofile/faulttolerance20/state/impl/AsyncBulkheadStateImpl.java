/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.microprofile.faulttolerance20.state.impl;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.microprofile.faulttolerance.spi.BulkheadPolicy;
import com.ibm.ws.microprofile.faulttolerance20.state.AsyncBulkheadState;
import com.ibm.ws.threading.PolicyExecutor;
import com.ibm.ws.threading.PolicyExecutorProvider;

/**
 * Implements the asynchronous bulkhead using a {@link PolicyExecutor}
 */
public class AsyncBulkheadStateImpl implements AsyncBulkheadState {

    private final PolicyExecutor executorService;

    public AsyncBulkheadStateImpl(PolicyExecutorProvider executorProvider, BulkheadPolicy policy) {
        this.executorService = executorProvider.create("Fault Tolerance-" + UUID.randomUUID());
        executorService.maxConcurrency(policy.getMaxThreads());
        executorService.maxQueueSize(policy.getQueueSize());
    }

    @Override
    @FFDCIgnore(RejectedExecutionException.class)
    public ExecutionReference submit(Runnable runnable) {
        ExecutionReferenceImpl execution = new ExecutionReferenceImpl();
        try {
            execution.future = executorService.submit(runnable);
            execution.accepted = true;
        } catch (RejectedExecutionException e) {
            execution.accepted = false;
        }
        return execution;
    }

    private class ExecutionReferenceImpl implements ExecutionReference {

        private Future<?> future;
        private boolean accepted = false;

        @Override
        public void abort() {
            if (future != null) {
                future.cancel(true);
            }
        }

        @Override
        public boolean wasAccepted() {
            return accepted;
        }
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

}
