Installation requirements
=========================
Java 1.7 or greater


Installing PDS
==============



1) Install VLC Player 64-bit (near bottom of webpage!) if your JVM is 64-bit, otherwise use the standard 32-bit VLC Player. 
http://www.videolan.org/vlc/download-windows.html



2) Extract this archive to a desired folder (if you haven't already).



3) This archive includes two subfolders: "64-bit" and "32-bit".  Move "opencv_java249.dll" from the appropriate folder into the directory containing "PDS.jar".



4) Run command:
java -jar PDS.jar video_stream_path [VLC_path]

(VLC_path should only be necessary if VLC is not installed to its default path.  If you find VLC_path necessary, path folders must be seperated by "//" rather than "/", unlike the video_stream_path parameter.)