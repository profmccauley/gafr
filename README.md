# The GaFr Game Framework

GameFrame AKA GaFr AKA "gaffer" is a minimal framework for writing 2D games in
Java that run in web browsers.  It is primarily aimed at users learning about
game development.

## What and Why is GaFr?
Imagine you want to write a game using Java and have other people play it.  How
would you do it?  Java is not the language of choice for web browsers today,
nor for mobile platforms, nor for game consoles.  Java is also not generally
available by default on desktop OSes today.  Getting Java to run in any of
these cases is *possible*, but it takes some work.

Once you figure out how to get Java to run where you want it, you need to pick
a graphics technology.  Java's Swing isn't really meant for games.  OpenGL is a
widespread graphics API; there are bindings for it for Java, e.g., JOGL.  Is
JOGL available on your target platform/OS?  Do you want to learn all the
complexities of OpenGL programming?  Just displaying an image on screen is not
trivial, much less resizing or rotating it.  For that matter, loading an image
from a file is not even part of OpenGL -- you'll need to find something else
for that.

The type of questions above get repeated over and over.  How does this specific
platform do keyboard and mouse input?  Can it do game controller input?  How
about sound?  The point is that before you can even start thinking about
*games*, you need to think a lot about the *platform* -- you need to figure out
the basic mechanics for just loading and displaying images, loading and playing
sounds, setting up user input, and so on.

Relieving you of that burden is what GaFr is about.  It provides a Java
framework designed specifically for writing 2D games, specifically for web
browsers.  It aims to take care of the basic utilities required for such games,
so that you can think about how to write games.

### Features
Below is a quick list of some of GaFr's main features.  However, as noted
elsewhere, GaFr is brand new, and this list is likely to expand over time.
Moreover, [as described below](#contributing-to-gafr), you can contribute new
features to GaFr!

- Runs in and makes use of features from modern web browsers (currently seems
  to work great with Chrome and Safari; Firefox is a bit touchy..)
- Draws images with the ability to rotate/resize/etc.
- Plays sounds
- Keyboard and mouse input
- Game controller input
- Can draw fixed-width image-based fonts
- Relatively quick compile-and-run times

### Comparison to libGDX
It might be illuminating to compare GaFr to libGDX, which is another Java-based
game development framework that can compile to the web:

- libGDX is eight years older (GaFr is brand new).
- libGDX's browser support is sort of a second-class feature, and uses GWT
  which has a number of quirks and (in my opinion) frustrating limitations.
- GaFr has a much less involved build process and therefore shorter build times.
- libGDX has support for 3D.  This is not a use case for GaFr.
- libGDX comes with a ton of features and a lot of code.  While this can be
  good, especially if you are writing a production-quality game, it can also
  be overwhelming.  It also means that you are likely to spend a lot of your
  time learning to *use* existing features instead of understanding *how* those
  features actually work (the latter being the more important use case for
  GaFr).
- Related to the point above, GaFr is just much more simple all around.  This
  stems from libGDX's support for a large number of features, support for 3D,
  multiple different deployment platforms (native desktop apps, mobile apps,
  the web, etc.), legacy, etc., etc.  GaFr is much more simple both due to
  specific decisions and due to the fact that it is new and has not
  accumulated as much extra stuff.
- GaFr is easy to get running, at least when compiling on Linux.  Maybe less so
  on other OSes, but you probably have easy access to Linux even if you're not
  primarily a Linux user.  The last time I messed with libGDX (in 2021),
  getting it to run was sort of a nightmare; lots of machines required slightly
  different configurations, and I had to trailblaze getting it to run on modern
  Macs with M1 processors.  I think that last issue may have been worked out by
  now, but the point remains: for its currently-intended scope, GaFr should
  "just work".

### Comparison to Game Engines like Unity
As with libGDX, one point about GaFr versus Unity is that GaFr is far more
lightweight.  This reflects the fact that it's a basic framework, providing
basic platform features (like displaying images), and not a game engine.  A
game engine has already made a lot of decisions about how the game should be
written (which data structures and so on).  GaFr, like libGDX, largely leaves
that to you.

### Technologies used by GaFr
It uses the following other technologies (which of course have their own
dependencies, but I do not list those exhaustively here):

- [Java 8](https://docs.oracle.com/javase/8/docs/api/).  Of the intended
  audience for GaFr, the two most well known languages are Java and Python.
  Python in web browsers is still pretty slow, so the decision was made to go
  with Java.  Java 8 is a bit old, but [by some measures](https://www.infoworld.com/article/3652408/java-8-still-dominates-but-java-17-wave-is-coming-survey.html)
  is still the most popular version.  Moreover, it's what's needed for the
  current version of CheerpJ (below).
- [CheerpJ](https://docs.leaningtech.com/cheerpj/Getting-Started.html).  This
  is a compiler that can translate Java bytecode (compiled .class files) to
  JavaScript.
- [WebGL 2](https://developer.mozilla.org/en-US/docs/Web/API/WebGL_API).  This
  is a browser-based API similar to OpenGL, which is a rasterization engine --
  meaning it draws stuff.
- [webgl-utils.js](https://webgl2fundamentals.org/docs/module-webgl-utils.html).
  This is a library that simplifies a few annoying things about WebGL.
- [howler.js](https://github.com/goldfire/howler.js#core).  This is a library
  that simplifies the handling of audio in browsers, for example the fact that
  there are lots of slight incompatibilities between them.

### Contributing to GaFr
GaFr is brand new, and might be considered to be under active development.
That means if you're using it now, you can have a big impact on it.  Some of
the ways you can do this are:

- Submitting bug reports
- Submitting feature requests
- Submitting documentation requests for things which are unclear
- Writing JavaDoc comments
- Writing documentation
- Figuring out how to set up GitHub Pages and GitHub Actions so that the
  documentation gets updated automatically
- Writing examples
- Fixing bugs
- Adding new features

In short: you can help to further the development of GaFr, up to and including
being a substantial contributor to the project, for whatever that's worth!
Here are a few ideas of new features that you could add:

- Fading of sounds
- Sound sprites
- Texture atlases, possibly using the format used by libGDX's texture packer
  utilities (runnable [from code/command-line](https://libgdx.com/wiki/tools/texture-packer),
  or as a [GUI](https://github.com/crashinvaders/gdx-texture-packer-gui)) and
  [Spine](http://en.esotericsoftware.com/spine-atlas-format)
- A 2D scene graph
- Fix psf\_to\_ffont so that it outs "squarer" textures instead of really
  wide ones (which are sometimes too wide to be loaded into a texture) by
  not putting all the characters on the same "line".
- Variable-width fonts, possibly using the format used by
  [libGDX's Hiero utility](https://libgdx.com/wiki/tools/hiero)
- Text measurement and centered text drawing
- Angled text drawing
- A general-purpose matrix transform stack
- Much more!

## Getting Started with GaFr
GaFr uses `make` and `scons` to build.  `make` runs the show, but `scons`
handles the Java compilation since it makes that easy.

To create a project with GaFr, you'll typically create a project directory
clone the GaFr repository into that project directory (or include it as
a git submodule), and then tell GaFr to initialize the project directory.
That will result in a simple skeleton project which can be built and run.
You can then rip everything out of it and replace it with your own stuff!

To do this, run something like the following commands:
```
mkdir MyProject
cd MyProject
git clone https://github.com/profmccauley/gafr
./gafr/tools/init_project.sh
```

That should run through some setup, possibly prompting you to build GaFr.
If the process is successful, it will tell you that you can build your
project with `make`.  So... run `make`!  If that succeeds, you should
now have a runnable project.  Place it somewhere accessible on the web,
navigate to the appropriate URL, and you should see it!

If you want to try your project locally without uploading it to a
dedicated web server, you can serve it using any of several small
web server programs.  Python comes with one.  From within your project
directory, try:
```
python3 -m http.server 9999
```

Now navigate to http://127.0.0.1:9999/ and you should be in business!

## Distribuing GaFr Projects
Using the `make dist` command builds a `WWW` directory that hopefully has
the bare essentials for your project (not the source code, for example).
The contents of this directory can be placed on a web server and with
any luck, your game will just run.  It also puts the same stuff in a
.zip file (`gafr_game.zip`) for easy distribution.

If you don't want to put your game up on a web server and just want to
download the files to your computer and open them... this doesn't quite
work.  GaFr really does currently require a web server.  *However*, the
zip file contains a Python script called `run_game.py`, which is a (very)
slightly specialized web server for running the game.  See the
`README.txt` file it includes for a bit more information on how to use
it to run your game on a machine with no normal web server involved.

## The Documentation
It's possible you're reading the documentation right now!  At least this
front page is also available on github, but the real version is built using
[Doxygen](https://doxygen.nl), which is similar to JavaDoc, but... nicer?
Eventually we should have the documentation published as GitHub Pages, but
we don't currently.  You can build it yourself, though.  From within the
GaFr directory, execute `make doc`.  Assuming you have Doxygen installed,
this should generate a `doc/html` directory.

If you're using a test webserver as described in the previous section, you
should be able to use that to read the documentation by just browsing to
[http://127.0.0.1:9999/gafr/doc/html](http://127.0.0.1:9999/gafr/doc/html).
