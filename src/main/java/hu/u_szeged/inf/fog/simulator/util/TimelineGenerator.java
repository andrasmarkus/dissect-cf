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

package hu.u_szeged.inf.fog.simulator.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;

/** This class is a helper class for visualizing the simulation ( clouds, virtual machines and tasks).
 * More information: https://developers.google.com/chart/interactive/docs/gallery/timeline
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
public abstract class TimelineGenerator {

	public static void generate() throws FileNotFoundException, UnsupportedEncodingException {
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		PrintWriter writer = new PrintWriter(sdf.format(cal.getTime())+".html", "UTF-8");
		writer.println("<!DOCTYPE html><html><head>");
		writer.println("<script type=\'text/javascript\' src=\'https://www.gstatic.com/charts/loader.js\'></script>");
		writer.println("<script type=\'text/javascript\'>");
		writer.println("google.charts.load(\'current\', {packages:[\'timeline\']});");
		writer.println("  google.charts.setOnLoadCallback(drawChart);");
		writer.println("function drawChart(){");
		writer.println("var container = document.getElementById('example');");
		writer.println("var chart = new google.visualization.Timeline(container);");
		writer.println("var dataTable = new google.visualization.DataTable();");
		writer.println("dataTable.addColumn({ type: 'string', id: 'Application' });");
		writer.println("dataTable.addColumn({ type: 'string', id: 'VM' });");
		writer.println("    dataTable.addColumn({ type: 'date', id: 'Start' });");
		writer.println("dataTable.addColumn({ type: 'date', id: 'End' });");
		writer.println("dataTable.addRows([");
		
		for (ComputingAppliance c : ComputingAppliance.allComputingAppliance) {
			for (Application a : c.applicationList) {
				for(TimelineCollector tc : a.timelineList) {
					writer.println("[ '"+a.name+"', '"+tc.vmId+"', new Date(0,0,0,0,0,0,"+tc.start +"), new Date(0,0,0,0,0,0,"+tc.stop+")],");
				}
			}
		}

		writer.println("]);");
		writer.println("chart.draw(dataTable);");
		writer.println("}</script>");
		writer.println("</head><body>");
		writer.println("<div id=\"example\" style=\"height: 1500px; width=100%;\"></div>");
		writer.println(" </body></html>");

		writer.close();
	}
	
	public static class TimelineCollector{
		public TimelineCollector(long start, long stop, String vmId) {
			super();
			this.start = start;
			this.stop = stop;
			this.vmId = vmId;
		}
		public long start;
		public long stop;
		public String vmId;
		
	}
}
