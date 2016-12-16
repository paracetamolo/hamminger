#!/bin/bash

set -e

CHR=chr22.vcf #chromosome file from 1000genomes project, uncompressed
NAME=${CHR%.vcf}
DIR=data

LIB=/usr/share/maven-repo/commons-io/commons-io/2.5/commons-io-2.5.jar

rm -rf bin *.class
mkdir -p bin
javac -cp .:$LIB -Xlint:unchecked -d bin Main.java

main () {
	java -cp .:$LIB:bin Main $*;

	# (time (java -cp .:$LIB:bin Main $*);
	#  uptime) 2>&1 | tee run.log
}


main hamming data/$NAME.vcf 100
main merge ${DIR}/${NAME}_K100_N1 10
main merge ${DIR}/${NAME}_K1000_N1 10
main collision ${DIR}/${NAME}_K100_probs
main collision ${DIR}/${NAME}_K1000_probs
main collision ${DIR}/${NAME}_K10000_probs
