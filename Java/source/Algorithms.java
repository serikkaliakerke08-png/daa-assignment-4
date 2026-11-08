import java.util.*;
/** Assignment 4 algorithms. All mutable state is local to one request. */
public final class Algorithms {
    static String str(Map<String,Object> x,String k){return (String)x.get(k);}
    static List<?> list(Map<String,Object>x,String k){return (List<?>)x.get(k);}
    static int num(Object x){return ((Number)x).intValue();}
    static Map<String,Object> lcs(Map<String,Object>x){
        String a=str(x,"a"),b=str(x,"b"); int n=a.length(),m=b.length();
        int[][] d=new int[n+1][m+1];
        for(int i=1;i<=n;i++)for(int j=1;j<=m;j++)d[i][j]=a.charAt(i-1)==b.charAt(j-1)?d[i-1][j-1]+1:Math.max(d[i-1][j],d[i][j-1]);
        StringBuilder s=new StringBuilder(); int i=n,j=m;
        while(i>0&&j>0){if(a.charAt(i-1)==b.charAt(j-1)){s.append(a.charAt(--i));j--;}else if(d[i-1][j]>=d[i][j-1])i--;else j--;}
        return Map.of("length",d[n][m],"sequence",s.reverse().toString());
    }
    static Map<String,Object> merge(Map<String,Object>x){
        List<?> a=list(x,"sizes");int n=a.size();long[] p=new long[n+1];for(int i=0;i<n;i++)p[i+1]=p[i]+((Number)a.get(i)).longValue();
        long[][] d=new long[n][n];int[][] split=new int[n][n];
        for(int len=2;len<=n;len++)for(int l=0;l+len<=n;l++){int r=l+len-1;d[l][r]=Long.MAX_VALUE;for(int k=l;k<r;k++){long v=d[l][k]+d[k+1][r]+p[r+1]-p[l];if(v<d[l][r]){d[l][r]=v;split[l][r]=k;}}}
        List<int[]> plan=new ArrayList<>();if(n>0)mergePlan(0,n-1,split,plan);return Map.of("cost",n==0?0L:d[0][n-1],"plan",plan);
    }
    static void mergePlan(int l,int r,int[][]s,List<int[]>out){if(l==r)return;int k=s[l][r];mergePlan(l,k,s,out);mergePlan(k+1,r,s,out);out.add(new int[]{l,k,r});}
    static Map<String,Object> tree(Map<String,Object>x){
        int n=num(x.get("n"));if(n==0)return Map.of("weight",0L,"selected",List.of());
        int[] head=new int[n],to=new int[2*(n-1)],next=new int[to.length];Arrays.fill(head,-1);int e=0;
        for(Object o:list(x,"edges")){List<?> a=(List<?>)o;int u=num(a.get(0)),v=num(a.get(1));to[e]=v;next[e]=head[u];head[u]=e++;to[e]=u;next[e]=head[v];head[v]=e++;}
        int[] parent=new int[n],order=new int[n];Arrays.fill(parent,-2);parent[0]=-1;order[0]=0;int size=1;
        for(int i=0;i<size;i++){int u=order[i];for(int h=head[u];h!=-1;h=next[h]){int v=to[h];if(v!=parent[u]){parent[v]=u;order[size++]=v;}}}
        long[] take=new long[n],skip=new long[n];List<?> w=list(x,"weights");
        for(int i=n-1;i>=0;i--){int u=order[i];take[u]+=((Number)w.get(u)).longValue();if(parent[u]>=0){int p=parent[u];take[p]+=skip[u];skip[p]+=Math.max(take[u],skip[u]);}}
        boolean[] chosen=new boolean[n];List<Integer> selected=new ArrayList<>();for(int u:order){chosen[u]=(parent[u]<0||!chosen[parent[u]])&&take[u]>skip[u];if(chosen[u])selected.add(u);}
        return Map.of("weight",Math.max(take[0],skip[0]),"selected",selected);
    }
    static List<Integer> boundaries(int n){List<Integer> a=new ArrayList<>();for(int i=0;i<=n;i++)a.add(i);return a;}
    static boolean equalAt(String t,String p,int i){for(int j=0;j<p.length();j++)if(t.charAt(i+j)!=p.charAt(j))return false;return true;}
    static Map<String,Object> naive(Map<String,Object>x){String t=str(x,"text"),p=str(x,"pattern");List<Integer>a=new ArrayList<>();for(int i=0;i<=t.length()-p.length();i++)if(equalAt(t,p,i))a.add(i);return Map.of("positions",a);}
    static Map<String,Object> kmp(Map<String,Object>x){String t=str(x,"text"),p=str(x,"pattern");int m=p.length();if(m==0)return Map.of("positions",boundaries(t.length()));int[] pi=new int[m];
        for(int i=1,j=0;i<m;i++){while(j>0&&p.charAt(i)!=p.charAt(j))j=pi[j-1];if(p.charAt(i)==p.charAt(j))j++;pi[i]=j;}
        List<Integer>a=new ArrayList<>();for(int i=0,j=0;i<t.length();i++){while(j>0&&t.charAt(i)!=p.charAt(j))j=pi[j-1];if(t.charAt(i)==p.charAt(j))j++;if(j==m){a.add(i-m+1);j=pi[j-1];}}return Map.of("positions",a);
    }
    static Map<String,Object> rk(Map<String,Object>x){String t=str(x,"text"),p=str(x,"pattern");int n=t.length(),m=p.length();if(m==0)return Map.of("positions",boundaries(n));List<Integer>a=new ArrayList<>();if(m>n)return Map.of("positions",a);
        long power=1,ph=0,th=0;for(int j=0;j<m;j++){if(j>0)power=power*31%101;ph=(ph*31+p.charAt(j))%101;th=(th*31+t.charAt(j))%101;}
        for(int i=0;i<=n-m;i++){if(ph==th&&equalAt(t,p,i))a.add(i);if(i<n-m)th=(((th-t.charAt(i)*power)%101+101)*31+t.charAt(i+m))%101;}return Map.of("positions",a);
    }
    static Map<String,Object> suffix(Map<String,Object>x){String t=str(x,"text");int n=t.length();if(n==0)return Map.of("sa",new int[0],"lcp",new int[0],"repeated","");
        Integer[] sa=new Integer[n];int[] rank=new int[n];for(int i=0;i<n;i++){sa[i]=i;rank[i]=t.charAt(i);}
        for(int width=1;width<n;width*=2){final int k=width;final int[] r=rank;Arrays.sort(sa,(a,b)->{int c=Integer.compare(r[a],r[b]);return c!=0?c:Integer.compare(a+k<n?r[a+k]:-1,b+k<n?r[b+k]:-1);});int[] nr=new int[n];for(int i=1;i<n;i++){int a=sa[i-1],b=sa[i];boolean same=r[a]==r[b]&&(a+k<n?r[a+k]:-1)==(b+k<n?r[b+k]:-1);nr[b]=nr[a]+(same?0:1);}rank=nr;if(rank[sa[n-1]]==n-1)break;}
        int[] pos=new int[n],lcp=new int[n];for(int i=0;i<n;i++)pos[sa[i]]=i;int best=0,start=0;
        for(int i=0,h=0;i<n;i++){int r=pos[i];if(r==0){h=0;continue;}int j=sa[r-1];while(i+h<n&&j+h<n&&t.charAt(i+h)==t.charAt(j+h))h++;lcp[r]=h;if(h>best){best=h;start=i;}if(h>0)h--;}
        return Map.of("sa",sa,"lcp",lcp,"repeated",t.substring(start,start+best));
    }
    static Map<String,Object> aho(Map<String,Object>x){
        List<?> patterns=list(x,"patterns"),texts=list(x,"texts");ArrayList<int[]> go=new ArrayList<>();go.add(new int[95]);int[] term=new int[patterns.size()];int states=1;
        for(int i=0;i<patterns.size();i++){String p=(String)patterns.get(i);int u=0;for(int j=0;j<p.length();j++){int c=p.charAt(j)-32;if(go.get(u)[c]==0){go.get(u)[c]=states++;go.add(new int[95]);}u=go.get(u)[c];}term[i]=u;}
        int[] fail=new int[states];int[] bfs=new int[states];int begin=0,end=0;for(int c=0;c<95;c++)if(go.get(0)[c]!=0)bfs[end++]=go.get(0)[c];
        while(begin<end){int u=bfs[begin++];for(int c=0;c<95;c++){int v=go.get(u)[c];if(v!=0){fail[v]=go.get(fail[u])[c];bfs[end++]=v;}else go.get(u)[c]=go.get(fail[u])[c];}}
        List<long[]> rows=new ArrayList<>();for(Object obj:texts){String t=(String)obj;long[] visits=new long[states];int u=0;for(int i=0;i<t.length();i++){u=go.get(u)[t.charAt(i)-32];visits[u]++;}for(int i=end-1;i>=0;i--){int v=bfs[i];visits[fail[v]]+=visits[v];}long[] row=new long[patterns.size()];for(int i=0;i<row.length;i++)row[i]=visits[term[i]];rows.add(row);}return Map.of("counts",rows);
    }
}
