for test in dlink grid clique
do
    python3 make-plots.py $test
    python3 fit.py -ps -lx plots/${test}-CONGEST-shuffle-a0-msgs
    python3 fit.py -ps -lx -ly plots/${test}-CONGEST-shuffle-a0-rnds
done
