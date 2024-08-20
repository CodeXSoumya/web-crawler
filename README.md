### WebCrawler

## Overview
The WebCrawler is a simple Java-based web crawler designed to recursively visit web pages, extract URLs, and add them to a queue for further crawling. The crawler is built to handle a maximum number of sites, process HTTP redirects, resolve relative URLs, and ensure that the same site is not crawled multiple times.

## Features
* URL Extraction: Extracts URLs from the HTML content of web pages using regular expressions.
* Relative URL Resolution: Converts relative URLs to absolute URLs using the current page's URL.
* HTTP Redirect Handling: Automatically follows HTTP redirects to their new locations.
* User-Agent Spoofing: Sends a custom User-Agent header to avoid being blocked by some websites.
* Rate Limiting: Includes a delay between requests to avoid overwhelming the target server.
* Error Handling: Handles malformed URLs, IOExceptions, and other common issues during crawling.
* Logging: Provides detailed logging for each step of the crawling process, including URLs added for crawling, errors, and redirects.

## Prerequisites
Java: Ensure that Java is installed on your system. This project was built using Java 8+.

## How to Run
1. Clone the Repository:
git clone https://github.com/yourusername/webcrawler.git

2. Navigate to the Project Directory:
cd webcrawler

3. Compile the Code:
javac -d bin src/com/soumya/spidy/WebCrawler.java

4. Run the Crawler:
java -cp bin com.soumya.spidy.WebCrawler

5. Enter the Starting URL:
When prompted, enter the URL from which you want the crawler to start.
Example: https://www.wikipedia.org/

6. View the Results:
After the crawling process completes, the application will display the total number of sites crawled and list them in the console.

## Code Explanation
# Main Components
* WebCrawler Class: The primary class that handles the crawling process.
    1. urlsToCrawl: A queue that holds the URLs yet to be crawled.
    2. crawledUrls: A set that keeps track of all URLs that have already been crawled.

* Crawling Logic:
    1. The crawl(String startUrl) method starts with a single URL and processes each one in the queue.
    2. For each URL, the openUrlStream(String urlStr) method opens a connection and retrieves the HTML content.
    3. The extractAndQueueUrls(String pageContent, String currentUrl) method extracts all URLs from the page content, resolves relative URLs to absolute ones, and adds new URLs to the queue.
    4. The crawler continues until the queue is empty or the maximum number of sites (1000) is reached.

* Handling Relative URLs:
    The resolveUrl(String relativeUrl, String baseUrl) method resolves relative URLs found on the page against the base URL.

* Logging:
    The crawler uses Java's built-in Logger to log information about each step in the crawling process, including errors and HTTP response codes.

## Limitations
* JavaScript-Rendered Pages: This crawler does not execute JavaScript, so it won't extract URLs generated dynamically on client-side.
* SSL Issues: The crawler might face issues with websites that have invalid SSL certificates.
* Robots.txt: The crawler does not check for robots.txt compliance and may unintentionally crawl pages that should be avoided.

## Future Enhancements
* Robots.txt Compliance: Adding support for robots.txt parsing to respect site rules.
* Depth Control: Implementing depth limits to control how deep the crawler goes into a site.
* Multi-threading: Enhancing performance by processing multiple URLs concurrently.

## Conclusion
The WebCrawler is a simple yet powerful tool for exploring the structure of websites by crawling them page by page. It's an educational example that can be extended to include more advanced features as needed.