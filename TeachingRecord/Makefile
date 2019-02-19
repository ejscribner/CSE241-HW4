JCC = javac

default: TeachingRecord.class
	jar cfmv TeachingRecord.jar ejs320/Manifest.txt TeachingRecord.class

jar: TeachingRecord.class
	cp TeachingRecord.class ejs320/TeachingRecord.class
	jar cfmv ejs320/TeachingRecord.jar ejs320/Manifest.txt ejs320/TeachingRecord.class

TeachingRecord.class: TeachingRecord.java
	$(JCC) TeachingRecord.java

#clean:
#	$(RM) *.class
