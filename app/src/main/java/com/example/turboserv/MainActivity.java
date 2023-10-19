package com.example.turboserv;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Button button_get_data;
    private TextView result_title;
    private TextView result_body;
    private static final String apiUrl = "https://jsonplaceholder.typicode.com/posts/1";
    private static final int TIMEOUT = 10000; // 10 секунд в миллисекундах
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_get_data = findViewById(R.id.button_get_data);
        result_title = findViewById(R.id.result_title);
        result_body = findViewById(R.id.result_body);

        button_get_data.setOnClickListener(view -> {

            new GetUrlData().execute(apiUrl);
        });
    }

    private class GetUrlData extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            result_title.setText("Ожидайте...");
            result_body.setText("Ожидайте...");

            timer = new CountDownTimer(TIMEOUT, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    // Время ожидания истекло, выводим ошибку
                    showErrorToUser();
                    result_title.setText("");
                    result_body.setText("");
                }
            }.start();
        }

        private void showErrorToUser() {
            Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(TIMEOUT); // Устанавливаем таймаут соединения

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    return buffer.toString();
                } else {

                    return null;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            if (timer != null) {
                timer.cancel();
            }
            super.onPostExecute(result);

            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    result_title.setText("Заголовок: " + jsonObject.getString("title"));
                    result_body.setText("Текст поста: " + jsonObject.getString("body"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // Обработка ошибки
                showErrorToUser();
                result_title.setText("");
                result_body.setText("");
            }
        }
    }
}