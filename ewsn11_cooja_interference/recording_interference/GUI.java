import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import gnu.io.*; // for rxtxSerial library



public class GUI extends JPanel
implements ActionListener{			
	static Enumeration<CommPortIdentifier> portList;
	public static JFrame frame;
	public static JInternalFrame iframe, iframe2;
	public static JComboBox combo1,combo2;
	public static JButton button1,button2,button3,button4;	
	public static saveRSSI readrssi;
	public static File saveFile;
	
    
    // Menu file 
    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);        
        JMenuItem menuItem = new JMenuItem("Quit");
        menuItem.setMnemonic(KeyEvent.VK_Q);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        menuItem.setActionCommand("quit");
        menuItem.addActionListener(this);
        menu.add(menuItem); 
        return menuBar;
    }
    
    
    // Detecting COM ports //
    static String getPortTypeName (int portType)
    {
        switch (portType)
        {
            case CommPortIdentifier.PORT_I2C: return "I2C port";
            case CommPortIdentifier.PORT_PARALLEL: return "Parallel port";
            case CommPortIdentifier.PORT_RAW: return "Raw port";
            case CommPortIdentifier.PORT_RS485: return "RS485 port";
            case CommPortIdentifier.PORT_SERIAL: return "Serial port";
            default: return "Unknown Type port";
        }
    }
    
    public String[] detect_ports(){
    	String existingPorts[] = new String[16];
    	int countPorts = 0;
		portList = CommPortIdentifier.getPortIdentifiers(); // Parse ports		
        while (portList.hasMoreElements()) 
        {
            CommPortIdentifier portId = portList.nextElement();
            String availability = "";
            try {
                CommPort thePort = portId.open("CommUtil", 50);
                thePort.close();
                availability = "Available";             
            } catch (PortInUseException e) {
                availability = "Already in use";
            } catch (Exception e) {
                availability = "Cannot open port";
            }
            existingPorts[countPorts] = portId.getName()  +  " - " +  getPortTypeName(portId.getPortType()) +  " - " + availability;            
            countPorts++;
        }        
        String Ports[] = new String[countPorts]; // Load the Ports string
	    for (int i=0;i<countPorts;i++){
	    	Ports[i] = existingPorts[i];
	    }
	    return Ports;
    }
   
    
    // Actions on Button event	
	public void actionPerformed(ActionEvent e) {
		// Select serial port for communication with the mote
	    if ("setport".equals(e.getActionCommand())) {	    	
	        combo1.setEnabled(false);
	        button1.setEnabled(false);	 
	        button2.setEnabled(true);	 	    		
	    	combo2.setEnabled(true);
	        CommPortIdentifier portIdentifier;
			try {
				String porta = (String)combo1.getSelectedItem();
				porta = porta.substring(0, porta.indexOf('-')-1);
				System.out.printf("Communication on port: %s\n",porta);
				portIdentifier = CommPortIdentifier.getPortIdentifier(porta);
			    CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);          
			    SerialPort serialPort = (SerialPort) commPort;
			    serialPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
			    // Create the new readRSSI object
			    readrssi = new saveRSSI(serialPort.getInputStream(), serialPort.getOutputStream(), saveFile);
			    // Add the RSSI visualizer to the frame
			    iframe2.add(readrssi);				    
			    // Start the thread
			    Thread threadrssi = new Thread(readrssi);					
			    threadrssi.start();	
			    // Stopping the communication by default at the beginning
		    	try {
					readrssi.out.write("stop\n");
					readrssi.out.flush();	
					System.out.println("Stopping capture by default at the beginning");
				} catch (IOException e1) {				
					e1.printStackTrace();
				}
			} catch (NoSuchPortException e2) { JOptionPane.showMessageDialog(frame,"The selected port does not exist!");
			} catch (PortInUseException e3) { JOptionPane.showMessageDialog(frame,"The selected port is already in use!");
			} catch (IOException e4) { JOptionPane.showMessageDialog(frame,"IO Exception!");				
			} catch (UnsupportedCommOperationException e5) { JOptionPane.showMessageDialog(frame,"Unsupported CommOperation!");
			} 		        
	    }
	    // Set channel
	    if ("setchannel".equals(e.getActionCommand())) { 
	    	button2.setEnabled(false);	 	    		
	    	combo2.setEnabled(false);
	    	button3.setEnabled(true);		    	
	    	try {
	    		Integer channel = (Integer) combo2.getSelectedItem();	    		
	    		readrssi.out.write("channel " + channel + "\n");
	    		iframe2.setTitle("Channel " + channel);
	    		readrssi.out.flush();	
	    		System.out.println("Setting channel to " + channel);
			} catch (IOException e1) {				
				e1.printStackTrace();
			}
	    }
	    // Start button
	    if ("start".equals(e.getActionCommand())) {
	    	button3.setEnabled(false);
	    	button4.setEnabled(true);
	    	iframe2.setVisible(true);
	    	try {
				readrssi.out.write("cont\n");
				readrssi.out.flush();	
				readrssi.setActive_capture(true);
				System.out.println("Starting capture");
			} catch (IOException e1) {				
				e1.printStackTrace();
			}
        }	   
	    // Stop button
	    if ("stop".equals(e.getActionCommand())) { 
	    	button3.setEnabled(true);
	    	button4.setEnabled(false);
	    	iframe2.setVisible(false);
	    	try {
				readrssi.out.write("stop\n");
				readrssi.out.flush();	
				readrssi.setActive_capture(false);
				System.out.println("Stopping capture");
			} catch (IOException e1) {				
				e1.printStackTrace();
			}
        }	  	    
	    // Quit
	    if ("quit".equals(e.getActionCommand())) { 
            System.exit(0);
        }
	} 


	public GUI() {
		// Creating the whole frame
		frame = new JFrame("EWSN 2011 Demo - Recording application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        frame.setSize(490, 290);
        frame.setLocationRelativeTo(null); // Make it at the center of the screen
        frame.setVisible(true);         
	    frame.setJMenuBar(createMenuBar()); // Add the menu to the Frame
	    
	    // Creating the panel
	    JPanel panel = new JPanel();
	    panel.setLayout(null);
	    
	    // Selection of the COM port	    	
	    String[] Allports = detect_ports();
	    combo1 = new JComboBox(Allports);
	    combo1.setBackground(Color.gray);
	    combo1.setForeground(Color.black);	    	    
	    button1 = new JButton("Set COM port"); 
	    button1.setActionCommand("setport");
	    button1.addActionListener(this);
	    panel.add(combo1);
	    panel.add(button1);
	    
	    // Selection of the channel where to record interference  
	    combo2 = new JComboBox(new Integer[] {11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26});
	    combo2.setSelectedItem(26);	        
	    combo2.setBackground(Color.gray);
	    combo2.setForeground(Color.black);	
	    combo2.setEnabled(false);
	    button2 = new JButton("Select Channel");
	    button2.setActionCommand("setchannel");
	    button2.addActionListener(this);
	    button2.setEnabled(false);	 	    		    	
	    panel.add(combo2);
	    panel.add(button2);
	    
	    // Start-Stop the recording	   
	    button3 = new JButton("Start the Capture");
	    button3.setActionCommand("start");
	    button3.addActionListener(this);
        button3.setEnabled(false);	 
	    panel.add(button3);
	    button4 = new JButton("Stop the Capture");
	    button4.setActionCommand("stop");
	    button4.addActionListener(this);
	    button4.setEnabled(false);
	    panel.add(button4);
	    
	    // Define the position of the objects in the frame	    	    	 
	    combo1.setBounds(10, 10, 300, 20);
	    button1.setBounds(320, 10, 135, 20);	    
	    combo2.setBounds(10, 40, 300, 20);
	    button2.setBounds(320, 40, 135, 20);	    
	    button3.setBounds(10, 70, 215, 30);
	    button4.setBounds(240, 70, 215, 30);
	    
	    iframe = new JInternalFrame("Recording the interference trace from sensor mote", true,false,false,false); // Caption,caption,minimize,maximize,close
	    iframe.setBounds(0, 0, 475, 140);
	    iframe.add(panel);
	    iframe.setVisible(true);	
	    
	    iframe2 = new JInternalFrame("Channel XX", true,false,false,false); // Caption,caption,minimize,maximize,close
	    iframe2.setBounds(0, 140, 475, 90);	    
	    iframe2.setVisible(false);
	    
	    JDesktopPane desk = new JDesktopPane();
	    desk.add(iframe);
	    desk.add(iframe2);
	    frame.add(desk);
	    
		// Selecting a file where to save the trace
		JFileChooser fc = new JFileChooser();
	    fc.setCurrentDirectory(new java.io.File("."));
	    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);   
	    fc.setDialogTitle("Save trace");
	    int returnVal = fc.showSaveDialog(iframe);
	    // Check if it returned a filename or pressed the cancel button
	    if (returnVal != JFileChooser.APPROVE_OPTION) {
	    	System.exit(0);
	    }	    
	    saveFile = fc.getSelectedFile();
	    if (saveFile.exists()) {
	        Object[] options = {"Overwrite", "Cancel"};
	        int n = JOptionPane.showOptionDialog(iframe, "A file '" + saveFile.getName() + "' already exists.\nDo you want to remove it?", "Overwrite existing file?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, "Overwrite");
	        if (n != JOptionPane.YES_OPTION) {
		    	System.exit(0);
		    }	  
	    }
	    if (saveFile.exists() && !saveFile.canWrite()) {
	        System.err.println("No write access to file: " + saveFile);
	        return;
	    }
         
	}
	
	public static void main(String[] args) {		
		GUI interface_GUI = new GUI();		
	} 
}