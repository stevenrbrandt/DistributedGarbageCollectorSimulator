#!/usr/bin/python3
# Make the plots
import os,sys,re
from math import sqrt

ttype = sys.argv[1]
mode = "CONGEST"
ordering = "shuffle"
advpri = 0
tests_per_data_point = 20
max_edges = 10000

assert mode == "CONGEST" or mode == "random"
assert ordering == "shuffle" or ordering == "sorted"

shuffle = "yes";
if ordering == "sorted":
    shuffle = "no"
sync = "no"
if mode == "CONGEST":
    sync = "yes"

name="{type}-{mode}-{shuf}-a{advpri}".format(**{
    "type":ttype,
    "mode":mode,
    "shuf":ordering,
    "advpri":advpri
})

fn1 = "plots/{name}-msgs.xg".format(**{"name":name})
if os.path.exists(fn1):
    exit(0)
fp1 = open("plots/{name}-msgs.xg".format(**{"name":name}),"w");
fp2 = open("plots/{name}-msgs-u.xg".format(**{"name":name}),"w");
fp3 = open("plots/{name}-msgs-l.xg".format(**{"name":name}),"w");
fp4 = open("plots/{name}-rnds.xg".format(**{"name":name}),"w");
fp5 = open("plots/{name}-rnds-u.xg".format(**{"name":name}),"w");
fp6 = open("plots/{name}-rnds-l.xg".format(**{"name":name}),"w");

class Matcher:
    def __init__(self):
        self.g = None
    def search(self,pattern,text):
        self.g = re.search(pattern,text)
        return self.g != None
    def group(self,n):
        return self.g.group(n)

m = Matcher()

size = 10
while True:
    size = int(size*1.2)
    msg_sum = 0
    msg2_sum = 0
    rnd_sum = 0
    rnd2_sum = 0
    count = 0
    for seed in range(1,tests_per_data_point+1):
        cmd="java -ea -Dadv-priority={advpri} -DCONGEST_mode={sync} -Dshuffle={shuffle} -Dverbose=no -Dtest={type} -Dsize={size} -Dseed={seed} -cp build/classes edu.lsu.cct.distgc.Main > run1.out 2>&1".format(**{
            "advpri":advpri,
            "sync":sync,
            "shuffle":shuffle,
            "type":ttype,
            "size":size,
            "seed":seed
        });
        os.system(cmd)
        fd = open("run1.out","r")
        edges = 0
        rounds = 0
        msgs = 0
        for line in fd.readlines():
            if m.search(r'edges: (\d+)',line):
                edges = int(m.group(1))
            elif m.search(r'rounds to converge: (\d+)',line):
                rounds = int(m.group(1))
            elif m.search(r'messages to converge: (\d+)',line):
                msgs = int(m.group(1))
        rounds = (1.0*rounds)/edges
        msgs = (1.0*msgs)/edges

        msg_sum += msgs
        msg2_sum += msgs**2

        rnd_sum += rounds
        rnd2_sum += rounds**2

        count += 1

    msg_avg = msg_sum/count
    msg2_avg = msg2_sum/count
    msg_dev = sqrt(abs(msg2_avg - msg_avg**2))

    rnd_avg = rnd_sum/count
    rnd2_avg = rnd2_sum/count
    rnd_dev = sqrt(abs(rnd2_avg - rnd_avg**2))

    print("=" * 50)
    print("edges=%d\n" % edges)
    print("msgs=%5.2f +/- %5.2f" % (msg_avg,msg_dev))
    print("rnds=%5.2f +/- %5.2f" % (rnd_avg,rnd_dev))

    msg_u = msg_avg + msg_dev
    msg_l = msg_avg - msg_dev
    rnd_u = rnd_avg + rnd_dev
    rnd_l = rnd_avg - rnd_dev

    fp1.write("%d %s\n" % (edges,msg_avg))
    fp2.write("%d %s\n" % (edges,msg_u))
    fp3.write("%d %s\n" % (edges,msg_l))

    fp4.write("%d %s\n" % (edges,rnd_avg))
    fp5.write("%d %s\n" % (edges,rnd_u))
    fp6.write("%d %s\n" % (edges,rnd_l))

    if edges > max_edges:
        break

fp1.close()
fp2.close()
fp3.close()
fp4.close()
fp5.close()
fp6.close()
