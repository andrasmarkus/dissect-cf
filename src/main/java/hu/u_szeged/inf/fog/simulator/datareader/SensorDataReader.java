package hu.u_szeged.inf.fog.simulator.datareader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Stream;

public class SensorDataReader {
    public static final int NO_ID_COLUMN = -1;
    public static final String CSV_SEMICOLON_SEPARATOR = ";";
    public static final String CSV_COMMA_SEPARATOR = ",";
    public static final String SIMPLE_SPACE_SEPARATOR = " ";
    public static final String TABULATED_TXT_SEPARATOR = "  ";

    public SensorDataReader() throws FileNotFoundException {
    }

    public static boolean hasNextLine(String path, int row){
        return true;
    }

    public static List<String> ReadData(String path) {
        List<String> list = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(path))) {
            while (scanner.hasNext()) {
                list.add(scanner.nextLine());
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
        return list;
    }


    public static SensorData ReadFromMemory(String path, String separator, int row, int dateColumn, int idColumn){
        return null;
    }


    public static String ReadData(String path, int row) {

        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            return lines.skip(row).findFirst().get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static SensorData ReadData(String path, String separator, boolean hasMiliseconds, int row, int dateColumn, int idColumn) throws IllegalArgumentException {
        if(row < 0 || dateColumn < 0 || idColumn < -1){
            throw new IllegalArgumentException("Column and row id-s must be higher than 0 (-1 in case of id column)");
        }
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            String[] split = lines.skip(row).findFirst().get().split(separator);

            String rawData = "";
            for(int i = 0; i < split.length; i++){
                if(i != dateColumn && i != idColumn){
                    rawData += split[i];
                }
            }

            final byte[] bytes = rawData.getBytes(StandardCharsets.UTF_8);

            if(idColumn == NO_ID_COLUMN){
                Random r = new Random();
                String id = row + "_measurement_" + split[dateColumn] + "_" + r.nextInt(1000);
                return new SensorData(getDate(split[dateColumn], hasMiliseconds), id, bytes.length);
            }

            return new SensorData(getDate(split[dateColumn], hasMiliseconds), split[idColumn], bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static SensorData ReadData(String path, String separator, boolean hasMiliseconds, int row, int dateColumn) {
        return ReadData(path, separator, hasMiliseconds, row, dateColumn, -1);
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
}
