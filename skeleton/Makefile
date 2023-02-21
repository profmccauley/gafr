SOURCES := $(shell find src -name "*.java")

all: gafr
	@`which time` -f"Finished in %E" make everything

everything: Game.jar.js

Game.jar.js: Game.jar
	cheerpjfy Game.jar --deps=gafr/GaFr.jar

Game.jar: $(SOURCES)
	@scons


debug/src/%.java: src/%.java
	gafr/devtools/numberizer.sh --root=src --out=debug/src $(patsubst src/%,%,$(SOURCES))

debug/Game.jar: $(addprefix debug/,$(SOURCES))
	@scons -f $(shell pwd)/SConstruct -C debug gfcp=../gafr/debug/GaFr.jar

debug/Game.jar.js: debug/Game.jar
	@cheerpjfy debug/Game.jar --deps=gafr/debug/GaFr.jar

.PHONY: debug
debug: gafr/debug/GaFr.jar.js debug/Game.jar.js

gafr/debug/GaFr.jar.js: gafr
	@cd gafr && make debug

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
	rm -rf classes debug
	scons -c
