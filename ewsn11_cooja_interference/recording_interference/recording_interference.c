#include "contiki.h"
#include "net/rime.h"
#include "dev/watchdog.h"
#include "dev/cc2420.h"
#include "dev/serial-line.h"
#include "dev/uart1.h"
#include "dev/leds.h"
#include "sys/rtimer.h"
#include <stdio.h>
#include <signal.h>
#include <stdlib.h>
#include "interferer_settings.h"

// CONFIGURATION
#define BUFFER_SIZE 4096
#define THRESHOLD_RSSI 1
#define DELIMITER1 101
#define DELIMITER2 102
#define DELIMITER3 103
#define DELIMITER4 104
#define DELIMITER5 105

// Buffers for RSSI
static char buffer_value[BUFFER_SIZE];
static char buffer_occurrencies[BUFFER_SIZE];
static long total_elements = 0;

// Other global variables
static int stop;
static struct etimer et;

/*---------------------------------------------------------------------------*/
// Flush the radio buffer
static void flushrx(void)
{
  uint8_t dummy;
  CC2420_READ_FIFO_BYTE(dummy);
  CC2420_STROBE(CC2420_SFLUSHRX);
  CC2420_STROBE(CC2420_SFLUSHRX);
}
/*---------------------------------------------------------------------------*/
// CPU Boosting 
uint16_t cpu1, cpu2;
void boost_cpu() // Temporarily boost CPU speed
{
 cpu1 = DCOCTL;
 cpu2 = BCSCTL1;
 DCOCTL = 0xff;
 BCSCTL1 |= 0x07;
}	
void restore_cpu() // Restore CPU speed
{
 DCOCTL = cpu1;
 BCSCTL1 = cpu2;
}
/*---------------------------------------------------------------------------*/
static void handle_command(char* cmd)
{
  int len = strlen(cmd);
  if(len == 0) {
    return;
  }
  if(len > 7 && strncmp("channel", cmd, 7) == 0) {
    my_set_channel(atoi(&cmd[7]));
  } else if(strncmp("stop", cmd, 4) == 0) {
    stop = 1;	
  } else if(strncmp("cont", cmd, 4) == 0) {
    stop = 0;
  } else {
    printf("unknown command: %s\n", cmd);
  }
}
/*---------------------------------------------------------------------------*/
void measure_RSSI()
{
 // RSSI measurements
 static signed char rssi;
 int k=0, cnt = 1, current=0;
 total_elements = 0;
 MY_FASTSPI_GETRSSI(current); // Get the first element;
 for(k=0; k<BUFFER_SIZE; ){					
	// Sample the RSSI fast
	MY_FASTSPI_GETRSSI(rssi);
	int diff = abs(current - rssi);
	// TODO: the two branches of the IF should take exactly the same time (using NOP)
	if((diff < THRESHOLD_RSSI)&&(cnt<255)){
		cnt++;
	}
	else{				
		buffer_value[k] = current;
		buffer_occurrencies[k] = cnt;	
		total_elements += cnt;
		k++;
		cnt = 1;
		current = rssi;
	}
 }	
}
/*---------------------------------------------------------------------------*/

PROCESS(frossi_process, "frossi");
AUTOSTART_PROCESSES(&frossi_process);
PROCESS_THREAD(frossi_process, ev, data)
{
 PROCESS_BEGIN();
 
 // Initial operations
 leds_on(LEDS_BLUE);
 watchdog_stop();
 stop = 1; // Should it start already scanning or not? (1 = not, 0 = yes)
 my_set_channel(26); // Default is channel 26

 // Configuring the AGC and avoiding wrong RSSI readings
 unsigned agc;
 CC2420_READ_REG(CC2420_AGCTST1, agc);
 CC2420_WRITE_REG(CC2420_AGCTST1, (agc + (1 << 8) + (1 << 13))); 
 
 while (1) {
	if(stop == 0) {
      
		leds_on(LEDS_GREEN);
		dint();					// Disable interrupt
		boost_cpu(); 			// Temporarily boost CPU speed
		CC2420_SPI_ENABLE(); 	// Enable SPI
	
		rtimer_clock_t now = RTIMER_NOW(); 	// MEASURING TIME (does not work)
		measure_RSSI();		
		now = (RTIMER_NOW()-now); 			// MEASURING TIME (does not work)

        CC2420_SPI_DISABLE();   // Disable SPI  
		restore_cpu();  		// Restore CPU speed
		eint(); 				// Re-enable interrupt	
		leds_off(LEDS_GREEN);	
		
        /// Printing part ///
		leds_on(LEDS_RED);	
		
		// Printing speed
		uart1_writeb(0xff&DELIMITER1);
		uart1_writeb(0xff&now);
		uart1_writeb(0xff&(now>>8));
		uart1_writeb(0xff&DELIMITER2);	
		uart1_writeb(0xff&total_elements);
		uart1_writeb(0xff&(total_elements>>8));
		uart1_writeb(0xff&(total_elements>>16));
		uart1_writeb(0xff&(total_elements>>24));
		uart1_writeb(0xff&DELIMITER3);	
		
		// Printing entries
		int v=0;
		for(v=0; v<BUFFER_SIZE; v++){	
			uart1_writeb(0xff&DELIMITER4);
			uart1_writeb(0xff&(buffer_value[v]+55));
			uart1_writeb(0xff&buffer_occurrencies[v]);
			//uart1_writeb(0xff&((buffer_value[v]+55) ^ buffer_occurrencies[v])); // CRC (Ex-or)
			uart1_writeb(0xff&DELIMITER5);					
        }
		
		leds_off(LEDS_RED);

        // Flush RXFIFO at overflows only
        if(CC2420_FIFOP_IS_1 && !CC2420_FIFO_IS_1) {
			flushrx();
        }
	
    }

    // Pause: Handle commands
    etimer_set(&et, CLOCK_SECOND/32);
    while (!etimer_expired(&et)) {
		PROCESS_YIELD();
		if(ev == serial_line_event_message) {
			handle_command((char*)data);
		}
    }
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
