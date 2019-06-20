/*
 *  ========================================================================
 *  DIScrete event baSed Energy Consumption simulaTor 
 *    					             for Clouds and Federations (DISSECT-CF)
 *  ========================================================================
 *  
 *  This file is part of DISSECT-CF.
 *  
 *  DISSECT-CF is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or (at
 *  your option) any later version.
 *  
 *  DISSECT-CF is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *  General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with DISSECT-CF.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  (C) Copyright 2019, Andras Markus (markusa@inf.u-szeged.hu)
 */

package hu.uszeged.inf.xml.model;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** This class is a helper class for creating objects from XML files using JAXB.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
@XmlRootElement( name = "applications" )
@XmlAccessorType(XmlAccessType.PROPERTY) 
public class ApplicationsModel
{
    ArrayList<ApplicationModel> applicationList;

    public ArrayList<ApplicationModel> getApplications(){
        return applicationList;
    }

    @XmlElement( name = "application" )
    public void setApplications( ArrayList<ApplicationModel> application ){
        this.applicationList = application;
    }

    public void add( ApplicationModel application ){
        if( this.applicationList == null )
        {
            this.applicationList = new ArrayList<ApplicationModel>();
        }
        this.applicationList.add( application );

    }

    @Override
    public String toString(){
        StringBuffer str = new StringBuffer();
        for( ApplicationModel device : this.applicationList )
        {	
            str.append( device.toString() );
            str.append("\n");
        }
        return str.toString();
    }

}
