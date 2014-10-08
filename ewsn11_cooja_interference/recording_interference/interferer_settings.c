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
 
#include "interferer_settings.h"

// Reset the interferer back to normal mode
void reset_jammer(){
	SPI_UNSET_CARRIER();
}

// Starts the interferer (0 = unmodulated carrier, !0 = modulated carrier)
void set_jammer(int carrier){
	if(carrier){
		/* 
		 * The CC2420 has a built-in test pattern generator that can generate pseudo random sequence using the CRC generator. 
		 * This is enabled by setting MDMCTRL1.TX_MODE to 3 and issue a STXON command strobe. The modulated spectrum is then available on the RF pins. 
		 * The low byte of the CRC word is transmitted and the CRC is updated with 0xFF for each new byte. 
		 * The length of the transmitted data sequence is 65535 bits. The transmitted data-sequence is then: [synch header] [0x00, 0x78, 0xb8, 0x4b, 0x99, 0xc3, 0xe9, …]	
		*/
		SPI_SET_MODULATED(0x050C);
	}
	else{
		/* 
		 * An unmodulated carrier may be transmitted by setting MDMCTRL1.TX_MODE to 2, writing 0x1800 to the DACTST register and issue a STXON command strobe.
		 * The transmitter is then enabled while the transmitter I/Q DACs are overridden to static values. 
		 * An unmodulated carrier will then be available on the RF output pins.
		*/	
		SPI_SET_UNMODULATED(0x1800,0x0100,0x0508,0x0004);
	}
}

// Sets the transmission power to pow
void power_jammer(int pow){
	// 0xa0ff is the initial value of the CC2420_TXCTRL register measured by me
	SPI_SET_TXPOWER((0xa0ff & 0xffe0) | (pow & 0x1f));
}

// Set the channel fast and discard the first wrong readings
void my_set_channel(int ch)
{
  int k, temp;
  SPI_SETCHANNEL_SUPERFAST(357+((ch-11)*5)); // Approx. 292 us
  // Discard first 16 bad readings
  CC2420_SPI_ENABLE();
  for (k=0; k<=16; k++) {
    MY_FASTSPI_GETRSSI(temp);
  }
  clock_delay(1);
  CC2420_SPI_DISABLE();
}

