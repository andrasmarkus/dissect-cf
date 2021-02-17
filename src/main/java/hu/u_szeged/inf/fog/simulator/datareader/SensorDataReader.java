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
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Stream;

public class SensorDataReader {
    public static final int NO_ID_COLUMN = -1;
    public static final String CSV_SEMICOLON_SEPARATOR = ";";
    public static final String CSV_COMMA_SEPARATOR = ",";
    public static final String SIMPLE_SPACE_SEPARATOR = " ";
    public static final String TABULATED_TXT_SEPARATOR = "  ";

    private final String path;
    private final String separator;
    private final boolean hasMiliseconds;
    private final int dateColumn;
    private final int idColumn;

    public SensorDataReader(String path, String separator, boolean hasMiliseconds, int dateColumn, int idColumn) {
        this.path = path;
        this.separator = separator;
        this.hasMiliseconds = hasMiliseconds;
        this.dateColumn = dateColumn;
        this.idColumn = idColumn;
    }

    public SensorDataReader(String path, String separator, boolean hasMiliseconds, int dateColumn) {
        this.path = path;
        this.separator = separator;
        this.hasMiliseconds = hasMiliseconds;
        this.dateColumn = dateColumn;
        this.idColumn = NO_ID_COLUMN;
    }


    public SensorData ReadData(int row) throws IllegalArgumentException {
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

    public ArrayList<SensorData> ReadAllLines() {
        int row = 0;
        ArrayList<SensorData> dataList = new ArrayList<>();
        if (dateColumn < 0 || idColumn < -1) {
            throw new IllegalArgumentException("Column and row id-s must be higher than 0 (-1 in case of id column)");
        }
        try (Scanner scanner = new Scanner(new File(path))) {
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                String[] split = scanner.nextLine().split(separator);
                String rawData = "";
                for (int i = 0; i < split.length; i++) {
                    if (i != dateColumn && i != idColumn) {
                        rawData += split[i];
                    }
                }

                final byte[] bytes = rawData.getBytes(StandardCharsets.UTF_8);

                if (idColumn == NO_ID_COLUMN) {
                    Random r = new Random();
                    String id = row + "_measurement_" + split[dateColumn] + "_" + r.nextInt(1000);
                    dataList.add(new SensorData(getDate(split[dateColumn], hasMiliseconds), id, bytes.length));
                }else{
                    dataList.add(new SensorData(getDate(split[dateColumn], hasMiliseconds), split[idColumn], bytes.length));
                }

                row++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return dataList;
    }




    private long getDate(String s, boolean hasMilisec){
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

    private long getDate(String s) {
        return getDate(s, false);
    }

}
