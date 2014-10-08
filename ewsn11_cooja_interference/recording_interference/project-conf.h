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
 * $Id: project-conf.h 1092 2009-11-26 13:16:54Z nfi $
 */

#ifndef __PROJECT_CONF_H__
#define __PROJECT_CONF_H__

#undef NETSTACK_MAC
#define NETSTACK_MAC nullmac_driver

#undef NETSTACK_RDC
#define NETSTACK_RDC nullrdc_driver

#undef MMEM_CONF_SIZE
#define MMEM_CONF_SIZE 0

#undef ELFLOADER_CONF_DATAMEMORY_SIZE
#define ELFLOADER_CONF_DATAMEMORY_SIZE 0

#undef ELFLOADER_CONF_TEXTMEMORY_SIZE
#define ELFLOADER_CONF_TEXTMEMORY_SIZE 0

#undef RIME_CONF_NO_POLITE_ANNOUCEMENTS
#define RIME_CONF_NO_POLITE_ANNOUCEMENTS 0

#undef QUEUEBUF_CONF_NUM
#define QUEUEBUF_CONF_NUM 0

#undef PACKETBUF_CONF_SIZE
#define PACKETBUF_CONF_SIZE 0

#undef SERIAL_LINE_CONF_BUFSIZE
#define SERIAL_LINE_CONF_BUFSIZE 128

#undef UART1_CONF_TX_WITH_INTERRUPT
#define UART1_CONF_TX_WITH_INTERRUPT 0

#undef DCOSYNCH_CONF_ENABLED
#define DCOSYNCH_CONF_ENABLED 0

#endif /* __PROJECT_CONF_H__ */
