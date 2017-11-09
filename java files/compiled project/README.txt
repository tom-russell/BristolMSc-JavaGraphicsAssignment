Both the raw files and the compiled jar are included. Both normal compilation and the 
jar file use an external jar used for working with JSON objects, but this jar is not 
stored with these files. The jar can be downloaded from:
https://github.com/stleary/JSON-java

The program can be run directly from the LoLStatViewer.jar file, either by double
clicking or on the command line with (json-20160810.jar must be in the libs folder):
jar -jar LoLStatViewer.jar

The jar file was compiled using:
jar cvfm LoLStatViewer.jar manifest.txt *.class stylesheet.css

The program can be compiled with ( with json-20160810.jar in the same folder):
javac -cp .;json-20160810.jar *.java

The program can then be run run with:
java -cp .;json-20160810.jar -ea LoLStatViewer
