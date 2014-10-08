DEFINES=PROJECT_CONF_H=\"project-conf.h\"

# Define the target platform
ifndef TARGET
TARGET=sky
endif

# Give a name to your project 
CONTIKI_PROJECT = recording_interference

# Compile project typing "make"
all: $(CONTIKI_PROJECT)

# Upload project typing "make upload"
upload: $(CONTIKI_PROJECT).upload

# Define additional source files to be compiled (if any)
CONTIKI_TARGET_SOURCEFILES += interferer_settings.c

CONTIKI = ../../../../contiki-2.x/
include ../../../Makefile.projects

%.class: %.java
	javac saveRSSI.java
	javac $(basename $<).java

run: GUI.class
	java -classpath . GUI
