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

import java.util.EnumSet;
import java.util.Map;

/**
 * @version $Rev: 751589 $ $Date: 2009-03-09 06:35:08 +0100 (Mon, 09 Mar 2009) $
 * @since 3.0
 */
public interface FilterRegistration {
    boolean setDescription(String description);

    void setAsyncSupported(boolean asyncSupported);

    void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String ... servletNames);

    void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String ... urlPatterns);

    boolean setInitParameter(String name, String value);

    void setInitParameters(Map<String, String> initParameters);

}
