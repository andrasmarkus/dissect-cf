package hu.u_szeged.inf.fog.simulator.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;

public abstract class FogSimulationChart {

	public static void generate() throws FileNotFoundException, UnsupportedEncodingException {
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		PrintWriter writer = new PrintWriter(sdf.format(cal.getTime())+".html", "UTF-8");
		writer.println("<!DOCTYPE html><html><head>");
		writer.println("<script type=\'text/javascript\' src=\'https://www.gstatic.com/charts/loader.js\'></script>");
		writer.println("<script type=\'text/javascript\'>");
		writer.println("google.charts.load('current', {packages: ['corechart']});");
		writer.println("google.charts.setOnLoadCallback(drawChart);");
		writer.println("function drawChart() {");
		writer.println("var data = google.visualization.arrayToDataTable([");
		writer.println("['', 'fog nodes'],");
		
		for (ComputingAppliance c : ComputingAppliance.allComputingAppliance) {
			for (Application a : c.applicationList) {
				if (a.canRead) {
					writer.println("['" + a.computingAppliance.name + "', " + a.computingAppliance.energyConsumption + "],");
				}
			}
		}
		
		writer.println("]);");
		writer.println("var options = {title: 'Consumptions of fog nodes'};");
		writer.println("var chart = new google.visualization.ColumnChart(document.getElementById('container'));");
		writer.println("chart.draw(data, options);");
		writer.println("}");
		writer.println("google.charts.setOnLoadCallback(drawChart);");
		writer.println("</script>");
		writer.println("]);");
		writer.println("chart.draw(dataTable);");
		writer.println("}</script>");
		writer.println("</head><body>");
		writer.println("<div id=\"example\" style=\"height: 1500px; width=100%;\"></div>");
		writer.println(" </body></html>");
		writer.close();
	}
	
}
