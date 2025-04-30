package com.example.musicplayer;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.FileChooser;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayerController {

    private MediaPlayer mediaPlayer;

    @FXML
    private Slider positionSlider;

    @FXML
    private Label timeLabel;

    @FXML
    private Label trackInfo;

    @FXML
    protected void playButtonClick() {
        mediaPlayer.play();
    }

    private String info;
    private String scrolledInfo;
    private Timer timer = new Timer();

    @FXML
    protected void openButtonClick() {

        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        Media media = new Media(file.toURI().toString());
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        mediaPlayer = new MediaPlayer(media);
        positionSlider.setMin(0);
        info = "";
        media.getMetadata().addListener(new MapChangeListener<String, Object>() {
            @Override
            public void onChanged(Change<? extends String, ?> change) {
                if (change.getMap().containsKey("artist")) {
                    info = change.getMap().get("artist").toString();
                    trackInfo.setText(info);
                }
                if (change.getMap().containsKey("title")) {
                    info += "-" + change.getMap().get("title").toString();
                    trackInfo.setText(info);
                }

            }
        });


        mediaPlayer.setOnPlaying(new Runnable() {
            @Override
            public void run() {
                startTrackInfoAnimation();
            }

        });

        mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                updateTimeElements();
            }
        });
        mediaPlayer.play();

    }

    @FXML
    protected void pauseButtonClick() {
        mediaPlayer.pause();
    }


    protected void updateTimeElements() {
        Platform.runLater(new Runnable() {
            public void run(){

                Double newValue = mediaPlayer.getCurrentTime().divide(mediaPlayer.getMedia().getDuration()).toMillis()*100.0;
                positionSlider.setValue(newValue);
                updateTimeLabel();

        }});

    }

    protected void updateTimeLabel() {
        String elapsedTime = String.format("%02d:%02d:%02d",(int)mediaPlayer.getCurrentTime().toHours(),(int)(mediaPlayer.getCurrentTime().toSeconds()/60),(int)(mediaPlayer.getCurrentTime().toSeconds()%60));
        timeLabel.setText(elapsedTime);
    }

    protected void startTrackInfoAnimation() {
        timer.cancel();
        timer = new Timer();
        scrolledInfo = info;
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                scrollInfo();
            }
        }, 2000, 1000);

    }

    protected void scrollInfo() {
        Platform.runLater(new Runnable() {
            public void run() {

                String firstChar = scrolledInfo.substring(0, 1);
                scrolledInfo = scrolledInfo.substring(1, scrolledInfo.length()) + firstChar;
                trackInfo.setText(scrolledInfo);
            }
        });
    }

}