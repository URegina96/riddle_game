package com.example.riddlegame;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private ImageView imageViewPhoto;

    private String url = "https://faterra.com/katalog-komnatnykh-tsvetov.html";
    private ArrayList<String> urls;
    private ArrayList<String> names;
    private int numberOfQuestion;
    private int numberOfRightAnswer;
    private ArrayList<Button> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = findViewById(R.id.buttonOne);
        button2 = findViewById(R.id.buttonTwo);
        button3 = findViewById(R.id.buttonThree);
        button4 = findViewById(R.id.buttonFour);
        buttons = new ArrayList<>();
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);
        buttons.add(button4);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        urls = new ArrayList<>();
        names = new ArrayList<>();
        getContent();
        playGame();
    }

    private void getContent() {
        DownloadContentTask task = new DownloadContentTask();
        try {
            String content = task.execute(url).get();
            String start = "<div class=\"catalog-letter\" id=\"А\">А</div><div class=\"catalog-flowers-letter\"><div class=\"three\">";
            String finish = "</div></div><div class=\"catalog-letter\" id=\"Б\">Б</div><div class=\"catalog-flowers-letter\"><div class=\"three\">";
            Pattern pattern = Pattern.compile(start + "(.*?)" + finish);
            Matcher matcher = pattern.matcher(content);
            String splitContent = "";
            while (matcher.find()) {
                splitContent = matcher.group(1);
            }
            Pattern patternImg = Pattern.compile("src=\"(.*?)\"");
            Pattern patternName = Pattern.compile("alt=\"(.*?)\"");
            Matcher matcherImg = patternImg.matcher(splitContent);
            Matcher matcherName = patternName.matcher(splitContent);
            while (matcherImg.find()) {
                urls.add(matcherImg.group(1));
            }
            while (matcherName.find()) {
                urls.add(matcherName.group(1));
            }
//            for (String s : names){
//                Log.i("JHGk", s);
//            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();//throw new RuntimeException(e);
        }
    }

    private void playGame() {
        generateQuestion();
        DownloadImgTask task = new DownloadImgTask();
        try {
            Bitmap bitmap = task.execute(urls.get(numberOfQuestion)).get();
            if (bitmap != null) {
                imageViewPhoto.setImageBitmap(bitmap);
                for (int i = 0; i < buttons.size(); i++) {
                    if (i == numberOfRightAnswer) {
                        buttons.get(i).setText(names.get(numberOfQuestion));
                    } else {
                        int wrongAnswer = generateWrongAnswer();
                        buttons.get(i).setText(names.get(wrongAnswer));
                    }
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();//throw new RuntimeException(e);
        }
    }

    private void generateQuestion() {
        numberOfQuestion = (int) (Math.random() * names.size());
        numberOfRightAnswer = (int) (Math.random() * buttons.size());
    }

    private int generateWrongAnswer() {
        return (int) (Math.random() * names.size());
    }

    public void onClickAnswer(View view) {
        Button button = (Button) view;
        String tag = button.getTag().toString();
        if (Integer.parseInt(tag) == numberOfRightAnswer) {
            Toast.makeText(this, "Верно", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ошибка, верный ответ: " + names.get(numberOfQuestion), Toast.LENGTH_SHORT).show();
        }
        playGame();
    }

    private static class DownloadContentTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    result.append(line);
                    line = reader.readLine();
                }
                return result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();//throw new RuntimeException(e);
            } catch (IOException e) {
                e.printStackTrace();//throw new RuntimeException(e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }

    private static class DownloadImgTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();//throw new RuntimeException(e);
            } catch (IOException e) {
                e.printStackTrace();//throw new RuntimeException(e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }
}