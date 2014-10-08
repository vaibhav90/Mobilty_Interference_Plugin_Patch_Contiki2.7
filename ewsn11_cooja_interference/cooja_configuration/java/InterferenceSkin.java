/*
 * Copyright (c) 2011, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.net.URL;

import org.apache.log4j.Logger;

import org.contikios.cooja.ClassDescription;
import org.contikios.cooja.Mote;
import org.contikios.cooja.Simulation;
import org.contikios.cooja.interfaces.Position;
import org.contikios.cooja.plugins.Visualizer;
import org.contikios.cooja.plugins.Visualizer.MoteMenuAction;
import org.contikios.cooja.plugins.VisualizerSkin;

@ClassDescription("Interference skin")
public class InterferenceSkin implements VisualizerSkin {
  private static Logger logger = Logger.getLogger(InterferenceSkin.class);

  final static int ICON_WIDTH = 50;
  final static int ICON_HEIGHT = 50;

  private Simulation simulation = null;
  private Visualizer visualizer = null;

  private static Image imageBluetoothOff = null;
  private static Image imageBluetoothOn = null;
  private static Image imageWifiOff = null;
  private static Image imageWifiOn = null;
  private static Image imageMicrowaveOff = null;
  private static Image imageMicrowaveOn = null;
  private static Image imageFrossi = null;
  private static Image imageGenericOff = null;
  private static Image imageGenericOn = null;

  public void setActive(Simulation simulation, Visualizer vis) {
    this.simulation = simulation;
    this.visualizer = vis;

    /* Load pngs from JAR */
    if (imageBluetoothOff == null) {
      URL imageURL = this.getClass().getClassLoader().getResource("bluetooth_off.png");
      imageBluetoothOff = Toolkit.getDefaultToolkit().getImage(imageURL);
      imageBluetoothOff = makeColorTransparent(imageBluetoothOff, Color.WHITE);

      imageURL = this.getClass().getClassLoader().getResource("bluetooth_on.png");
      imageBluetoothOn = Toolkit.getDefaultToolkit().getImage(imageURL);
      imageBluetoothOn = makeColorTransparent(imageBluetoothOn, Color.WHITE);

      imageURL = this.getClass().getClassLoader().getResource("wifi_off.png");
      imageWifiOff = Toolkit.getDefaultToolkit().getImage(imageURL);
      imageWifiOff = makeColorTransparent(imageWifiOff, Color.WHITE);
      
      imageURL = this.getClass().getClassLoader().getResource("wifi_on.png");
      imageWifiOn = Toolkit.getDefaultToolkit().getImage(imageURL);
      imageWifiOn = makeColorTransparent(imageWifiOn, Color.WHITE);
      
      imageURL = this.getClass().getClassLoader().getResource("microwave_off.png");
      imageMicrowaveOff = Toolkit.getDefaultToolkit().getImage(imageURL);
      imageMicrowaveOff = makeColorTransparent(imageMicrowaveOff, Color.WHITE);
      
      imageURL = this.getClass().getClassLoader().getResource("microwave_on.png");
      imageMicrowaveOn = Toolkit.getDefaultToolkit().getImage(imageURL);
      imageMicrowaveOn = makeColorTransparent(imageMicrowaveOn, Color.WHITE);
      
      imageURL = this.getClass().getClassLoader().getResource("frossi.png");
      imageFrossi = Toolkit.getDefaultToolkit().getImage(imageURL);
      imageFrossi = makeColorTransparent(imageFrossi, Color.WHITE);

      imageURL = this.getClass().getClassLoader().getResource("generic_off.png");
      imageGenericOff = Toolkit.getDefaultToolkit().getImage(imageURL);
      imageGenericOff = makeColorTransparent(imageGenericOff, Color.WHITE);		  
	  
      imageURL = this.getClass().getClassLoader().getResource("generic_on.png");
      imageGenericOn = Toolkit.getDefaultToolkit().getImage(imageURL);
      imageGenericOn = makeColorTransparent(imageGenericOn, Color.WHITE);	  
    }
    
    visualizer.registerMoteMenuAction(ToggleActivateInterferer.class);
  }
  public void setInactive() {
    visualizer.unregisterMoteMenuAction(ToggleActivateInterferer.class);
  }


  public static class ToggleActivateInterferer implements MoteMenuAction {
    public boolean isEnabled(Visualizer visualizer, Mote mote) {
    	return mote instanceof AbstractInterferer;
    }
    public String getDescription(Visualizer visualizer, Mote mote) {
    	if (!(mote instanceof AbstractInterferer)) {
    		return "Activate interferer";
    	}
    	AbstractInterferer m = (AbstractInterferer) mote;
    	if (m.isActive()) {
    		return "Deactivate interferer";
    	}
  		return "Activate interferer";
    }
    public void doAction(Visualizer visualizer, Mote mote) {
    	AbstractInterferer m = (AbstractInterferer) mote;
    	m.setActive(!m.isActive());
    	visualizer.repaint();
    }
  };
  public static Image makeColorTransparent(Image im, final Color color) {
    ImageFilter filter = new RGBImageFilter() {
      public int markerRGB = color.getRGB() | 0xFF000000;
      public final int filterRGB(int x, int y, int rgb) {
        if ( ( rgb | 0xFF000000 ) == markerRGB ) {
          return 0x00FFFFFF & rgb;
        } else {
          return rgb;
        }
      }
    }; 
    ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
    return Toolkit.getDefaultToolkit().createImage(ip);
  }

  public Color[] getColorOf(Mote mote) {
    return null;
  }

  public void paintBeforeMotes(Graphics g) {
  }

  public void paintAfterMotes(Graphics g) {
    for (Mote m: simulation.getMotes()) {
      if (!(m instanceof AbstractInterferer)) {
      	/* Special case: frossi mote */
        Position pos = m.getInterfaces().getPosition();
        Point pixel = visualizer.transformPositionToPixel(pos);
      	if (m.getType().getContikiFirmwareFile().getName().contains("frossi.sky")) {
          g.drawImage(
          		imageFrossi,
          		pixel.x - ICON_WIDTH/2, pixel.y - ICON_HEIGHT/2,
          		ICON_WIDTH, ICON_HEIGHT, 
          		null);
      	}
      	continue;
      }

      AbstractInterferer mote = (AbstractInterferer) m;
      boolean active = mote.isActive();
      
      Position pos = mote.getInterfaces().getPosition();
      Point pixel = visualizer.transformPositionToPixel(pos);
      if (mote instanceof MicrowaveInterferer) {
        g.drawImage(
        		active?imageMicrowaveOn:imageMicrowaveOff,
        		pixel.x - ICON_WIDTH/2, pixel.y - ICON_HEIGHT/2,
        		ICON_WIDTH, ICON_HEIGHT, 
        		visualizer);
      } else if (mote.getClass().getName().endsWith("BluetoothInterferer")) {
        g.drawImage(
        		active?imageBluetoothOn:imageBluetoothOff,
        		pixel.x - ICON_WIDTH/2, pixel.y - ICON_HEIGHT/2,
        		ICON_WIDTH, ICON_HEIGHT, 
        		visualizer);
      } else if (mote.getClass().getName().endsWith("WifiInterferer")) {
        g.drawImage(
        		active?imageWifiOn:imageWifiOff,
        		pixel.x - ICON_WIDTH/2, pixel.y - ICON_HEIGHT/2,
        		ICON_WIDTH, ICON_HEIGHT, 
        		visualizer);
      } else {
        g.drawImage(
        		active?imageGenericOn:imageGenericOff,
        		pixel.x - ICON_WIDTH/2, pixel.y - ICON_HEIGHT/2,
        		ICON_WIDTH, ICON_HEIGHT, 
        		visualizer);
      }
    }
  }

  public Visualizer getVisualizer() {
    return visualizer;
  }
}
