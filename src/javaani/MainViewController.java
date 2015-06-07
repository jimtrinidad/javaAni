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
import javafx.scene.text.Text;
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
            
            ListView episodeList = new ListView();
            ObservableList<String> items = FXCollections.observableArrayList();
            ObservableList<String> episodesLink = FXCollections.observableArrayList();
            
            int i = 0;
            for (Element row:episodesRows) {
                String episideNo    = row.select("div.list_header_epnumber").first().text();
                String episideTitle = row.select("div.list_header_epname").first().text();
                String episideLink  = row.select("div.list_header_eptype a").first().attr("href");
                
                items.add(i, episideNo + "    " + episideTitle);
                episodesLink.add(i, episideLink);
                
                i++;
            }
            
            episodeList.setItems(items);
            episodeList.setCursor(Cursor.HAND);
            episodeList.getStylesheets().add(getClass().getResource("/listview.css").toExternalForm());
            
            episodeList.setOnMouseClicked(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent click) {
                    if (click.getClickCount() == 2) {
                       //Use ListView's getSelected Item
                        int index = episodeList.getSelectionModel().getSelectedIndex();
                        System.out.println(index);
                        System.out.println(episodesLink.get(index));
                       //use this to do whatever you want to. Open Link etc.
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
        
    }
    
    private ImageView createImageView(String imageUrl, String link) {

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
