/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package javax.servlet;

/**
 * @version $Rev: 743426 $ $Date: 2009-02-11 18:52:39 +0100 (Wed, 11 Feb 2009) $
 * @since 3.0
 */
public interface AsyncContext {

    String ASYNC_CONTEXT_PATH = "javax.servlet.async.context_path";
    String ASYNC_PATH_INFO = "javax.servlet.async.path_info";
    String ASYNC_QUERY_STRING = "javax.servlet.async.query_string";
    String ASYNC_REQUEST_URI = "javax.servlet.async.request_uri";
    String ASYNC_SERVLET_PATH = "javax.servlet.async.servlet_path";

    void complete();

    void dispatch();

    void dispatch(ServletContext servletContext, String path);

    ServletRequest getRequest();

    ServletResponse getResponse();

    boolean hasOriginalRequestAndResonse();

    void start(Runnable run);
}
