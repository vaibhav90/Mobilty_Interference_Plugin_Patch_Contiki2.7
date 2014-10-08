<?xml version="1.0" encoding="UTF-8"?>
<simconf>
  <project EXPORT="discard">[APPS_DIR]/avrora</project>
  <project EXPORT="discard">[APPS_DIR]/mobility</project>
  <project EXPORT="discard">[APPS_DIR]/mrm</project>
  <project EXPORT="discard">[APPS_DIR]/mspsim</project>
  <project EXPORT="discard">[APPS_DIR]/ewsn11_cooja_interference/frossi-scanner</project>
  <project EXPORT="discard">[APPS_DIR]/ewsn11_cooja_interference/cooja_configuration</project>
  <simulation>
    <title>External Interference (EWSN 2011)</title>
    <randomseed>generated</randomseed>
    <motedelay_us>1000000</motedelay_us>
    <radiomedium>
      org.contikios.mrm.MRM
      <frequency value="866.4521907514452" />
      <obstacles />
    </radiomedium>
    <events>
      <logoutput>4000000</logoutput>
    </events>
    <motetype>
      org.contikios.cooja.motes.ImportAppMoteType
      <identifier>apptype1</identifier>
      <description>Microwave</description>
      <motepath>[APPS_DIR]/ewsn11_cooja_interference/cooja_configuration/build</motepath>
      <moteclass>MicrowaveInterferer</moteclass>
    </motetype>
    <motetype>
      org.contikios.cooja.mspmote.SkyMoteType
      <identifier>sky1</identifier>
      <description>Frossi scanner</description>
      <source EXPORT="discard">[CONTIKI_DIR]/examples/sky-shell/sky-shell.c</source>
      <commands EXPORT="discard">make sky-shell.sky TARGET=sky</commands>
      <firmware EXPORT="copy">[CONTIKI_DIR]/examples/sky-shell/sky-shell.sky</firmware>
      <moteinterface>org.contikios.cooja.interfaces.Position</moteinterface>
      <moteinterface>org.contikios.cooja.interfaces.RimeAddress</moteinterface>
      <moteinterface>org.contikios.cooja.interfaces.IPAddress</moteinterface>
      <moteinterface>org.contikios.cooja.interfaces.Mote2MoteRelations</moteinterface>
      <moteinterface>org.contikios.cooja.interfaces.MoteAttributes</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.MspClock</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.MspMoteID</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyButton</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyFlash</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyCoffeeFilesystem</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.Msp802154Radio</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.MspSerial</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyLED</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.MspDebugOutput</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyTemperature</moteinterface>
    </motetype>
    <motetype>
      org.contikios.cooja.motes.ImportAppMoteType
      <identifier>apptype2</identifier>
      <description>Wifi basestation</description>
      <motepath>[APPS_DIR]/ewsn11_cooja_interference/cooja_configuration/build</motepath>
      <moteclass>WifiInterferer</moteclass>
    </motetype>
    <motetype>
      org.contikios.cooja.motes.ImportAppMoteType
      <identifier>apptype3</identifier>
      <description>Bluetooth</description>
      <motepath>[APPS_DIR]/ewsn11_cooja_interference/cooja_configuration/build</motepath>
      <moteclass>BluetoothInterferer</moteclass>
    </motetype>
    <motetype>
      org.contikios.cooja.motes.ImportAppMoteType
      <identifier>apptype4</identifier>
      <description>Trace file</description>
      <motepath>[APPS_DIR]/ewsn11_cooja_interference/cooja_configuration/build</motepath>
      <moteclass>LiveTraceInterferer</moteclass>
    </motetype>
    <motetype>
      org.contikios.cooja.mspmote.SkyMoteType
      <identifier>sky2</identifier>
      <description>Collect</description>
      <source EXPORT="discard">[CONTIKI_DIR]/examples/sky-shell/sky-shell.c</source>
      <commands EXPORT="discard">echo make clean TARGET=sky
make sky-shell.sky TARGET=sky DEFINES=NETSTACK_MAC=nullmac_driver,NETSTACK_RDC=nullrdc_noframer_driver,CC2420_CONF_AUTOACK=0</commands>
      <firmware EXPORT="copy">[CONTIKI_DIR]/examples/sky-shell/sky-shell.sky</firmware>
      <moteinterface>org.contikios.cooja.interfaces.Position</moteinterface>
      <moteinterface>org.contikios.cooja.interfaces.RimeAddress</moteinterface>
      <moteinterface>org.contikios.cooja.interfaces.IPAddress</moteinterface>
      <moteinterface>org.contikios.cooja.interfaces.Mote2MoteRelations</moteinterface>
      <moteinterface>org.contikios.cooja.interfaces.MoteAttributes</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.MspClock</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.MspMoteID</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyButton</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyFlash</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyCoffeeFilesystem</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.Msp802154Radio</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.MspSerial</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyLED</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.MspDebugOutput</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyTemperature</moteinterface>
    </motetype>
    <motetype>
      org.contikios.cooja.mspmote.SkyMoteType
      <identifier>sky3</identifier>
      <description>Sky Mote Type #sky3</description>
      <source EXPORT="discard">[CONTIKI_DIR]/examples/ipv6/simple-udp-rpl/broadcast-example.c</source>
      <commands EXPORT="discard">make broadcast-example.sky TARGET=sky</commands>
      <firmware EXPORT="copy">[CONTIKI_DIR]/examples/ipv6/simple-udp-rpl/broadcast-example.sky</firmware>
      <moteinterface>org.contikios.cooja.interfaces.Position</moteinterface>
      <moteinterface>org.contikios.cooja.interfaces.RimeAddress</moteinterface>
      <moteinterface>org.contikios.cooja.interfaces.IPAddress</moteinterface>
      <moteinterface>org.contikios.cooja.interfaces.Mote2MoteRelations</moteinterface>
      <moteinterface>org.contikios.cooja.interfaces.MoteAttributes</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.MspClock</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.MspMoteID</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyButton</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyFlash</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyCoffeeFilesystem</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.Msp802154Radio</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.MspSerial</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyLED</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.MspDebugOutput</moteinterface>
      <moteinterface>org.contikios.cooja.mspmote.interfaces.SkyTemperature</moteinterface>
    </motetype>
    <mote>
      <breakpoints />
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>129.37906957150938</x>
        <y>41.41510047645189</y>
        <z>0.0</z>
      </interface_config>
      <interface_config>
        org.contikios.cooja.mspmote.interfaces.MspMoteID
        <id>1</id>
      </interface_config>
      <motetype_identifier>sky2</motetype_identifier>
    </mote>
    <mote>
      <breakpoints />
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>41.499179912183855</x>
        <y>2.861342432360684</y>
        <z>0.0</z>
      </interface_config>
      <interface_config>
        org.contikios.cooja.mspmote.interfaces.MspMoteID
        <id>2</id>
      </interface_config>
      <motetype_identifier>sky2</motetype_identifier>
    </mote>
    <mote>
      <breakpoints />
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>30.15983931098053</x>
        <y>51.90399053256494</y>
        <z>0.0</z>
      </interface_config>
      <interface_config>
        org.contikios.cooja.mspmote.interfaces.MspMoteID
        <id>3</id>
      </interface_config>
      <motetype_identifier>sky2</motetype_identifier>
    </mote>
    <mote>
      <breakpoints />
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>134.48177284205087</x>
        <y>33.1940785405795</y>
        <z>0.0</z>
      </interface_config>
      <interface_config>
        org.contikios.cooja.mspmote.interfaces.MspMoteID
        <id>4</id>
      </interface_config>
      <motetype_identifier>sky2</motetype_identifier>
    </mote>
    <mote>
      <breakpoints />
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>129.66255308653945</x>
        <y>55.589276227956006</y>
        <z>0.0</z>
      </interface_config>
      <interface_config>
        org.contikios.cooja.mspmote.interfaces.MspMoteID
        <id>5</id>
      </interface_config>
      <motetype_identifier>sky2</motetype_identifier>
    </mote>
    <mote>
      <breakpoints />
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>119.74063006048657</x>
        <y>64.94423222394873</y>
        <z>0.0</z>
      </interface_config>
      <interface_config>
        org.contikios.cooja.mspmote.interfaces.MspMoteID
        <id>6</id>
      </interface_config>
      <motetype_identifier>sky2</motetype_identifier>
    </mote>
    <mote>
      <interface_config>
        org.contikios.cooja.motes.AbstractApplicationMoteType$SimpleMoteID
        <id>7</id>
      </interface_config>
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>84.58867419675636</x>
        <y>42.83251805160231</y>
        <z>0.0</z>
      </interface_config>
      <motetype_identifier>apptype1</motetype_identifier>
    </mote>
    <mote>
      <breakpoints />
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>35.27282368710045</x>
        <y>62.093853083320205</y>
        <z>0.0</z>
      </interface_config>
      <interface_config>
        org.contikios.cooja.mspmote.interfaces.MspMoteID
        <id>9</id>
      </interface_config>
      <motetype_identifier>sky3</motetype_identifier>
    </mote>
    <mote>
      <interface_config>
        org.contikios.cooja.motes.AbstractApplicationMoteType$SimpleMoteID
        <id>10</id>
      </interface_config>
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>54.2265204207993</x>
        <y>22.883587503052937</y>
        <z>0.0</z>
      </interface_config>
      <motetype_identifier>apptype3</motetype_identifier>
    </mote>
  </simulation>
  <plugin>
    org.contikios.cooja.plugins.SimControl
    <width>259</width>
    <z>0</z>
    <height>205</height>
    <location_x>790</location_x>
    <location_y>267</location_y>
  </plugin>
  <plugin>
    org.contikios.cooja.plugins.Visualizer
    <plugin_config>
      <skin>org.contikios.cooja.plugins.skins.GridVisualizerSkin</skin>
      <skin>org.contikios.cooja.plugins.skins.IDVisualizerSkin</skin>
      <skin>org.contikios.cooja.plugins.skins.TrafficVisualizerSkin</skin>
      <skin>org.contikios.cooja.plugins.skins.AttributeVisualizerSkin</skin>
      <skin>InterferenceSkin</skin>
      <viewport>3.703570508004103 0.0 0.0 3.703570508004103 -43.41424291344168 88.93863671231102</viewport>
    </plugin_config>
    <width>522</width>
    <z>1</z>
    <height>494</height>
    <location_x>186</location_x>
    <location_y>56</location_y>
  </plugin>
  <plugin>
    org.contikios.cooja.plugins.LogListener
    <plugin_config>
      <filter />
      <formatted_time />
      <coloring />
    </plugin_config>
    <width>746</width>
    <z>2</z>
    <height>523</height>
    <location_x>30</location_x>
    <location_y>509</location_y>
  </plugin>
  <plugin>
    org.contikios.cooja.plugins.TimeLine
    <plugin_config>
      <mote>0</mote>
      <mote>1</mote>
      <mote>2</mote>
      <mote>3</mote>
      <mote>4</mote>
      <mote>5</mote>
      <mote>6</mote>
      <mote>7</mote>
      <mote>8</mote>
      <showRadioRXTX />
      <showRadioHW />
      <zoomfactor>17906.61479114932</zoomfactor>
    </plugin_config>
    <width>1374</width>
    <z>3</z>
    <height>336</height>
    <location_x>3</location_x>
    <location_y>641</location_y>
  </plugin>
  <plugin>
    org.contikios.mrm.AreaViewer
    <plugin_config>
      <controls_visible>true</controls_visible>
      <zoom_x>2.115207324543569</zoom_x>
      <zoom_y>2.115207324543569</zoom_y>
      <pan_x>3.1358058333208207</pan_x>
      <pan_y>98.70125897717439</pan_y>
      <show_background>true</show_background>
      <show_obstacles>true</show_obstacles>
      <show_channel>true</show_channel>
      <show_radios>true</show_radios>
      <show_arrow>true</show_arrow>
      <vis_type>signalStrengthButton</vis_type>
      <resolution>100</resolution>
    </plugin_config>
    <width>673</width>
    <z>-1</z>
    <height>632</height>
    <location_x>601</location_x>
    <location_y>329</location_y>
    <minimized>true</minimized>
  </plugin>
  <plugin>
    org.contikios.cooja.plugins.ScriptRunner
    <plugin_config>
      <script>TIMEOUT(120000000)&#xD;
sinkMote = mote.getSimulation().getMoteWithID(1);&#xD;
&#xD;
GENERATE_MSG(3000, "continue");&#xD;
YIELD_THEN_WAIT_UNTIL(msg.equals("continue"));&#xD;
&#xD;
write(sinkMote, "netcmd { repeat 0 2 { randwait 2 collect-view-data | send 31 } }");&#xD;
GENERATE_MSG(500, "continue");&#xD;
YIELD_THEN_WAIT_UNTIL(msg.equals("continue"));&#xD;
write(sinkMote, "collect | timestamp | binprint &amp;");&#xD;
&#xD;
log.log("Started sink at " + sinkMote + "\n");</script>
      <active>true</active>
    </plugin_config>
    <width>403</width>
    <z>-1</z>
    <height>196</height>
    <location_x>877</location_x>
    <location_y>660</location_y>
    <minimized>true</minimized>
  </plugin>
</simconf>

