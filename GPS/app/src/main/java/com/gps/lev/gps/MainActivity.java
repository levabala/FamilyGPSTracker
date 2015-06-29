package com.gps.lev.gps;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.InputSource;


public class MainActivity extends ActionBarActivity {
    ArrayList<Location> locations = new ArrayList<>();
    String sLocations = new String();

    int count = 0;

    TextView tvLocationGPS;
    TextView tvLocationNet;
    TextView locationsLength;

    private LocationManager locationManager;
    StringBuilder sbGPS = new StringBuilder();
    StringBuilder sbNet = new StringBuilder();                  //Здесь лежат какие-то переменные, я их не использую. Когда-ниубдь уберу :)


    public void SwitchChanged(View v) {   //Тестовая функция для отслеживания переключения свитча
        Switch rb = (Switch) v;
        Toast.makeText(this, "Отслеживание переключения: " + (rb.isChecked() ? "on" : "off"),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){   //Создание активити...
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String entryWords = "<?xml version =\"1.0\" encoding=\"UTF-8\"?>\n" +  //Заголовок для xml-файла
                "\n" +
                "<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0\"\n" +
                " xmlns=\"http://www.topografix.com/GPX/1/0\" creator=\"LB company\" \n" +
                "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\"><time>" + formatDate(new Date()) + "</time><trk><name>GPS-трекер</name><trkseg>";
        try{  //Запись заголовка в файл хранения
            FileOutputStream fos = openFileOutput("Locations.data", Context.MODE_PRIVATE);
            fos.write(entryWords.getBytes());
            fos.close();
        }
        catch (java.io.FileNotFoundException e){
            Toast.makeText(this,"Ошибка чтения файла: \n" + e, Toast.LENGTH_LONG).show();
        }
        catch (java.io.IOException e){
            Toast.makeText(this,"Ошибка записи в файл: \n" + e, Toast.LENGTH_LONG).show();
        }


        startService( //Запуск сервиса по отслеживанию геоданных
                new Intent(MainActivity.this, MyService.class)
        );
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy() { //При закрытии приложения остановить и сервис (не всегда срабатывает)
        super.onDestroy();
        stopService(
                new Intent(MainActivity.this, MyService.class)
        );
        Toast.makeText(this,"@string/app_name " + "закрыт", Toast.LENGTH_LONG).show(); //Почему-то не работает получение имени приложения
    }


    public void onClickLocationSittings(View view){  //Открытие настроек получения местоположения
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    };

    public void onClickOpenHistory(View view){ //Показать историю (тестовая функция)
        try {
            InputStream is = openFileInput("Locations.data");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);  //Чтение файла
            StringBuilder string = new StringBuilder();
            String line = "";

            while ((line = reader.readLine()) != null){
                string.append(line);  //Через StringBuilder добавляю строки в String
            }

            is.close();

            Toast.makeText(this, string.toString() + "</trkseg></trk></gpx>", Toast.LENGTH_SHORT).show(); //Показываю содержимое файла
        }
        catch (java.io.IOException e){
            Toast.makeText(this, "Ошибка работы с файлом: \n" + e, Toast.LENGTH_LONG).show();
        }
    };

    private void FinalWriting(){  //Функция записи конца файла
        try{
            InputStream is = openFileInput("Locations.data");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder string = new StringBuilder();
            String line = "";

            while ((line = reader.readLine()) != null){
                string.append(line);
            }

            String s = string.toString() + "</trkseg></trk></gpx>";  //Вся суть в этих закрывающих тегах
            FileOutputStream fos = openFileOutput("Locations.data", Context.MODE_PRIVATE);
            fos.write(s.getBytes());
            fos.close();
            is.close();
        }
        catch (java.io.FileNotFoundException e){
            Toast.makeText(this,"Ошибка чтения файла\n", Toast.LENGTH_LONG).show();
        }
        catch (java.io.IOException e){
            Toast.makeText(this,"Ошибка записи в файл\n", Toast.LENGTH_LONG).show();
        }
    }

    public String getHistoryFile(){ //Быстрое получение содержимого файла хранения (для последующей отправки)
        try{
            InputStream is = openFileInput("Locations.data");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder string = new StringBuilder();
            String line = "";

            while ((line = reader.readLine()) != null){
                string.append(line);
            }
            is.close();
            reader.close();
            isr.close();
            string.append("</trkseg></trk></gpx>");
            return string.toString();
        }
        catch (java.io.FileNotFoundException e){
            Toast.makeText(this,"Ошибка чтения файла\n", Toast.LENGTH_LONG).show();
        }
        catch (java.io.IOException e){
            Toast.makeText(this,"Ошибка записи в файл\n", Toast.LENGTH_LONG).show();
        }
        return "";
    }

    Boolean SendingSuccessful = true;  //Эта штука пока что не работает
    public void SendPathToServer(View v){
        try {
            //Toast.makeText(this,"Задача создана", Toast.LENGTH_LONG).show();
            HttpReq ReqTask = new HttpReq();
            ReqTask.execute(getHistoryFile());
        }
        catch (Exception e) {Toast.makeText(this,"Ошибка: " + e, Toast.LENGTH_LONG).show();}

        if (SendingSuccessful) { //Это надо заменить на EventListener. Генерить событие в AsyncTask
            Toast.makeText(this,"Новый маршрут",Toast.LENGTH_LONG).show(); //Очистка маршрута. Надо диалоговое окошко выбора сделать
            try {
                FileOutputStream fos = openFileOutput("Locations.data", Context.MODE_PRIVATE);
                String c = "<?xml version =\"1.0\" encoding=\"UTF-8\"?>\n" +      //Опять заголовок... Пора его куда-нибудь вынести
                        "\n" +
                        "<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0\"\n" +
                        " xmlns=\"http://www.topografix.com/GPX/1/0\" creator=\"LB company\" \n" +
                        "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\"><time>" + formatDate(new Date()) + "</time><trk><name>GPS-трекер</name><trkseg>";
                fos.write(c.getBytes());
            } catch (java.io.FileNotFoundException e) {
                Toast.makeText(this, "Ошибка чтения файла\n", Toast.LENGTH_LONG).show();
            } catch (java.io.IOException e) {
                Toast.makeText(this, "Ошибка записи в файл\n", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String formatDate(Date d){ //Форматирование даты под формат GPX
        SimpleDateFormat[] formats = new SimpleDateFormat[]{
                new SimpleDateFormat("yyyy-MM-dd"),
                new SimpleDateFormat("kk:mm:ss")
        };
        String date =
                (formats[0].format(d)) +
                "T" +
                (formats[1].format(d)) +
                "Z";
        return date;
    };


    private class HttpReq extends AsyncTask<String,Void,String>{ //Асинхронная задача отправки маршрута
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String aString) {
            super.onPostExecute(aString);
            Toast.makeText(getApplicationContext(),"Статус завершения подключения: " + aString, Toast.LENGTH_LONG).show();
            if (aString == "Ошибка") SendingSuccessful = false; //Надо заменить на создание callback
        }

        @Override
        protected String doInBackground(String... params) {
            String inf = "";
            try{  //Ну а тут начинатся белеберда с http-запросом :)
                Switch rb = (Switch)findViewById(R.id.switch1);
                String answer = "";

                URL url;
                if (rb.isChecked()) url = new URL("http://192.168.3.5:3000/android");  //Генерю URL
                else url = new URL("http://62.84.166.86:3000/android");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //Открываю соединение

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);  //Задаю параметры запроса
                connection.setReadTimeout(5000);

                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.write(params[0].getBytes()); //Пишу трек в выходящий поток

                connection.disconnect();
                outputStream.close(); //Закрываю соединение

                return String.valueOf(connection.getResponseMessage());  //Если всё ок, вывожу ответ сервера (должен быть Accepted)
            }
            catch (Exception e){
                return "Ошибка";
            }
        }
    }
}
