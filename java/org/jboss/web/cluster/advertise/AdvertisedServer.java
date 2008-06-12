/*
 *
 *  Copyright(c) 2008 Red Hat Middleware, LLC,
 *  and individual contributors as indicated by the @authors tag.
 *  See the copyright.txt in the distribution for a
 *  full listing of individual contributors.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library in the file COPYING.LIB;
 *  if not, write to the Free Software Foundation, Inc.,
 *  59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
 *
 */

package org.jboss.web.cluster.advertise;

import java.util.Date;
import java.util.HashMap;
import java.util.Collection; 

/**
 * Advertised server instance
 *
 * @author Mladen Turk
 *
 */
public class AdvertisedServer
{
    private String server;
    private Date   date;
    private int    status;
    private String status_desc;
    private HashMap<String, String> headers;

    /** Manager-Address header          */
    public static String MANAGER_ADDRESS   = "X-Manager-Address";
    /** Manager-Url header              */
    public static String MANAGER_URL       = "X-Manager-Url";
    /** Manager-Protocol header          */
    public static String MANAGER_PROTOCOL  = "X-Manager-Protocol";
    /** Manager-Version header          */
    public static String MANAGER_VERSION   = "X-Manager-Version";
    /** Manager-Host header             */
    public static String MANAGER_HOST      = "X-Manager-Host";

    private AdvertisedServer()
    {
        // Disable creation.
    }

    protected AdvertisedServer(String server)
    {
        this.server = server;
        headers     = new HashMap<String, String>();
    } 

    protected boolean setStatus(int status, String desc)
    {
        boolean rv = false;
        status_desc = desc;
        if (this.status == 0 ) {
            // First time
            this.status = status;                
        }
        else if (this.status != status) {
            this.status = status;
            rv = true;
        }
        return rv;
    }
    
    /** Set the Date of the last Advertise message
     */
    protected void setDate(Date date)
    {
        this.date = date;
    }

    /** Set the Header
     */
    protected void setParameter(String name, String value)
    {
        headers.put(name, value);
    }

    /** Get Date of the last Advertise message
     */
    public Date getDate()
    {
        return date;
    }

    /** Get Status code of the last Advertise message
     */
    public int getStatusCode()
    {
        return status;
    }

    /** Get Status description of the last Advertise message
     */
    public String getStatusDescription()
    {
        return status_desc;
    }

    /** Get Advertise parameter
     */
    public String getParameter(String name)
    {
        return headers.get(name);
    }

    public String toString()
    {
        return server;
    } 
}
