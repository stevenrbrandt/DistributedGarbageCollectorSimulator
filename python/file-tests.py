#!/usr/bin/python3
import os, re, sys, queue, threading

max_seed = 100

jobs = queue.Queue()

files = []

class Job:
    def __init__(self,fn,sd):
        self.file_name = fn
        self.seed = sd
    def run(self,wid):
        if wid==0:
            sys.stdout.write('Test %s seed=%d\n' % (self.file_name,self.seed))
        cmd = 'java -ea -DCheckCounts=no -Dverbose=yes -Dseed=%d -Dfileloc=/tmp/%s -Dtest=file-input -cp build/classes edu.lsu.cct.distgc.Main > worker%d.out 2>&1' % (self.seed,self.file_name,wid)
        rc = os.system(cmd)
        if rc != 0:
            sys.stderr.write("Died '%s' seed=%d, worker=%d\n" % (self.file_name,self.seed,wid))
            fw = open('worker%d.sh' % wid,"w")
            fw.write(cmd)
            fw.write("\ntail -20 worker%d.out\n" % wid)
            fw.close()
            os.kill(os.getpid(),9)

for fn in os.listdir("."):
    if re.match(r'^test.*\.txt$',fn):
        c = open(fn).read()
        c = re.sub('(?m)^\s*seed\s+(\d+)','',c)
        c += 'runall\n'
        fw = open('/tmp/%s' % fn,"w")
        fw.write(c)
        fw.close()
        files += [fn]

for seed in range(max_seed):
    for file_name in files:
        jobs.put(Job(file_name,seed))

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
for wid in range(4):
    t = threading.Thread(target=worker,args=(wid,))
    threads += [t]
    t.start()
for th in threads:
    th.join()
print("All tests passed")
