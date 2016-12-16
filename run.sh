#!/bin/bash

set -e

CORES=3

if [[ ( -z $1 ) || ( ! -r $1 ) ]]; then echo 'Pass the vcf uncompressed file.'; exit 1; fi

NAME=${1%.vcf} #chromosome file from 1000genomes project, uncompressed


# compile
LIB=/usr/share/java/commons-io.jar
rm -rf bin *.class
mkdir -p bin
javac -cp .:$LIB -Xlint:unchecked -d bin Main.java

#run
main () {
	java -cp .:$LIB:bin Main $*;

	# (time (java -cp .:$LIB:bin Main $*);
	#  uptime) 2>&1 | tee run.log
}

echo "Hamming ${NAME}"
main hamming ${NAME}.vcf 100 $CORES

echo "Merging"
main merge ${NAME}_K100_N1 10
main merge ${NAME}_K1000_N1 10

for size in 100 1000 10000; do
	echo "collisions for size $size"
	main collision ${NAME}_K${size}_probs
done
