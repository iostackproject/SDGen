/*
 * file_pipe.h
 *
 *  Created on: May 5, 2014
 *      Author: raul
 */

#define JAVA_COMM_PIPE_NAME "generation_pipe"

void init_pipe();
void write_to_pipe(char * to_write);
void close_pipe();

