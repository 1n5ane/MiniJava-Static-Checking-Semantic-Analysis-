all: compile

compile:
	java -jar ../jtb132di.jar -te minijava.jj
	java -jar ../javacc5.jar minijava-jtb.jj
	javac Main.java SymbolTable.java  ClassSymbolTable.java  Method.java semanticError.java Field.java

clean:
	rm -f *.class *~
