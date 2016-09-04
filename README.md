# Crawler
This is a simple crawler in Java (single thread) to get the below items from user input url
  - The title of the product (e.g. Madison RX3400)
 - The price of the product
 - The brand
 - Additional information 
 - The short description
 - The category structure
 
The code is written in such as a way that it will find these items on other product pages within the same site. The code download the page for processing so that it caches a copy for subsequent runs.

The program will ask user to input the url to crawl and after it is done, user can input another URL to continue or enter N to terminate it.

### Folder structure
- output: contains output.txt to store items
- download: contains all downloaded html from the same site
- main: contains the main class to run the program

### Package & Run
- mvn package
- java -jar spiderCrawler-0.1-SNAPSHOT-jar-with-dependencies.jar
- First run will take longer than subsequent runs which are cached by downloaded html