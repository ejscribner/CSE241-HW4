JCC = javac

default: TeachingRecord.java
	$(JCC) TeachingRecord.java

jar: TeachingRecord.class
	cp TeachingRecord.class ejs320/TeachingRecord.class
	jar cfmv ejs320/TeachingRecord.jar ejs320/Manifest.txt TeachingRecord.class #has to be in ejs320/ ?

TeachingRecord.class: TeachingRecord.java
	$(JCC) TeachingRecord.java

clean:
	rm *.class
	rm ejs320/TeachingRecord.class
	rm ejs320/TeachingRecord.jar
