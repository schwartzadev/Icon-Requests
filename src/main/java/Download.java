import java.util.List;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
/** @author Andrew Schwartz
 * @date December 2016
 * This class uses a csv file, test.csv that contains Google Play URLs. This then retrieves
 * the number of reviews for the given app and then if the app has at least *long max* reviews
 * it will download the icon for the app into the folder specified by imgDestination.* */

public class Download {
    private static String imgDestination = "img/";
    public static void main(String args[]) {
        print("running...");
        Document document;
        String[] url = {};
        List<String> newUrl = new ArrayList<String>();
        String line = "";
        // Make String array from CSV
        try (BufferedReader br = new BufferedReader(new FileReader("test.csv"))) {
            while ((line = br.readLine()) != null) {
                url = line.split(",");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (int i = 0; i < url.length; i++) { // for every given URL from the csv
            try {
                //Connect to given Play Store link
                document = Jsoup.connect(url[i]).get();

                String title = (document.title().substring(0, document.title().length() - 30));   // Get title of app, without the information @ Google Play
                print("[" + i + "/" + url.length + "] " + title); //Print title.

                Element ratings = document.select("div > div > div > div > div > div > div > div > div > div > div > div > span > span").first(); //Get # of reviews
                int ratingCount = 0;
                try {
                    ratingCount = Integer.parseInt(ratings.text().replace(",", "")); // # ratings to int
                } catch (NumberFormatException n) {
                    n.printStackTrace();
                }
                long max = 50000; // specified maximum amount of reviews to filer out apps with low reviews
                if (ratingCount > max) {
                    System.out.print("YES	");
                    newUrl.add(url[i]); // add url to newUrl string list
                    Element img = document.select("img").first(); // Get image URL
                    downloadImage(img.absUrl("src"), title); // save image
                }
                else { // if # of reviews is less than specified max, say no
                    System.out.println("NO");
                }
                print(""); // console spacing

            } catch (HttpStatusException h) { // catch 404 error, print it for the given URL
                print("[" + i + "/" + url.length + "] 404 ERROR");
                print("NO");
                print("");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        // Print to console and CSV
        try {
            PrintWriter writer = new PrintWriter("bigRequests.csv");
            for (String s : newUrl) {
                writer.println(s);
                print(s);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        print("\ndone");
    }


    public static void print(String string) {
        System.out.println(string);
    }

    private static void downloadImage(String strImageURL, String t){
        //get file name from image path
        String strImageName = t + ".png";
        try {
            //open the stream from URL
            URL urlImage = new URL(strImageURL);
            InputStream in = urlImage.openStream();

            byte[] buffer = new byte[4096];
            int n;

            OutputStream os = new FileOutputStream(imgDestination + strImageName);

            //write bytes to the output stream
            while ( (n = in.read(buffer)) != -1 ){
                os.write(buffer, 0, n);
            }
            os.close();
            print("Saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
