package Models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Parser {

    public static ArrayList<Weather> parseYandexWeather(String url) throws IOException {
        ArrayList<Weather> Weathers = new ArrayList<>();

        String response = getRequest(url);

        String[] tmp = response.split("<div class=\"climate-calendar__cell\">");

        ArrayList<String> climate_cell = new ArrayList<>();
        for (String i : tmp) {
            if (!(i.contains("climate-calendar-day_colorless_yes") || i.contains("<!DOCTYPE html>"))) {
                if (i.contains("climate-calendar__cell_separator_wide")) {
                    var cell_separator_wide = i.
                            split("<div class=\"climate-calendar__cell climate-calendar__cell_separator_wide\">");
                    climate_cell.addAll(Arrays.stream(cell_separator_wide).toList());
                    continue;
                }
                climate_cell.add(i);
            }
        }

        for (String i : climate_cell) {
            String dateStr;
            tmp = i.split("<h6 class=\"climate-calendar-day__detailed-day\">");
            if (tmp.length != 2) {
                tmp = i.split("<h6 class=\"climate-calendar-day__detailed-day climate-calendar-day__detailed-day_holiday\">");
            }
            tmp = tmp[1].split("</h6>");
            var dateStr_tmp = tmp[0].split(",");
            dateStr = dateStr_tmp[0];
            Date date = createDate(dateStr);

            tmp = tmp[1].split("<span class=\"temp__value temp__value_with-unit\">");
            int maxTemp = Integer.parseInt(tmp[1].split("</span>")[0]);
            int minTemp = Integer.parseInt(tmp[2].split("</span>")[0]);

            tmp = tmp[3].split("</td>");
            var pressure_tmp = tmp[1].split(">");
            var humidity_tmp = tmp[3].split(">");
            int pressure = Integer.parseInt(pressure_tmp[1].split(" ")[0]);
            int humidity = Integer.parseInt(humidity_tmp[1].split("%")[0]);

            Weathers.add(new WeatherBuilder()
                    .targetDate(date)
                    .minTemperature(minTemp)
                    .maxTemperature(maxTemp)
                    .pressure(pressure)
                    .humidity(humidity)
                    .buildWeather());
        }

        return Weathers;
    }

    public static ArrayList<Weather> parseRambler(String url) throws IOException {
        ArrayList<Weather> Weathers = new ArrayList<>();

        String response = getRequest(url);

        String[] tmp = response.split("div id=\"app\"");
        tmp = tmp[1].split("</h2>");
        tmp = tmp[1].split("data-weather");

        for (int i = 1; i < tmp.length - 1; i++) {
            if (!tmp[i].contains("link_past")) {
                var date_tmp = tmp[i].split("</span></span>");
                if (date_tmp.length == 3) {
                    String[] day_tmp = date_tmp[0].split("<!--");
                    int day = Integer.parseInt(day_tmp[0].split("\">")[4]);
                    Date targetDate = createDate(day);

                    String[] data_tmp = date_tmp[2].split("<!-- -->");
                    var maxTemp_tmp = data_tmp[0].split(">");
                    int maxTemp = Integer.parseInt(maxTemp_tmp[maxTemp_tmp.length - 1]);
                    var minTemp_tmp = data_tmp[1].split(">");
                    int minTemp = Integer.parseInt(minTemp_tmp[minTemp_tmp.length - 1]);

                    Weathers.add(new WeatherBuilder()
                            .targetDate(targetDate)
                            .minTemperature(minTemp)
                            .maxTemperature(maxTemp)
                            .buildWeather());
                } else {
                    int day;
                    String[] day_tmp;
                    if (date_tmp[0].contains("<!-- -->")) {
                        day_tmp = date_tmp[0].split("<!-- -->");
                        if (Objects.equals(day_tmp[1], " ")) {
                            day_tmp = day_tmp[0].split(">");
                        } else {
                            day_tmp = day_tmp[1].split(">");
                        }
                    } else {
                        day_tmp = date_tmp[0].split(">");
                    }

                    day = Integer.parseInt(day_tmp[day_tmp.length - 1]);
                    System.out.println(day);
                    Date targetDate = createDate(day);

                    var data_tmp = date_tmp[1].split("<!-- -->");
                    var maxTemp_tmp = data_tmp[0].split(">");
                    int maxTemp = Integer.parseInt(maxTemp_tmp[maxTemp_tmp.length - 1]);
                    var minTemp_tmp = data_tmp[1].split(">");
                    int minTemp = Integer.parseInt(minTemp_tmp[minTemp_tmp.length - 1]);

                    Weathers.add(new WeatherBuilder()
                            .targetDate(targetDate)
                            .minTemperature(minTemp)
                            .maxTemperature(maxTemp)
                            .buildWeather());
                }
            }
        }
        return Weathers;
    }

    public static ArrayList<Weather> parseWorldWeather(String url) throws IOException{
        ArrayList<Weather> Weathers = new ArrayList<>();

        String response = getRequest(url);

        String[] tmp = response.split("<li class='ww-month-week");

        for(int i = 1; i < tmp.length; i++){
            int day = Integer.parseInt(tmp[i].split("<div>")[1].split("</div>")[0]);
            Date targetDate = createDate(day);
            int maxTemp = Integer.parseInt(tmp[i].split("<span>")[1].split("°</span>")[0]);
            int minTemp = Integer.parseInt(tmp[i].split("<p>")[1].split("°</p>")[0]);

            Weathers.add(new WeatherBuilder()
                    .targetDate(targetDate)
                    .minTemperature(minTemp)
                    .maxTemperature(maxTemp)
                    .buildWeather());
        }
        return Weathers;
    }

    private static String getRequest(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    private static Date createDate(String dateStr) {
        int day = Integer.parseInt(dateStr.split(" ")[0]);
        String monthStr = dateStr.split(" ")[1];
        int month = switch (monthStr) {
            case "февраля" -> Calendar.FEBRUARY;
            case "марта" -> Calendar.MARCH;
            case "апреля" -> Calendar.APRIL;
            case "мая" -> Calendar.MAY;
            case "июня" -> Calendar.JUNE;
            case "июля" -> Calendar.JULY;
            case "августа" -> Calendar.AUGUST;
            case "сентября" -> Calendar.SEPTEMBER;
            case "октября" -> Calendar.OCTOBER;
            case "ноября" -> Calendar.NOVEMBER;
            case "дерабря" -> Calendar.DECEMBER;
            default -> Calendar.JANUARY;
        };

        return new GregorianCalendar(2021, month, day).getTime();
    }

    private static Date createDate(int day) {
        Date date;
        Calendar calendar = Calendar.getInstance();
        if (day >= calendar.get(Calendar.DAY_OF_MONTH)) {
            date = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day).getTime();
        } else if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
            date = new GregorianCalendar(calendar.get(Calendar.YEAR) + 1, Calendar.JANUARY, day).getTime();
        } else {
            date = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, day).getTime();
        }
        return date;
    }
}
