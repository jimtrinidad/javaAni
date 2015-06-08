/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaani;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author jimtrinidad
 */
public class MainViewController implements Initializable {
    
    @FXML
    private Parent root;
    
    @FXML
    ScrollPane contentContainer;
            
    @FXML
    private void handleButtonAction(ActionEvent event) {
        
    }
    
    @FXML
    private void handlePopularAction(ActionEvent event) {
        
        TilePane tile = new TilePane();
        tile.setPadding(new Insets(15, 15, 15, 15));
        tile.setHgap(10);
        tile.setVgap(10);
        
        List<Map<String, String>> anilist = Scraper.getPopular();
        
        for (Map<String, String> anime:anilist) {
            ImageView imageView;
            imageView = createImageView(anime.get("imageUrl"), anime.get("link"));
            tile.getChildren().addAll(imageView);
        }
        
        contentContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Horizontal
        contentContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertical scroll bar
        contentContainer.setFitToWidth(true);
        contentContainer.setContent(tile);
        
    }
    
    @FXML
    private void openAnime(String url, String imgUrl) {
        
        Map<String, String> anime = Scraper.animeData(url);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(JavaAni.class.getResource("animeView.fxml"));
        try {
            AnchorPane animeView = (AnchorPane) loader.load();
            
            Label animeTitle = (Label) animeView.lookup("#animeTitle");
            animeTitle.setText(anime.get("title"));
            
            Text animeDesc = (Text) animeView.lookup("#animeDesc");
            animeDesc.setText(anime.get("description"));
            
            Text animeMisc = (Text) animeView.lookup("#animeMisc");
            animeMisc.setText(anime.get("details").replace("<br> ",""));
            
            ImageView animeImage = (ImageView) animeView.lookup("#animeImage");
            Image image = new Image(imgUrl.replace("_thumb", "_image"), true);
            animeImage.setImage(image);
            animeImage.setCache(true);
            animeImage.setCacheHint(CacheHint.SPEED);
            
            Document doc    = Scraper.pagedoc;
            Elements episodesRows   = doc.select("div.episode_box");
            
            final ListView episodeList = new ListView();
            ObservableList<String> items = FXCollections.observableArrayList();
            final ObservableList<String> episodesLink = FXCollections.observableArrayList();
            
            int i = 0;
            for (Element row:episodesRows) {
                String episideNo    = row.select("div.list_header_epnumber").first().text();
                String episideTitle = row.select("div.list_header_epname").first().text();
                String episodeLink  = row.select("div.list_header_eptype a").first().attr("href");
                
                items.add(i, episideNo + "    " + episideTitle);
                episodesLink.add(i, episodeLink);
                
                i++;
            }
            
            episodeList.setItems(items);
            episodeList.setCursor(Cursor.HAND);
            episodeList.getStylesheets().add(getClass().getResource("/listview.css").toExternalForm());
            
            episodeList.setOnMouseClicked(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent click) {
                    if (click.getClickCount() == 2) {
                        
                        int index = episodeList.getSelectionModel().getSelectedIndex();
                        openEpisode(episodesLink.get(index));
                        
                    }
                }
            });
            
            VBox episodesCont = (VBox) animeView.lookup("#animeEpisodesCont");
            episodesCont.getChildren().add(episodeList);
            
            contentContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Horizontal
            contentContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertical scroll bar
            contentContainer.setFitToWidth(true);
            contentContainer.setContent(animeView);
            
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void openEpisode(String url) {
        
        List<Map<String, String>> mirrorList = Scraper.openEpisode(url);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(JavaAni.class.getResource("episodeView.fxml"));
        
        final AnchorPane episodeView;
        try {
            
            episodeView = (AnchorPane) loader.load();
            final ListView mirrorListView = new ListView();
            ObservableList<String> items = FXCollections.observableArrayList();
            final ObservableList<String> mirrorLinks = FXCollections.observableArrayList();
            
            int i = 0;
            for (Map<String, String> mirror:mirrorList) {
                String rnID     = mirror.get("rnID");
                String provider = mirror.get("provider");
                String type     = new String();
                String quality  = new String();
                String videoUrl = new String();
                switch (mirror.get("type")) {
                    case "subbed_trait" : type = "SUB"; break;
                    case "dubbed_trait" : type = "DUB"; break;
                }
                switch (mirror.get("quality")) {
                    case "hd_720p_trait" : quality = "HD720"; break;
                    case "sd_trait" : quality = "SD"; break;
                }
                switch (mirror.get("provider").toLowerCase()) {
                    case "mp4upload" : videoUrl = Scraper.getMp4UploadUrl(rnID, mirror.get("auth_key")); break;
                    case "arkvid" : videoUrl = Scraper.getArkvidUrl(rnID, mirror.get("auth_key")); break;
                }
                String itemlabel = type + " - " + provider + "\n" + quality;
                items.add(i, itemlabel);
                
                mirrorLinks.add(i, videoUrl);
            }

            mirrorListView.setItems(items);
            mirrorListView.setCursor(Cursor.HAND);
            mirrorListView.getStylesheets().add(getClass().getResource("/listview.css").toExternalForm());

            mirrorListView.setOnMouseClicked(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent click) {
                    if (click.getClickCount() == 2) {

                        int index = mirrorListView.getSelectionModel().getSelectedIndex();
                        System.out.println(mirrorLinks.get(index));
                        openVideo(mirrorLinks.get(index), episodeView);
                        
                    }
                }
            });

            VBox mirrorsCont = (VBox) episodeView.lookup("#mirrorContainer");
            mirrorsCont.getChildren().add(mirrorListView);

            contentContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Horizontal
            contentContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Vertical scroll bar
            contentContainer.setFitToWidth(true);
            contentContainer.setContent(episodeView);
            
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }    
        
    }
    
    private void openVideo(String videoUrl, AnchorPane episodeView) {
        
        Media media = new Media(videoUrl);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
        MediaControl mediaControl = new MediaControl(mediaPlayer);
        
        VBox videoContainer = (VBox) episodeView.lookup("#videoContainer");
        videoContainer.getChildren().clear();
        videoContainer.getChildren().add(mediaControl);
        
        videoContainer.setPadding(new Insets(5, 0, 5, 5));
        
    }
    
    private void openVideoWeb(String videoUrl, AnchorPane episodeView) {

        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load(videoUrl);
        
        VBox videoContainer = (VBox) episodeView.lookup("#videoContainer");
        videoContainer.getChildren().removeAll();
        videoContainer.getChildren().add(browser);
        
    }
    
    private void openVideoVLC(String videoUrl, AnchorPane episodeView) {
        
    }
    
    private ImageView createImageView(final String imageUrl, final String link) {

        Image image = new Image(imageUrl, true);
        ImageView imageView = null;
        imageView = new ImageView(image);
        imageView.setFitWidth(100);
        imageView.setFitHeight(150);
        imageView.setCache(true);
        imageView.setCacheHint(CacheHint.SPEED);
        imageView.setCursor(Cursor.HAND);
                                       
        imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {

                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){

                    if(mouseEvent.getClickCount() == 2){
                        
                        openAnime(link, imageUrl);

                    }
                }
            }
        });
        
        return imageView;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
