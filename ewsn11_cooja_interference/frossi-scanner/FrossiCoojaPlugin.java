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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jdom.Element;

import org.contikios.cooja.ClassDescription;
import org.contikios.cooja.Cooja;
import org.contikios.cooja.HasQuickHelp;
import org.contikios.cooja.COOJAProject;
import org.contikios.cooja.Mote;
import org.contikios.cooja.MotePlugin;
import org.contikios.cooja.PluginType;
import org.contikios.cooja.Simulation;
import org.contikios.cooja.VisPlugin;
import org.contikios.cooja.interfaces.SerialPort;

/**
 * COOJA plugin that starts Frossi, and connects it to a simulated mote.
 * The code is adopted from the Collect View plugin.
 * 
 * @see CollectView
 * @author Niclas Finne, Fredrik Osterlind
 */
@ClassDescription("Frossi")
@PluginType(PluginType.MOTE_PLUGIN)
public class FrossiCoojaPlugin extends VisPlugin implements MotePlugin, HasQuickHelp {
  private static final long serialVersionUID = 1L;

  private static Logger logger = Logger.getLogger(FrossiCoojaPlugin.class);

  private final Mote mote;
  private final SerialPort serialPort;
  private Observer serialDataObserver;

  private JLabel inLabel, outLabel;
  private int inBytes = 0, outBytes = 0;

  private Process commandProcess;
  private DataOutputStream out;
  private boolean isRunning;

  public FrossiCoojaPlugin(Mote mote, Simulation simulation, final Cooja gui) {
    super("Frossi (" + mote + ")", gui, false);
    this.mote = mote;

    /* Mote serial port */
    serialPort = (SerialPort) mote.getInterfaces().getLog();
    if (serialPort == null) {
      throw new RuntimeException("No mote serial port");
    }

    /* Cooja components */
    if (Cooja.isVisualized()) {
      inLabel =  new JLabel("      0 bytes", JLabel.RIGHT);
      outLabel = new JLabel("      0 bytes", JLabel.RIGHT);

      JPanel panel = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets.left = c.insets.right = c.insets.top = 6;
      c.gridy = 0;

      panel.add(new JLabel("Frossi -> Mote:"), c);
      panel.add(inLabel, c);

      c.insets.bottom = 6;
      c.gridy++;
      panel.add(new JLabel("Mote -> Frossi:"), c);
      panel.add(outLabel, c);

      getContentPane().add(BorderLayout.NORTH, panel);
      pack();
    }

    File frossiClassFile = null;
    File projectDir = null;
    for (COOJAProject project: gui.getProjects()) {
    	if (project == null) {
    		continue;
    	}
    	if (project.getConfigPlugins() == null) {
    		continue;
    	}
    	for (String plugin: project.getConfigPlugins()) {
      	if (plugin == null) {
      		continue;
      	}
    		if (plugin.equals(FrossiCoojaPlugin.class.getName())) {
    			projectDir = project.configFile.getParentFile();
    			break;
    		}
    	}
    }
    frossiClassFile = new File(projectDir, "Frossi.class");
    if (frossiClassFile == null || !frossiClassFile.canRead()) {
      logger.fatal("Could not find the Frossi application: " + frossiClassFile.getAbsolutePath());
      if (Cooja.isVisualized()) {
        JOptionPane.showMessageDialog(Cooja.getTopParentContainer(),
            "Could not find the Frossi application:\n" +
            frossiClassFile + "\n\nPlease try to recompile it!",
            "Frossi application not found", JOptionPane.ERROR_MESSAGE);
      }
      // Could not find the Frossi application
      cleanup();
      return;
    }

    try {
      String[] cmd = new String[] {
          "java", frossiClassFile.getName().replace(".class", ""), "-stdin"
      };

      isRunning = true;
      commandProcess = Runtime.getRuntime().exec(cmd, null, projectDir);
      final BufferedReader input = new BufferedReader(new InputStreamReader(commandProcess.getInputStream()));
      final BufferedReader err = new BufferedReader(new InputStreamReader(commandProcess.getErrorStream()));
      out = new DataOutputStream(commandProcess.getOutputStream());

      /* Start thread listening on standard out */
      Thread readInput = new Thread(new Runnable() {
        public void run() {
          String line;
          try {
            while ((line = input.readLine()) != null) {
              if (line.length() > 0) {
                System.err.println("Frossi Serial> " + line);
                for (int i = 0, n = line.length(); i < n; i++) {
                  serialPort.writeByte((byte) line.charAt(i));
                }
                serialPort.writeByte((byte) '\n');
                inBytes += line.length() + 1;
                if (Cooja.isVisualized()) {
                  inLabel.setText(inBytes + " bytes");
                }
              }
            }
            input.close();
          } catch (IOException e) {
            if (isRunning) {
              logger.error("The Frossi application died!", e);
            }
          } finally {
            cleanup();
          }
        }
      }, "read input stream thread");

      /* Start thread listening on standard err */
      Thread readError = new Thread(new Runnable() {
        public void run() {
          String line;
          try {
            while ((line = err.readLine()) != null) {
              System.err.println("Frossi> " + line);
            }
            err.close();
          } catch (IOException e) {
            if (isRunning) {
              logger.error("The Frossi application died!", e);
            }
          }
        }
      }, "read error stream thread");

      readInput.start();
      readError.start();
    } catch (Exception e) {
      throw (RuntimeException) new RuntimeException(
          "Frossi error: " + e.getMessage()).initCause(e);
    }

    /* Observe serial port for outgoing data */
    serialPort.addSerialDataObserver(serialDataObserver = new Observer() {
      public void update(Observable obs, Object obj) {
        DataOutputStream out = FrossiCoojaPlugin.this.out;
        if (out != null) {
          try {
            byte b = serialPort.getLastSerialData();
            out.write(b);
            outBytes++;
            out.flush();
            if (Cooja.isVisualized()) {
            	outLabel.setText(outBytes + " bytes");
            }
          } catch (IOException e) {
            if (isRunning) {
              logger.warn("Frossi output error", e);
            }
          }
        }
      }
    });
  }

  public boolean setConfigXML(Collection<Element> configXML, boolean visAvailable) {
    return true;
  }

  public Collection<Element> getConfigXML() {
    return null;
  }

  private void cleanup() {
    if (serialDataObserver != null) {
      serialPort.deleteSerialDataObserver(serialDataObserver);
      serialDataObserver = null;
    }

    if (isRunning) {
      logger.fatal("The Frossi application died!");
      if (Cooja.isVisualized()) {
        JOptionPane.showMessageDialog(this, "The Frossi application died!",
            "Frossi died!", JOptionPane.ERROR_MESSAGE);
      }
    }
    isRunning = false;
    if (commandProcess != null) {
      commandProcess.destroy();
      commandProcess = null;
    }
    if (out != null) {
      try {
        out.close();
        out = null;
      } catch (IOException e) {
      }
    }

    if (Cooja.isVisualized()) {
      EventQueue.invokeLater(new Runnable() {
        public void run() {
          setTitle(getTitle() + " *DISCONNECTED*");
          inLabel.setEnabled(false);
          outLabel.setEnabled(false);
        }
      });
    }
  }

  public void closePlugin() {
    isRunning = false;
    cleanup();
  }

  public Mote getMote() {
    return mote;
  }

  public String getQuickHelp() {
    return "<b>Frossi</b><p>" +
    "The Frossi plugin starts an instance of the Frossi application at Contiki projects.";
  }

}
