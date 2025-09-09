#!/bin/bash

cd /home/javaapps/sbt-projects/iahx-analyzer

sbt "runMain org.bireme.dia.tools.IndexDeCS -database=DECS -collection=current -index=resources/decs/main -host=172.17.1.230"

cd - || exit
