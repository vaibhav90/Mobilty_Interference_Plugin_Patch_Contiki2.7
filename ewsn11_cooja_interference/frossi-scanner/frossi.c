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
 * Author: Fredrik Osterlind <fros@sics.se>
 */

#include "contiki.h"
#include "net/rime.h"
#include "dev/watchdog.h"
#include "dev/cc2420.h"
#include "dev/cc2420.c"
#include "dev/serial-line.h"
#include "dev/uart1.h"
#include "dev/leds.h"
#include "node-id.h"
#include "dev/cc2420_const.h"
#include <stdio.h>
#include <signal.h>
#include <stdlib.h>
#include "dev/spi.h"


/* CONFIGURATION */
#define WITH_BOOST_CPU 1 /* Temporarily increase CPU speed (buffered mode) */
#define WITH_FLUSH_RADIO 0 /* Slower, but avoids bad RSSI values due to interrupts */
#define AUTOSTOP_SAMPLES 1024 /* Samples before stopping on CCA (stream mode) */
#define BUFFER_SIZE 8192 /* Samples in buffered mode */

#define CALIBRATION_MODE 0

/*---------------------------------------------------------------------------*/
#define NOP_DELAY() \
  do {\
    _NOP(); _NOP(); _NOP(); _NOP(); \
    _NOP(); _NOP(); _NOP(); _NOP(); \
    _NOP(); _NOP(); _NOP(); _NOP(); \
    _NOP(); _NOP(); _NOP(); _NOP(); \
  } while (0);
/*---------------------------------------------------------------------------*/
#define MY_FASTSPI_SETCHANNEL(v)\
     do {\
          CC2420_SPI_ENABLE();\
          SPI_TXBUF = CC2420_FSCTRL;\
          SPI_WAITFOREOTx();\
          SPI_TXBUF = ((u8_t) ((v) >> 8));\
          SPI_TXBUF = ((u8_t) (v));\
          SPI_WAITFOREOTx();\
          SPI_TXBUF = CC2420_SRXON;\
          SPI_WAITFOREOTx();\
          CC2420_SPI_DISABLE();\
     } while (0)
/*---------------------------------------------------------------------------*/
#if WITH_FLUSH_RADIO
#define MY_FASTSPI_GETRSSI(v)\
  do {\
    /* Flushing the radio to avoid that RSSI readings get stuck because of the interrupt */\
    CC2420_TX_ADDR(CC2420_SFLUSHRX);\
    /* Request CCA THRESHOLD + RSSI */\
    SPI_TXBUF = (CC2420_RSSI) | 0x40;\
    NOP_DELAY();\
    SPI_RXBUF;\
    \
    /* Receive LSB: CCA THRESHOLD */\
    SPI_TXBUF = 0;\
    /*while ((IFG1 & URXIFG0) == 0);*/\
    NOP_DELAY();\
    SPI_RXBUF;\
    \
    /* Receive LSB: **RSSI** */\
    SPI_TXBUF = 0;\
    NOP_DELAY();\
    v = SPI_RXBUF;\
    \
    /*clock_delay(1);*/\
  } while (0)
/*---------------------------------------------------------------------------*/
#else /* WITH_FLUSH_RADIO */
#define MY_FASTSPI_GETRSSI(v)\
  do {\
    /* Request CCA THRESHOLD + RSSI */\
    SPI_TXBUF = (CC2420_RSSI) | 0x40;\
    NOP_DELAY();\
    SPI_RXBUF;\
    \
    /* Receive LSB: CCA THRESHOLD */\
    SPI_TXBUF = 0;\
    /*while ((IFG1 & URXIFG0) == 0);*/\
    NOP_DELAY();\
    SPI_RXBUF;\
    \
    /* Receive LSB: **RSSI** */\
    SPI_TXBUF = 0;\
    NOP_DELAY();\
    v = SPI_RXBUF;\
    \
    /*clock_delay(1);*/\
  } while (0)
#endif /* WITH_FLUSH_RADIO */
/*---------------------------------------------------------------------------*/
/*static void
flushrx(void)
{
  uint8_t dummy;
  CC2420_READ_FIFO_BYTE(dummy);
  CC2420_STROBE(CC2420_SFLUSHRX);
  CC2420_STROBE(CC2420_SFLUSHRX);
 }*/
/*---------------------------------------------------------------------------*/
static void
my_set_channel(int ch)
{
  int k, temp;

  /* Approx. 292 us */
  MY_FASTSPI_SETCHANNEL(357+((ch-11)*5));

  /* Discard first 15 bad readings */
  CC2420_SPI_ENABLE();
  for (k=0; k<=15; k++) {
    MY_FASTSPI_GETRSSI(temp);
  }
  clock_delay(1);
  CC2420_SPI_DISABLE();
}
/*---------------------------------------------------------------------------*/
#if UART1_CONF_TX_WITH_INTERRUPT
#error Bad configuration: UART1_CONF_TX_WITH_INTERRUPT must be 0
#endif

static char buffer[BUFFER_SIZE];

static int auto_stop, stop;
static volatile uint16_t delay, buffered;
static struct etimer et;

static int autostop_counter;
static uint16_t buffer_counter;

/*---------------------------------------------------------------------------*/
PROCESS(frossi_process, "frossi");
AUTOSTART_PROCESSES(&frossi_process);
/*---------------------------------------------------------------------------*/
static void
handle_command(char* cmd)
{
  int len = strlen(cmd);
  if(len == 0) {
    return;
  }

  if(len > 7 && strncmp("channel", cmd, 7) == 0) {
    my_set_channel(atoi(&cmd[7]));
  } else if(len > 5 && strncmp("astop", cmd, 5) == 0) {
    auto_stop = atoi(&cmd[5]);
    autostop_counter = -1;
  } else if(strncmp("buffered", cmd, 8) == 0) {
    buffered = 1;
  } else if(len > 5 && strncmp("delay", cmd, 5) == 0) {
    delay = atoi(&cmd[5]);
    buffered = 0;
  } else if(strncmp("stop", cmd, 4) == 0) {
    stop = 1;
  } else if(strncmp("cont", cmd, 4) == 0) {
    autostop_counter = -1;
    stop = 0;
  } else {
    printf("unknown command: %s\n", cmd);
  }
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(frossi_process, ev, data)
{
  /* make frossi.ihex DEFINES=NETSTACK_MAC=nullmac_driver,NETSTACK_RDC=nullrdc_driver TARGET=sky
   * make upload-ihex FILE=frossi.ihex
   * make login */

  PROCESS_BEGIN();

  static signed char rssi;

  stop = 0;
  auto_stop = 0; /* init */
  autostop_counter = -1;
  delay = 10; /* init */
  buffered = 0; /* init */

  watchdog_stop();
  my_set_channel(26); /* init */
  cc2420_on();

  /* Avoid bad RSSI reading */
  {
    int agc;
    CC2420_READ_REG(CC2420_AGCTST1, agc);
    CC2420_WRITE_REG(CC2420_AGCTST1, (agc + (1 << 8) + (1 << 13)));
  }

  while (1) {

    /* SAMPLE RSSI/CCA */
    if(!stop) {
      if(buffered) {
        /* Buffered mode */

#if CALIBRATION_MODE
        rtimer_clock_t now;
#endif /* CALIBRATION_MODE */
#if WITH_BOOST_CPU
        uint16_t cpu1, cpu2;
#endif /* WITH_BOOST_CPU */

        dint();

#if WITH_BOOST_CPU
        /* Temporarily boost CPU speed */
        cpu1 = DCOCTL;
        cpu2 = BCSCTL1;
        DCOCTL = 0xff;
        BCSCTL1 |= 0x07;
#endif /* WITH_BOOST_CPU */

        while (auto_stop && CC2420_CCA_IS_1); /* (Optional) Wait for network activity before sampling */

        /* Sample */
        leds_on(LEDS_RED);
        CC2420_SPI_ENABLE();
        buffer_counter=0;
#if CALIBRATION_MODE
        now = RTIMER_NOW();
#endif /* CALIBRATION_MODE */
        while (buffer_counter < BUFFER_SIZE) {
          MY_FASTSPI_GETRSSI(buffer[buffer_counter]);
          buffer_counter++;
        }
#if CALIBRATION_MODE
        now = (RTIMER_NOW()-now);
#endif /* CALIBRATION_MODE */
        clock_delay(1);
        CC2420_SPI_DISABLE();
        leds_off(LEDS_RED);

#if WITH_BOOST_CPU
        /* Restore CPU speed */
        DCOCTL = cpu1;
        BCSCTL1 = cpu2;
#endif /* WITH_BOOST_CPU */

        eint();

        /* Print */
        buffer_counter=0;
        while (buffer_counter < BUFFER_SIZE) {
#if !CALIBRATION_MODE
          uart1_writeb(0xff&(buffer[buffer_counter]+55));
#endif /* CALIBRATION_MODE */
          buffer_counter++;
        }
#if CALIBRATION_MODE
        printf("duration=%d/%d, first=%d\n", now, RTIMER_SECOND, buffer[0]);
#endif /* CALIBRATION_MODE */
        stop = 1;
      } else {
        /* Stream mode */
        leds_on(LEDS_GREEN);
        while (!stop && !serial_line_process.needspoll) {
          CC2420_READ_REG(CC2420_RSSI, rssi);
#if WITH_FLUSH_RADIO
          flushrx();
#endif
          rssi += 55; /* SSI+55 <=> SS+100 */

          /* Auto stop */
          if(autostop_counter == 0) {
            stop = 1;
          } else if(autostop_counter > 0) {
            autostop_counter--;
          } else if(auto_stop && !CC2420_CCA_IS_1) {
            autostop_counter = AUTOSTOP_SAMPLES;
          }

          if(delay > 0) {
            clock_delay(delay*100); /* TODO Calibrate */
          }
#if !CALIBRATION_MODE
          uart1_writeb(rssi&0xff);
#endif /* CALIBRATION_MODE */

#if !WITH_FLUSH_RADIO
          /* Flush RXFIFO at overflows only */
          if(CC2420_FIFOP_IS_1 && !CC2420_FIFO_IS_1) {
            flushrx();
          }
#endif
        }
        leds_off(LEDS_GREEN);
      }
    }

    /* PAUSE, HANDLE COMMANDS */
    etimer_set(&et, CLOCK_SECOND/8);
    while (!etimer_expired(&et)) {
      PROCESS_YIELD();
      if(ev == serial_line_event_message) {
        leds_toggle(LEDS_BLUE);
        handle_command((char*)data);
      }
    }
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
