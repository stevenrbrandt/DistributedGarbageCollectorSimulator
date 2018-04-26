#!/usr/bin/python3
import numpy as np
import sys
import matplotlib.pyplot as plt
from scipy.optimize import curve_fit
import re
import matplotlib

font = {'family' : 'Bitstream Vera Sans',
        'weight' : 'normal',
        'size'   : 18}

matplotlib.rc('font', **font)

g = None
logx = None
logy = None
fname = None
loc = 'upper right'
printout = False
for arg in sys.argv:
    if g == None:
        g = re.search(r'(\w+)-(\w+)-(\w+)-a0-(\w+)',arg)
        fname = arg
    if arg == "-lx":
        logx = True
    elif arg == '-ly':
        logy = True
    elif arg == '-low':
        loc = 'lower right'
    elif arg == '-ps':
        printout = True

if g:
    print(g.groups())
    title = 'Scaling for Test: '+g.groups()[0]
    title += ", " + g.groups()[1]
    title = re.sub(r'clique','highly-connected',title)
    if g.groups()[2] == "sorted":
        title += ", sorted"
else:
    raise Exception(sys.argv[1])

val = np.genfromtxt(fname+".xg")
valu = np.genfromtxt(fname+"-u.xg")
vald = np.genfromtxt(fname+"-l.xg")
skip = 15

x = val[:,0]
y = val[:,1]
xu = valu[:,0]
yu = valu[:,1]
xd = vald[:,0]
yd = vald[:,1]

funs = {
    "a" : lambda x,a : a*np.ones(x.shape),
    "a*log(x)" : lambda x,a : a*np.log(x),
    "a*log(x)**2" : lambda x,a : a*np.log(x)**2,
    "a*log(x)**(1./2.)" : lambda x,a : a*np.log(x)**(1./2.),
    "a*log(x)**(1./3.)" : lambda x,a : a*np.log(x)**(1./3.),
    "a*log(x)**(3./2.)" : lambda x,a : a*np.log(x)**(3./2.),
    "a*log(x)**(2./3.)" : lambda x,a : a*np.log(x)**(2./3.),
    "a*log(x)**(3./4.)" : lambda x,a : a*np.log(x)**(3./4.),
    "a*x" : lambda x,a : a*x,
    "a/x" : lambda x,a : a/x,
    "a/x**(2./3.)" : lambda x,a : a/x**(2./3.),
    "a/x**(1./3.)" : lambda x,a : a/x**(1./3.),
    "a/x**(1./4.)" : lambda x,a : a/x**(1./4.),
    "a/x**(3./4.)" : lambda x,a : a/x**(3./4.),
    "a/x**(3./2.)" : lambda x,a : a/x**(3./2.),
    "a/x**(5./9.)" : lambda x,a : a/x**(5./9.),
    "a/x**(7./9.)" : lambda x,a : a/x**(7./9.),
    "a*sqrt(x)" : lambda x,a : a*np.sqrt(x),
    "a*x**(1./3)" : lambda x,a : a*np.exp(1./3.*np.log(x)),
    "a*x**(2./3)" : lambda x,a : a*np.exp(2./3.*np.log(x)),
    "a/sqrt(x)" : lambda x,a : a/np.sqrt(x),
    "a*x**1.5" : lambda x,a : a*np.sqrt(x)*x
}

bestfn = None
bestv = 1e1000
bestfit = None

for f in funs:
    ff = funs[f]
    popt, pcurv = curve_fit(ff,x[skip:],y[skip:])
    v = np.sum(np.abs(y - ff(x,*popt)))
    if v < bestv:
        bestv = v
        bestfn = f
        bestfit = popt

print("-> best:",bestfn,"a="+str(bestfit[0]))
bestf = funs[bestfn]

fig, ax = plt.subplots()
fig.suptitle(title)
ax.set_xlabel('Number of Edges')

if logy:
    ax.semilogy(x,y,'g--')

if logx:
    ax.semilogx(x,y,'g--')

if g.groups()[3] == "msgs":
    ax.set_ylabel('Number of Messages/Edge')
else:
    ax.set_ylabel('Number of Rounds/Edge')
#plt.plot(x,y,'rs-')
#plt.plot(xu,yu,'r--')
#plt.plot(xd,yd,'r-+')
ax.errorbar(x,y,yerr=[y-yd,yu-y],fmt='g--')[0].set_label('Experimental Result')
yf = bestf(x,*bestfit)
plt.plot(x,yf,'b^-')[0].set_label('Fit: %s,a=%.2f' % (bestfn,bestfit[0]))
ax.legend(loc=loc)
if printout:
    plt.savefig(fname+'.eps',format='eps')
else:
    plt.show()
