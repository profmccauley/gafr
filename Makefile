SOURCES := $(shell find GaFr -name "*.java")

all: GaFr.jar.js

PSF_FONTS = $(shell find fonts -name "*.psf" -or -name "*.psfu")
FFONT_FONTS = $(addsuffix .ffont.png,$(basename $(PSF_FONTS)))

fonts/spleen/AUTHORS:
	@mkdir -p fonts/spleen
	@curl -L https://github.com/fcambus/spleen/releases/download/1.9.1/spleen-1.9.1.tar.gz | tar zx --strip-components 1 -C fonts/spleen

%.ffont.png: %.psf
	python3 tools/psf_to_ffont.py $*.psf

%.ffont.png: %.psfu
	python3 tools/psf_to_ffont.py $*.psfu

.PHONY: fonts
fonts: fonts/spleen/AUTHORS
	@make fonts2

.PHONY: fonts2
fonts2: $(FFONT_FONTS)


dyn4j:
	git clone https://github.com/dyn4j/dyn4j
	rm dyn4j/src/main/java/module-info.java


GaFr.jar: $(SOURCES) native/GaFr/GFN_native.js
	@scons -Q

native/GaFr/GFN_native.js: native_stubs/GaFr/GFN_native.js
	@bash devtools/check_stubs.sh
	@touch native/GaFr/GFN_native.js

GaFrNat.jar: GaFr/GFN.java
	@scons
	@cp GaFr.jar GaFrNat.jar

GaFr.jar.js: GaFr.jar native/GaFr/GFN_native.js native_stubs/GaFr/GFN_native.js
	@cheerpjfy --natives=native GaFr.jar

native_stubs/GaFr/GFN_native.js: GaFrNat.jar
	@mkdir -p native_stubs
	@cheerpjfy --stub-natives=native_stubs GaFrNat.jar

doxygen-awesome-css:
	git clone https://github.com/jothepro/doxygen-awesome-css
	git -C doxygen-awesome-css checkout a13955ef815dc31c52d0c6a32c52a850f0f7c6f6

numberizer/target/numberizer-1.0-SNAPSHOT-shaded.jar:
	@mvn -f numberizer install

debug/GaFr/%.java: GaFr/%.java
	devtools/numberizer.sh --root=GaFr --out=debug/GaFr --prefix=GaFr. --skip=GFST.java --skip=GFU.java $(patsubst GaFr/%,%,$(SOURCES))

debug/GaFr.jar: $(addprefix debug/,$(SOURCES)) native/GaFr/GFN_native.js
	@scons -f $(shell pwd)/SConstruct -C debug

debug/GaFr.jar.js: debug/GaFr.jar native/GaFr/GFN_native.js native_stubs/GaFr/GFN_native.js
	@cheerpjfy --natives=native debug/GaFr.jar

.PHONY: debug
debug: $(addprefix debug/,$(SOURCES)) debug/GaFr.jar.js

.PHONY: doc
doc: doxygen-awesome-css
	@doxygen
	@bash devtools/fix_doc_treeview_width.sh


.PHONY: clean
clean:
	@rm -f GaFrNat.jar GaFrNat.jar.js GaFr.jar GaFr.jar.js
	@rm -rf classes native_stubs numberizer/target numberizer/*-pom.xml debug
	@scons -c

.PHONY: cleanall
cleanall: clean
	@rm -rf doxygen-awesome-css doc fonts/spleen dyn4j
