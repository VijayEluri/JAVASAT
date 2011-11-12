#!/bin/sh
for f in ./test-files/*.cnf
do
    echo $f
    time java Solver $f
done

