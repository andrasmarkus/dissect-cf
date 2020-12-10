package hu.u_szeged.inf.fog.simulator.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import hu.u_szeged.inf.fog.simulator.iot.Station;

public abstract class MicrocontrollerConsumptionChartGenerator {
	
	public static void generate() throws FileNotFoundException, UnsupportedEncodingException {
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		PrintWriter writer = new PrintWriter(sdf.format(cal.getTime())+"-Microcontroller-Consumptions"+".html", "UTF-8");
		writer.println("<!DOCTYPE html><html><head>");
		writer.println("<script type=\'text/javascript\' src=\'https://www.gstatic.com/charts/loader.js\'></script>");
		writer.println("<script type=\'text/javascript\'>");
		writer.println("google.load('visualization', '1.0', {'packages':['corechart']});");
		writer.println("google.setOnLoadCallback(drawTable63);");
		writer.println("function drawTable63() {");
		writer.println("var data = google.visualization.arrayToDataTable([");
		writer.println("['Consumption', 'Count'],");
		
		/*for (ComputingAppliance c : ComputingAppliance.allComputingAppliance) {
			for (Application a : c.applicationList) {
				if (a.canRead) {
					if (a.computingAppliance.name.contains("fog")) {
						writer.println("['" + a.computingAppliance.name + "', " + a.computingAppliance.energyConsumption + ", '#00FF80'],");
					} else {
						writer.println("['" + a.computingAppliance.name + "', " + a.computingAppliance.energyConsumption + ", '#6666FF'],");
					}
				}
			}
		}*/
		
		ArrayList<Double> consumptions = new ArrayList<Double>();
		
		for (Station s : Station.allStations) {
			consumptions.add(s.getMicrocontrollerEnergyConsumption());
		}
		
		Collections.sort(consumptions);
		
		ArrayList<Double> CopyOfConsumptions = consumptions;
		
		int length = removeMultipleElements(consumptions);
		
		for (int i=0; i<length; i++) {
			writer.println("["+String.valueOf(consumptions.get(i))+","+ Collections.frequency(CopyOfConsumptions, consumptions.get(i)) +"],");
		}
		
		//writer.println("[(2).toString(), 402 ],");
		
		writer.println("]);");
		writer.println("var options = {title: 'Microcontroller consumptions', width:1250,\r\n" + 
				"        height:300,\r\n" + 
				"        legend: \"none\",\r\n" + 
				"        chartArea: {\r\n" + 
				"            width: 700,\r\n" + 
				"            left: 50\r\n" + 
				"        },\r\n" + 
				"        bar: {\r\n" + 
				"            groupWidth: \"80%\"\r\n" + 
				"        },\r\n" + 
				"        tooltip: {\r\n" + 
				"            isHtml: true\r\n" + 
				"        },\r\n" + 
				"        hAxis: {\r\n" + 
				"        		baselineColor: '#FFFFFF',\r\n" + 
				"            textStyle: {\r\n" + 
				"                fontSize: 8\r\n" + 
				"            }\r\n" + 
				"        },\r\n" + 
				"        vAxis: {\r\n" + 
				"            viewWindow: {\r\n" + 
				"                min:0\r\n" + 
				"            },\r\n" + 
				"            textStyle: {\r\n" + 
				"                fontSize: 10\r\n" + 
				"            }\r\n" + 
				"        },\r\n" + 
				"        titleTextStyle: {\r\n" + 
				"            fontSize: 14\r\n" + 
				"        },\r\n" + 
				"        dataOpacity: 0.7};");
		writer.println("var chart = new google.visualization.ColumnChart(document.getElementById('table_63'));");
		writer.println("chart.draw(data, options);");
		writer.println("}");
		writer.println("google.charts.setOnLoadCallback(drawTable63);");
		writer.println("</script>");
		writer.println("</script>");
		writer.println("</head><body>");
		writer.println("<div id=\"table_63\" class=\"pie\" style=\"width:750px; margin:0 auto;\"></div>");
		writer.println("</body></html>");
		writer.close();
	}
	
	private static int removeMultipleElements(ArrayList<Double> al) {
		if (al.size()==0 || al.size()==1) {
			return al.size();
		}
		
		int j = 0;
		
		for (int i=0;i<al.size()-1;i++) {
			if (al.get(i) != al.get(i+1)) {
				al.set(j++, al.get(i));
			}
		}
		
		al.set(j++, al.get(al.size()-1));
		
		return j;
	}

}
