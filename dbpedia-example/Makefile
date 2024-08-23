# Makefile for the DbPediaSparqlExample project

# Variables
MVN = mvn
JAVAC = javac
JAR = jar
MAIN_CLASS = DbPediaSparqlExample
SRC_DIR = src/main/java
BIN_DIR = target/classes
POM_FILE = pom.xml
JAR_FILE = target/dbpedia-example.jar

# Default target
all: compile jar

# Compile the Java source files
compile:
	@echo "Compiling Java source files..."
	$(MVN) compile

# Package the compiled classes into a JAR file
jar: compile
	@echo "Packaging JAR file..."
	$(MVN) package

# Run the main class
run: compile
	@echo "Running the main class..."
	$(MVN) exec:java -Dexec.mainClass="dev.langchain4j.DbPediaSparqlExample"

# Clean up the project
clean:
	@echo "Cleaning up..."
	$(MVN) clean

# Help target to describe each target
help:
	@echo "Available targets:"
	@echo "  all     - Compile the source files and package into a JAR"
	@echo "  compile - Compile the Java source files"
	@echo "  jar     - Package the compiled classes into a JAR file"
	@echo "  run     - Run the main class"
	@echo "  clean   - Clean up the project"
	@echo "  help    - Show this help message"

.PHONY: all compile jar run clean help
