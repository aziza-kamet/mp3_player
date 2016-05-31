package sample;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.beans.EventHandler;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Controller implements Initializable{

    @FXML
    Pane logoPane;

    @FXML
    MediaView mediaView;
    @FXML
    Slider slider;
    @FXML
    Pane controlPane;
    @FXML
    Pane rightPane;

    @FXML
    ListView videoList;
    @FXML
    TableView audioList;
    @FXML
    TableColumn artistColumn;
    @FXML
    TableColumn songColumn;

    @FXML
    Button playButton;
    @FXML
    Button stopButton;
    @FXML
    Button pauseButton;
    @FXML
    Button nextButton;
    @FXML
    Button prevButton;

    @FXML
    ImageView imageView;
    @FXML
    Label nameLabel;
    @FXML
    Label artistLabel;
    @FXML
    Label albumLabel;
    @FXML
    ProgressBar progressBar;
    @FXML
    Label currentDurationLabel;
    @FXML
    Label totalDurationLabel;
    @FXML
    Rectangle albumFrame;


    Timeline appear = new Timeline();
    Timeline disappear = new Timeline();
    Timeline slideIn = new Timeline();
    Timeline slideOut = new Timeline();

    private MediaPlayer mediaPlayer;
    private HashMap<Object, String> audioMap = new HashMap<Object, String>();
    private Object obj = new Object();
    private Random rand = new Random();
    private ObservableList<AudioData> listOfAudio;

    private int position;
    private boolean audioSelected = false;
    private String currentDirectory = "C:/Users/Aziza/IdeaProjects/MediaPlayer";

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        artistColumn.setCellValueFactory(new PropertyValueFactory<AudioData, String>("Artist"));
        songColumn.setCellValueFactory(new PropertyValueFactory<AudioData, String>("Song"));

        setVideo();
        setAudio();

        audioList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection)->{
            if(newSelection!=null){
                onAudioSelected();
            }
        });

        appear.getKeyFrames().addAll(
                new KeyFrame(new Duration(0), new KeyValue(controlPane.opacityProperty(), 0.0)),
                new KeyFrame(new Duration(400), new KeyValue(controlPane.opacityProperty(), 1.0))
        );

        disappear.getKeyFrames().addAll(
                new KeyFrame(new Duration(0), new KeyValue(controlPane.opacityProperty(), 1.0)),
                new KeyFrame(new Duration(400), new KeyValue(controlPane.opacityProperty(), 0.0))
        );

        slideIn.getKeyFrames().addAll(
                new KeyFrame(new Duration(500), new KeyValue(rightPane.translateXProperty(), 300))
        );

        slideOut.getKeyFrames().addAll(
                new KeyFrame(new Duration(500), new KeyValue(rightPane.translateXProperty(),-300))
        );

        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                progressBar.setProgress(newValue.doubleValue()/mediaPlayer.getTotalDuration().toSeconds());
            }
        });
    }

    public void onVideoTabSelected(){
        audioSelected = false;
    }

    public void onAudioTabSelected(){
        audioSelected = true;
    }

    public void onVideoSelected() {

        if(mediaPlayer!=null)
            mediaPlayer.stop();
        String name = new File("video/"+videoList.getSelectionModel().getSelectedItem()).toURI().toASCIIString();
        Media media = new Media(name);
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();

        imageView.setOpacity(0);
        nameLabel.setText("");
        artistLabel.setText("");
        albumLabel.setText("");

        changeGUIElements();
    }

    public void onAudioSelected() {

        if(mediaPlayer!=null)
            mediaPlayer.stop();
        String name = new File("audio/"+ audioMap.get(audioList.getSelectionModel().getSelectedItem())).toURI().toASCIIString();
        Media media = new Media(name);
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();

        imageView.setImage(new Image("images/music.png"));
        imageView.setOpacity(1);
        artistLabel.setText("Unknown");
        albumLabel.setText("Unknown");
        nameLabel.setText("Unknown");


        media.getMetadata().addListener(new MapChangeListener<String, Object>() {
            @Override
            public void onChanged(Change<? extends String, ? extends Object> ch){
                if(ch.wasAdded()){
                    handleMetadata(ch.getKey(), ch.getValueAdded());
                }
            }
        });

        changeGUIElements();
    }

    public void onAddButtonClicked(){

        FileChooser fileChooser = new FileChooser();

        if(audioSelected){
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MP3","*.mp3"),
                    new FileChooser.ExtensionFilter("MP4","*.mp4","*.m4a"),
                    new FileChooser.ExtensionFilter("AIFF","*.aif","*.aiff"),
                    new FileChooser.ExtensionFilter("WAV","*.wav"));
            fileChooser.setInitialDirectory(new File("c:/Users/Aziza"));
            File file = fileChooser.showOpenDialog(Main.stage);
            if(file!=null){
                Path pathFrom = Paths.get(file.getParent(), file.getName());
                Path pathTo = Paths.get(currentDirectory+"/audio", file.getName());
                try{
                    Files.copy(pathFrom, pathTo);
                    setAudio();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }else {
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MP4","*.mp4","*.m4v"),
                    new FileChooser.ExtensionFilter("FXM, FLV","*.fxm","*.flv"));
            fileChooser.setInitialDirectory(new File("c:/Users/Aziza"));
            File file = fileChooser.showOpenDialog(Main.stage);
            if(file!=null){
                Path pathFrom = Paths.get(file.getParent(), file.getName());
                Path pathTo = Paths.get(currentDirectory+"/video", file.getName());
                try{
                    Files.copy(pathFrom, pathTo);
                    setVideo();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

    }

    public void handleMetadata(String key, Object value){
        if(key.equals("image")) {
            imageView.setImage((Image) value);
            albumFrame.setVisible(true);
        }
        else if(key.equals("title"))
            nameLabel.setText(value+"");
        else if(key.equals("artist"))
            artistLabel.setText(value+"");
        else if(key.equals("album"))
            albumLabel.setText(value+"");
    }

    public void setAudio(){

        audioList.getItems().clear();
        listOfAudio = FXCollections.observableArrayList();
        File audioFolder = new File(currentDirectory+"/audio");
        File[] audios = audioFolder.listFiles();
        for(int i=0; i<audios.length; i++){
            getMetaData(audios[i].getName());
            try {
                synchronized (obj) {
                    obj.wait(100);
                }
            }catch (Exception e){}
        }
        audioList.setItems(listOfAudio);
    }

    public void setVideo(){

        videoList.getItems().clear();
        ObservableList listOfVideo = FXCollections.observableArrayList();
        File videoFolder = new File(currentDirectory+"/video");
        File[] videos = videoFolder.listFiles();
        for(int i=0; i<videos.length; i++){
            listOfVideo.add(videos[i].getName());
        }
        videoList.setItems(listOfVideo);

    }

    public void getMetaData(String name){
        Media media = new Media(new File("audio/"+name).toURI().toASCIIString());
        MediaPlayer tmp = new MediaPlayer(media);
        tmp.setOnReady(new Runnable() {
            @Override
            public void run() {
                AudioData tempAudioData = new AudioData();
                tempAudioData.setArtist(media.getMetadata().get("artist")+"");
                tempAudioData.setSong(media.getMetadata().get("title")+"");
                audioMap.put(tempAudioData, name);
                listOfAudio.add(tempAudioData);
                synchronized (obj){
                    obj.notify();
                }
            }
        });
    }

    public void onMouseEnteredView(){
        appear.play();
    }

    public void onMouseExitedView(){
        disappear.play();
    }

    public void onMouseEnteredRight(){
        slideOut.play();
    }

    public void onMouseExitedRight(){
        slideIn.play();
    }

    public void onSliderClicked(){
        mediaPlayer.seek(Duration.seconds(slider.getValue()));
    }

    public void onPlayButtonClicked(){

        mediaPlayer.play();

        playButton.setVisible(false);
        pauseButton.setVisible(true);

    }

    public void onStopButtonClicked(){

        mediaPlayer.stop();

        slider.setValue(0);
        Date time = new Date(0);
        currentDurationLabel.setText(new SimpleDateFormat("mm:ss").format(time));

        playButton.setVisible(true);
        pauseButton.setVisible(false);
    }

    public void onPauseButtonClicked(){

        mediaPlayer.pause();

        playButton.setVisible(true);
        pauseButton.setVisible(false);

    }

    public void onNextButtonClicked(){

        if(audioSelected)
            nextAudio();
        else
            nextVideo();
    }

    public void onPrevButtonClicked(){

        if(audioSelected)
            prevAudio();
        else
            prevVideo();
    }

    public void nextAudio(){

        int index = audioList.getSelectionModel().getSelectedIndex()+1;
        if(index<audioList.getItems().size()) {
            audioList.getSelectionModel().select(index);
            onAudioSelected();
        }
    }

    public void nextVideo(){

        int index = videoList.getSelectionModel().getSelectedIndex()+1;
        if(index<videoList.getItems().size()) {
            videoList.getSelectionModel().select(index);
            onVideoSelected();
        }
    }

    public void prevAudio(){

        int index = audioList.getSelectionModel().getSelectedIndex()-1;
        if(index>=0) {
            audioList.getSelectionModel().select(index);
            onAudioSelected();
        }
    }

    public void prevVideo(){

        int index = videoList.getSelectionModel().getSelectedIndex()-1;
        if(index>=0) {
            videoList.getSelectionModel().select(index);
            onVideoSelected();
        }
    }

    public void changeGUIElements(){

        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {

                slider.setMin(0.0);
                slider.setMax(mediaPlayer.getTotalDuration().toSeconds());
                slider.setValue(0.0);
                Date time = new Date((long)mediaPlayer.getTotalDuration().toSeconds()*1000);
                totalDurationLabel.setText(new SimpleDateFormat("mm:ss").format(time));
            }
        });

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {

                if(newValue.toSeconds()-slider.getValue()>0.3 && newValue.toSeconds()-slider.getValue() < 1.0) {
                    slider.setValue(newValue.toSeconds());
                    Date time = new Date((long) slider.getValue() * 1000);
                    String sdf = new SimpleDateFormat("mm:ss").format(time);
                    currentDurationLabel.setText(sdf);
                }
            }
        });

        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                if(audioSelected)
                    nextAudio();
                else
                    nextVideo();
            }
        });

        logoPane.setVisible(false);

        playButton.setVisible(false);
        pauseButton.setVisible(true);
        albumFrame.setVisible(false);
        slider.setDisable(false);
        playButton.setDisable(false);
        stopButton.setDisable(false);
        pauseButton.setDisable(false);
        nextButton.setDisable(false);
        prevButton.setDisable(false);
    }

    public void onShuffleClicked(){
        if(audioSelected) {
            double t = mediaPlayer.getCurrentTime().toMillis();
            if(audioList.getSelectionModel()!=null)
                audioList.setItems(shuffle(audioList.getItems(), audioList.getSelectionModel().getSelectedIndex()));
            else
                audioList.setItems(shuffle(audioList.getItems(), -1));
            audioList.getSelectionModel().select(0);
        }
        else {
            videoList.setItems(shuffle(videoList.getItems(), videoList.getSelectionModel().getSelectedIndex()));
            videoList.getSelectionModel().select(0);
        }
    }

    public void onRepeatClicked(){
        if(audioSelected){
            ObservableList tempList = audioList.getItems();
            int mid = tempList.size()/2;
            audioList.setItems(sort(tempList, mid, 0, tempList.size()-1));
        }else {
            ObservableList tempList = videoList.getItems();
            int mid = tempList.size()/2;
            videoList.setItems(sort(tempList, mid, 0, tempList.size()-1));
        }

    }

    public ObservableList shuffle(ObservableList list, int selectedIndex){

        ObservableList result = FXCollections.observableArrayList();
        if(selectedIndex>=0) {
            result.add(list.get(selectedIndex));
            list.remove(selectedIndex);
        }
        for (int i = list.size(); i>0; i--){
            int index = rand.nextInt(i);
            result.add(list.get(index));
            list.set(index, list.get(i-1));
        }

        return result;
    }

    public ObservableList sort(ObservableList arr, int mid, int beg, int end){

        if(beg==end)
            return arr;

        ObservableList leftList = sort(arr, beg+(mid-beg)/2, beg, mid);
        ObservableList rightList = sort(arr, mid+1+(end-mid-1)/2, mid+1, end);

        ObservableList tempList = FXCollections.observableArrayList();
        for (int i=0; i<arr.size(); i++) {
            tempList.add(new Integer(0));
        }

        int l=beg, r=mid+1, t=beg;
        String leftString, rightString;
        while(l<=mid && r<=end){

            if(leftList.get(l) instanceof AudioData){
                leftString = ((AudioData)leftList.get(l)).getSong();
                rightString = ((AudioData)rightList.get(r)).getSong();
            }else {
                leftString = (String) leftList.get(l);
                rightString = (String) rightList.get(r);
            }
            if(leftString.compareTo(rightString)<0){
                tempList.set(t,leftList.get(l));
                l++;
            }else {
                tempList.set(t,rightList.get(r));
                r++;
            }
            t++;
        }

        if(l<=mid)
            for (; l<=mid; l++) {
                tempList.set(t,leftList.get(l));
                t++;
            }
        else if(r<=end)
            for (; r<=end; r++) {
                tempList.set(t,rightList.get(r));
                t++;
            }

        return tempList;
    }

}
