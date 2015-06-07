/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaani;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 *
 * @author jimtrinidad
 */
public class Scraper {
    
    public static Document pagedoc;
    
    public static void main(String[] args) {
        
        List<Map<String, String>> anilist = Scraper.getPopular();
        
        for (Map<String, String> anime:anilist) {
            System.out.println(anime.get("title") + " - " + anime.get("imageUrl") + " - " + anime.get("link"));
        }
        
    }
    
    public static Map animeData(String animeUrl) {
        
        Map<String, String> anime = new HashMap<String, String>();
        Document doc;
        
        try {
            doc     = Jsoup.connect(animeUrl).get();
            pagedoc = doc;
            
            doc.select("div.latest_subdub").remove();
            
            String title    = doc.select("div.anime_info_title").first().text();
            String desc     = doc.select("div.anime_info_synopsis").first().text();
            String misc     = doc.select("div.anime_info_misc").first().html();
            
            anime.put("title", title);
            anime.put("description", desc);
            anime.put("details", misc);
            
            
        } catch (IOException ex) {
            Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return anime;
        
    }
    
    public static Elements openEpisode(String episodeUrl) {
        
        Elements episodeElement = new Elements();
        
        return episodeElement;
        
    }
    
    public static List getPopular() {
        
        Document doc;
        Elements animeListElements = new Elements();
        //String[][] animeList = new String[animeListElements.size()][2];
        
        List<Map<String, String>> animeList = new ArrayList<Map<String, String>>();
        
        try {
            doc = Jsoup.connect("http://rawranime.tv/list/popularongoing/").get();
            animeListElements = doc.select("table.list_table tr");
        } catch (IOException ex) {
            Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for (Element row: animeListElements) {
            String title    = row.select("td.animetitle").first().text();
            String link     = row.select("td.animetitle a").first().attr("href");
            String imageUrl = row.select("td.image noscript img").first().absUrl("src");
            
            Map<String, String> anime = new HashMap<String, String>();
            anime.put("title", title);
            anime.put("link", link);
            anime.put("imageUrl", imageUrl);
            animeList.add(anime);

        }
        
        return animeList;
        
    }
    
}
