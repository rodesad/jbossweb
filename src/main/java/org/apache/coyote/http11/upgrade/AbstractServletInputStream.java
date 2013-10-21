/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.coyote.http11.upgrade;

import javax.servlet.ServletInputStream;

import org.apache.coyote.http11.upgrade.servlet31.ReadListener;

/**
 * Implements the new Servlet 3.1 methods for {@link ServletInputStream}.
 */
public abstract class AbstractServletInputStream extends ServletInputStream {

    /**
     * New Servlet 3.1 method.
     */
    public abstract boolean isFinished();

    /**
     * New Servlet 3.1 method.
     */
    public abstract boolean isReady();

    /**
     * New Servlet 3.1 method.
     */
    public abstract void setReadListener(ReadListener listener);

}