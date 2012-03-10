#!/bin/sh
for f in ./test-files/*.cnf
do
    echo $f
    time java -cp ./build/classes com.trevorstevens.javasat.Solver $f
done

