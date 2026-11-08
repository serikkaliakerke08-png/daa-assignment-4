"""Independent small oracles and published-domain stress tests. Run from repo root."""
import itertools,json,random,subprocess,sys,pathlib
ROOT=pathlib.Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'tools'))
from run import build,execute
from public_validate import check
r=random.Random(2537); cases=[]
def add(x,y): cases.append({'input':x,'output':y})
for _ in range(80):
 a=''.join(r.choices('ab !',k=r.randrange(9)));b=''.join(r.choices('ab !',k=r.randrange(9)))
 subs={''.join(a[i] for i in range(len(a)) if mask>>i&1) for mask in range(1<<len(a))}
 def issub(s,t):
  it=iter(t);return all(any(c==v for v in it) for c in s)
 s=max((s for s in subs if issub(s,b)),key=len)
 add(dict(task='lcs',a=a,b=b),dict(length=len(s),sequence=s))
 t=''.join(r.choices('ab !',k=r.randrange(25)));p=''.join(r.choices('ab !',k=r.randrange(8)))
 positions=[i for i in range(len(t)+1) if t.startswith(p,i)]
 for alg in ['naive','kmp','rk']:add(dict(task=alg,text=t,pattern=p),dict(positions=positions))
 sa=sorted(range(len(t)),key=lambda i:t[i:]);lcp=[0]*len(t)
 for k in range(1,len(t)):
  a,b=sa[k-1],sa[k];h=0
  while a+h<len(t) and b+h<len(t) and t[a+h]==t[b+h]:h+=1
  lcp[k]=h
 k=max(range(len(t)),key=lambda k:lcp[k]) if t else 0
 add(dict(task='suffix',text=t),dict(sa=sa,lcp=lcp,repeated=t[sa[k]:sa[k]+lcp[k]] if t else ''))
 pats=[''.join(r.choices('ab !',k=r.randrange(1,5))) for _ in range(r.randrange(8))];texts=[t,t,'']
 add(dict(task='aho',patterns=pats,texts=texts),dict(counts=[[sum(text.startswith(p,i) for i in range(len(text))) for p in pats] for text in texts]))
 n=r.randrange(1,10);weights=[r.randrange(-5,10) for _ in range(n)];edges=[[i,r.randrange(i)] for i in range(1,n)]
 valid=[mask for mask in range(1<<n) if all(not(mask>>u&1 and mask>>v&1) for u,v in edges)]
 mask=max(valid,key=lambda mask:sum(weights[i] for i in range(n) if mask>>i&1));selected=[i for i in range(n) if mask>>i&1]
 add(dict(task='tree',n=n,weights=weights,edges=edges),dict(weight=sum(weights[i] for i in selected),selected=selected))
 sizes=[r.randrange(6) for _ in range(r.randrange(7))]
 def merge_oracle(blocks):
  if len(blocks)<2:return 0,[]
  opts=[]
  for i in range(len(blocks)-1):
   l,k,a=blocks[i];_,end,b=blocks[i+1];cost,plan=merge_oracle(blocks[:i]+[(l,end,a+b)]+blocks[i+2:]);opts.append((a+b+cost,[[l,k,end]]+plan))
  return min(opts,key=lambda x:x[0])
 cost,plan=merge_oracle([(i,i,v) for i,v in enumerate(sizes)])
 add(dict(task='merge',sizes=sizes),dict(cost=cost,plan=plan))
add(dict(task='rk',text='abdj',pattern='ab'),dict(positions=[0]))
add(dict(task='tree',n=0,weights=[],edges=[]),dict(weight=0,selected=[]))
add(dict(task='aho',patterns=[],texts=['','a']),dict(counts=[[],[]]))
command=build('Java');answers=execute(command,[c['input'] for c in cases])
for c,a in zip(cases,answers):
 assert check(c['input'],a,c['output']),(c,a)
print(f'PASS {len(cases)} small oracle and boundary cases, seed 2537')
# Large requests are checked against analytically known answers.
stress=[]
def s(x,y):stress.append({'input':x,'output':y})
s(dict(task='lcs',a='a'*1000,b='a'*1000),dict(length=1000,sequence='a'*1000))
s(dict(task='merge',sizes=[10**9]*200),dict(cost=1544*10**9,plan=[]))
# Merge witness is checked by a separate verifier below, with independent optimal recurrence.
stress.pop()
n=100000;s(dict(task='tree',n=n,weights=[10**9]*n,edges=[[i-1,i] for i in range(1,n)]),dict(weight=50000*10**9,selected=list(range(0,n,2))))
s(dict(task='kmp',text='a'*200000,pattern='a'*100000),dict(positions=list(range(100001))))
s(dict(task='rk',text='a'*10000,pattern='a'*2000),dict(positions=list(range(8001))))
n=50000;s(dict(task='suffix',text='a'*n),dict(sa=list(range(n-1,-1,-1)),lcp=list(range(n)),repeated='a'*(n-1)))
s(dict(task='aho',patterns=['a'*10]*10000,texts=['a'*200000]),dict(counts=[[199991]*10000]))
answers=execute(command,[c['input'] for c in stress])
for c,a in zip(stress,answers):assert check(c['input'],a,c['output']),c['input']['task']
# 200-block large-cost chronological witness; optimal uniform cost via scalar length DP.
d=[0]*201
for n in range(2,201):d[n]=n+min(d[k]+d[n-k] for k in range(1,n))
x=dict(task='merge',sizes=[10**9]*200);a=execute(command,[x])[0]
assert check(x,a,dict(cost=d[200]*10**9,plan=a['plan']))
print('PASS 7 full-domain stress cases (including deep tree and 64-bit costs)')
