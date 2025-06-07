package com.example.musicplayer;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class MusicPlayerController {

    @FXML
    public Slider volumeSlider;

    @FXML
    private ImageView coverArt;

    @FXML
    private ListView playlist;

    private List<File> playlistFiles = new java.util.ArrayList<File>();

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

    @FXML
    private Button openPlaylistButton;

    @FXML
    private Button savePlaylistButton;


    private boolean playlistIsVisible = false;

    private InvalidationListener positionInvalidationListener;

    private boolean hasFinishedMedia = false;

    @FXML
    public void initialize() {
        volumeSlider.setValue(100);
    }

    @FXML
    protected void playButtonClick() {
        positionSlider.setMin(0);
        Media media = mediaPlayer.getMedia();
        mediaPlayer = new MediaPlayer(media);
        updateInfoAndCoverArt(media);
        mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
        updateUI(mediaPlayer);
        mediaPlayer.play();
        hasFinishedMedia = false;
        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.stop();
            hasFinishedMedia = true;
        });
    }

    private String info;
    private String scrolledInfo;
    private Timer timer = new Timer();
    String playListFileDescription = "";

    @FXML
    protected void openButtonClick() {

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter mp3Filter = new FileChooser.ExtensionFilter("MP3 Files (*.mp3)", "*.mp3");
        fileChooser.getExtensionFilters().add(mp3Filter);
        File file = fileChooser.showOpenDialog(null);
        info = "";
        coverArt.setImage(new Image("file:src/main/resources/com/example/musicplayer/icons/unknown_cover.png"));

        if (file != null) {
            Media media = new Media(file.toURI().toString());
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            mediaPlayer = new MediaPlayer(media);
            positionSlider.setMin(0);
            updateInfoAndCoverArt(media);
            mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
            updateUI(mediaPlayer);
            mediaPlayer.play();

            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.stop();
                hasFinishedMedia = true;
            });
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
        if (playlistIsVisible) {
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
            openPlaylistButton.setVisible(true);
            savePlaylistButton.setVisible(true);
        }

        playlistIsVisible = !playlistIsVisible;
    }

    protected void updateTimeElements() {
        Platform.runLater(new Runnable() {
            public void run() {

                Double newValue = mediaPlayer.getCurrentTime().divide(mediaPlayer.getMedia().getDuration()).toMillis() * 100.0;
                positionSlider.setValue(newValue);
                updateTimeLabel();

            }
        });

    }

    protected void updateTimeLabel() {
        String elapsedTime = String.format("%02d:%02d:%02d", (int) mediaPlayer.getCurrentTime().toHours(), (int) (mediaPlayer.getCurrentTime().toSeconds() / 60), (int) (mediaPlayer.getCurrentTime().toSeconds() % 60));
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
                } else {
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
            playlistFiles.add(file);
            playlist.getItems().add(file.getName());

        }

    }

    @FXML
    private void removeFromPlaylist() {
        if (playlist.getSelectionModel().getSelectedItem() != null) {
            playlistFiles.remove(playlist.getSelectionModel().getSelectedIndex());
            playlist.getItems().remove(playlist.getSelectionModel().getSelectedIndex());
        }
    }

    @FXML
    private void selectFromPlaylist(MouseEvent event) {
        if (event.getClickCount() == 2) {
            playFromPlaylist();
        }
    }

    private void playFromPlaylist() {
        AtomicInteger chosenIndex = new AtomicInteger(playlist.getSelectionModel().getSelectedIndex());
        File file = playlistFiles.get(chosenIndex.get());
        Media media = new Media(file.toURI().toString());
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        mediaPlayer = new MediaPlayer(media);
        coverArt.setImage(new Image("file:src/main/resources/com/example/musicplayer/icons/unknown_cover.png"));
        updateInfoAndCoverArt(media);
        mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
        updateUI(mediaPlayer);
        mediaPlayer.play();
        mediaPlayer.setOnEndOfMedia(() -> {
            int nextIndex = chosenIndex.incrementAndGet();
            if (nextIndex < playlistFiles.size()) {
                File nextFile = playlistFiles.get(nextIndex);
                Media nextMedia = new Media(nextFile.toURI().toString());
                mediaPlayer = new MediaPlayer(nextMedia);
                coverArt.setImage(new Image("file:src/main/resources/com/example/musicplayer/icons/unknown_cover.png"));
                updateInfoAndCoverArt(nextMedia);
                mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
                updateUI(mediaPlayer);
                playlist.getSelectionModel().selectNext();
                mediaPlayer.play();
            }
        });
    }


    private void updateUI(MediaPlayer mediaPlayer) {

        mediaPlayer.setOnPlaying(new Runnable() {
            @Override
            public void run() {
                startTrackInfoAnimation();
            }

        });

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                updateTimeElements();
            }

        });

        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (volumeSlider.isValueChanging()) {
                    mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
                }

            }
        });

        if (!volumeSlider.isValueChanging()) {
            volumeSlider.setValue((int) Math.round(mediaPlayer.getVolume()
                    * 100));
        }

        if (positionInvalidationListener != null) {
            positionSlider.valueProperty().removeListener(positionInvalidationListener);
        }
        positionInvalidationListener = createInvalidationListener(mediaPlayer);

        positionSlider.valueProperty().addListener(positionInvalidationListener);
    }

    private void updateInfoAndCoverArt(Media media) {
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
                    coverArt.setImage((Image) change.getMap().get("image"));
                }
            }
        });
    }

    private InvalidationListener createInvalidationListener(MediaPlayer mediaPlayer) {
        return new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (positionSlider.isValueChanging()) {
                    Duration duration = mediaPlayer.getMedia().getDuration();
                    // multiply duration by percentage calculated by slider position
                    mediaPlayer.seek(duration.multiply(positionSlider.getValue() / 100.0));
                }
            }
        };
    }

    @FXML
    protected void previousButtonClick() {
        if (playlistFiles.isEmpty())
            return;
        playlist.getSelectionModel().selectPrevious();
        playFromPlaylist();
    }

    @FXML
    protected void nextButtonClick() {
        if (playlistFiles.isEmpty())
            return;
        playlist.getSelectionModel().selectNext();
        playFromPlaylist();
    }

    @FXML
    protected void openPlaylist() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter playlistFilter = new FileChooser.ExtensionFilter("Playlist Files (*.playlist)", "*.playlist");
        fileChooser.getExtensionFilters().add(playlistFilter);
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            List<String> lines = readLinesFromFile(file.toPath());

            if (lines != null) {
                playlist.getItems().clear();
                for (String line:lines)
                {
                    File tempFile = new File(line);
                    playlistFiles.add(tempFile);
                    playlist.getItems().add(tempFile.getName());
                }

            }
        } else {
            System.out.println("No file selected.");
        }
    }


    public static List<String> readLinesFromFile(Path filePath) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            System.err.println("Error reading the file:");
            e.printStackTrace();
        }
        return lines;
    }


    @FXML
    protected void savePlaylist() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter playlistFilter = new FileChooser.ExtensionFilter("playlist files","*.playlist");
        fileChooser.getExtensionFilters().add(playlistFilter);
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            savePlaylistToFile(file);
        } else {
            System.out.println("Save canceled.");
        }
    }

    private void savePlaylistToFile(File file) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            StringBuilder content = new StringBuilder();
            for (File playlistFile : playlistFiles) {
                content.append(playlistFile.getAbsolutePath()).append("\n");
            }
            fileWriter.write(content.toString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
