<?xml version="1.0" encoding="UTF-8"?>
<simconf>
  <project EXPORT="discard">[CONTIKI_DIR]/tools/cooja/apps/mrm</project>
  <project EXPORT="discard">[CONTIKI_DIR]/tools/cooja/apps/mspsim</project>
  <project EXPORT="discard">[CONTIKI_DIR]/tools/cooja/apps/avrora</project>
  <project EXPORT="discard">[CONTIKI_DIR]/tools/cooja/apps/serial_socket</project>
  <project EXPORT="discard">[CONTIKI_DIR]/tools/cooja/apps/collect-view</project>
  <project EXPORT="discard">[CONFIG_DIR]/../frossi-scanner</project>
  <project EXPORT="discard">[CONFIG_DIR]</project>
  <simulation>
    <title>External Interference (EWSN 2011)</title>
    <delaytime>0</delaytime>
    <randomseed>generated</randomseed>
    <motedelay_us>1000000</motedelay_us>
    <radiomedium>
      se.sics.mrm.MRM
      <rt_diffr_coefficient>-10.0</rt_diffr_coefficient>
      <bg_noise_var>1.0</bg_noise_var>
      <rt_max_diffractions>0</rt_max_diffractions>
      <snr_threshold>6.0</snr_threshold>
      <rt_max_reflections>1</rt_max_reflections>
      <tx_power>1.5</tx_power>
      <rx_antenna_gain>0.0</rx_antenna_gain>
      <apply_random>false</apply_random>
      <rt_ignore_non_direct>false</rt_ignore_non_direct>
      <rt_refrac_coefficient>-3.0</rt_refrac_coefficient>
      <rt_max_refractions>1</rt_max_refractions>
      <system_gain_var>4.0</system_gain_var>
      <rt_max_rays>1</rt_max_rays>
      <rt_disallow_direct_path>false</rt_disallow_direct_path>
      <rx_sensitivity>-100.0</rx_sensitivity>
      <obstacle_attenuation>-3.0</obstacle_attenuation>
      <rt_reflec_coefficient>-5.0</rt_reflec_coefficient>
      <bg_noise_mean>-100.0</bg_noise_mean>
      <rt_fspl_on_total_length>true</rt_fspl_on_total_length>
      <system_gain_mean>0.0</system_gain_mean>
      <tx_antenna_gain>0.0</tx_antenna_gain>
      <wavelength>0.346</wavelength>
      <obstacles />
    </radiomedium>
    <events>
      <logoutput>4000000</logoutput>
    </events>
    <motetype>
      se.sics.cooja.motes.ImportAppMoteType
      <identifier>apptype1</identifier>
      <description>Microwave</description>
      <motepath>[CONFIG_DIR]/build</motepath>
      <moteclass>MicrowaveInterferer</moteclass>
    </motetype>
    <motetype>
      se.sics.cooja.mspmote.SkyMoteType
      <identifier>sky1</identifier>
      <description>Frossi scanner</description>
      <firmware EXPORT="copy">[CONFIG_DIR]/../frossi-scanner/frossi.sky</firmware>
      <moteinterface>se.sics.cooja.interfaces.Position</moteinterface>
      <moteinterface>se.sics.cooja.interfaces.RimeAddress</moteinterface>
      <moteinterface>se.sics.cooja.interfaces.IPAddress</moteinterface>
      <moteinterface>se.sics.cooja.interfaces.Mote2MoteRelations</moteinterface>
      <moteinterface>se.sics.cooja.interfaces.MoteAttributes</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.MspClock</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.MspMoteID</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.SkyButton</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.SkyFlash</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.SkyCoffeeFilesystem</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.SkyByteRadio</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.MspSerial</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.SkyLED</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.MspDebugOutput</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.SkyTemperature</moteinterface>
    </motetype>
    <motetype>
      se.sics.cooja.motes.ImportAppMoteType
      <identifier>apptype2</identifier>
      <description>Wifi basestation</description>
      <motepath>[CONFIG_DIR]/build</motepath>
      <moteclass>WifiInterferer</moteclass>
    </motetype>
    <motetype>
      se.sics.cooja.motes.ImportAppMoteType
      <identifier>apptype3</identifier>
      <description>Bluetooth</description>
      <motepath>[CONFIG_DIR]/build</motepath>
      <moteclass>BluetoothInterferer</moteclass>
    </motetype>
    <motetype>
      se.sics.cooja.motes.ImportAppMoteType
      <identifier>apptype4</identifier>
      <description>Trace file</description>
      <motepath>[CONFIG_DIR]/build</motepath>
      <moteclass>LiveTraceInterferer</moteclass>
    </motetype>
    <motetype>
      se.sics.cooja.mspmote.SkyMoteType
      <identifier>sky2</identifier>
      <description>Collect</description>
      <source EXPORT="discard">[CONTIKI_DIR]/examples/sky-shell/sky-shell.c</source>
      <commands EXPORT="discard">echo make clean TARGET=sky
make sky-shell.sky TARGET=sky DEFINES=NETSTACK_MAC=nullmac_driver,NETSTACK_RDC=nullrdc_noframer_driver,CC2420_CONF_AUTOACK=0</commands>
      <firmware EXPORT="copy">[CONTIKI_DIR]/examples/sky-shell/sky-shell.sky</firmware>
      <moteinterface>se.sics.cooja.interfaces.Position</moteinterface>
      <moteinterface>se.sics.cooja.interfaces.RimeAddress</moteinterface>
      <moteinterface>se.sics.cooja.interfaces.IPAddress</moteinterface>
      <moteinterface>se.sics.cooja.interfaces.Mote2MoteRelations</moteinterface>
      <moteinterface>se.sics.cooja.interfaces.MoteAttributes</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.MspClock</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.MspMoteID</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.SkyButton</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.SkyFlash</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.SkyCoffeeFilesystem</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.SkyByteRadio</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.MspSerial</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.SkyLED</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.MspDebugOutput</moteinterface>
      <moteinterface>se.sics.cooja.mspmote.interfaces.SkyTemperature</moteinterface>
    </motetype>
    <mote>
      <breakpoints />
      <interface_config>
        se.sics.cooja.interfaces.Position
        <x>129.37906957150938</x>
        <y>41.41510047645189</y>
        <z>0.0</z>
      </interface_config>
      <interface_config>
        se.sics.cooja.mspmote.interfaces.MspMoteID
        <id>1</id>
      </interface_config>
      <motetype_identifier>sky2</motetype_identifier>
    </mote>
    <mote>
      <breakpoints />
      <interface_config>
        se.sics.cooja.interfaces.Position
        <x>41.499179912183855</x>
        <y>2.861342432360684</y>
        <z>0.0</z>
      </interface_config>
      <interface_config>
        se.sics.cooja.mspmote.interfaces.MspMoteID
        <id>2</id>
      </interface_config>
      <motetype_identifier>sky2</motetype_identifier>
    </mote>
    <mote>
      <breakpoints />
      <interface_config>
        se.sics.cooja.interfaces.Position
        <x>111.51960812461418</x>
        <y>25.256540119737192</y>
        <z>0.0</z>
      </interface_config>
      <interface_config>
        se.sics.cooja.mspmote.interfaces.MspMoteID
        <id>3</id>
      </interface_config>
      <motetype_identifier>sky2</motetype_identifier>
    </mote>
    <mote>
      <breakpoints />
      <interface_config>
        se.sics.cooja.interfaces.Position
        <x>134.48177284205087</x>
        <y>33.1940785405795</y>
        <z>0.0</z>
      </interface_config>
      <interface_config>
        se.sics.cooja.mspmote.interfaces.MspMoteID
        <id>4</id>
      </interface_config>
      <motetype_identifier>sky2</motetype_identifier>
    </mote>
    <mote>
      <breakpoints />
      <interface_config>
        se.sics.cooja.interfaces.Position
        <x>129.66255308653945</x>
        <y>55.589276227956006</y>
        <z>0.0</z>
      </interface_config>
      <interface_config>
        se.sics.cooja.mspmote.interfaces.MspMoteID
        <id>5</id>
      </interface_config>
      <motetype_identifier>sky2</motetype_identifier>
    </mote>
    <mote>
      <breakpoints />
      <interface_config>
        se.sics.cooja.interfaces.Position
        <x>119.74063006048657</x>
        <y>64.94423222394873</y>
        <z>0.0</z>
      </interface_config>
      <interface_config>
        se.sics.cooja.mspmote.interfaces.MspMoteID
        <id>6</id>
      </interface_config>
      <motetype_identifier>sky2</motetype_identifier>
    </mote>
    <mote>
      <interface_config>
        se.sics.cooja.motes.AbstractApplicationMoteType$SimpleMoteID
        <id>7</id>
      </interface_config>
      <interface_config>
        se.sics.cooja.interfaces.Position
        <x>45.18446560757491</x>
        <y>6.263144612721676</y>
        <z>0.0</z>
      </interface_config>
      <interfererActive />
      <motetype_identifier>apptype1</motetype_identifier>
    </mote>
  </simulation>
  <plugin>
    se.sics.cooja.plugins.SimControl
    <width>259</width>
    <z>0</z>
    <height>205</height>
    <location_x>0</location_x>
    <location_y>0</location_y>
  </plugin>
  <plugin>
    se.sics.cooja.plugins.Visualizer
    <plugin_config>
      <skin>se.sics.cooja.plugins.skins.GridVisualizerSkin</skin>
      <skin>se.sics.cooja.plugins.skins.IDVisualizerSkin</skin>
      <skin>se.sics.cooja.plugins.skins.TrafficVisualizerSkin</skin>
      <skin>se.sics.cooja.plugins.skins.AttributeVisualizerSkin</skin>
      <skin>InterferenceSkin</skin>
      <viewport>3.527541980329555 0.0 0.0 3.527541980329555 -10.390099289477455 8.906494449749397</viewport>
    </plugin_config>
    <width>522</width>
    <z>1</z>
    <height>494</height>
    <location_x>758</location_x>
    <location_y>0</location_y>
  </plugin>
  <plugin>
    se.sics.cooja.plugins.LogListener
    <plugin_config>
      <filter />
      <coloring />
    </plugin_config>
    <width>746</width>
    <z>3</z>
    <height>523</height>
    <location_x>11</location_x>
    <location_y>328</location_y>
  </plugin>
  <plugin>
    se.sics.cooja.plugins.TimeLine
    <plugin_config>
      <mote>0</mote>
      <mote>1</mote>
      <mote>2</mote>
      <mote>3</mote>
      <mote>4</mote>
      <mote>5</mote>
      <mote>6</mote>
      <showRadioRXTX />
      <showRadioHW />
      <split>109</split>
      <zoomfactor>17906.61479114932</zoomfactor>
    </plugin_config>
    <width>1374</width>
    <z>2</z>
    <height>336</height>
    <location_x>3</location_x>
    <location_y>641</location_y>
  </plugin>
  <plugin>
    se.sics.mrm.AreaViewer
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
      <show_activity>true</show_activity>
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
    se.sics.cooja.plugins.ScriptRunner
    <plugin_config>
      <script>TIMEOUT(120000000)
sinkMote = mote.getSimulation().getMoteWithID(1);

GENERATE_MSG(3000, "continue");&#xD;
YIELD_THEN_WAIT_UNTIL(msg.equals("continue"));&#xD;

write(sinkMote, "netcmd { repeat 0 2 { randwait 2 collect-view-data | send 31 } }");
GENERATE_MSG(500, "continue");
YIELD_THEN_WAIT_UNTIL(msg.equals("continue"));
write(sinkMote, "collect | timestamp | binprint &amp;");

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
  <plugin>
    se.sics.cooja.plugins.collectview.CollectView
    <mote_arg>0</mote_arg>
    <width>213</width>
    <z>5</z>
    <height>75</height>
    <location_x>545</location_x>
    <location_y>4</location_y>
  </plugin>
  <plugin>
    se.sics.cooja.plugins.RadioLogger
    <plugin_config>
      <split>150</split>
    </plugin_config>
    <width>522</width>
    <z>4</z>
    <height>355</height>
    <location_x>758</location_x>
    <location_y>494</location_y>
  </plugin>
</simconf>

