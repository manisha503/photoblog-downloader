photoblog-downloader
====================

PREP:
- Need to set TUMBLR_API_KEY, Secret, username and password in PhotoblogDownloader.java
- Create list of classes by running find . -print | grep '\.java' > classes in the top level
  directory. Open the file and remove the leading './' from all the lines

MACOS:
BUILD (make sure list of classes is up to date):

javac -classpath /Users/manisha/Projects/photoblog-downloader/libs/commons-codec-1.6.jar:/Users/manisha/Projects/photoblog-downloader/libs/commons-logging-1.1.1.jar:/Users/manisha/Projects/photoblog-downloader/libs/httpclient-4.0.1.jar:/Users/manisha/Projects/photoblog-downloader/libs/httpcore-4.0.1.jar:/Users/manisha/Projects/photoblog-downloader/libs/httpmime-4.0.1.jar:/Users/manisha/Projects/photoblog-downloader/src -d bin -verbose -sourcepath /Users/manisha/Projects/photoblog-downloader/src @classes

RUN: 
/usr/bin/java -classpath /Users/manisha/Projects/photoblog-downloader/bin:/Users/manisha/Projects/photoblog-downloader/libs/commons-codec-1.6.jar:/Users/manisha/Projects/photoblog-downloader/libs/commons-logging-1.1.1.jar:/Users/manisha/Projects/photoblog-downloader/libs/httpclient-4.0.1.jar:/Users/manisha/Projects/photoblog-downloader/libs/httpcore-4.0.1.jar:/Users/manisha/Projects/photoblog-downloader/libs/httpmime-4.0.1.jar com.photoblogdownloader.app.PhotoBlogDownloader /Users/manisha/Projects/downloaderData/ /Users/manisha/Projects/downloaderData/postIds.txt

LINUX:
BUILD (make sure list of classes is up to date):
javac -classpath /home/app/photoblog-downloader/libs/commons-codec-1.6.jar:/home/app/photoblog-downloader/src -d bin -verbose -sourcepath /home/app/photoblog-downloader/src @classes

RUN:
/usr/bin/java -classpath /home/app/photoblog-downloader/bin:/home/app/photoblog-downloader/libs/commons-codec-1.6.jar com.photoblogdownloader.app.PhotoBlogDownloader
