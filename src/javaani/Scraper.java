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
        
        Map<String, String> anime = new HashMap<>();
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
    
    public static List openEpisode(String episodeUrl) {
        
        Elements episodeElement;
        List<Map<String, String>> mirrors = new ArrayList<>();
        Document doc;
        
        try {
            
            doc     = Jsoup.connect(episodeUrl).get();
            episodeElement = doc.select("div#mirror_container div.mirror");
            for (Element elem:episodeElement) {
                
                Map<String, String> mirror = new HashMap<>();
                Element b = elem.select("b").first();
                
                mirror.put("rnID", elem.attr("rn"));
                mirror.put("thumb", elem.select("img").first().absUrl("src"));
                mirror.put("provider", b.nextSibling().toString().trim());
                mirror.put("type", elem.select("div.mirror_traits div").first().attr("class"));
                mirror.put("quality", elem.select("div.mirror_traits div").last().attr("class"));
                mirror.put("auth_key", doc.select("input[name=auth_key]").first().val());
                mirrors.add(mirror);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return mirrors;
        
    }
    
    public static List getPopular() {
        
        Document doc;
        Elements animeListElements = new Elements();
        //String[][] animeList = new String[animeListElements.size()][2];
        
        List<Map<String, String>> animeList = new ArrayList<>();
        
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
            
            Map<String, String> anime = new HashMap<>();
            anime.put("title", title);
            anime.put("link", link);
            anime.put("imageUrl", imageUrl);
            animeList.add(anime);

        }
        
        return animeList;
        
    }
    
    public static String getArkvidUrl(String rnID, String token) {
        
        String videoUrl = new String();
        String mirrorUrl = "http://rawranime.tv/index.php?s=a8dd596a0e7afa519556a77d0933a8b5&app=anime&module=ajax&section=anime_watch_handler&do=getvid";

        mirrorUrl = mirrorUrl.concat("&md5check=" + token);
        mirrorUrl = mirrorUrl.concat("&id=" + rnID);
        
        Document doc;
        try {
            doc = Jsoup.connect(mirrorUrl).get();
            videoUrl = doc.select("iframe").first().attr("src");
            doc = Jsoup.connect(videoUrl).get();
            videoUrl = doc.select("video source").first().absUrl("src");
        } catch (IOException ex) {
            Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return videoUrl;
        
    }
    
    public static String getMp4UploadUrl(String rnID, String  token) {
        
        String videoUrl = new String();
        String mirrorUrl = "http://rawranime.tv/index.php?s=a8dd596a0e7afa519556a77d0933a8b5&app=anime&module=ajax&section=anime_watch_handler&do=getvid";

        mirrorUrl = mirrorUrl.concat("&md5check=" + token);
        mirrorUrl = mirrorUrl.concat("&id=" + rnID);
        
        Document doc;
        try {
            doc = Jsoup.connect(mirrorUrl).get();
            videoUrl = doc.select("iframe").first().attr("src");
            doc = Jsoup.connect(videoUrl).get();
            videoUrl = doc.toString();
            int start   = videoUrl.indexOf("mp4upload.com:182");
            int end     = videoUrl.indexOf("video.mp4", start);
            videoUrl = videoUrl.substring((start - 12), (end + 9));
        } catch (IOException ex) {
            Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return videoUrl;
        
    }
    
}
