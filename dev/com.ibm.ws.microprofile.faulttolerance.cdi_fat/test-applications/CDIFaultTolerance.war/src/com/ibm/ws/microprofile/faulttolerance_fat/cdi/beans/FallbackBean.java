/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.microprofile.faulttolerance_fat.cdi.beans;

import javax.enterprise.context.RequestScoped;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;

import com.ibm.ws.microprofile.faulttolerance_fat.util.ConnectException;
import com.ibm.ws.microprofile.faulttolerance_fat.util.Connection;

/**
 * Servlet implementation class Test
 */
@RequestScoped
@Retry(maxRetries = 2)
public class FallbackBean {

    private int connectCountA = 0;
    private int connectCountB = 0;

    @Fallback(MyFallbackHandler.class)
    public Connection connectA() throws ConnectException {
        throw new ConnectException("FallbackBean.connectA: " + (++connectCountA));
    }

    @Fallback(MyFallbackHandler.class)
    public Connection connectB(String param) throws ConnectException {
        throw new ConnectException("FallbackBean.connectB: " + (++connectCountB));
    }

    public Connection fallback(ExecutionContext executionContext) {
        return new Connection() {

            @Override
            public String getData() {
                return "Fallback Connection: " + executionContext.getMethod().getName();
            }
        };
    }

    public int getConnectCountA() {
        return connectCountA;
    }

    public int getConnectCountB() {
        return connectCountB;
    }

}
