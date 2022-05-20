package com.example.weatherapp;
/*
 Приложение Погода
 показывает температуру и облачность в указанном пользователем
 городе или по индексу
*/


import androidx.appcompat.app.AppCompatActivity; //AppCompatActivity применяется для обратной совместимости в плане дизайна

import android.os.AsyncTask;//механизм для перемещения трудоёмких операций в фоновый пото
import android.os.Bundle; //оболочка над коллекцией ArrayMap для создания более комфортного в работе контейнера
import android.view.View; //View – компонент макета, который пользователь может видеть и взаимодействовать с ним
import android.widget.EditText; //текстовое поле для пользовательского ввода
import android.widget.TextView; //отображения текста без возможности редактировани

import org.json.JSONObject; //JavaScript Object Notation обмен данными с сервером
import org.json.JSONException;
import java.io.BufferedInputStream; //для упрощения чтения текста из двоичных входных потоков
import java.io.BufferedReader; //читает текст из потока ввода символов
import java.io.IOException; //Обработчик исключений
import java.io.InputStream; //интерфейс потока чтения
import java.io.InputStreamReader; //мост, позволяющий преобразовать byte stream в character stream
import java.net.MalformedURLException; //проверяет правильность адреса URL
import java.net.URL; //Обработка URL (унифицированный указатель ресурсов)

import javax.net.ssl.HttpsURLConnection; //забираем JSON по SSL

public class MainActivity extends AppCompatActivity {
    private final String APPID = "4a2180cc176960d45b97c3b3912454c8"; //API ключ получаем после регистрации в openweathermap.org
    private final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&lang=ru&units=metric&APPID=" + APPID;
    //синтаксис берем из документации разработчика openweathermap
    // Забанили РФ, с 20.05.2022 нужен VPN!
    private TextView textViewWeather; //объявляем наши поля для отображения погоды
    private EditText editTextCity; //город или индекс

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextCity = findViewById(R.id.editTextCity); //R есть Java-класс, автоматически создается из наших ресурсов в процессе сборки
        textViewWeather = findViewById(R.id.textViewWeather); //findViewById метод, который находит представление по заданному идентификатору
    }

    public void onClickShowWeather(View view) {
        String city = editTextCity.getText().toString().trim();
        if (!city.isEmpty()) { //если получили ответ и город найден
            DownloadWeatherTask task = new DownloadWeatherTask();
            String url = String.format(WEATHER_URL, city); //формируем строку
            task.execute(url); //запускаем загрузчик
        }
    }

    private class DownloadWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            //ответ ждем в фоновом режиме
            URL url = null;
            HttpsURLConnection urlConnection = null;
            StringBuffer result = new StringBuffer();
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine(); //пытаемся получить ответ
                while (line != null) {
                    result.append(line);
                    line = reader.readLine();
                }
                return result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect(); //в конце нужно закрыть соединение
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.isEmpty()) {
                textViewWeather.setText("Город не найден"); //Если ответ отличается от нашего формата, разбирать дальше не будем
            } else {
                super.onPostExecute(s);
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String city = jsonObject.getString("name"); //разбираем на JSON по тегам
                    String temp = jsonObject.getJSONObject("main").getString("temp");
                    String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    String weather = String.format("%s\nтемпература: %s\n %s\n", city, temp, description); //формируем строку с ответом
                    textViewWeather.setText(weather); //выводим
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}