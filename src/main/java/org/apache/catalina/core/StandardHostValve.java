/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.core;


import static org.jboss.web.CatalinaMessages.MESSAGES;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.valves.ValveBase;
import org.jboss.servlet.http.HttpEvent;
import org.jboss.servlet.http.HttpEvent.EventType;
import org.jboss.web.CatalinaLogger;


/**
 * Valve that implements the default basic behavior for the
 * <code>StandardHost</code> container implementation.
 * <p>
 * <b>USAGE CONSTRAINT</b>:  This implementation is likely to be useful only
 * when processing HTTP requests.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1880 $ $Date: 2011-12-01 12:47:33 +0100 (Thu, 01 Dec 2011) $
 */

final class StandardHostValve
    extends ValveBase {

    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardHostValve/1.0";


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Select the appropriate child Context to process this request,
     * based on the specified request URI.  If no matching Context can
     * be found, return an appropriate HTTP error.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     * @param valveContext Valve context used to forward to the next Valve
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    public final void invoke(Request request, Response response)
        throws IOException, ServletException {

        // Select the Context to be used for this Request
        Context context = request.getContext();
        if (context == null) {
            response.sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 MESSAGES.noContext());
            return;
        }

        // Bind the context CL to the current thread
        if (context.getLoader() != null) {
            Thread.currentThread().setContextClassLoader(context.getLoader().getClassLoader());
        }
        context.getThreadBindingListener().bind();

        // Enter application scope
        Object instances[] = context.getApplicationEventListeners();

        ServletRequestEvent event = null;

        if ((instances != null) 
                && (instances.length > 0)) {
            event = new ServletRequestEvent(context.getServletContext(), request.getRequest());
            // create pre-service event
            for (int i = 0; i < instances.length; i++) {
                if (instances[i] == null)
                    continue;
                if (!(instances[i] instanceof ServletRequestListener))
                    continue;
                ServletRequestListener listener = (ServletRequestListener) instances[i];
                try {
                    listener.requestInitialized(event);
                } catch (Throwable t) {
                    container.getLogger().error(MESSAGES.requestListenerInitException(instances[i].getClass().getName()), t);
                    ServletRequest sreq = request.getRequest();
                    sreq.setAttribute(RequestDispatcher.ERROR_EXCEPTION, t);
                    return;
                }
            }
        }

        // Ask this Context to process this request
        context.getPipeline().getFirst().invoke(request, response);

        // Access a session (if present) to update last accessed time, based on a
        // strict interpretation of the specification
        if (Globals.STRICT_SERVLET_COMPLIANCE) {
            request.getSession(false);
        }

        // Error page processing
        response.setSuspended(false);

        Throwable t = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        if (t != null) {
            throwable(request, response, t);
        } else {
            status(request, response);
        }

        // Exit application scope
        if ((instances !=null ) &&
                (instances.length > 0)) {
            // create post-service event
            for (int i = instances.length - 1; i >= 0; i--) {
                if (instances[i] == null)
                    continue;
                if (!(instances[i] instanceof ServletRequestListener))
                    continue;
                ServletRequestListener listener = (ServletRequestListener) instances[i];
                try {
                    listener.requestDestroyed(event);
                } catch (Throwable t2) {
                    container.getLogger().error(MESSAGES.requestListenerDestroyException(instances[i].getClass().getName()), t2);
                    ServletRequest sreq = request.getRequest();
                    sreq.setAttribute(RequestDispatcher.ERROR_EXCEPTION, t2);
                }
            }
        }

        context.getThreadBindingListener().unbind();
        // Restore the context classloader
        Thread.currentThread().setContextClassLoader(StandardHostValve.class.getClassLoader());

    }


    /**
     * Process Comet event.
     *
     * @param request Request to be processed
     * @param response Response to be processed
     * @param event The event to be processed
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    public final void event(Request request, Response response, HttpEvent event)
        throws IOException, ServletException {

        // Select the Context to be used for this Request
        Context context = request.getContext();

        // Some regular callback events should be filtered out for Servlet 3 async
        if (request.getAsyncContext() != null) {
            Request.AsyncContextImpl asyncContext = (Request.AsyncContextImpl) request.getAsyncContext();
            if (event.getType() == EventType.EVENT && asyncContext.getRunnable() == null 
                    && asyncContext.getPath() == null) {
                return;
            }
        }

        // Bind the context CL to the current thread
        if (context.getLoader() != null) {
            Thread.currentThread().setContextClassLoader(context.getLoader().getClassLoader());
        }
        context.getThreadBindingListener().bind();

        // Enter application scope
        Object instances[] = context.getApplicationEventListeners();
        ServletRequestEvent event2 = null;
        if (instances != null && (instances.length > 0)) {
            event2 = new ServletRequestEvent(context.getServletContext(), request.getRequest());
            // create pre-service event
            for (int i = 0; i < instances.length; i++) {
                if (instances[i] == null)
                    continue;
                if (!(instances[i] instanceof ServletRequestListener))
                    continue;
                ServletRequestListener listener = (ServletRequestListener) instances[i];
                try {
                    listener.requestInitialized(event2);
                } catch (Throwable t) {
                    container.getLogger().error(MESSAGES.requestListenerInitException(instances[i].getClass().getName()), t);
                    ServletRequest sreq = request.getRequest();
                    sreq.setAttribute(RequestDispatcher.ERROR_EXCEPTION, t);
                    Thread.currentThread().setContextClassLoader(StandardHostValve.class.getClassLoader());
                    return;
                }
            }
        }

        // Ask this Context to process this request
        context.getPipeline().getFirst().event(request, response, event);

        // Access a session (if present) to update last accessed time, based on a
        // strict interpretation of the specification
        if (Globals.STRICT_SERVLET_COMPLIANCE) {
            request.getSession(false);
        }

        // Error page processing
        response.setSuspended(false);

        if (request.getAsyncContext() == null) {
            Throwable t = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
            if (t != null) {
                throwable(request, response, t);
            } else {
                status(request, response);
            }
        } else {
            Request.AsyncContextImpl asyncContext = (Request.AsyncContextImpl) request.getAsyncContext();
            if ((event.getType() == EventType.TIMEOUT || event.getType() == EventType.ERROR)
                    && request.isEventMode() && asyncContext.getPath() == null) {
                Throwable t = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
                if (t != null) {
                    throwable(request, response, t);
                } else {
                    status(request, response);
                }
                // An error page (if any) must call complete or dispatch, if not the container calls complete
                if (request.isEventMode() && asyncContext.getPath() == null) {
                    asyncContext.complete();
                }
            }
        }

        // Exit application scope
        if (instances != null && (instances.length > 0)) {
            // create post-service event
            for (int i = instances.length - 1; i >= 0; i--) {
                if (instances[i] == null)
                    continue;
                if (!(instances[i] instanceof ServletRequestListener))
                    continue;
                ServletRequestListener listener = (ServletRequestListener) instances[i];
                try {
                    listener.requestDestroyed(event2);
                } catch (Throwable t) {
                    container.getLogger().error(MESSAGES.requestListenerDestroyException(instances[i].getClass().getName()), t);
                    ServletRequest sreq = request.getRequest();
                    sreq.setAttribute(RequestDispatcher.ERROR_EXCEPTION, t);
                }
            }
        }

        context.getThreadBindingListener().unbind();
        // Restore the context classloader
        Thread.currentThread().setContextClassLoader(StandardHostValve.class.getClassLoader());

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Handle the specified Throwable encountered while processing
     * the specified Request to produce the specified Response.  Any
     * exceptions that occur during generation of the exception report are
     * logged and swallowed.
     *
     * @param request The request being processed
     * @param response The response being generated
     * @param throwable The exception that occurred (which possibly wraps
     *  a root cause exception
     */
    protected void throwable(Request request, Response response,
                             Throwable throwable) {
        Context context = request.getContext();
        if (context == null)
            return;

        Throwable realError = throwable;

        if (realError instanceof ServletException) {
            realError = ((ServletException) realError).getRootCause();
            if (realError == null) {
                realError = throwable;
            }
        }

        // If this is an aborted request from a client just log it and return
        if (realError instanceof ClientAbortException ) {
            CatalinaLogger.CORE_LOGGER.clientAbortException(realError.getCause().getMessage());
            return;
        }

        ErrorPage errorPage = findErrorPage(context, throwable);
        if ((errorPage == null) && (realError != throwable)) {
            errorPage = findErrorPage(context, realError);
        }

        if (errorPage != null) {
            response.setAppCommitted(false);
            request.setAttribute
                (ApplicationFilterFactory.DISPATCHER_REQUEST_PATH_ATTR,
                 errorPage.getLocation());
            request.setAttribute(ApplicationFilterFactory.DISPATCHER_TYPE_ATTR,
                    ApplicationFilterFactory.ERROR_INTEGER);
            request.setAttribute
                (RequestDispatcher.ERROR_STATUS_CODE,
                        Integer.valueOf(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
            request.setAttribute(RequestDispatcher.ERROR_MESSAGE,
                              throwable.getMessage());
            request.setAttribute(RequestDispatcher.ERROR_EXCEPTION,
                              realError);
            Wrapper wrapper = request.getWrapper();
            if (wrapper != null)
                request.setAttribute(RequestDispatcher.ERROR_SERVLET_NAME,
                                  wrapper.getName());
            request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI,
                                 request.getRequestURI());
            request.setAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE,
                              realError.getClass());
            if (custom(request, response, errorPage)) {
                try {
                    response.flushBuffer();
                } catch (IOException e) {
                    container.getLogger().warn("Exception Processing " + errorPage, e);
                }
            }
        } else {
            // A custom error-page has not been defined for the exception
            // that was thrown during request processing. Check if an
            // error-page for error code 500 was specified and if so,
            // send that page back as the response.
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // The response is an error
            response.setError();

            status(request, response);
        }


    }


    /**
     * Handle the HTTP status code (and corresponding message) generated
     * while processing the specified Request to produce the specified
     * Response.  Any exceptions that occur during generation of the error
     * report are logged and swallowed.
     *
     * @param request The request being processed
     * @param response The response being generated
     */
    protected void status(Request request, Response response) {

        int statusCode = response.getStatus();

        // Handle a custom error page for this status code
        Context context = request.getContext();
        if (context == null)
            return;

        /* Only look for error pages when isError() is set.
         * isError() is set when response.sendError() is invoked. This
         * allows custom error pages without relying on default from
         * web.xml.
         */
        if (!response.isError())
            return;

        ErrorPage errorPage = context.findErrorPage(statusCode);
        if (errorPage != null) {
            response.setAppCommitted(false);
            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE,
                              Integer.valueOf(statusCode));

            String message = response.getMessage();
            if (message == null)
                message = "";
            request.setAttribute(RequestDispatcher.ERROR_MESSAGE, message);
            request.setAttribute
                (ApplicationFilterFactory.DISPATCHER_REQUEST_PATH_ATTR,
                 errorPage.getLocation());
            request.setAttribute(ApplicationFilterFactory.DISPATCHER_TYPE_ATTR,
                    ApplicationFilterFactory.ERROR_INTEGER);


            Wrapper wrapper = request.getWrapper();
            if (wrapper != null)
                request.setAttribute(RequestDispatcher.ERROR_SERVLET_NAME,
                                  wrapper.getName());
            request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI,
                                 request.getRequestURI());
            if (custom(request, response, errorPage)) {
                try {
                    response.flushBuffer();
                } catch (ClientAbortException e) {
                    // Ignore
                } catch (IOException e) {
                    container.getLogger().warn("Exception Processing " + errorPage, e);
                }
            }
        }

    }


    /**
     * Find and return the ErrorPage instance for the specified exception's
     * class, or an ErrorPage instance for the closest superclass for which
     * there is such a definition.  If no associated ErrorPage instance is
     * found, return <code>null</code>.
     *
     * @param context The Context in which to search
     * @param exception The exception for which to find an ErrorPage
     */
    protected static ErrorPage findErrorPage
        (Context context, Throwable exception) {

        if (exception == null)
            return (null);
        Class<?> clazz = exception.getClass();
        String name = clazz.getName();
        while (!Object.class.equals(clazz)) {
            ErrorPage errorPage = context.findErrorPage(name);
            if (errorPage != null)
                return (errorPage);
            clazz = clazz.getSuperclass();
            if (clazz == null)
                break;
            name = clazz.getName();
        }
        return (null);

    }


    /**
     * Handle an HTTP status code or Java exception by forwarding control
     * to the location included in the specified errorPage object.  It is
     * assumed that the caller has already recorded any request attributes
     * that are to be forwarded to this page.  Return <code>true</code> if
     * we successfully utilized the specified error page location, or
     * <code>false</code> if the default error report should be rendered.
     *
     * @param request The request being processed
     * @param response The response being generated
     * @param errorPage The errorPage directive we are obeying
     */
    protected boolean custom(Request request, Response response,
                             ErrorPage errorPage) {

        if (container.getLogger().isDebugEnabled())
            container.getLogger().debug("Processing " + errorPage);

        request.setPathInfo(errorPage.getLocation());

        try {

            // Reset the response (keeping the real error code and message)
            response.resetBuffer(true);

            // Forward control to the specified location
            ServletContext servletContext =
                request.getContext().getServletContext();
            RequestDispatcher rd =
                servletContext.getRequestDispatcher(errorPage.getLocation());
            rd.forward(request.getRequest(), response.getResponse());

            // If we forward, the response is suspended again
            response.setSuspended(false);

            // Indicate that we have successfully processed this custom page
            return (true);

        } catch (Throwable t) {

            // Report our failure to process this custom page
            container.getLogger().error("Exception Processing " + errorPage, t);
            return (false);

        }

    }


}
