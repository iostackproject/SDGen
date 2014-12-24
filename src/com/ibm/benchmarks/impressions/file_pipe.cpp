/*
 * file_pipe.cpp
 *
 *  Created on: May 5, 2014
 *      Author: raul
 */
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include "file_pipe.h"

//ADDED BY RAUL GRACIA: open fifo for writing

FILE *fp_out;

int run_command(const char *strCommand)
{
	int iForkId, iStatus;
	iForkId = vfork();
	if (iForkId == 0) {
		iStatus = execl("/bin/sh","sh","-c", strCommand, (char*) NULL);
		exit(iStatus);
	}else if (iForkId > 0){
		iStatus = 0;
	}else{
		iStatus = -1;
	}
	return(iStatus);
}

void init_pipe() {
	fp_out = fopen(JAVA_COMM_PIPE_NAME, "w");
	//Create the pipe to sent commands to the java program
	char cmd [100];
	printf("Creating pipe with java program...\n");
	sprintf(cmd, "mkfifo %s", JAVA_COMM_PIPE_NAME);
	system(cmd);
	printf("Starting java program...\n");
	sprintf(cmd, "java -jar ./extension_helpers/exec.jar");
	run_command(cmd);
	printf("Executing OK...\n");
}

void write_to_pipe(char * to_write) {
	fprintf(fp_out, to_write);
}

void close_pipe() {
	printf("Sending quit signal to producer...\n");
    //Notify the process to finish
    fprintf(fp_out,"quit\n");
    //Close file and delete it
    printf("Closing and deleting pipe...\n");
    fclose(fp_out);
    remove(JAVA_COMM_PIPE_NAME);
}


