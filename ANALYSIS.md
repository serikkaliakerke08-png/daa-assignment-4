# Assignment 4 — Dynamic Programming and String Processing

## Environment and commands

Language: Java; OpenJDK/Javac 17.0.20, Python 3.12.14 launcher. Measurements were performed in a Linux 6.18.57 x86-64 container, AMD EPYC 9V74, 9 visible logical CPUs and 9967 MiB visible RAM. Shared-container scheduling and JIT compilation affect short timings. The Java solution requires no external libraries.

Run from the repository root (Windows PowerShell uses the same commands):

```text
python tools/run.py --setup
python tools/run.py --check
python tests/student/check.py
javac --release 17 -encoding UTF-8 -d Java/build Java/source/Json.java Java/source/Main.java Java/source/Algorithms.java
java -Xmx384m -cp Java/build Main
javac --release 17 -cp Java/build -d Java/build tests/student/Benchmark.java
java -Xmx384m -cp Java/build Benchmark
```

Main reads one UTF-8 JSON request per line and prints one JSON object per line. For a request file, use `python tools/run.py --input YOUR_REQUEST_FILE.jsonl`, substituting its actual path. The benchmark command can be redirected to `tests/student/benchmark.csv`.

## LCS

`d[i][j]` is the optimal subsequence length of prefixes a[0..i) and b[0..j). Empty prefixes have length zero. Process increasing i and j. Equal final characters extend an optimal solution for the two shorter prefixes. Otherwise an optimum omits at least one final character, giving max(d[i-1][j],d[i][j-1]). Induction on prefix lengths proves optimality: the transition covers an optimal choice and every candidate is attainable.

Reconstruct from (n,m): a matching pair contributes its character and decreases both indices; otherwise follow a predecessor of equal optimal value. Prefer decreasing i on a tie. Reverse the collected characters. Each step preserves the remaining target length, hence reconstruction yields a common subsequence of optimal length, including the empty answer. Time and auxiliary storage O((n+1)(m+1)); output O(length+1), input O(n+m+1). The noncontiguous example and small exhaustive subsequence oracle catch confusion with substring search.

## Adjacent-block merging

`d[l][r]` is the cheapest complete merge of closed original interval [l,r]. Single blocks cost zero. Prefix sums give its combined size in O(1). Enumerate interval lengths increasingly and all last splits k: d[l][k]+d[k+1][r]+sum(l..r). Both subintervals are shorter and already computed. Every legal last merge has such a split; replacing either child with its optimum cannot increase the total. Induction proves the minimum is exact.

Store the earliest minimizing split. Recursive postorder emits the two child plans before [l,k,r], so both adjacent intervals exist when used. Recursion depth is at most 200. Empty and singleton arrays return an empty plan and zero cost. Long prefix sums and DP costs prevent 32-bit overflow. Time O(n^3+1), auxiliary O(n^2+1) including the O(n) reconstruction stack, output O(n+1), input O(n+1). Exhaustively enumerated small merge orders verify the optimum; the 200-block billion-size case checks arithmetic and chronology.

## Maximum-weight independent set on a tree

Build flat adjacency arrays, root at vertex 0, and obtain parent-before-child order iteratively. Reverse order is postorder. `take[u]` includes u and forces every child to be skipped; `skip[u]` excludes u and permits the maximum of each child's two states. Each subtree interacts with its parent only through whether its root is chosen, so these exhaustive disjoint cases prove correctness inductively. Negative weights and zero skip bases allow the empty set.

During parent-before-child reconstruction, choose u only if its parent is unchosen and take[u]>skip[u]; prefer skipping on ties. A chosen parent forces the correct skip state, while an unchosen parent allows a best state. Each vertex occurs once. Iteration avoids stack failure on a 100000-vertex chain. Long totals support sums up to 10^14. Time and auxiliary memory O(n+1), selected output O(n+1), input O(n+1). Exhaustive subset oracles and the full-depth chain check both optimum and witness validity.

## Naive search

For each start 0 through n-m inclusive, compare characters left to right and emit only complete matches. These are all and only possible starts. Increment by one to keep overlaps. An empty pattern requires zero comparisons at every boundary 0..n, including the last; a longer pattern creates no valid starts. Time O(n*m+n+1), O(1) auxiliary memory, O(z+1) output memory and O(n+m+1) input storage. Empty-pattern and overlapping cases detect skipped boundaries and nonoverlapping search bugs.

## KMP

`pi[i]` is the longest proper prefix of pattern[0..i] that is also its suffix. Build pi left to right, repeatedly falling back to pi[j-1] after a mismatch; every shorter possible matching border is on this chain. During scanning, j is the length of the pattern prefix matching a suffix of the processed text. Matching advances j, and fallback preserves this invariant without moving the text backwards. A full match ends at i, so emit i-m+1, then use pi[m-1] to retain overlaps. Return all boundaries for an empty pattern before indexing it.

The invariant proves soundness; fallback removes only impossible candidates, proving completeness. In each loop j increases at most once per character and decreases only after an increase, giving amortized O(n+m+z+1) time. Auxiliary O(m+1), output O(z+1), input O(n+m+1). Repeated letters test overlap fallback and the 200000-character stress test exercises linear scanning.

## Exact Rabin–Karp

Hash a length-m window using ASCII codes, base 31, modulus 101, with highest power on the left. Maintain power=31^(m-1) mod 101. Remove the departing character times power, normalize the remainder to [0,100], multiply by 31 and add the entering character. Algebra gives the next window's polynomial hash. Unequal hashes exclude a match; equal hashes invoke character-by-character verification, so collisions never become false matches. Handle m=0 and m>n before hashing.

For 1<=m<=n, time is O(n+m+c*m), where c counts equal-hash windows. A bound over all domains is O(n*m+n+m+1). This exact version is not guaranteed linear: many candidates may each need m comparisons. Rolling state uses O(1) auxiliary memory, output O(z+1), input O(n+m+1). The explicit ab/dj collision (both hash to 75) must return only position 0 in abdj. Random comparisons also exercise negative-remainder normalization.

## Suffix array: prefix doubling

Initially ranks order single ASCII characters. For width k, compare (rank[i],rank[i+k]), using -1 beyond the text. These pairs order the first 2k characters of suffixes. Java `Arrays.sort(Integer[], comparator)` comparison-sorts indices; no suffix strings are materialized. Assign new contiguous equal-pair ranks. Induction on doubled prefix length proves the final ordering is lexicographic suffix order. Stop early when all ranks are distinct. n=1 already has its unique order; n=0 returns empty arrays.

There are O(log(n+1)) rounds of O(n log(n+1)) comparisons with constant-time rank access, giving O(n log^2(n+1)) time. Arrays, boxed indices and object-sort temporary storage are all O(n+1). Output SA and LCP occupy O(n+1); input occupies O(n+1).

## Kasai LCP and repeated substring

Use the inverse suffix order to find each suffix's predecessor. Compare from reused h; after moving the text start one place, at least h-1 characters can be reused, so decrement h by one. The suffix with no predecessor resets h=0. The total increases of h are O(n), because decreases total O(n), proving linear time and O(n+1) auxiliary memory. lcp[0]=0, and lcp[r] compares exactly sa[r-1] and sa[r].

Suffixes sharing a prefix form a contiguous lexicographic interval. Thus any repeated substring is a prefix of adjacent suffixes somewhere in that interval, and maximum adjacent LCP is a longest repeat. Keep the first strict maximum encountered in text-position order; any tied answer is valid, including overlaps. Small direct-suffix and direct-LCP oracles verify sorting and Kasai, and 50000 equal characters stress long shared prefixes.

## Aho–Corasick

Build a trie with 95 ASCII transitions per allocated node, sharing terminal nodes for duplicate patterns. A BFS sets failure links to the longest proper suffix represented in the trie; missing transitions inherit the failure state's transition. The root handles missing letters by remaining at root. BFS dependencies are ready because failures have smaller depth. The scanning state is consequently the longest dictionary-prefix suffix of the processed text.

For each independent text, allocate fresh long visit counts and start at root. Count visits during scanning, then propagate in reverse BFS order from each state to its failure parent. Every ending occurrence of a pattern corresponds to a scanned state whose failure chain contains that pattern's terminal. Reverse propagation counts exactly these visits, including overlaps and suffix patterns, without enumerating individual matches. Read terminal counts in original dictionary order; duplicates obtain identical values. Empty dictionaries give an empty row per text; empty texts give zero rows; no texts gives no rows. All arrays are local to the request.

Let L include duplicate pattern lengths, p be dictionary size, and S actual trie states. Transition rows are allocated only when a new trie node is created and held in an ArrayList. With the fixed 95-character alphabet, construction takes O(L+95*S+p) time and O(95*S+p) auxiliary memory, including amortized list growth. Per text, scanning and reverse propagation take O(|text|+S+p), with O(S+p) fresh working/row space. Retained output uses O(q*(p+1)+1). Input storage is O(L+total text length+p+q+1). No inherited output lists or per-match records are copied. Duplicate/suffix patterns and repeated texts check reset; a 10000-duplicate dictionary with 200000 text characters checks aggregation.

## Own tests and results

`python tests/student/check.py` passed 643 small oracle/boundary cases and 7 full-domain stress cases, seed 2537. The official launcher passed setup, all 44 public cases, and two complete mixed batches in a single process. These observations are local checks, not a claim about hidden grading.

|Command|Fixture / oracle|Expected|Actual|Defect detected|
|---|---|---|---|---|
|lcs|check.py exhaustive subsequences|Optimal length and valid subsequence|PASS|Substring confusion, ties|
|merge|check.py exhaustive legal merge sequences|Minimum cost and executable plan|PASS|Adjacency, chronology|
|tree|check.py exhaustive subsets|Independent optimum|PASS|Negative weights, reconstruction|
|naive/kmp/rk|check.py Python startswith oracle|All starts in order|PASS|Empty, overlap, case, punctuation|
|rk|abdj / ab|[0]|PASS|Unverified collision|
|suffix|check.py sorted suffix oracle and direct LCP|Exact SA and LCP; optimal repeat|PASS|Rank/Kasai indexing|
|aho|check.py independent startswith counts|Counts per original pattern/text|PASS|Duplicates, suffixes, reset|
|stress|check.py analytical large cases|Exact values and valid witnesses|PASS|Deep recursion, limits, 64-bit sums|

## Search comparison

Source: `tests/student/Benchmark.java`; raw measurements: `tests/student/benchmark.csv`. Each input is identical for all three algorithms. Random text uses Java Random(seed=2537+n), alphabet a..d, m=n/5; its pattern is the substring at n/3 of length m, so at least one occurrence exists. Repetitive text is a^n and pattern a^(m-1)b, with no occurrences. Sizes n=500,1500,3000, within the common input domain.

Before timing each configuration, compare complete result maps against naive; small independent Python-oracle tests also verify all three. Warm up each algorithm/configuration with 100 untimed calls. Each of three measurements times 30 calls using System.nanoTime, divided by 30 for milliseconds per call. Keep results in a volatile sink. Timing includes map extraction, algorithm and result construction; excludes compilation, JVM startup, input generation, correctness checks, JSON parsing and serialization. JVM JIT stays enabled; warm-up does not guarantee every compilation is complete.

|Case|n|m|Seed / generator|Algorithm|Run 1 ms|Run 2 ms|Run 3 ms|Median ms|Correct outputs checked|
|---|---:|---:|---|---|---:|---:|---:|---:|---|
|random|500|100|3037|naive|0.029719|0.016545|0.011250|0.016545|yes|
|random|500|100|3037|kmp|0.056468|0.053117|0.048386|0.053117|yes|
|random|500|100|3037|rk|0.062858|0.041967|0.039243|0.041967|yes|
|random|1500|300|4037|naive|0.032676|0.031102|0.029939|0.031102|yes|
|random|1500|300|4037|kmp|0.040387|0.036201|0.039353|0.039353|yes|
|random|1500|300|4037|rk|0.095995|0.100609|0.089028|0.095995|yes|
|random|3000|600|5537|naive|0.073821|0.078416|0.072852|0.073821|yes|
|random|3000|600|5537|kmp|0.120696|0.139500|0.119738|0.120696|yes|
|random|3000|600|5537|rk|0.024439|0.023433|0.024842|0.024439|yes|
|repetitive|500|100|a^n / a^(m-1)b|naive|0.041004|0.043670|0.040265|0.041004|yes|
|repetitive|500|100|a^n / a^(m-1)b|kmp|0.002317|0.002337|0.002313|0.002317|yes|
|repetitive|500|100|a^n / a^(m-1)b|rk|0.003722|0.003682|0.004524|0.003722|yes|
|repetitive|1500|300|a^n / a^(m-1)b|naive|0.426698|0.385564|0.381837|0.385564|yes|
|repetitive|1500|300|a^n / a^(m-1)b|kmp|0.006968|0.008292|0.009289|0.008292|yes|
|repetitive|1500|300|a^n / a^(m-1)b|rk|0.011055|0.011728|0.011061|0.011061|yes|
|repetitive|3000|600|a^n / a^(m-1)b|naive|1.550409|1.526516|1.557618|1.550409|yes|
|repetitive|3000|600|a^n / a^(m-1)b|kmp|0.014546|0.014257|0.013919|0.014257|yes|
|repetitive|3000|600|a^n / a^(m-1)b|rk|0.022488|0.022879|0.022825|0.022825|yes|

On repetitive near-match inputs, naive repeatedly compares almost m characters and grows sharply; KMP avoids repeating that work. RK rejects the uniform windows by hash for these particular patterns, so it is fast here. Random results fluctuate with JIT compilation, tiny timing scope and shared CPU scheduling; the observed rank changes are not proofs of asymptotic behavior. The deliberately small modulus 101 can create many false candidates, each requiring exact verification. Other repetitive or adversarial inputs can therefore make RK quadratic; these measurements must not be generalized to all hashes or workloads.

## Sources and AI assistance

Sources: the official RZhaniya/daa-assignment-4-archive README, docs/guide/CONTRACTS.md, TESTING.md, supplied Java JSON transport, launcher and public validator. The contract defines recurrences, allowed algorithm families, interfaces, limits and collision parameters. Standard prefix DP, interval DP, tree DP, KMP, rolling hash, prefix doubling/Kasai and Aho–Corasick techniques were used.

OpenAI ChatGPT/Codex generated the Java implementation, independent test driver, benchmark harness and this analysis; it executed and inspected local checks and measurements. Verification performed by the assistant includes public checks, exhaustive small oracles, full-domain stress tests and equal-output search benchmarks. The student should review the code, rerun these commands on her own machine, and understand the invariants and reconstruction before submitting; independent student review has not been represented as already completed. No assistance was used on the assessed individual defence quiz as part of this work.
