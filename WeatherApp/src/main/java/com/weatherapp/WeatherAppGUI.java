package com.weatherapp;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherAppGUI extends JFrame {

    private static final String API_KEY = "4dfe5cf28505fb54d27842cece069379"; 
    private static final String CURRENT_WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?q=";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast?q=";

    private JTextField cityInput;
    private JButton getWeatherButton;
    private JButton getCurrentWeatherButton; 
    private JLabel currentWeatherLabel;
    private JPanel weatherPanel;
    private JSpinner datePicker;
    private BufferedImage backgroundImage;

    public WeatherAppGUI() {
        setTitle("Weather App");
        setSize(1000, 600); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 

       
        try {
            backgroundImage = ImageIO.read(new URL("https://img.freepik.com/free-vector/gorgeous-clouds-background-with-blue-sky-design_1017-25501.jpg"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        mainPanel.setLayout(new BorderLayout(20, 20));  

        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inputPanel.setOpaque(false);

        JLabel cityLabel = new JLabel("City:");
        cityLabel.setForeground(new Color(255, 255, 255, 200));
        cityLabel.setFont(new Font("Verdana", Font.BOLD, 18));

        cityInput = new JTextField(12);
        cityInput.setFont(new Font("Verdana", Font.PLAIN, 16));

        getWeatherButton = new JButton("Get Forecast");
        getWeatherButton.setFont(new Font("Verdana", Font.BOLD, 16));
        getWeatherButton.setBackground(new Color(255, 255, 255, 180));
        getWeatherButton.setForeground(Color.DARK_GRAY);

        getCurrentWeatherButton = new JButton("Get Current Weather");
        getCurrentWeatherButton.setFont(new Font("Verdana", Font.BOLD, 16));
        getCurrentWeatherButton.setBackground(new Color(255, 255, 255, 180));
        getCurrentWeatherButton.setForeground(Color.DARK_GRAY);

        currentWeatherLabel = new JLabel("Enter a city and click 'Get Weather'");
        currentWeatherLabel.setForeground(new Color(255, 255, 255, 200));
        currentWeatherLabel.setFont(new Font("Verdana", Font.PLAIN, 16));

        
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        datePicker = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(datePicker, "yyyy-MM-dd");
        datePicker.setEditor(dateEditor);

        inputPanel.add(cityLabel);
        inputPanel.add(cityInput);
        inputPanel.add(new JLabel("Date:"));
        inputPanel.add(datePicker);
        inputPanel.add(getWeatherButton);
        inputPanel.add(getCurrentWeatherButton);  

        
        weatherPanel = new JPanel();
        weatherPanel.setOpaque(false);
        weatherPanel.setLayout(new BoxLayout(weatherPanel, BoxLayout.Y_AXIS));
        weatherPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

     
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(weatherPanel, BorderLayout.CENTER);

        getWeatherButton.addActionListener(e -> {
            getCurrentWeather();
            getForecastForDate();
        });

        getCurrentWeatherButton.addActionListener(e -> {
            getCurrentWeather();  
        });

        add(mainPanel);
    }

    private void getCurrentWeather() {
        String city = cityInput.getText().trim();
        if (city.isEmpty()) {
            currentWeatherLabel.setText("Please enter a city name.");
            return;
        }

        String urlString = CURRENT_WEATHER_URL + city + "&appid=" + API_KEY + "&units=metric";

        try {
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            parseAndDisplayCurrentWeather(response.toString());

        } catch (Exception e) {
            currentWeatherLabel.setText("Error fetching current weather.");
            e.printStackTrace();
        }
    }

    private void parseAndDisplayCurrentWeather(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject main = jsonObject.getJSONObject("main");
            double temp = main.getDouble("temp");
            String weatherDescription = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
            double humidity = main.getDouble("humidity");
            double pressure = main.getDouble("pressure");
            double windSpeed = jsonObject.getJSONObject("wind").getDouble("speed");

            currentWeatherLabel.setText("<html><center><b>Current Weather</b><br>" +
                    "<b>Temperature:</b> " + temp + "°C<br>" +
                    "<b>Condition:</b> " + weatherDescription + "<br>" +
                    "<b>Humidity:</b> " + humidity + "%<br>" +
                    "<b>Pressure:</b> " + pressure + " hPa<br>" +
                    "<b>Wind Speed:</b> " + windSpeed + " m/s</center></html>");

           
            JPanel currentWeatherPanel = new JPanel();
            currentWeatherPanel.setOpaque(false);
            currentWeatherPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            currentWeatherPanel.add(new JLabel(currentWeatherLabel.getText()));

            
            weatherPanel.removeAll();
            weatherPanel.add(currentWeatherPanel);
            weatherPanel.revalidate();
            weatherPanel.repaint();

        } catch (Exception e) {
            currentWeatherLabel.setText("Error parsing current weather data.");
            e.printStackTrace();
        }
    }

    private void getForecastForDate() {
        String city = cityInput.getText().trim();
        Date selectedDate = (Date) datePicker.getValue();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(selectedDate);

        String urlString = FORECAST_URL + city + "&appid=" + API_KEY + "&units=metric";

        try {
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            parseAndDisplayForecast(response.toString(), formattedDate);

        } catch (Exception e) {
            weatherPanel.removeAll();
            weatherPanel.add(new JLabel("Error fetching forecast for selected date."));
            e.printStackTrace();
        }
    }

    private void parseAndDisplayForecast(String response, String date) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray list = jsonObject.getJSONArray("list");

            JPanel forecastPanel = new JPanel();
            forecastPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            forecastPanel.setOpaque(false);

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a"); 

            boolean forecastFound = false;
            for (int i = 0; i < list.length(); i++) {
                JSONObject forecast = list.getJSONObject(i);
                String forecastDate = forecast.getString("dt_txt").split(" ")[0];

                if (forecastDate.equals(date)) {
                    forecastFound = true;
                    JSONObject main = forecast.getJSONObject("main");
                    double temp = main.getDouble("temp");
                    String description = forecast.getJSONArray("weather").getJSONObject(0).getString("description");
                    String time = forecast.getString("dt_txt").split(" ")[1];
                    String formattedTime = timeFormat.format(new SimpleDateFormat("HH:mm:ss").parse(time));

                    JPanel hourPanel = new JPanel();
                    hourPanel.setOpaque(false);
                    hourPanel.setPreferredSize(new Dimension(150, 100));
                    hourPanel.setBackground(new Color(255, 255, 255, 180)); 

                    JLabel tempLabel = new JLabel("<html><b>" + temp + "°C</b><br>" + description + "<br>" + formattedTime + "</html>");
                    tempLabel.setForeground(Color.BLACK);
                    tempLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    tempLabel.setFont(new Font("Verdana", Font.PLAIN, 14));

                    hourPanel.add(tempLabel);
                    forecastPanel.add(hourPanel);
                }
            }

            if (!forecastFound) {
                forecastPanel.add(new JLabel("<html><center>No forecast data available for this date.</center></html>"));
            }

            weatherPanel.removeAll();
            weatherPanel.add(forecastPanel);
            weatherPanel.revalidate();
            weatherPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            weatherPanel.add(new JLabel("Error parsing forecast data."));
            weatherPanel.revalidate();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WeatherAppGUI().setVisible(true);
        });
    }
}