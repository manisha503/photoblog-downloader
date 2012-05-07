photoblog-downloader
====================

PREP:
- Need to set TUMBLR_API_KEY, Secret, username and password in PhotoblogDownloader.java

BUILD (make sure list of classes is up to date):
javac -classpath /home/app/photoblog-downloader/libs/commons-codec-1.6.jar:/home/app/photoblog-downloader/src -d bin -verbose -sourcepath /home/app/photoblog-downloader/src @classes

RUN:
/usr/bin/java -classpath /home/app/photoblog-downloader/bin:/home/app/photoblog-downloader/libs/commons-codec-1.6.jar com.photoblogdownloader.app.PhotoBlogDownloader
