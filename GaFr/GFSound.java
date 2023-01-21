package GaFr;

/** A Sound.
  *
  * Currently quite bare-bones, this class is suitable for simple sound
  * effects.
  *
  * Which file formats work depends on the browser, unfortunately, but
  * basically everything should support standard .wav files, and .mp3s.
  * Support for .aac is also likely.
  *
  * You can convert many formats to a supported format with the sox
  * or ffmpeg tools:
  *
  *     ffmpeg -i input_file.wav output_file.aac
  *
  * ... or ...
  *
  *     sox input_file.wav output_file.mp3
  *
  */
public class GFSound
{
  public GFSound (String fileName)
  {
    String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
    assert ext.indexOf("/") == -1;
    GFN.loadSound(this, GFU.loadDataFile(fileName), ext);
  }

  /** Play the sound.
    */
  public void play ()
  {
    GFN.playSound(this);
  }
}
