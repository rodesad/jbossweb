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


package org.apache.catalina.loader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Subclass implementation of <b>java.net.URLClassLoader</b> that knows how
 * to load classes from disk directories, as well as local and remote JAR
 * files.  It also implements the <code>Reloader</code> interface, to provide
 * automatic reloading support to the associated loader.
 * <p>
 * In all cases, URLs must conform to the contract specified by
 * <code>URLClassLoader</code> - any URL that ends with a "/" character is
 * assumed to represent a directory; all other URLs are assumed to be the
 * address of a JAR file.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - Local repositories are searched in
 * the order they are added via the initial constructor and/or any subsequent
 * calls to <code>addRepository()</code>.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - At present, there are no dependencies
 * from this class to any other Catalina class, so that it could be used
 * independently.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 467222 $ $Date: 2006-10-24 05:17:11 +0200 (mar., 24 oct. 2006) $
 */

public class StandardClassLoader
    extends URLClassLoader
    implements StandardClassLoaderMBean {

	public StandardClassLoader(URL repositories[]) {
        super(repositories);
    }

    public StandardClassLoader(URL repositories[], ClassLoader parent) {
        super(repositories, parent);
    }

}

