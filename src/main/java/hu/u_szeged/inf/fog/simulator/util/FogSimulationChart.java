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
		writer.println("['', 'fog nodes', { role: 'style' }],");
		
		for (ComputingAppliance c : ComputingAppliance.allComputingAppliance) {
			for (Application a : c.applicationList) {
				if (a.canRead) {
					if (a.computingAppliance.name.contains("fog")) {
						writer.println("['" + a.computingAppliance.name + "', " + a.computingAppliance.energyConsumption + ", '#00FF80'],");
					} else if (a.computingAppliance.name.contains("cloud")) {
						writer.println("['" + a.computingAppliance.name + "', " + a.computingAppliance.energyConsumption + ", '#6666FF'],");
					} else {
						writer.println("['" + a.computingAppliance.name + "', " + a.computingAppliance.energyConsumption + ", '#f2a03d'],");
					}
				}
			}
		}
		
		writer.println("]);");
		writer.println("var options = {title: 'Consumptions of fog nodes', legend: { position: 'none' }};");
		writer.println("var chart = new google.visualization.ColumnChart(document.getElementById('container'));");
		writer.println("chart.draw(data, options);");
		writer.println("}");
		writer.println("google.charts.setOnLoadCallback(drawChart);");
		writer.println("</script>");
		writer.println("</script>");
		writer.println("</head><body>");
		writer.println("<div id=\"container\" style=\"height: 800px; width=100%;\"></div>");
		writer.println(" </body></html>");
		writer.close();
	}
	
}
