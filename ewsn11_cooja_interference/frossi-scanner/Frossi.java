/*
 * Copyright (c) 2010, Swedish Institute of Computer Science.
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
 *
 * This file is part of the Contiki operating system.
 *
 * $Id: hello-world.c,v 1.1 2006/10/02 21:46:46 adamdunkels Exp $
 */

/* javac Frossi.java */

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.NumberFormat;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

/**
 * RSSI scanner for Tmote Sky node.
 * 
 * @author Fredrik Osterlind, Joakim Eriksson, Adam Dunkels
 */
public class Frossi extends JPanel {
  private static final long serialVersionUID = -3622477940975403618L;

  public final static int RSSI_MAX_VALUE = 100;
  public final static int MARGIN_BOTTOM = 20;
  public final static int MARGIN_RIGHT = 75;
  public final static int MARGIN_TOP = 12;
  public final static int NR_SAMPLES = 16*8192;

  static int[] rssi = new int[NR_SAMPLES];
  static private int rssiPos = 0;
  static double currentZoom = 10;
  static Frossi rssiPanel;
  static JLabel sampleRateLabel;
  static int samplesPerSecond = 0;
  static long updateRateLabelTime = 0;
  static boolean buffered = false;
  
  /* Process/Network */
  static Process process = null;
  static Socket socket = null;
  static BufferedReader in;
  static BufferedWriter out;

  /* Reading from System.in, supress debug output */
  static boolean stdin = false;

  /* Mouse selection */
	static int mouseSelectionIndex = -1;
	static int mouseSelectionWidth = 0;
	static Color mouseSelectionColor = null;
	static String mouseSelectionString = null;
  static int lastSamplesPerSecond = 0;


	final float dash1[] = {10.0f};
  final BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);        
  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    int h = getHeight();
    int w = getWidth();
    if (w > getPreferredSize().width) {
      g.setColor(Color.gray);     
      g.fillRect(0, 0, w, h); 
      g.translate(w - getPreferredSize().width, 0);
      w = getPreferredSize().width;
    }
    double yFactor = (h - (MARGIN_BOTTOM*1.0)) / RSSI_MAX_VALUE;

    /* Background */
    g.setColor(Color.white);     
    g.fillRect(0, 0, w, h); 
    GradientPaint redtowhite = new GradientPaint(w-MARGIN_RIGHT,0,Color.WHITE,w, 0,Color.lightGray, false);
    g2.setPaint(redtowhite);
    g2.fillRect(w-MARGIN_RIGHT, 0, MARGIN_RIGHT, h);
    
    /* Y axis */
    int base_dBm = -100;
    int ytics = 10;
  	g.setColor(Color.red);
  	Stroke s = g2.getStroke();
  	g2.setStroke(dashed);
  	Rectangle c = g2.getClipBounds();
    for(int i=-ytics;i<=0;i++){    	    	
    	g.drawString(String.valueOf(((ytics+i)*(base_dBm/ytics))+"dBm"), (int) (w-MARGIN_RIGHT+20), (int) (h+i*(h/ytics))-MARGIN_BOTTOM);
    	g2.drawLine(c.x, (int) (h+i*(h/ytics))-MARGIN_BOTTOM, Math.min(c.x + c.width, (int) (w-MARGIN_RIGHT+7)), (int) (h+i*(h/ytics))-MARGIN_BOTTOM);
    }

    /* Draw last RSSI values */
    g.setColor(Color.DARK_GRAY);
  	g2.setStroke(s);
    double xpos = w - MARGIN_RIGHT;
    boolean highlighted = false;
    for (int i=NR_SAMPLES-1+rssiPos; i >= rssiPos && xpos >= c.x; i--) {
    	int i2 = i%NR_SAMPLES;
      int rssiVal = (int) (rssi[i2] * yFactor);
      if (mouseSelectionIndex >= 0) {
      	if (i >= mouseSelectionIndex && i < mouseSelectionIndex+mouseSelectionWidth ||
      			i2 >= mouseSelectionIndex && i2 < mouseSelectionIndex+mouseSelectionWidth) {
      		if (!highlighted) {
        		highlighted = true;
            g.setColor(mouseSelectionColor);
      		}
      	} else {
      		if (highlighted) {
        		highlighted = false;
            g.setColor(Color.DARK_GRAY);
      		}
      	}
      }
      g.drawLine((int) xpos, h - MARGIN_BOTTOM - rssiVal, (int) xpos, h - MARGIN_BOTTOM);
      xpos -= currentZoom;
    }
    
    /* Mouse selection */
    if (mouseSelectionIndex >= 0 && mouseSelectionString != null) {
      g.setColor(Color.BLACK);
      Rectangle clip = g.getClipBounds();
      g.drawString(mouseSelectionString, clip.x+22, 10);
    }

  }

  static void cleanup() {
    try {
      if (socket != null) {
        socket.close();
        socket = null;
      }
    } catch (IOException e) {
    }
    if (process != null) {
      process.destroy();
      try {
        process.waitFor();
      } catch (InterruptedException e) {
      }
      process = null;
    }
    try {
      if (in != null) {
        in.close();
        in = null;
      }
    } catch (IOException e) {
    }
    try {
      if (out != null) {
        out.close();
        out = null;
      }
    } catch (IOException e) {
    }
  }

  static void startReadInputThread(final BufferedReader in) {
    new Thread(new Runnable() {
      public void run() {
        try {
          int b;
          while ((b = in.read()) != -1) {
            /*System.out.println("READ>: " + b);*/
          	rssi[rssiPos] = b; /* b is RSSI+55 <==> dB+100 */
          	rssiPos = (rssiPos+1) % NR_SAMPLES;
            samplesPerSecond++;

            if (streaming && streamWriter != null) {
              String log = System.currentTimeMillis() + " " + b + "\n";
              streamWriter.write(log);
            }
            if (System.currentTimeMillis() > updateRateLabelTime) {
              final int last = b;
              updateRateLabelTime = System.currentTimeMillis() + 1000;
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
									if (buffered) {
                    final int NR_TICKS = 2215; /* WITH_FLUSH_RADIO 0 */
                    //final int NR_TICKS = 3169; /* WITH_FLUSH_RADIO 1 */
                    final int NR_SAMPLES = 8192;
                    final int TICKS_PER_SECOND = 16384;
                    NumberFormat form = NumberFormat.getNumberInstance();
                    String freq = form.format(0.001*NR_SAMPLES/
                        ((double)NR_TICKS/(double)TICKS_PER_SECOND));
                    sampleRateLabel.setText("Precalibrated sample rate: ca " + freq + " kHz");
                    lastSamplesPerSecond = 
                    	(int) (NR_SAMPLES/((double)NR_TICKS/(double)TICKS_PER_SECOND));
                  } else {
                    System.err.println("LAST>: " + last);
                    sampleRateLabel.setText("Sample rate: " + samplesPerSecond + " Hz");
                    lastSamplesPerSecond = samplesPerSecond;
                    samplesPerSecond = 0;
                  }
                }
              });
            }
              
            if (rssiPanel != null) rssiPanel.repaint();
          }
        } catch (IOException e) {
          System.err.println("Error: " + e.getMessage());
        }
        cleanup();
      }
    }).start();
  }
  
  public static void main(String[] args) throws IOException {
  	if (args.length == 1 && args[0].equals("-stdin")) {
  	} else if (args.length == 3 && args[0].equals("-connect")) {
  	} else if (args.length < 2) {
      System.err.println("Usage: java Frossi [command]+");
      System.err.println("Example: java Frossi make login");
      System.err.println("Example: java Frossi serialdump-windows -b115200 /dev/com5");
      System.err.println("Usage: java Frossi -connect [server] [port]");
      System.err.println("Example: java Frossi -connect localhost 60000");
      System.exit(1);
    }
    
    updateRateLabelTime = System.currentTimeMillis()+1000;

    if (args[0].equals("-stdin")) {
      System.err.println("Reading from stdin");
      socket = null;
      process = null;
      stdin = true;
      in = new BufferedReader(new InputStreamReader(System.in));
      out = new BufferedWriter(new OutputStreamWriter(System.out));
      out.flush();
    } else if (args[0].equals("-connect")) {
      /* Connect to process server */
      System.err.println("Connecting to: " + args[1] + ":" + args[2]);
      socket = new Socket(args[1], Integer.parseInt(args[2]));
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      out.flush();
    } else {
      /* Connect to mote via command */
      String fullCommand = "";
      for (String a: args) {
        fullCommand += a + " ";
      }
      System.err.println("Executing command: " + fullCommand);
      process = Runtime.getRuntime().exec(args);
      in =  new BufferedReader(new InputStreamReader(process.getInputStream()));
      out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
      out.flush();

      /* Listen to process error stream */
      new Thread(new Runnable() {
        public void run() {
          BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
          String line;
          try {
            while ((line = err.readLine()) != null) {
              System.err.println("process stderr> " + line);
            }
            err.close();
          } catch (IOException e) {
            System.err.println("error: " + e.getMessage());
          }

          System.err.println("Closing Frossi: input process exited");
          cleanup();
          System.exit(1);
        }
      }).start();
    }
    startReadInputThread(in);

    /* RSSI values */
    rssiPanel = new Frossi();
    
    /* Popup */
    final JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(new JMenuItem(saveLog));
    popupMenu.add(new JMenuItem(savePNG));
    popupMenu.add(streamToFile);
    rssiPanel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
      	if (!e.isPopupTrigger()) {
      		double ratio = 
      			(double) e.getX() / (rssiPanel.getPreferredSize().width-MARGIN_RIGHT);
      		int i = (int) (ratio*rssi.length + rssiPos)%rssi.length;
      		
    			mouseSelectionIndex = i;
    			mouseSelectionWidth = 0;
    			boolean high = rssi[i] >= 33;

    			if (high) {
      			mouseSelectionColor = Color.GREEN;
      			while (rssi[(i+mouseSelectionWidth)%rssi.length] >= 33) {
      				mouseSelectionWidth++;
      				if (mouseSelectionWidth > 16384) {
      					break;
      				}
      			}
    				mouseSelectionWidth--;
      			while (rssi[(mouseSelectionIndex+rssi.length)%rssi.length] >= 33) {
      				mouseSelectionIndex--;
      				mouseSelectionWidth++;
      				if (mouseSelectionWidth > 16384) {
      					break;
      				}
      			}
      			mouseSelectionIndex++;
      		} else {
      			mouseSelectionColor = Color.RED;
      			while (rssi[(i+mouseSelectionWidth)%rssi.length] < 33) {
      				mouseSelectionWidth++;
      				if (mouseSelectionWidth > 16384) {
      					break;
      				}
      			}
    				mouseSelectionWidth--;
      			while (rssi[(mouseSelectionIndex+rssi.length)%rssi.length] < 33) {
      				mouseSelectionIndex--;
      				mouseSelectionWidth++;
      				if (mouseSelectionWidth > 16384) {
      					break;
      				}
      			}
      			mouseSelectionIndex++;
    				mouseSelectionWidth--;
      		}

      		if (mouseSelectionWidth >= 16383) {
      			mouseSelectionIndex = -1;
      		}

      		if (mouseSelectionIndex >= 0) {
      			/* Update label */
      			double duration = (double)mouseSelectionWidth/lastSamplesPerSecond;
      			
      			String str = 
      				mouseSelectionWidth + " samples selected: " + 
      				String.format("%1.3f", 1000.0*duration) + " ms, " + 
      				"ca " + String.format("%1.3f", 250000/8.0 * duration) + " bytes @ 250kbit";
      			mouseSelectionString = str;
      			System.err.println(str);
      		}
      		rssiPanel.repaint();
      	}
        super.mouseClicked(e);
        popup(e);
      }
      public void mousePressed(MouseEvent e) {
        super.mouseClicked(e);
        popup(e);
      }
      public void mouseReleased(MouseEvent e) {
        super.mouseClicked(e);
        popup(e);
      }
      private void popup(MouseEvent e) {
        if (e.isPopupTrigger()) {
          popupMenu.show(rssiPanel, e.getX(), e.getY());
        }
      }
    });
    
    /* Configuration */
    Box configBoxWest = Box.createHorizontalBox();
    configBoxWest.add(new JLabel("Sample delay: "));
    JComboBox delay = new JComboBox(new Object[] {
        "BUFFERED", 0,1,2,3,4,5,6,7,8,9,10,12,14,16,18,20,50,100
    });
    delay.setSelectedItem(10);
    delay.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (out != null) {
          String cmd;
          if (((JComboBox)e.getSource()).getSelectedItem().equals("BUFFERED")) {
            cmd = "buffered";
            buffered = true;
          } else {
            Integer delay = (Integer) ((JComboBox)e.getSource()).getSelectedItem();
            buffered = false;
            cmd = "delay " + delay;
          }
          
          System.err.println("cmd> " + cmd);
          try {
            out.write(cmd + "\n");
            out.flush();
            rssi = new int[NR_SAMPLES];
            rssiPos = 0;
          } catch (IOException e1) {
            System.err.println("Error: " + e1.getMessage());
          }
        }
      }
    });
    configBoxWest.add(delay);
    configBoxWest.add(Box.createHorizontalStrut(5));
    configBoxWest.add(new JLabel("Channel: "));
    JComboBox channel = new JComboBox(new Integer[] {
        11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26
    });
    channel.setSelectedItem(26);
    channel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (out != null) {
          Integer channel = (Integer) ((JComboBox)e.getSource()).getSelectedItem();
          System.err.println("cmd> channel " + channel);
          try {
            out.write("channel " + channel + "\n");
            out.flush();
            rssi = new int[NR_SAMPLES];
            rssiPos = 0;
          } catch (IOException e1) {
            System.err.println("Error: " + e1.getMessage());
          }
        }
      }
    });
    configBoxWest.add(channel);
    configBoxWest.add(Box.createHorizontalStrut(5));
    JCheckBox autoStopCheckBox = new JCheckBox(new AbstractAction("Pause on CCA") {
      private static final long serialVersionUID = -4799016742557148499L;
      public void actionPerformed(ActionEvent e) {
        int astop = ((JCheckBox)e.getSource()).isSelected()?1:0;
        if (out != null) {
          System.err.println("cmd> astop " + astop);
          try {
            out.write("astop " + astop + "\n");
            out.flush();
            rssi = new int[NR_SAMPLES];
            rssiPos = 0;
          } catch (IOException e1) {
            System.err.println("Error: " + e1.getMessage());
          }
        }
      }
    });
    autoStopCheckBox.setBackground(Color.LIGHT_GRAY);
    configBoxWest.add(autoStopCheckBox);
    configBoxWest.add(Box.createHorizontalStrut(5));
    JButton stopButton = new JButton("Pause");
    stopButton.setEnabled(true);
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (out != null) {
          System.err.println("cmd> stop");
          try {
            out.write("stop\n");
            out.flush();
          } catch (IOException e1) {
            System.err.println("Error: " + e1.getMessage());
          }
        }
      }
    });
    configBoxWest.add(stopButton);
    JButton continueButton = new JButton("Start");
    continueButton.setEnabled(true);
    continueButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (out != null) {
          System.err.println("cmd> cont");
          try {
            out.write("cont\n");
            out.flush();
            rssi = new int[NR_SAMPLES];
            rssiPos = 0;
          } catch (IOException e1) {
            System.err.println("Error: " + e1.getMessage());
          }
        }
      }
    });
    configBoxWest.add(continueButton);

    Box configBoxEast = Box.createHorizontalBox();
    configBoxEast.add(new JLabel("Zoom: "));
    JComboBox zoom = new JComboBox(new Double[] {
        0.1, 0.2, 0.3, 0.4, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0
    });
    zoom.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        currentZoom = (Double) ((JComboBox)e.getSource()).getSelectedItem();
        if (rssiPanel != null) {
          final Rectangle r = rssiPanel.getVisibleRect();
          final double ratio;
          if (r.width == 0) {
            ratio = 1;
          } else {
            ratio = (double) (r.x+r.width)/(rssiPanel.getPreferredSize().width);
          }

          final Dimension newSize = new Dimension(
              (int) (currentZoom*NR_SAMPLES)+MARGIN_RIGHT, 
              0
          );
          rssiPanel.setPreferredSize(newSize);
          rssiPanel.revalidate();
          rssiPanel.repaint();

          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              int rightPixel = (int) (ratio*newSize.width);
              rssiPanel.scrollRectToVisible(new Rectangle(rightPixel-r.width, 0, r.width, r.height));
            };
          });
        }
      }
    });
    configBoxEast.add(zoom);
    configBoxEast.add(Box.createHorizontalStrut(5));
    configBoxEast.add(sampleRateLabel = new JLabel("Sample rate: ????? Hz"));
    
    JPanel configPanel = new JPanel(new BorderLayout());
    configPanel.setBackground(Color.LIGHT_GRAY);
    configPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    configPanel.add(BorderLayout.WEST, configBoxWest);
    configPanel.add(BorderLayout.EAST, configBoxEast);
    
    /* Setup */
    JFrame frame = new JFrame("Frossi RSSI scanner ");
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        new Timer(500, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            /* Watchdog */
            System.exit(1);
          }
        }).start();
        new Thread() {
          public void run() {
            cleanup();
            System.exit(0);
          }
        }.start();
      }
    });
    frame.setSize(900, 500);
    JScrollPane scroll = new JScrollPane(
        rssiPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
    );
    scroll.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (rssiPanel != null) rssiPanel.repaint();
			}
		});
    frame.getContentPane().add(BorderLayout.CENTER, scroll);
    frame.getContentPane().add(BorderLayout.SOUTH, configPanel);
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    zoom.setSelectedItem(1.0);

    /* Init commands */
    out.write("delay 10\n");
    out.write("channel 26\n");
    out.write("astop 0\n");
    out.write("cont\n");
    out.flush();

  }

  private static boolean streaming = false;
  private static PrintWriter streamWriter = null;
  private static JCheckBoxMenuItem streamToFile = new JCheckBoxMenuItem("Stream timestamp/RSSI to stream.log") {
    private static final long serialVersionUID = 3130662457576625168L;
    {
      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          streaming = streamToFile.isSelected();

          /* Close any open file writer */
          if (streamWriter != null) {
            try {
              streamWriter.close();
            } catch (Exception e2) { }
            streamWriter = null;
          }

          if (!streaming) {
            return;
          }
          
          /* Open file writer */
          try {
            streamWriter = new PrintWriter(new FileWriter("stream.log"));
          } catch (IOException e1) {
            System.err.println("Error when opening stream file: " + e1.getMessage());
            e1.printStackTrace();
            streamWriter = null;
          }
        }
      });
    }
  };

  private static Action saveLog = new AbstractAction("Save to log") {
    private static final long serialVersionUID = -4140706275748686944L;
    public void actionPerformed(ActionEvent e) {
      JFileChooser fc = new JFileChooser();
      fc.setCurrentDirectory(new java.io.File("."));
      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fc.addChoosableFileFilter(new FileFilter() {
        public boolean accept(File f) {
          if (f.isDirectory()) {
            return true;
          }
          String filename = f.getName();
          if (filename == null) {
            return false;
          }
          return true;
        }
        public String getDescription() {
          return "Text file";
        }
      });
      fc.setDialogTitle("Save RSSI values");
      int returnVal = fc.showSaveDialog(rssiPanel);
      if (returnVal != JFileChooser.APPROVE_OPTION) {
        return;
      }
      File saveFile = fc.getSelectedFile();
      if (saveFile.exists()) {
        String s1 = "Overwrite";
        String s2 = "Cancel";
        Object[] options = { s1, s2 };
        int n = JOptionPane.showOptionDialog(
            rssiPanel,
            "A file '" + saveFile.getName() + "' already exists.\nDo you want to remove it?",
            "Overwrite existing file?", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE, null, options, s1);
        if (n != JOptionPane.YES_OPTION) {
          return;
        }
      }
      if (saveFile.exists() && !saveFile.canWrite()) {
        System.err.println("No write access to file: " + saveFile);
        return;
      }
      try {
        PrintWriter os = new PrintWriter(new FileWriter(saveFile));
        for (int i=0; i < NR_SAMPLES; i++) {
          os.println(rssi[(i+rssiPos)%NR_SAMPLES]);
        }
        os.close();
      } catch (Exception ex) {
        System.err.println("Could not write to file: " + saveFile);
        return;
      }
    }
  };
  private static Action savePNG = new AbstractAction("Save screenshot") {
    private static final long serialVersionUID = -4140706275748686944L;
    public void actionPerformed(ActionEvent e) {
      JFileChooser fc = new JFileChooser();
      fc.setCurrentDirectory(new java.io.File("."));
      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fc.addChoosableFileFilter(new FileFilter() {
        public boolean accept(File f) {
          if (f.isDirectory()) {
            return true;
          }
          String filename = f.getName();
          if (filename == null) {
            return false;
          }
          if (filename.endsWith(".png")) {
            return true;
          }
          return false;
        }
        public String getDescription() {
          return ".png";
        }
      });
      fc.setDialogTitle("Save screenshot");
      int returnVal = fc.showSaveDialog(rssiPanel);
      if (returnVal != JFileChooser.APPROVE_OPTION) {
        return;
      }
      File tmpFile = fc.getSelectedFile();
      if (!tmpFile.getName().endsWith(".png")) {
        tmpFile = new File(tmpFile.getParentFile(), tmpFile.getName() + ".png");
      }
      final File saveFile = tmpFile;

      if (saveFile.exists()) {
        String s1 = "Overwrite";
        String s2 = "Cancel";
        Object[] options = { s1, s2 };
        int n = JOptionPane.showOptionDialog(
            rssiPanel,
            "A file '" + saveFile.getName() + "' already exists.\nDo you want to remove it?",
            "Overwrite existing file?", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE, null, options, s1);
        if (n != JOptionPane.YES_OPTION) {
          return;
        }
      }
      if (saveFile.exists() && !saveFile.canWrite()) {
        System.err.println("No write access to file: " + saveFile);
        return;
      }

      /* Save screenshot */
      try {
        Rectangle r = rssiPanel.getVisibleRect();
        int w = r.width;
        int h = r.height;
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = bi.createGraphics();

        g2d.translate(-r.x, 0);
        rssiPanel.paint(g2d);
        g2d.dispose();

        ImageIO.write(bi, "png", saveFile);
      } catch (IOException err) {
        System.err.println("Error: " + err.getMessage());
      }
    }
  };
}
