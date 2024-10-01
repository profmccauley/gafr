SOURCES := $(shell find src -name "*.java")
MYDIR := $(shell pwd)

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
debug: Game.jar.js gafr/debug/GaFr.jar.js debug/Game.jar.js

gafr/debug/GaFr.jar.js: gafr
	@cd gafr && make debug

gafr/README.md:
	@echo "Press enter to download GaFr, or Ctrl-C to abort."
	@read IGNORE
	git clone https://github.com/profmccauley/gafr

.PHONY: dist
dist: all
	rm -f gafr_game.zip
	rm -rf WWW
	mkdir -p WWW
	cp -a assets WWW
	cp index.html Game.jar Game.jar.js WWW
	mkdir -p WWW/gafr
	cp gafr/GaFr.jar gafr/GaFr.jar.js WWW/gafr
	mkdir -p WWW/gafr/fonts
	cd gafr/fonts ; find . -name "*.png" -or -name "*.json" | cpio -pdm "$(MYDIR)/WWW/gafr/fonts"
	cp -a gafr/deps WWW/gafr
	cp gafr/devtools/run_game.py WWW
	cp gafr/deps/README.txt WWW
	cp gafr/deps/favicon.ico WWW
	[ -f favicon.ico ] && cp favicon.ico WWW || true
	zip -q -9 -r gafr_game.zip WWW

.PHONY: gafr
gafr: gafr/README.md
	@cd gafr && make && make fonts

.PHONY: clean
clean:
	rm -f Game.jar Game.jar.js gafr_game.zip
	rm -rf classes debug WWW
	scons -c
