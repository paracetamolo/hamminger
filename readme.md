# Compute Hamming distances over portions of genomes

This code provide a simple way to compute Hamming distances over portions of genomes for all 2504 users in the [1000 Genomes Project](http://www.internationalgenome.org/).

Download a compressed VCF file from the project [ftp](ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/release/20130502/), for example [chromosome 22](ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/release/20130502/ALL.chr22.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz) which is the smallest at 205 MB.
Uncompress the file and run the script.

```bash
mkdir data; cd data
wget ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/release/20130502/ALL.chr22.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz
gunzip ALL.chr22* > chr22.vcf
cd ..
./run.sh chr22.vcf
```

## Details
The script simply calls the three main commands of Main.java:

1. `hamming`: takes a vcf file `chr22.vcf` and a sample size `100`.

	The vcf file is split in chunks of the given size and a distance matrix is populated with the pair-wise hamming distances of all users.
	For each sample i, the distance matrix is saved to `chr22_K100_N$i`. 
	The probability of collision for each user is saved to `chr22_K100_prob`, one line per sample.

2. `merge`: takes a distance matrix file `chr22_K100_N1` and a size `10`.

	Sums 10 distance matrices of chunk size `100` to obtains the matrix of chunk size `1000`.

3. `collision`: takes the collisions probability file `chr22_K100_prob`.

	For each user (for each column), computes average over samples and saves it to `chr22_K100_probs_avg`.
	Additionally prints some statistics over all distances of all users:
   `[average, variance, minimum, 1st quartile, median, 3rd quartile, maximum]`
