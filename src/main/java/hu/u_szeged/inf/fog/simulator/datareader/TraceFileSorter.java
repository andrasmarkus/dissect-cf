package hu.u_szeged.inf.fog.simulator.datareader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TraceFileSorter {
    private static String path;
    private static String separator;
    private static boolean hasColumnHeader;
    private static boolean hasMiliseconds;
    private static int[] dateColumn;

    private static ArrayList<data> ReadAllLines() {
        int row = 0;
        ArrayList<data> dataList = new ArrayList<>();
        for(int i : dateColumn){
            if(i < 0 && i != SensorDataReader.NO_SECOND_DATE_COLUMN){
                throw new IllegalArgumentException("Column and row id-s must be higher than 0 (-1 in case of id column)");
            }
        }
        try (Scanner scanner = new Scanner(new File(path))) {
            if(hasColumnHeader){
                scanner.nextLine();
            }
            while (scanner.hasNextLine()) {
                String[] split = scanner.nextLine().split(separator);
                String rawData = "";
                for (int i = 0; i < split.length; i++) {
                    if(i != dateColumn[0] && i != dateColumn[1]){
                        if(i != 0){
                            rawData += "," + split[i];
                        }
                        else{
                            rawData += split[i];
                        }

                    }
                }
                if(dateColumn[1] != SensorDataReader.NO_SECOND_DATE_COLUMN) {
                    String rawdate = split[dateColumn[0]] + " " + split[dateColumn[1]];
                    dataList.add(new data(getDate(rawdate), rawData));
                }
                else{
                    dataList.add(new data(getDate(split[dateColumn[0]]), rawData));
                }
                row++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    private static long getDate(String s, boolean hasMilisec){
        SimpleDateFormat format;
        if(hasMilisec){
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSS");
        }
        else{
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        String date = "", time = "";
        String[] split = s.split(" ");
        for(String fdate : split){
            if(fdate.contains("-")){
                date = fdate;
            }
        }
        for(String ftime : split){
            if(ftime.contains(":")){
                time = ftime;
            }
        }

        String dateTime = date + " " + time;
        try {
            return(format.parse(dateTime).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static long getDate(String s) {
        return getDate(s, false);
    }

    public static void sortFile(String pathi, String separatori, boolean hasColumnHeaderi, boolean hasMilisecondsi, int[] dateColumni){
        path = pathi;
        separator = separatori;
        hasColumnHeader = hasColumnHeaderi;
        hasMiliseconds = hasMilisecondsi;
        dateColumn = dateColumni;

        ArrayList<data> dataList = ReadAllLines();
        Comparator<data> compareByTime = (data o1, data o2) -> Long.valueOf(o1.date).compareTo(o2.date);
        Collections.sort(dataList, compareByTime);
        writeFile(dataList);
    }

    public static void sortFile(String pathi, String separatori, boolean hasColumnHeaderi, boolean hasMilisecondsi, int dateColumni) {
        int[] temp = {dateColumni, SensorDataReader.NO_SECOND_DATE_COLUMN};
        sortFile(pathi, separatori, hasColumnHeaderi, hasMilisecondsi, temp);
    }

    private static void writeFile(ArrayList<data> dataList){
        try {
            String name = "asd.csv";
            FileWriter file = new FileWriter(name);
            BufferedWriter output = new BufferedWriter(file);
            for (var item : dataList) {
                output.write(item + "");
                output.newLine();
            }
            output.close();
        }
        catch (Exception e) {
            e.getStackTrace();
        }
    }

    private static class data{
        public final long date;
        public final String data;

        public data(long date, String data){
            this.date = date;
            this.data = data;
        }

        @Override
        public String toString() {
            Date d=new Date(date);
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateText = df2.format(d);
            return dateText + data;
        }


    }
}
