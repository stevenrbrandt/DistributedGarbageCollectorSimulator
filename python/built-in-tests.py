#!/usr/bin/python3
import os, re, sys, queue, threading

big_machine = False
if big_machine:
    workers = 40
    seeds_per_test = 1000
    max_size = 50
    mega = True
else:
    workers = 3
    seeds_per_test = 100
    max_size = 30
    mega = False

jobs = queue.Queue()

tests = ["rand","randm1","grid","gridm1","cyclem1","dlink","dlinkm1","clique","cliquem1"]

class Job:
    def __init__(self,tn,sz,sd):
        self.test_name = tn
        self.size = sz
        self.seed = sd
    def run(self,wid):
        if wid==0:
            sys.stdout.write('Test %15s seed=%4d size=%2d\n' % (self.test_name,self.seed,self.size))
        congest = "no"
        priority = 0
        if mega:
            if self.test_name in ["clique","cliquem1"]:
                congest = "yes"
            if self.test_name in ["grid","gridm1"]:
                priority = 5
        cmd = 'java -ea -Dadv-priority=%d -DCONGEST_mode=%s -DCheckCounts=yes -Dverbose=no -Dseed=%d -Dsize=%d -Dtest=%s -cp build/classes edu.lsu.cct.distgc.Main > worker%d.out 2>&1' % \
            (priority,congest,self.seed,self.size,self.test_name,wid)
        rc = os.system(cmd)
        if rc != 0:
            sys.stderr.write("Died '%s' seed=%d, worker=%d\n" % (self.test_name,self.seed,wid))
            fw = open('worker%d.sh' % wid,"w")
            fw.write(re.sub(r'verbose=no','verbose=yes',cmd))
            fw.write("\ntail -20 worker%d.out\n" % wid)
            fw.close()
            os.kill(os.getpid(),9)


# The really big one
if mega:
    for size in [1000000]:
        for seed in range(3):
            for test in tests:
                jobs.put(Job(test,size,seed))

for size in range(max_size,1,-1):
    for seed in range(seeds_per_test):
        for test in tests:
            jobs.put(Job(test,size,seed))

def rmfile(fn):
    if os.path.isfile(fn):
        os.unlink(fn)

def worker(worker_id):
    rmfile("worker%d.out"%worker_id)
    rmfile("worker%d.sh"%worker_id)
    while True:
        try:
            job = jobs.get(block=False)
        except queue.Empty:
            break
        job.run(worker_id)
        jobs.task_done()

threads = []
for wid in range(workers):
    t = threading.Thread(target=worker,args=(wid,))
    threads += [t]
    t.start()
for th in threads:
    th.join()
print("All tests passed")
