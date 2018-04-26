import random
msgs = []
id_seq = -1

nodes = {}

def rm1(value,li):
    new_li = []
    found = False
    for i in range(len(li)):
        if not found and li[i] == value:
            found = True
        else:
            new_li += [li[i]]
    assert found
    return new_li

def prmsg(msg):
    print("Message:",end=" ")
    msg_fields = ["mtype","weight","sender","target"]
    for k in msg:
        assert k in msg_fields
    for k in msg_fields:
        if k in msg:
            print(k,"=",str(msg[k]),sep="",end="")
            if k != "target":
                print(end=",")
    print()

def node_info():
    print()
    print("=" * 50)
    for node in nodes:
        if node > 0:
            print(nodes[node].info())
        else:
            edges = {}
            for e in nodes[0].edges:
                edges[e.id]=1
            print("root=",[id for id in edges])
    print("=" * 50)

def mark_and_sweep():
    for id in nodes:
        nodes[id].mark = False
        nodes[id].wc = 0
        nodes[id].sc = 0
    root = nodes[0]
    proc_set = [nodes[0]]
    while len(proc_set)>0:
        node = proc_set[0]
        del proc_set[0]
        if not node.mark:
            for n in node.edges:
                if node.weight < n.weight:
                    n.sc += 1
                else:
                    n.wc += 1
            proc_set += node.edges
            proc_set += node.deledges
            node.mark = True
    for id in nodes:
        if id == 0:
            continue
        node = nodes[id]
        if node.mark:
            assert node.strong_count > 0, node.info()
            assert node.state == "healthy", node.info()
            assert node.sc == node.strong_count, "sc=%d, %s" % (node.sc,node.info())
            assert node.wc == node.weak_count, node.info()
        else:
            assert node.strong_count == 0, node.info()
            assert node.state == "dead", node.info()
            assert node.sc == 0, node.info()
            assert node.wc == 0, node.info()
        assert node.phantom_count == 0, node.info()

class Node:
    def __init__(self,weight):
        global id_seq, nodes
        id_seq += 1
        self.id = id_seq
        self.weight = weight + 1
        self.max_weight = weight
        self.strong_count = 1
        self.weak_count = 0
        self.wait_count = 0
        self.phantom_count = 0
        self.state = "healthy"
        self.edges = []
        self.deledges = []
        self.parent = -1
        self.mark = False
        nodes[self.id] = self

    def __str__(self):
        return "Node(id=%d)" % self.id

    def send(self,msg):
        global msgs
        if "target" in msg:
            assert type(msg["target"]) != int
        print("    Send: ",end="")
        prmsg(msg)
        msgs += [msg]
        assert msg["target"] != None

    def send_all(self,mtype,sender):
        for node in self.edges:
            self.send({
                "mtype":mtype,
                "weight":self.weight,
                "sender":self,
                "target":node
            })
            if mtype != "infect":
                self.wait_count += 1
        self.state = mtype
        if self.parent < 0 and sender != None:
            self.parent = sender.id
        if self.wait_count == 0 and mtype != "infect":
            self.wait_count = 1
            self.ret()

    def incr(self,weight):
        if weight > self.max_weight:
            self.max_weight = weight
        if weight < self.weight:
            self.strong_count += 1
        else:
            self.weak_count += 1

    def info(self):
        if self.parent != None:
            sinit = str(self.parent)
        ids = []
        for node in self.edges:
            ids += [node.id]
        return "Node(id=%d,weight=%d/%d,state=%s,strong=%d,weak=%d,phantom=%d,parent=%s,wait=%d,edges=%s)" % \
            (self.id,self.weight,self.max_weight,self.state, \
             self.strong_count,self.weak_count,self.phantom_count, \
             self.parent,self.wait_count,str(ids))

    def edge(self,node):
        global msgs
        assert node.id != 0
        print()
        print("Create Edge:",str(self))
        self.send({
            "mtype":"incr",
            "weight":self.weight,
            "target":node
        })
        self.edges += [node]
        self.mwait()

    def create(self):
        node = Node(self.weight)
        self.edges += [node]
        return node

    def deledge(self,node):
        global msgs
        print()
        print("Delete Edge:",str(self))
        self.send({
            "mtype":"decr",
            "weight":self.weight,
            "target":node
        })
        self.edges = rm1(node,self.edges)
        self.mwait()

    # ===START OF ALGO===
    def decr(self,weight):
        global msgs
        if weight < self.weight:
            self.strong_count -= 1
            assert self.strong_count >= 0
        else:
            self.weak_count -= 1
            assert self.weak_count >= 0
        if self.strong_count == 0:
            self.parent = 0
            self.send_all("phantom",None)
            self.toggle()

    def toggle(self):
        print("    Toggle")
        self.strong_count = self.weak_count
        self.weak_count = 0
        self.weight = self.max_weight + 1

    def phantom(self,weight,sender):
        if weight < self.weight:
            self.strong_count -= 1
            assert self.strong_count >= 0
        else:
            self.weak_count -= 1
            assert self.weak_count >= 0
        self.phantom_count += 1
        if self.strong_count == 0 and self.state == "healthy":
            self.send_all("phantom",sender)
            self.toggle()
        else:
            self.return_msg(sender)

    def recover(self,weight,sender):
        if self.state == "phantom" and self.parent < 0:
            if self.strong_count > 0:
                self.send_all("build",sender)
            else:
                self.send_all("recover",sender)
        else:
            self.return_msg(sender)

    def build(self,weight,sender):
        if self.state in ["phantom","recover"] and self.strong_count == 0 and self.weak_count == 0:
            self.weight = weight + 1
            self.max_weight = weight
        if weight < self.weight:
            self.strong_count += 1
        else:
            self.weak_count += 1
        if weight > self.max_weight:
            self.max_weight = weight
        self.phantom_count -= 1
        if self.state in ["phantom","recover"] and self.parent < 0:
            self.send_all("build",sender)
        else:
            self.return_msg(sender)
        if self.parent < 0 and self.phantom_count == 0:
            self.healthy()

    def ret(self):
        self.wait_count -= 1
        assert self.wait_count >= 0
        if self.wait_count > 0:
            return
        if self.parent == 0:
            if self.state == "phantom":
                if self.strong_count > 0 or self.weak_count > 0:
                    self.send_all("build",None)
                else:
                    self.send_all("recover",None)
            elif self.state == "build":
                self.healthy()
            elif self.state == "recover":
                if self.strong_count > 0:
                    self.send_all("build",None)
                else:
                    self.send_all("infect",None)
                    self.edges = []
        else:
            if self.state == "recover" and self.strong_count > 0:
                self.send_all("build",-1)
            elif self.parent > 0:
                self.return_msg(nodes[self.parent])
                self.parent = -1
        if self.wait_count == 0 and self.parent <= 0:
            if self.strong_count > 0 and self.phantom_count == 0:
                self.healthy()
            elif self.strong_count == 0 and self.weak_count == 0 and self.phantom_count == 0:
                assert self.wait_count == 0
                self.send_all("infect",None)
                self.state = "dead"

    def infect(self):
        self.phantom_count -= 1
        assert self.phantom_count >= 0
        if self.strong_count == 0 and self.weak_count == 0:
            self.send_all("infect",None)
            self.deledges += [e for e in self.edges]
            self.edges = []
            if self.phantom_count == 0:
                self.state = "dead"
        elif self.phantom_count == 0:
            self.healthy()
    # ===END OF ALGO===

    def healthy(self):
        self.state = "healthy"
        self.parent = -1

    def return_msg(self,target):
        self.send({
            "mtype":"return",
            "target":target
        })

    def mwait(self):
        global msgs
        while len(msgs) > 0:
            n = random.randrange(0,len(msgs))
            msg = msgs[n]
            del msgs[n]
            target = msg["target"]
            print()
            prmsg(msg)
            print("  Before:",target.info())
            if msg["mtype"] == "phantom":
                msg["target"].phantom(msg["weight"],msg["sender"])
            elif msg["mtype"] == "recover":
                msg["target"].recover(msg["weight"],msg["sender"])
            elif msg["mtype"] == "build":
                msg["target"].build(msg["weight"],msg["sender"])
            elif msg["mtype"] == "return":
                msg["target"].ret()
            elif msg["mtype"] == "decr":
                msg["target"].decr(msg["weight"])
            elif msg["mtype"] == "incr":
                msg["target"].incr(msg["weight"])
            elif msg["mtype"] == "infect":
                msg["target"].infect()
            else:
                raise Exception(msg["mtype"])
            print("  After: ",target.info())
        node_info()
        mark_and_sweep()

root = Node(-1)

def dlink(n):
    nlist = []
    for i in range(n):
        nlist += [root.create()]
    for i in range(1,n):
        nlist[i-1].edge(nlist[i])
        nlist[i].edge(nlist[i-1])
    for i in range(n):
        root.deledge(nlist[i])

def rand(n):
    nlist = []
    m = int(n/2)
    for i in range(m):
        nlist += [root.create()]
    for i in range(1,n):
        r1 = random.randrange(0,m)
        r2 = random.randrange(0,m)
        nlist[r1].edge(nlist[r2])
    for i in range(m):
        root.deledge(nlist[i])

def cycle(n):
    nlist = []
    for i in range(n):
        nlist += [root.create()]
    for i in range(n):
        nx = (i+1)%n
        nlist[i].edge(nlist[nx])
    for i in range(n):
        root.deledge(nlist[i])

def connected(n):
    m = 2
    while m*(m-1) < n:
        m += 1
    nlist = []
    for i in range(m):
        nlist += [root.create()]
    count = 0
    for off in range(1,m):
        for i in range(m):
            if count >= n:
                break
            nx = (i+off)%m
            nlist[i].edge(nlist[nx])
            count += 1
    for i in range(m):
        root.deledge(nlist[i])

def grid(n):
    m = 2
    while 4*(m-1)**2 < n:
        m += 1
    nlist = []
    for i in range(m):
        sublist = []
        for j in range(m):
            sublist += [root.create()]
        nlist += [sublist]
    count = 0
    for i in range(1,m):
        for j in range(m):
            if count >= n:
                break
            nlist[i-1][j].edge(nlist[i][j])
            count += 1
            if count >= n:
                break
            nlist[i][j].edge(nlist[i-1][j])
            count += 1
    for i in range(m):
        for j in range(1,m):
            if count >= n:
                break
            nlist[i][j-1].edge(nlist[i][j])
            count += 1
            if count >= n:
                break
            nlist[i][j].edge(nlist[i][j-1])
            count += 1
    for i in range(m):
        for j in range(m):
            root.deledge(nlist[i][j])

tmap = {
    "cycle":cycle,
    "dlink":dlink,
    "connected":connected,
    "grid":grid,
    "rand":rand
}

for sz in range(2,70):
    #for seed in range(53,54):
    for seed in range(0,100):
        for test in ["rand","cycle","dlink","connected","grid"]:
            random.seed(seed)
            nodes = {0:root}
            id_seq = 0
            assert len(msgs)==0
            print("TEST: %s, size=%d, seed=%d" % (test,sz,seed))
            tmap[test](sz)
