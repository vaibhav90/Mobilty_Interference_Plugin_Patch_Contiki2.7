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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JPanel;
import javax.swing.Timer;

/* javac ProcessServer.java */

/**
 * Simple program that forwards data between a process and a socket.
 * 
 * Usage: java ProcessServer [server port] [command]+
 * Example: java ProcessServer 60000 make login
 * 
 * Note: if the process exits, it will be restarted automatically!
 * 
 * @author Fredrik Osterlind
 */
public class ProcessServer extends JPanel {
  private static final long serialVersionUID = -3622477940975403618L;

  /* Process */
  static PrintWriter pOut;
  static int toProcess = 0;

  /* Network */
  static Socket nSocket;
  static DataInputStream nIn;
  static DataOutputStream nOut;
  static int toNetwork = 0;

  static void cleanupNetwork() {
    try {
      if (nSocket != null) {
        nSocket.close();
        nSocket = null;
      }
    } catch (IOException e) {
    }
    try {
      if (nIn != null) {
        nIn.close();
        nIn = null;
      }
    } catch (IOException e) {
    }
    try {
      if (nOut != null) {
        nOut.close();
        nOut = null;
      }
    } catch (IOException e) {
    }
  }

  static void startSocketThread(final DataInputStream in) {
    new Thread(new Runnable() {
      public void run() {
        int numRead = 0;
        byte[] data = new byte[1024];

        while (true) {
          numRead = -1;
          try {
            numRead = in.read(data);
          } catch (IOException e) {
            numRead = -1;
          }

          if (numRead >= 0) {
            for (int i=0; i < numRead; i++) {
              pOut.write((char)data[i]);
              toProcess++;
            }
            pOut.flush();
          } else {
            cleanupNetwork();
            break;
          }
        }
        cleanupNetwork();
      }
    }).start();
  }

  static void startProcessThreads(final Process p) {
    new Thread(new Runnable() {
      public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        int b;
        try {
          while ((b = in.read()) != -1) {
            /*System.out.println("FWD>: " + b);*/
            if (nOut != null) {
              nOut.write(b);
              toNetwork++;
            }
          }
          in.close();
        } catch (IOException e) {
          System.err.println("Stream close: " + e);
          System.exit(1);
        }
      }
    }).start();
    new Thread(new Runnable() {
      public void run() {
        BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line;
        try {
          while ((line = err.readLine()) != null) {
            System.out.println("process stderr> " + line);
          }
          err.close();
        } catch (IOException e) {
          System.err.println("Stream close: " + e);
          System.exit(1);
        }
      }
    }).start();
  }
  
  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.err.println("Usage: java ProcessServer [server port] [command]+");
      System.err.println("Example: java ProcessServer 60000 make login");
      System.exit(1);
    }

    /* Debug throughput output timer */
    new Timer(5000, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.out.println("to process: " + toProcess/5 + " bytes/s. to network: " + toNetwork/5 + " bytes/s.");
        toProcess = 0;
        toNetwork = 0;
      }
    }).start();

    /* Server */
    int port = Integer.parseInt(args[0]);
    System.err.println("Opening server on port: " + port);
    final ServerSocket server = new ServerSocket(port);
    new Thread() {
      public void run() {
        while (server != null) {
          try {
            nSocket = server.accept();
            nIn = new DataInputStream(nSocket.getInputStream());
            nOut = new DataOutputStream(nSocket.getOutputStream());
            nOut.flush();
            System.err.println("Client connected from: " + nSocket.getInetAddress().getHostAddress());

            startSocketThread(nIn);
          } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            cleanupNetwork();
            System.exit(1);
          }
        }
      }
    }.start();

    /* Process */
    String[] processArgs = new String[args.length - 1];
    System.arraycopy(args, 1, processArgs, 0, args.length-1);
    String fullCommand = "";
    for (String a: processArgs) {
      fullCommand += a + " ";
    }
    System.err.println("Executing command: " + fullCommand);
    while (true) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e1) {
      }
      Process p = Runtime.getRuntime().exec(processArgs);
      startProcessThreads(p);
      pOut = new PrintWriter(new OutputStreamWriter(p.getOutputStream()));
      try {
        p.waitFor();
      } catch (InterruptedException e) {
      }
    }
  }
}
