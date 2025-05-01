package com.example.musicplayer;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayerController {

    @FXML
    private ImageView coverArt;
    @FXML
    private ListView playlist;

    @FXML
    private Button addToPlaylistButton;

    @FXML
    private Button removeFromPlaylistButton;

    private MediaPlayer mediaPlayer;

    @FXML
    private Slider positionSlider;

    @FXML
    private Label timeLabel;

    @FXML
    private Label trackInfo;

    private Stage playlistWindow;

    private boolean playlistIsVisible = false;

    @FXML
    public void initialize() {

    }

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
        FileChooser.ExtensionFilter mp3Filter = new FileChooser.ExtensionFilter("MP3 Files (*.mp3)", "*.mp3");
        fileChooser.getExtensionFilters().add(mp3Filter);
        File file = fileChooser.showOpenDialog(null);
        info = "";

        if (file != null) {
            Media media = new Media(file.toURI().toString());
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            mediaPlayer = new MediaPlayer(media);
            positionSlider.setMin(0);

            media.getMetadata().addListener(new MapChangeListener<String, Object>() {
                @Override
                public void onChanged(Change<? extends String, ?> change) {
                    if (change.getMap().containsKey("artist")) {
                        info = "*" + change.getMap().get("artist").toString();
                        trackInfo.setText(info);
                    }
                    if (change.getMap().containsKey("title")) {
                        info += "-" + change.getMap().get("title").toString() + "*";
                        trackInfo.setText(info);
                    }
                    if (change.getMap().containsKey("image")) {
                        coverArt.setImage((Image)change.getMap().get("image"));
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

            positionSlider.valueProperty().addListener(new InvalidationListener() {
                public void invalidated(Observable ov) {
                    if (positionSlider.isValueChanging()) {
                        Duration duration = mediaPlayer.getMedia().getDuration();
                        // multiply duration by percentage calculated by slider position
                        mediaPlayer.seek(duration.multiply(positionSlider.getValue() / 100.0));
                    }
                }});

            mediaPlayer.play();

        }



    }

    @FXML
    protected void pauseButtonClick() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @FXML
    protected void stopButtonClick() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }


    @FXML
    protected void openPlaylistWindow() {
        Stage currentStage = (Stage) timeLabel.getScene().getWindow();
        if(playlistIsVisible) {
            currentStage.setHeight(currentStage.getHeight() - 250);
            currentStage.setWidth(currentStage.getWidth() + 0);
            playlist.setVisible(false);
            addToPlaylistButton.setVisible(false);
            removeFromPlaylistButton.setVisible(false);
        } else {
            currentStage.setHeight(currentStage.getHeight() + 250);
            currentStage.setWidth(currentStage.getWidth() + 0);
            playlist.setVisible(true);
            addToPlaylistButton.setVisible(true);
            removeFromPlaylistButton.setVisible(true);
        }

        playlistIsVisible = !playlistIsVisible;
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
        }, 2000, 500);

    }

    protected void scrollInfo() {
        Platform.runLater(new Runnable() {
            public void run() {

                String firstChar = scrolledInfo.substring(0, 1);

                if ("*".equals(firstChar)) {
                    scrolledInfo = scrolledInfo.substring(1);
                }else {
                    scrolledInfo = scrolledInfo.substring(1) + firstChar;
                }

                if (!scrolledInfo.contains("*")) {
                    scrolledInfo += "*";
                }
                trackInfo.setText(scrolledInfo);
            }
        });
    }

    @FXML
    private void addToPlaylist() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter mp3Filter = new FileChooser.ExtensionFilter("MP3 Files (*.mp3)", "*.mp3");
        fileChooser.getExtensionFilters().add(mp3Filter);
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            playlist.getItems().add(file.getName());
        }
    }


}