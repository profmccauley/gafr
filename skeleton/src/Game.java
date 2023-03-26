import GaFr.GFGame;
import GaFr.GFStamp;
import GaFr.GFFont;
import GaFr.Gfx;
import GaFr.GFU;
import GaFr.Easings;

public class Game extends GFGame
{
  {
    Gfx.clearColor(Gfx.Color.BLACK);
  }

  GFStamp logo = new GFStamp("gafr/deps/GaFrLogo.png").centerPin();
  GFFont font = new GFFont("gafr/fonts/spleen/spleen-32x64.ffont.json");

  double x = WIDTH/2;
  double y = HEIGHT/2;
  double vx = Math.random() * 10 - 5;
  double vy = Math.random() * 10 - 5;

  float hue = 0;

  protected double reflect (double v)
  {
    v = -v;
    if      (Math.random() < 0.1 && Math.abs(v) > 4) v -= Math.signum(v);
    else if (Math.random() < 0.1 && Math.abs(v) < 7) v += Math.signum(v);
    return v;
  }

  @Override
  public void onDraw (int frameCount)
  {
    hue += 0.001;
    font.color = java.awt.Color.HSBtoRGB(hue, 1, 1);

    float p = GFU.unscaleClampf(frameCount/60f, 0, 3); // p goes from 0 to 1 in three seconds
    p = Easings.easeOutBounce.easef(p);
    font.draw(128+16,GFU.scalef(p, HEIGHT, 20), "Welcome to GaFr!");

    logo.moveTo( (int)x, (int)y ).stamp();
    x += vx;
    y += vy;
    if (x < logo.width/2)              { x = logo.width/2;         vx = reflect(vx); }
    else if (x > WIDTH-logo.width/2)   { x = WIDTH-logo.width/2;   vx = reflect(vx); }
    if (y < logo.height/2)             { y = logo.height/2;        vy = reflect(vy); }
    else if (y > HEIGHT-logo.height/2) { y = HEIGHT-logo.height/2; vy = reflect(vy); }
  }
}
