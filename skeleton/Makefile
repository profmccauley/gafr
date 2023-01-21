SOURCES := $(shell find src -name "*.java")

all: gafr
	@`which time` -f"Finished in %E" make everything

everything: Game.jar.js

Game.jar.js: Game.jar
	cheerpjfy Game.jar --deps=gafr/GaFr.jar

Game.jar: $(SOURCES)
	@scons

gafr/README.md:
	@echo "Press enter to download GaFr, or Ctrl-C to abort."
	@read IGNORE
	git clone https://github.com/profmccauley/gafr

.PHONY: gafr
gafr: gafr/README.md
	@cd gafr && make && make fonts

.PHONY: clean
clean:
	rm -f Game Game.jar.js
	rm -rf classes
	scons -c
