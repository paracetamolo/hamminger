# Compute Hamming distances over portions of genomes

This code provides a simple way to compute Hamming distances over portions of genomes for all 2504 users in the [1000 Genomes Project](http://www.internationalgenome.org/).

The code comes with a `script.sh` that shows an example of usage. Note that it is very disk intensive and if run as-is requires ~87GB.

## Example
Download a compressed VCF file from the project [ftp](ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/release/20130502/), for example [chromosome 22](ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/release/20130502/ALL.chr22.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz) which is the smallest at 205 MB.
Uncompress the file and run the script.

```bash
sudo aptitude install libcommons-io-java
wget ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/release/20130502/ALL.chr22.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz
gunzip ALL.chr22*
mkdir data
mv ALL.chr22* data/chr22.vcf
./run.sh data/chr22.vcf
```

The script computes sample sizes 100, 1000, 10.000 and for each shows some statistics on the probability of collision: average, variance, minimum, 1st quartile, median, 3rd quartile, maximum. Most of the actual work of the script is dumped to file.
```
collisions for size 100
[0.19262522, 0.008270801, 0.033016175, 0.15280944, 0.19697355, 0.2579172, 0.93151367]
collisions for size 1000
[0.031221937, 2.3436794E-4, 0.0075452225, 0.02068054, 0.027477818, 0.037648186, 0.22095276]
collisions for size 10000
[0.0034555113, 1.850459E-6, 0.0013029928, 0.0025280614, 0.0030661847, 0.0040463037, 0.01641739]
```

## Details
The script simply calls the three main commands of Main.java:

1. `hamming`: takes a vcf file `chr22.vcf`, a sample size `100` and the number of cores to use (defaut 3).

	The vcf file is split in chunks of the given size and a distance matrix is populated with the pairwise hamming distances of all users.
	For each sample i, the distance matrix is saved to `chr22_K100_N$i`. 
	The probability of collision for each user is saved to `chr22_K100_prob`, one line per sample.

	Note: to increase the parallelism edit the `CORES` variable in `run.sh`.
	
2. `merge`: takes a distance matrix file `chr22_K100_N1` and a size `10`.

	Sums 10 distance matrices of chunk size `100` to obtains the matrix of chunk size `1000`.

3. `collision`: takes the collisions probability file `chr22_K100_prob`.

	For each user (for each column), computes average over samples and saves it to `chr22_K100_probs_avg`.
	Additionally prints some statistics over all probability of collision of all users:
   `[average, variance, minimum, 1st quartile, median, 3rd quartile, maximum]`
