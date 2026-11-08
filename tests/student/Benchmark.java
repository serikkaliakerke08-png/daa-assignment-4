import java.util.*;
public class Benchmark {
 static volatile Object sink;
 static Object run(String alg,Map<String,Object>x){switch(alg){case "naive":return Algorithms.naive(x);case "kmp":return Algorithms.kmp(x);default:return Algorithms.rk(x);}}
 public static void main(String[]args){
  System.out.println("case,n,m,algorithm,run1_ms,run2_ms,run3_ms,median_ms");
  for(String kind:new String[]{"random","repetitive"})for(int n:new int[]{500,1500,3000}){
   int m=n/5;Random r=new Random(2537+n);StringBuilder t=new StringBuilder(),p=new StringBuilder();
   for(int i=0;i<n;i++)t.append(kind.equals("random")?(char)('a'+r.nextInt(4)):'a');
   if(kind.equals("random"))p.append(t.substring(n/3,n/3+m));else {p.append("a".repeat(m-1));p.append('b');}
   Map<String,Object>x=Map.of("text",t.toString(),"pattern",p.toString());Object expected=Algorithms.naive(x);
   for(String alg:new String[]{"naive","kmp","rk"}){
    if(!expected.equals(run(alg,x)))throw new AssertionError("mismatch");
    for(int w=0;w<100;w++)sink=run(alg,x);
    double[] ms=new double[3];for(int j=0;j<3;j++){long start=System.nanoTime();for(int k=0;k<30;k++)sink=run(alg,x);ms[j]=(System.nanoTime()-start)/1e6/30;}
    double[] sorted=ms.clone();Arrays.sort(sorted);System.out.printf(Locale.ROOT,"%s,%d,%d,%s,%.6f,%.6f,%.6f,%.6f%n",kind,n,m,alg,ms[0],ms[1],ms[2],sorted[1]);
   }
  }
 }
}
