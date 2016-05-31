package sample;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Aziza on 1/16/2016.
 */
public class AudioData {

    private final SimpleStringProperty artist;
    private final SimpleStringProperty song;

    public AudioData(){
        this.artist = new SimpleStringProperty("Unknown");
        this.song = new SimpleStringProperty("Unknown");
    }

    public AudioData(String artist, String song) {
        this.artist = new SimpleStringProperty(artist);
        this.song = new SimpleStringProperty(song);
    }

    public String getArtist() {
        return artist.get();
    }

    public SimpleStringProperty artistProperty() {
        return artist;
    }

    public void setArtist(String artist) {
        if(!artist.equals("null"))
            this.artist.set(artist);
    }

    public String getSong() {
        return song.get();
    }

    public SimpleStringProperty songProperty() {
        return song;
    }

    public void setSong(String song) {
        if(!song.equals("null"))
            this.song.set(song);
    }
}
