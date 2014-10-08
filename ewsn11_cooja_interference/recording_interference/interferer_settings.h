/*
 * Copyright (c) 2010, University of Luebeck, Germany.
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
 * Author: Carlo Alberto Boano <cboano@iti.uni-luebeck.de>
 *
 */
 
#include "contiki.h"
#include "dev/spi.h"
#include "dev/cc2420.h"
#include "dev/cc2420_const.h"	

// Sets the transmission power to pow
void power_jammer(int pow);

// Reset the interferer back to normal mode
void reset_jammer();

// Starts the interferer. Takes as input parameter the carrier type (0 = unmodulated carrier, !0 = modulated carrier)
void set_jammer(int carrier);

// Set the channel fast and discard the first wrong readings
void my_set_channel(int ch);

// -------- Definition of fast SPI operations -------- //
#define MY_FASTSPI_GETRSSI(v)\
  do {\
    /* Flush the radio. THIS IS NECESSARY TO AVOID THE WRONG RSSI READINGS BECAUSE OF INTERRUPTS */\
	SPI_TXBUF = CC2420_SFLUSHRX;\
	/* Request CCA THRESHOLD + RSSI */\
    SPI_TXBUF = (CC2420_RSSI) | 0x40;\
    NOP_DELAY();\
    SPI_RXBUF;\
    /* Receive LSB: CCA THRESHOLD */\
    SPI_TXBUF = 0;\
    /*while ((IFG1 & URXIFG0) == 0);*/\
    NOP_DELAY();\
    SPI_RXBUF;\
    /* Receive LSB: **RSSI** */\
    SPI_TXBUF = 0;\
    NOP_DELAY();\
    v = SPI_RXBUF;\
  } while (0) 
/* --------------------------------------------------- */
#define NOP_DELAY() \
  do {\
    _NOP(); _NOP(); _NOP(); _NOP(); \
    _NOP(); _NOP(); _NOP(); _NOP(); \
    _NOP(); _NOP(); _NOP(); _NOP(); \
    _NOP(); _NOP(); _NOP(); _NOP(); \
	_NOP(); _NOP(); _NOP(); _NOP(); \
  } while (0);
/* --------------------------------------------------- */
#define SPI_SET_UNMODULATED(a,b,c,d)\
     do {\
		  /* Enable SPI */\
		  /*CC2420_SPI_ENABLE();\*/\
		  /* Setting CC2420_DACTST */\
		  SPI_TXBUF = CC2420_DACTST;\
		  NOP_DELAY();\
		  SPI_TXBUF = ((u8_t) ((a) >> 8));\
		  NOP_DELAY();\
		  SPI_TXBUF = ((u8_t) (a));\
		  NOP_DELAY();\
		  /* Setting CC2420_MANOR */\
		  SPI_TXBUF = CC2420_MANOR;\
		  NOP_DELAY();\
		  SPI_TXBUF = ((u8_t) ((b) >> 8));\
		  NOP_DELAY();\
		  SPI_TXBUF = ((u8_t) (b));\
		  NOP_DELAY();\
		  /* Setting CC2420_MDMCTRL1 */\
		  SPI_TXBUF = CC2420_MDMCTRL1;\
		  NOP_DELAY();\
		  SPI_TXBUF = ((u8_t) ((c) >> 8));\
		  NOP_DELAY();\
		  SPI_TXBUF = ((u8_t) (c));\
		  NOP_DELAY();\
		  /* Setting CC2420_TOPTST */\
		  SPI_TXBUF = CC2420_TOPTST;\
		  NOP_DELAY();\
		  SPI_TXBUF = ((u8_t) ((d) >> 8));\
		  NOP_DELAY();\
		  SPI_TXBUF = ((u8_t) (d));\
		  NOP_DELAY();\
		  /* STROBE STXON */\
		  SPI_TXBUF = CC2420_STXON;\
		  NOP_DELAY();\
		  /* Disable SPI */\
		  /*CC2420_SPI_DISABLE();\*/\
     } while (0)
/* --------------------------------------------------- */
#define SPI_UNSET_CARRIER()\
     do {\
		  /* Enable SPI */\
		  /*CC2420_SPI_ENABLE();\*/\
		  /* TURN OFF THE RADIO */\
		  SPI_TXBUF = CC2420_SRFOFF;\
		  NOP_DELAY();\
		  /* Disable SPI */\
		  /*CC2420_SPI_DISABLE();\*/\
     } while (0)
/* --------------------------------------------------- */
#define SPI_SET_MODULATED(c)\
     do {\
		  /* Enable SPI */\
		  /*CC2420_SPI_ENABLE();\*/\
		  /* Setting MDMCTRL1 */\
		  SPI_TXBUF = CC2420_MDMCTRL1;\
		  NOP_DELAY();\
		  SPI_TXBUF = ((u8_t) ((c) >> 8));\
		  NOP_DELAY();\
		  SPI_TXBUF = ((u8_t) (c));\
		  NOP_DELAY();\
		  /* STROBE STXON */\
		  SPI_TXBUF = CC2420_STXON;\
		  NOP_DELAY();\
		  /* Disable SPI */\
		  /*CC2420_SPI_DISABLE();\*/\
     } while (0)	 
/* --------------------------------------------------- */
#define SPI_SET_TXPOWER(c)\
     do {\
		  /* Enable SPI */\
		  /*CC2420_SPI_ENABLE();\*/\
		  /* Setting TXCTRL */\
		  SPI_TXBUF = CC2420_TXCTRL;\
		  NOP_DELAY();\
		  SPI_TXBUF = ((u8_t) ((c) >> 8));\
		  NOP_DELAY();\
		  SPI_TXBUF = ((u8_t) (c));\
		  NOP_DELAY();\
		  /* Disable SPI */\
		  /*CC2420_SPI_DISABLE();\*/\
     } while (0)	 
/* --------------------------------------------------- */
#define SPI_SETCHANNEL_SUPERFAST(v)\
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
/* --------------------------------------------------- */
