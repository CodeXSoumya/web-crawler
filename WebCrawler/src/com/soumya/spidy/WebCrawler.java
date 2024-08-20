package com.soumya.spidy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebCrawler {

    private static final Logger logger = Logger.getLogger(WebCrawler.class.getName());
    private static final String URL_PATTERN = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)";
    private static final int MAX_SITES_TO_CRAWL = 1000;
    private static final int REQUEST_DELAY_MS = 1000;

    private final Queue<String> urlsToCrawl = new LinkedList<>();
    private final Set<String> crawledUrls = new HashSet<>();

    public void crawl(String startUrl) {
        urlsToCrawl.add(startUrl);

        while (!urlsToCrawl.isEmpty()) {
            String currentUrl = urlsToCrawl.poll();
            logger.info("Crawling site: " + currentUrl);

            if (crawledUrls.size() >= MAX_SITES_TO_CRAWL) {
                logger.info("Reached maximum crawl limit. Exiting.");
                return;
            }

            try (BufferedReader reader = openUrlStream(currentUrl)) {
                if (reader == null) continue;
                StringBuilder pageContent = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    pageContent.append(line);
                }

                extractAndQueueUrls(pageContent.toString(), currentUrl);

                Thread.sleep(REQUEST_DELAY_MS);

            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error reading from URL: " + currentUrl, e);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Thread was interrupted during delay", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private BufferedReader openUrlStream(String urlStr) {
        while (true) {
            try {
                URL url = new URL(urlStr);
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                connection.setRequestProperty("Referer", urlStr);
                connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                connection.setRequestProperty("Accept", "text/html");
                connection.setRequestProperty("Connection", "keep-alive");

                if (connection instanceof HttpURLConnection) {
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    httpConnection.setInstanceFollowRedirects(true);

                    int responseCode = httpConnection.getResponseCode();
                    logger.info("Response Code for URL " + urlStr + ": " + responseCode);

                    if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                        String newUrl = httpConnection.getHeaderField("Location");
                        logger.info("Redirecting to: " + newUrl);
                        urlStr = newUrl;
                        continue; // Retry with the new URL
                    } else if (responseCode != HttpURLConnection.HTTP_OK) {
                        logger.warning("Non-OK response for URL " + urlStr + ": " + responseCode);
                        return null;
                    }
                }

                return new BufferedReader(new InputStreamReader(connection.getInputStream()));

            } catch (MalformedURLException e) {
                logger.warning("Malformed URL detected: " + urlStr);
                return null;
            } catch (IOException e) {
                logger.warning("IO Exception for URL: " + urlStr + " - " + e.getMessage());
                if (!urlsToCrawl.isEmpty()) {
                    urlStr = urlsToCrawl.poll();
                } else {
                    return null;
                }
            }
        }
    }

    private void extractAndQueueUrls(String pageContent, String currentUrl) {
        Pattern pattern = Pattern.compile(URL_PATTERN);
        Matcher matcher = pattern.matcher(pageContent);

        int urlCount = 0; // To keep track of found URLs

        while (matcher.find()) {
            String foundUrl = matcher.group();

            // Convert relative URLs to absolute URLs
            if (!foundUrl.startsWith("http")) {
                foundUrl = resolveUrl(foundUrl, currentUrl);
            }

            if (crawledUrls.add(foundUrl)) {
                logger.info("Added for crawling: " + foundUrl);
                urlsToCrawl.add(foundUrl);
                urlCount++;
            }
        }

        logger.info("Total URLs found on page: " + urlCount);
    }

    // Helper method to resolve relative URLs
    private String resolveUrl(String relativeUrl, String baseUrl) {
        try {
            URL base = new URL(baseUrl);
            return new URL(base, relativeUrl).toString();
        } catch (MalformedURLException e) {
            logger.warning("Failed to resolve relative URL: " + relativeUrl);
            return relativeUrl; // Return as-is if resolution fails
        }
    }

    public void displayResults() {
        logger.info("--- Crawl Results ---");
        logger.info("Total sites crawled: " + crawledUrls.size());
        crawledUrls.forEach(logger::info);
    }

    public static void main(String[] args) {
        WebCrawler crawler = new WebCrawler();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Enter the URL to start crawling:");
        String startUrl = scanner.nextLine();
        
        try {
            crawler.crawl(startUrl);
            crawler.displayResults();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred during crawling", e);
        } finally {
            scanner.close();
        }
    }
}
