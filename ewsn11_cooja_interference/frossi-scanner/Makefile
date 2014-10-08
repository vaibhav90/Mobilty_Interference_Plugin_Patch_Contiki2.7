all: frossi.ihex Frossi.class ProcessServer.class help
DEFINES=PROJECT_CONF_H=\"project-conf.h\"

TARGET=sky

CONTIKI = ../../../../contiki/
include ../../../Makefile.projects

%.class: %.java
	javac $(basename $<).java

help:
	@echo ""
	@echo "Upload Frossi firmware to mote: > make upload"
	@echo "Start Frossi on mote: > make run"
	@echo "Start Frossi, connect to server: > make connect SERVER=[server] PORT=[port]"
	@echo "Start Process Server on mote: > make server PORT=[port]"

run: Frossi.class
	java Frossi make login

ifndef SERVER
connect: help
else
 ifndef PORT
connect: help
 else
connect: Frossi.class
	java Frossi -connect $(SERVER) $(PORT) 
 endif
endif

ifndef PORT
server: help
else
server: ProcessServer.class
	java ProcessServer $(PORT) make login 
endif

upload:
	make frossi.ihex
	make upload-ihex FILE=frossi.ihex
	