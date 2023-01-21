SOURCES := $(shell find src -name "*.java")

all:
	@`which time` -f"Finished in %E" make everything

everything: Game.jar.js

Game.jar.js: Game.jar
	cheerpjfy Game.jar --deps=gafr/GaFr.jar

Game.jar: $(SOURCES)
	scons

.PHONY: gafr
gafr:
	cd gafr && make

.PHONY: clean
clean:
	rm -f Game Game.jar.js
	rm -rf classes
	scons -c
