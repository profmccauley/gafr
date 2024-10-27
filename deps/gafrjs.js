"use strict";

function getGL ()
{
  return document.querySelector("#canvas").getContext("webgl2");
}
var gl = getGL();
const glcanv = document.querySelector("#canvas");


let firstTimestamp, prevTimestamp;

function gafr_drawFunction ()
{
}

let gafr_running = true;


function gafr_redraw (timestamp)
{

  if (!gafr_running) return;
  window.requestAnimationFrame(gafr_redraw);
  if (firstTimestamp === undefined) firstTimestamp = timestamp;
  const elapsed = timestamp - firstTimestamp;

  if (elapsed < 1/60) return;

  //TODO: update_function()?

  if (prevTimestamp === timestamp) return;

  //webglUtils.resizeCanvasToDisplaySize(gl.glcanv);
  //gl.viewport(0, 0, gl.canv.width, gl.canv.height);
  gl.viewport(0, 0, gl.drawingBufferWidth, gl.drawingBufferHeight);
  //gl.clear(gl.COLOR_BUFFER_BIT);

  gafr_drawFunction();
}


function gafr_begin (o, w, h)
{
  console.log("gafr_begin()");
  gafr_jsInit(w, h).then(gafr_jsInitDone).catch(gafr_initFail);
}

function gafr_jsInitDone ()
{
  console.log("gafr_jsInitDone()");
  document.getElementById("spinner").style.visibility = "hidden";
}

async function gafr_initEarly ()
{
  console.log("gafr_initEarly()");
  document.getElementById("spinner").style.borderBottom = "16px solid #331188";
}

function gafr_initFail ()
{
  console.log("gafr_initFail()");
  document.getElementById("spinner").style.border = "16px solid #ff2020";
}

function gafr_null () { }

var gafrj_onKeyDown = gafr_null;
var gafrj_onKeyUp = gafr_null;
var gafrj_onMouseMove = gafr_null;
var gafrj_onMouseDown = gafr_null;
var gafrj_onMouseUp = gafr_null;
var gafrj_eventToGaFr = gafr_null;

function gafr_getModFlags (ev)
{
  let flags = 0;
  if (ev.shiftKey) flags |= 0b0001;
  if (ev.ctrlKey)  flags |= 0b0010;
  if (ev.altKey)   flags |= 0b0100;
  if (ev.metaKey)  flags |= 0b1000;
  return flags;
}


function gafr_onMouseMove (ev)
{
  var x = ev.offsetX, y = ev.offsetY;
  if (ev.target != glcanv) { x -= glcanv.offsetLeft; y -= glcanv.offsetTop; };
  gafrj_onMouseMove(x, y, ev.buttons, gafr_getModFlags(ev));
}

function gafr_onMouseUp (ev)
{
  gafrj_onMouseUp(ev.offsetX, ev.offsetY, ev.buttons, gafr_getModFlags(ev), ev.button);
}

function gafr_onMouseDown (ev)
{
  glcanv.setPointerCapture(ev.pointerId);
  gafrj_onMouseDown(ev.offsetX, ev.offsetY, ev.buttons, gafr_getModFlags(ev), ev.button);
}

function gafr_onFocusIn (ev)
{
  cjCall("GaFr.GFBoot", "onFocusChange", true);
}

function gafr_onFocusOut (ev)
{
  cjCall("GaFr.GFBoot", "onFocusChange", false);
}

function gafr_onKeyDown (ev)
{
  if (ev.isComposing || ev.keyCode === 229) return;
  if (ev.repeat) return;
  if (ev.key === "`" && ev.ctrlKey)
  {
    if (gafr_running)
    {
      console.info("** BREAK **");
    }
    else
    {
      console.info("** RESUME **");
      window.requestAnimationFrame(gafr_redraw);
    }
    gafr_running = !gafr_running;
    return;
  }

  gafrj_onKeyDown(ev.key, gafr_keyConvert(ev.code), gafr_getModFlags(ev));
}

function gafr_onKeyUp (ev)
{
  if (ev.isComposing || ev.keyCode === 229) return;
  if (ev.repeat) return; // Does this ever happen?

  gafrj_onKeyUp(ev.key, gafr_keyConvert(ev.code), gafr_getModFlags(ev));
}

function gafr_isDebugOn ()
{
 return window.location.hash.indexOf("debug") >= 0;
}

async function gafr_jsInit (width, height)
{
  console.log("gafr_jsInit() started");

  glcanv.width = width;
  glcanv.height = height;

  var cb = await cjResolveCall("GaFr.GFBoot", "onDraw", null);
  gafr_drawFunction = cb;

  cb = await cjResolveCall("GaFr.GFBoot", "onKeyDown", null);
  gafrj_onKeyDown = cb;
  cb = await cjResolveCall("GaFr.GFBoot", "onKeyUp", null);
  gafrj_onKeyUp = cb;
  gafrj_onMouseMove = await cjResolveCall("GaFr.GFBoot", "onMouseMove", null);
  gafrj_onMouseDown = await cjResolveCall("GaFr.GFBoot", "onMouseDown", null);
  gafrj_onMouseUp   = await cjResolveCall("GaFr.GFBoot", "onMouseUp",   null);
  gafrj_eventToGaFr = await cjResolveCall("GaFr.GFBoot", "onEventFromBrowser",null);

  const c = glcanv;//document.querySelector("#canvas");
  //c.addEventListener('mousemove', gafr_onMouseMove);
  //c.addEventListener('mousedown', gafr_onMouseDown);
  //c.addEventListener('mouseup',   gafr_onMouseUp);
//  c.addEventListener('pointermove', gafr_onMouseMove);
  c.addEventListener('pointerdown', gafr_onMouseDown);
  c.addEventListener('pointerup',   gafr_onMouseUp);

  window.addEventListener('pointermove', gafr_onMouseMove);
//  window.addEventListener('pointerdown', gafr_onMouseDown);
//  window.addEventListener('pointerup',   gafr_onMouseUp);

  window.addEventListener('keydown', gafr_onKeyDown);
  window.addEventListener('keyup', gafr_onKeyUp);
  window.addEventListener('focus', gafr_onFocusIn);
  window.addEventListener('blur', gafr_onFocusOut);

  //TODO: Don't actually start until all sounds are loaded
  window.requestAnimationFrame(gafr_redraw);

  console.log("gafr_jsInit() complete");
}

function gafr_glInit ()
{
  console.log("gafr_glInit()");
  gl.enable(gl.BLEND);
  gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
  //gl.clearColor(0, 0, 0, 0);
  gl.clearColor(0.25,0.25,0.25,1);

  gafr_stampInit();
}


window.gafr_stampInit = function ()
{
//TODO: It would probably be nice to move the whole stamp code to Java.
const vshader = `#version 300 es

// Per-vertex data
in vec2 a_coord;

// Per-quad tc group
in vec2 a_tc1;
in vec2 a_tc2;
//in vec2 a_tc3;
//in vec2 a_tc4;

// Per-quad data
in vec2 a_pin;
in vec2 a_size;
in vec2 a_pos;
in vec2 a_rotation;

// Per-quad data to pass to fragment shader
in uint a_tex;

uniform vec2 u_canvsize;

// Stuff to the fragment shader
out vec2 v_tc;
flat out uint a_tunit;
flat out uint a_frag_tint;
in uint a_tint;

void main ()
{
  vec2 p = a_coord * a_size;
  p = vec2( a_pin.x + (p.x-a_pin.x) * a_rotation.y + (p.y-a_pin.y) * a_rotation.x,
            a_pin.y + (p.y-a_pin.y) * a_rotation.y - (p.x-a_pin.x) * a_rotation.x );
  p += a_pos;
  p -= a_pin;
  p = (2.0 * (p / u_canvsize)) - 1.0;

  gl_Position = vec4(p * vec2(1, -1), 0, 1);

  //vec2 tcs[4] = vec2[](a_tc1,a_tc2,a_tc3,a_tc4); // Kinda gross?
  //v_tc = tcs[gl_VertexID%4];
  switch(gl_VertexID % 4)
  {
    case 0:
      v_tc = vec2(a_tc1.x,a_tc1.y); //0,0
      break;
    case 1:
      v_tc = vec2(a_tc2.x,a_tc1.y); //1,0
      break;
    case 2:
      v_tc = vec2(a_tc1.x,a_tc2.y); //0,1
      break;
    default:
      v_tc = vec2(a_tc2.x,a_tc2.y); //1,1
      break;
  }

  a_tunit = a_tex; // Pass forward
  a_frag_tint = a_tint;
}
`;

const fshader = `#version 300 es

precision highp float;

uniform sampler2D u_sampler0;
uniform sampler2D u_sampler1;
uniform sampler2D u_sampler2;
uniform sampler2D u_sampler3;
uniform sampler2D u_sampler4;
uniform sampler2D u_sampler5;
flat in uint a_tunit;

// Passed in from the vertex shader
in vec2 v_tc;

out vec4 fragColor;

flat in uint a_frag_tint;

void main ()
{
  vec4 a_frag_tint2 = vec4( float((a_frag_tint >> 16) & uint(0xff))/255.0,
                            float((a_frag_tint >>  8) & uint(0xff))/255.0,
                            float((a_frag_tint >>  0) & uint(0xff))/255.0,
                            float((a_frag_tint >> 24) & uint(0xff))/255.0 );
  /**/ if (a_tunit == 0u) fragColor = texture(u_sampler0, v_tc).bgra * a_frag_tint2;
  else if (a_tunit == 1u) fragColor = texture(u_sampler1, v_tc).bgra * a_frag_tint2;
  else if (a_tunit == 2u) fragColor = texture(u_sampler2, v_tc).bgra * a_frag_tint2;
  else if (a_tunit == 3u) fragColor = texture(u_sampler3, v_tc).bgra * a_frag_tint2;
  else if (a_tunit == 4u) fragColor = texture(u_sampler4, v_tc).bgra * a_frag_tint2;
  else if (a_tunit == 5u) fragColor = texture(u_sampler5, v_tc).bgra * a_frag_tint2;
}
`;

  const program = webglUtils.createProgramFromSources(gl, [vshader, fshader]);

  // Per-vertex
  const a_coord = gl.getAttribLocation(program, "a_coord");
  const a_tc1 = gl.getAttribLocation(program, "a_tc1");
  const a_tc2 = gl.getAttribLocation(program, "a_tc2");
//  const a_tc3 = gl.getAttribLocation(program, "a_tc3");
//  const a_tc4 = gl.getAttribLocation(program, "a_tc4");

  // Per-quad
  const a_pin = gl.getAttribLocation(program, "a_pin");
  const a_size = gl.getAttribLocation(program, "a_size");
  const a_pos = gl.getAttribLocation(program, "a_pos");
  const a_rotation = gl.getAttribLocation(program, "a_rotation");

  // Per-quad
  const a_tex = gl.getAttribLocation(program, "a_tex");
  const a_tint = gl.getAttribLocation(program, "a_tint");

  // Vertex shader uniforms
  const u_canvsize = gl.getUniformLocation(program, "u_canvsize");

  // Uniforms used by fragment shader
  const u_sampler0 = gl.getUniformLocation(program, "u_sampler0");
  const u_sampler1 = gl.getUniformLocation(program, "u_sampler1");
  const u_sampler2 = gl.getUniformLocation(program, "u_sampler2");
  const u_sampler3 = gl.getUniformLocation(program, "u_sampler3");
  const u_sampler4 = gl.getUniformLocation(program, "u_sampler4");
  const u_sampler5 = gl.getUniformLocation(program, "u_sampler5");

  // These hold the data used by the shaders.
  // They should be typed arrays coming from CheerpJ, so the first element
  // should be ignored (CheerpJ uses it for type identification).
  const ARRAY_OFF = 1;
  var vinfo = null; // quad info data
  var tcs = null; // texture coordinate data
  var tex = null; // which texture unit to use
  var stampTint = null;

  const vao = gl.createVertexArray();
  gl.bindVertexArray(vao);

  // Set up a VBO with the coordinates.
  // It's the same coordinates for every single one; we use the vertex
  // attributes to move it around.
  const coord_vbo = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, coord_vbo);
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([
      0.0,  0.0,
      1.0,  0.0,
      0.0,  1.0,
      1.0,  1.0,
  ]), gl.STATIC_DRAW);
  // Set up how the attributes are stored/fetched in/from the VBO
  // two components, which are floats, don't normalize, tightly packed, starting at beginning
  gl.enableVertexAttribArray(a_coord);
  gl.vertexAttribPointer(a_coord, 2, gl.FLOAT, false, 0, 0);

  const usage = gl.STATIC_DRAW;

  const quad_vbo = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, quad_vbo);
  // stamp info is 8 float32s:
  // pinx,piny
  // sizex,sizey
  // posx,posy
  // sin,cos
  gl.enableVertexAttribArray(a_pin);
  gl.enableVertexAttribArray(a_size);
  gl.enableVertexAttribArray(a_pos);
  gl.enableVertexAttribArray(a_rotation);
  gl.vertexAttribDivisor(a_pin, 1);
  gl.vertexAttribDivisor(a_size, 1);
  gl.vertexAttribDivisor(a_pos, 1);
  gl.vertexAttribDivisor(a_rotation, 1);
  gl.vertexAttribPointer(a_pin,      2, gl.FLOAT, false, 4*8, 4*(0));
  gl.vertexAttribPointer(a_size,     2, gl.FLOAT, false, 4*8, 4*(2));
  gl.vertexAttribPointer(a_pos,      2, gl.FLOAT, false, 4*8, 4*(2+2));
  gl.vertexAttribPointer(a_rotation, 2, gl.FLOAT, false, 4*8, 4*(2+2+2));

  // Set up a VBO for the TCs.
  // There's one set of TCs per quad; the way this works right now is
  // that we pass four attributes (one for each quad corner).  In the
  // shader, these get put into an array and we pick the right one
  // out using the vertex number.  It seems like there should be a
  // cleaner way (e.g., ideally, we'd pass them in as an array).
  // If we're going to keep doing them like this, we might as well
  // merge them into quad_vbo.
  // Each entry is 8 float32s.
  const tc_vbo = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, tc_vbo);
  gl.enableVertexAttribArray(a_tc1);
  gl.enableVertexAttribArray(a_tc2);
//  gl.enableVertexAttribArray(a_tc3);
//  gl.enableVertexAttribArray(a_tc4);
  gl.vertexAttribDivisor(a_tc1, 1);
  gl.vertexAttribDivisor(a_tc2, 1);
//  gl.vertexAttribDivisor(a_tc3, 1);
//  gl.vertexAttribDivisor(a_tc4, 1);
  //NOTE: the stride used to be 4*8, but I'm removing tc3 and tc4 for now...
  gl.vertexAttribPointer(a_tc1, 2, gl.FLOAT, false, 4*4/*note!*/, 4*(0));
  gl.vertexAttribPointer(a_tc2, 2, gl.FLOAT, false, 4*4/*note!*/, 4*(2));
//  gl.vertexAttribPointer(a_tc3, 2, gl.FLOAT, false, 4*8, 4*(2+2));
//  gl.vertexAttribPointer(a_tc4, 2, gl.FLOAT, false, 4*8, 4*(2+2+2));

  // A vertex buffer that specifies which texture to use for a stamp.
  // One byte per entry.
  const tu_vbo = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, tu_vbo);
  gl.enableVertexAttribArray(a_tex);
  gl.vertexAttribIPointer(a_tex, 1, gl.UNSIGNED_BYTE, false, 0, 0);
  gl.vertexAttribDivisor(a_tex, 1);

  // A vertex buffer that specifies the multiplicative color for a stamp.
  // One 32 bit int per entry.
  const tint_vbo = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, tint_vbo);
  gl.enableVertexAttribArray(a_tint);
  gl.vertexAttribIPointer(a_tint, 1, gl.UNSIGNED_INT, false, 0, 0);
  gl.vertexAttribDivisor(a_tint, 1);

  window.gafr_stampSetup = function (maxStamps, maxTextures, _vinfo, _tcs, _tex, _stampTint)
  {
    vinfo = _vinfo;
    tcs = _tcs;
    tex = _tex;


    stampTint = new Uint32Array(_stampTint.buffer);
  }

  window.gafr_stampDraw = function (numStamps, numTextures)
  {
    if (vinfo == null) return;
    gl.bindVertexArray(vao);

    gl.bindBuffer(gl.ARRAY_BUFFER, quad_vbo);
    gl.bufferData(gl.ARRAY_BUFFER, vinfo, usage, ARRAY_OFF, numStamps * 8);

    gl.bindBuffer(gl.ARRAY_BUFFER, tc_vbo);
    gl.bufferData(gl.ARRAY_BUFFER, tcs, usage, ARRAY_OFF, numStamps * 4/*8!*/);

    gl.bindBuffer(gl.ARRAY_BUFFER, tu_vbo);
    gl.bufferData(gl.ARRAY_BUFFER, tex, usage, ARRAY_OFF, numStamps);

    gl.bindBuffer(gl.ARRAY_BUFFER, tint_vbo);
    gl.bufferData(gl.ARRAY_BUFFER, stampTint, usage, ARRAY_OFF, numStamps*1);

    //console.log(tcs[1],tcs[2],tcs[3],tcs[4],tcs[5],tcs[6],tcs[7],tcs[8]);
    //console.log("stamps:",numStamps," textures:",numTextures, vinfo.length, tcs.length, tex.length);
    gl.useProgram(program);

    if (numTextures > 0) gl.uniform1i(u_sampler0, 0);
    if (numTextures > 1) gl.uniform1i(u_sampler1, 1);
    if (numTextures > 2) gl.uniform1i(u_sampler2, 2);
    if (numTextures > 3) gl.uniform1i(u_sampler3, 3);
    if (numTextures > 4) gl.uniform1i(u_sampler4, 4);
    if (numTextures > 5) gl.uniform1i(u_sampler5, 5);

    gl.uniform2f(u_canvsize, gl.canvas.width, gl.canvas.height);

    gl.drawArraysInstanced(gl.TRIANGLE_STRIP, 0/*offset*/, 4, numStamps);
  }
};


//var _sounds_pending = 0;
function gafr_loadSound (snd, data, format)
{
  format = cjStringJavaToJs(format); //TODO: move this to _native
  // slice(1) to get rid of type data from CheerpJ
  const blob = new Blob([data], {type:"application/octet-stream"}).slice(1);
  const url = URL.createObjectURL(blob);
  const opts = { preload: true,
                 src: url,
                 format: format,
               };
  //++ _sounds_pending;
  const howl = new Howl(opts);
  snd._gafrx_id = howl;
  /*
  howl.once('load', function()
  {
    -- _sounds_pending;
    console.log("Sound loaded.");
  });
  */
  URL.revokeObjectURL(url);
  //console.log("Sound loading.");
}



function gafr_getGamepadData (index, axes, buttons, counts)
{
  var gps = navigator.getGamepads();
  if (index >= gps.length) return false;
  if (gps[index] === null) return false;
  const gp = gps[index];
  counts[1] = gp.axes.length;
  counts[2] = gp.buttons.length;
  if (axes !== null)
  {
    for (var i = 0; i < axes.length-1; ++i)
    {
      if (i >= gp.axes.length)
        axes[i+1] = 0;
      else
        axes[i+1] = gp.axes[i];
    }
  }

  if (buttons !== null)
  {
    for (var i = 0; i < buttons.length-1; ++i)
    {
      if (i >= gp.buttons.length)
        buttons[i+1] = false;
      else
        buttons[i+1] = gp.buttons[i].pressed;
    }
  }
  return true;
}


function gafr_setCursorStyle (style)
{
  glcanv.style.cursor = cjStringJavaToJs(style);
}
function gafr_setTitle (title)
{
  document.title = cjStringJavaToJs(title);
}

/** Convert a JavaScript key code to a GaFr key code */
function gafr_keyConvert (code)
{
  switch (code)
  {
    case "ArrowUp":
      return 128;
    case "ArrowDown":
      return 129;
    case "ArrowLeft":
      return 130;
    case "ArrowRight":
      return 131;
    case "ShiftLeft":
      return 132;
    case "ShiftRight":
      return 133;
    case "ControlLeft":
      return 134;
    case "ControlRight":
      return 135;
    case "AltLeft":
      return 136;
    case "AltRight":
      return 137;
    case "OSLeft":
    case "MetaLeft":
      return 138;
    case "OSRight":
    case "MetaRight":
      return 139;
    case "Period":
      return 46;
    case "Comma":
      return 44;
    case "Quote":
      return 39;
    case "Semicolon":
      return 59;
    case "Slash":
      return 47;
    case "Home":
      return 140;
    case "End":
      return 141;
    case "PageUp":
      return 142;
    case "PageDown":
      return 143;
    case "Insert":
      return 144;
    case "Delete":
      return 127;
    case "Minus":
      return 45;
    case "Equal":
      return 61;
    case "Backquote":
      return 96;
    case "BracketLeft":
      return 91;
    case "BracketRight":
      return 93;
    case "Backslash":
      return 92;
    case "Tab":
      return 9;
    case "Space":
      return 32;
    case "Backspace":
      return 8;
    case "Enter":
      return 10;
    case "NumpadEnter":
      return 145;
    case "NumpadSubtract":
      return 146;
    case "NumpadAdd":
      return 147;
    case "NumpadMultiply":
      return 148;
    case "NumpadDivide":
      return 149;
    case "NumpadDecimal":
      return 150;
    case "KeyA":
      return 65;
    case "KeyB":
      return 66;
    case "KeyC":
      return 67;
    case "KeyD":
      return 68;
    case "KeyE":
      return 69;
    case "KeyF":
      return 70;
    case "KeyG":
      return 71;
    case "KeyH":
      return 72;
    case "KeyI":
      return 73;
    case "KeyJ":
      return 74;
    case "KeyK":
      return 75;
    case "KeyL":
      return 76;
    case "KeyM":
      return 77;
    case "KeyN":
      return 78;
    case "KeyO":
      return 79;
    case "KeyP":
      return 80;
    case "KeyQ":
      return 81;
    case "KeyR":
      return 82;
    case "KeyS":
      return 83;
    case "KeyT":
      return 84;
    case "KeyU":
      return 85;
    case "KeyV":
      return 86;
    case "KeyW":
      return 87;
    case "KeyX":
      return 88;
    case "KeyY":
      return 89;
    case "KeyZ":
      return 90;
    case "Digit0":
      return 48;
    case "Numpad0":
      return 151;
    case "Digit1":
      return 49;
    case "Numpad1":
      return 152;
    case "Digit2":
      return 50;
    case "Numpad2":
      return 153;
    case "Digit3":
      return 51;
    case "Numpad3":
      return 154;
    case "Digit4":
      return 52;
    case "Numpad4":
      return 155;
    case "Digit5":
      return 53;
    case "Numpad5":
      return 156;
    case "Digit6":
      return 54;
    case "Numpad6":
      return 157;
    case "Digit7":
      return 55;
    case "Numpad7":
      return 158;
    case "Digit8":
      return 56;
    case "Numpad8":
      return 159;
    case "Digit9":
      return 57;
    case "Numpad9":
      return 160;
    case "F1":
      return 161;
    case "F2":
      return 162;
    case "F3":
      return 163;
    case "F4":
      return 164;
    case "F5":
      return 165;
    case "F6":
      return 166;
    case "F7":
      return 167;
    case "F8":
      return 168;
    case "F9":
      return 169;
    case "F10":
      return 170;
    case "F11":
      return 171;
    case "F12":
      return 172;
  }
  return 0;
}
