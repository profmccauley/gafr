function _CHEERPJ_COMPRESS(ZN4GaFr3GFN5beginEN4java4lang6ObjectIIEV)(a0,a1,a2,p)
{
  return gafr_begin(a0, a1, a2);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN13consoleLogObjEN4java4lang6ObjectEV)(a0,p)
{
  console.log(a0);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN8debugSetEN4java4lang6ObjectEV)(a0,p)
{
  window.java_debug = a0;
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN13setCanvasSizeEIIEV)(a0,a1,p)
{
  glcanv.width = a0;
  glcanv.height = a1;
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN14getCanvasWidthEVEV)(p)
{
  return glcanv.width;
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN15getCanvasHeightEVEV)(p)
{
  return glcanv.height;
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN16gl_createTextureEN4GaFr9GFTextureEV)(a0,p)
{
  a0._gafrx_id = gl.createTexture();
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN16gl_texParameteriEIIIEV)(a0,a1,a2,p)
{
  gl.texParameteri(a0, a1, a2);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN16gl_activeTextureEIEV)(a0,p)
{
  gl.activeTexture(a0);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN14gl_bindTextureEIN4GaFr9GFTextureEV)(a0,a1,p)
{
  gl.bindTexture(a0, a1._gafrx_id);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN13gl_texImage2DEIIIIIAIIEV)(a0,a1,a2,a3,a4,a5,a6,p)
{
  gl.texImage2D(gl.TEXTURE_2D,
                a0,
                a1,
                a2, a3, 0,
                a4,
                gl.UNSIGNED_BYTE,
                new Uint8Array(a5.buffer, (a6+1)*4)); /* +1 to skip cheerpj type info */
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN17gl_generateMipmapEIEV)(a0,p)
{
  gl.generateMipmap(a0);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN21gl_getUniformLocationEN4GaFr10Gl$ProgramN4java4lang6StringEI)(a0,a1,p)
{
  return gl.getUniformLocation(a0._gafrx_id, a1);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN23gl_getAttributeLocationEN4GaFr10Gl$ProgramN4java4lang6StringEI)(a0,a1,p)
{
  return gl.getAttributeLocation(a0._gafrx_id, a1);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN12gl_uniform2fEIFFEV)(a0,a1,a2,p)
{
  return gl.uniform2f(a0, a1, a2);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN12gl_uniform1iEIIEV)(a0,a1,p)
{
  return gl.uniform1i(a0, a1);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN13gl_useProgramEN4GaFr10Gl$ProgramEV)(a0,p)
{
  return gl.useProgream(a0._gafrx_id);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN27gl_createProgramFromSourcesEN4GaFr10Gl$ProgramN4java4lang6StringN4java4lang6StringEV)(a0,a1,a2,p)
{
  a0._gafrx_id = webglUtils.createProgramFromSources(gl, [a2,a2]);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN13gl_clearColorEFFFFEV)(a0,a1,a2,a3,p)
{
  return gl.clearColor(a0,a1,a2,a3);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN8gl_clearEIEV)(a0,p)
{
  return gl.clear(a0);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN11gl_viewportEIIIIEV)(a0,a1,a2,a3,p)
{
  gl.viewport(a0, a1, a2, a3);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN18gl_viewportDefaultEVEV)(p)
{
  gl.viewport(0, 0, gl.drawingBufferWidth, gl.drawingBufferHeight);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN10stampSetupEIIAFAFABAIEV)(a0,a1,a2,a3,a4,a5,p)
{
  gafr_stampSetup(a0,a1,a2,a3,a4,a5);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN9stampDrawEIIEV)(a0,a1,p)
{
  gafr_stampDraw(a0,a1);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN9loadSoundEN4GaFr7GFSoundABN4java4lang6StringEV)(a0,a1,a2,p)
{
  gafr_loadSound(a0, a1, a2);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN9playSoundEN4GaFr7GFSoundEV)(a0,p)
{
  a0._gafrx_id.play();
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN9setVolumeEN4GaFr7GFSoundFEV)(a0,a1,p)
{
  a0._gafrx_id.volume(a1);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN10pauseSoundEN4GaFr7GFSoundEV)(a0,p)
{
  a0._gafrx_id.pause();
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN9stopSoundEN4GaFr7GFSoundEV)(a0,p)
{
  a0._gafrx_id.stop();
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN14getGamepadDataEIAFAZAIEZ)(a0,a1,a2,a3,p)
{
  return gafr_getGamepadData(a0, a1, a2, a3);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN14setCursorStyleEN4java4lang6StringEV)(a0,p)
{
  return gafr_setCursorStyle(a0);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN8setTitleEN4java4lang6StringEV)(a0,p)
{
  return gafr_setTitle(a0);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN9initEarlyEVEV)(p)
{
  return gafr_initEarly();
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN15gl_getParameterEIEI)(a0,p)
{
  return gl.getParameter(a0);
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN14eventToBrowserEIIN4java4lang6StringN4java4lang6StringEI)(a0,a1,a2,a3,p)
{
  if (!window.onEventFromGaFr) return 0;
  a2 = a2 ? cjStringJavaToJs(a2) : "";
  a3 = a3 ? cjStringJavaToJs(a3) : "";
  return window.onEventFromGaFr(a0,a1,a2,a3) || 0;
}
function _CHEERPJ_COMPRESS(ZN4GaFr3GFN17eventToBrowserStrEIIN4java4lang6StringN4java4lang6StringEN4java4lang6String)(a0,a1,a2,a3,p)
{
  if (!window.onEventFromGaFrStr) return null;
  a2 = a2 ? cjStringJavaToJs(a2) : "";
  a3 = a3 ? cjStringJavaToJs(a3) : "";
  return window.onEventFromGaFrStr(a0,a1,a2,a3) || null;
}
