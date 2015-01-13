SDGen
=====

**Table of Contents**

- [Introduction](#introduction)
- [Architecture](#architecture)
  - [Scan Phase: Characterizations](#scan-phase) 
  - [Generation Phase](#generation-phase)
- [How to Extend SDGen](#how-to-extend-sdgen)
- [A Data Mimicking Method](#a-data-mimicking-method)
  - [Method Rationale](#method-rationale)
  - [Implementation](#implementation)
- [A "Hello World" Test](#a-"hello-world"-test)
- [Integration with Benchmarks](#integration-with-benchmarks)
- [Licensing](#licensing)
- [Contact](#contact)


# Introduction
SDGen is a synthetic data generator for storage benchmarks. The objective
of this framework is to enable users creating methods to generate realistic
data to feed storage benchmarking tools.

# Architecture
SDGen is designed to capture characteristics of data that can affect the outcome of applying data reduction techniques
on it. As we show next, SDGen works in two phases: A priming scan phase which build data characterizations
to be used by a subsequent generation phase.

# Scan Phase
To capture the characteristics of data, SDGen implements
a two-level scan phase: chunk level and dataset level.
Many compression algorithms (e.g. lz4, zlib) partition
the input data stream into chunks, and apply compression
separately for every chunk; such algorithms
try to exploit redundancy which stems from locality of
data (repetitions, common bytes) while minimizing the
size of their internal data structures. Therefore, a central
element in our design is the chunk characterization
(CC). A CC is a user-defined module that contains
the necessary information for every data chunk. SDGen
scans a given dataset by splitting its contents into chunks
(e.g., from 8KB to 128KB, configurable by the user) that
are characterized individually (step 1 and 2, Fig. 3). 

In a higher level, SDGen builds dataset characterizations
(DC), which provide a more holistic characterization.
In the current version of SDGen, DCs store the
deduplication ratio of the entire dataset as well as a list
of all the previously generated CCs.
To support the above scans, SDGen applies two
modules: Chunk scanners and Dataset scanners.
These modules are loaded from the configuration in
a manager class (DataScanner), which processes the
dataset, and concurrently uses it as input for the scanners
in order to build the characterization. The DataScanner
life-cycle appears in Fig. 3.

The scan phase ends by persistently storing a DC (step
4, Fig. 3). SDGen also includes a way of transparently
storing and loading DCs, enabling users to easily creating
and sharing them.

<p align="center">
  <img width="500" src="http://ast-deim.urv.cat/web/images/software/SDGen/sdgen_architecture.png">
</p>

# Generation Phase
Once in possession of a DC, users may load it in SDGen
to generate synthetic data similar to the original dataset.

The heart of the generation phase is the generation algorithm.
This algorithm is designed by the user and receives
as input a CC filled with the data characteristics
captured by chunk scanners (see Section 4.3). Since CCs
are read-only and independent of each other, the generageneration
algorithm can utilize parallelism for faster data generation.
A module called DataProducer orchestrates
the content generation process. The DataProducer
is also responsible for taking into account dataset-level
characteristics during the generation process. Currently,
this is mainly used for generating duplicated data. However,
we concentrate on data compression, leaving the
analysis of deduplicated data for future work.

The DataProducer module generates data using two
API calls: getSynData() and getSynData(size).
The first call retrieves entire synthetic chunks with the
same size as the original chunk. This is adequate for
generating large amounts of content, such as file system
images. The second call specifies the size of the synthetic
data to be generated. This call is an optimization to avoid
wasting synthetic data in benchmarks that require small
amounts of data per operation (e.g. OLTP, databases).
Technically, successive executions of this method will retrieve
subparts of a synthetic chunk until it is exhausted
and a new one is created.

# How to Extend SDGen
SDGen enables users to integrate novel data generation
methods in the framework. To this end, one should follow
three steps:

1. Characterization: Create a CC extending the
AbstractChunkCharacterization class. This
user-defined characterization should contain the required
information for the data generation process.

2. Scanners: Provide the necessary scanners to fill
the content of CCs and DCs during the scan
process. Chunk-level scanners should extend
from AbstractChunkScanner and implement the
method setInfo, to set the appropriate CC fields.

3. Generation: Design a data generation algorithm
according to the properties captured during the
scan phase. This algorithm should be embedded
in a module extending AbstractDataGenerator,
to benefit from the parallel execution offered by
DataProducer. Concretely, a user only needs to
override the fill(byte[]) method to fill with synthetic
data the input array.

SDGen manages the life-cycle of the user-defined
modules to scan/generate data, which are loaded from
a simple configuration file. 

# A Data Mimicking Method
Next, we describe a practical example of how to make use of the framework integrating
novel data mimicking mehtods. Concretely, our method implemented in SDGen tries to
emulate the compression ratios and times of a target dataset for a variety of compression
engines (lz4, zlib,...). 

# Method Rationale

Mimicking data for compressors requires first to understand
how they compress data. Technically, the compressors
we target encode (i) each repetition by length and
back point distance, and (ii) bytes or literals. Moreover,
compressors using Huffman codes encode lengths, back
point distances and bytes based on their frequencies.

<p align="center">
  <img width="500" src="http://ast-deim.urv.cat/web/images/software/SDGen/sdgen_method_rationale.png">
</p>

Given that, we identified two main characteristics that
affect the behavior of compression algorithms: repetition
length distribution and frequencies of bytes. First, compression
algorithms exploit the existence of n repeated
sequences within a data chunk by substituting the remaining
n-1 repetitions by pointers to the first one.
However, we empirically found that the distribution of
repetitions tends to follow a power-law. As can
be seen in the previous figure (left), the majority of repetitions are
short ones (< 10 bytes). Consequently, compression algorithms
perform many operations to exploit these small
repetitions, which in turn has an impact on performance.

Second, several compression algorithms resort to encoding
schemes (e.g. Huffman coding) to represent the
bytes within a data chunk in the shortest way possible.
In essence, the encoding associates identifiers to bytes so
that the most frequent bytes are represented by the lower
(shorter) identifiers, saving storage space. Therefore, to
generate synthetic content we must also take into account
the distribution of bytes during the scan process. As we
observe in the previous figure (right), the skew in the distribution
of byte frequency changes significantly from text files to
random-like data (PDFs). This may impact the encoding
process speed. These observations guided the design of
our mimicking method for compression algorithms.

# Implementation

Following the first point mentioned in "How to Extend SDGen",
to capture the aforementioned data characteristics, in our
method every Chunk Characterization (CC) contains:

- Byte frequency histogram. We build a histogram that
relates the bytes that appear in a data chunk with their
frequencies, encoding it as a <byte, frequency> map
that we use to generate synthetic data that mimics this
byte distribution. This information is key to emulate the
entropy of the original data, among other aspects.

- Repetition length histogram. To encode this histogram,
we use a map whose keys represent the length of repetitions
found in a chunk and the values are frequencies of
repetitions of a given length. Our aim is to mimic the distribution
of repetition lengths in the synthetic data. For
repetition finding, we use the zlib’s Deflate algorithm.

- Compression ratio. Every CC also includes the compression
ratio of the original data chunk. In the generation
phase, SDGen will try to create a synthetic chunk
with similar compressibility.

Our CC module (MotifChunkCharacterization) is located in package
com.ibm.characterization.user and extends
AbstractChunkCharacterization, as other data mimicking methods should do.

Second, we need specific scanners to fill CCs when scanning a targeted
dataset. To this end, we created two scanners: DataCompressibilityScanner
that extracts from a data chunk the compression ratio and the
repetition length histogram, and AlphabetScanner that gets the
distribution of bytes within a data chunk. Both scanners are located
at package com.ibm.scan.user and extend from AbstractChunkScanner,
as other user-defined scanners should do.

Finally, we created a data generation algorithm that is called
by DataProducer every time a new synthetic chunk of data is created.
Very succintly, the algorithm interleaves random and repeated data whose
lengths and byte distributions are drawn by the histograms captured
in the CC that represents a real data chunk. The ratio at which
random and repeated sequences are interleaved is dictated by the
compression ratio of the scanned data chunk. This algorithm is
implemented in the class MotifDataGenerator and extends AbstractDataGenerator. As other
user-defined data generation algorithms, this class is located at
package com.ibm.generation.user.

# A "Hello World" Test
Next, we describe how to run a simple test with the framework (com.ibm.test.HelloWorldTest).
This tests consists in the following parts that show the usability of the framework:
1.- The test scans a dataset.
2.- Once the scan finishes, we persist a dataset characterization that can be shared with others.
3.- We load the characterization to check that loading a characterization works
4.- We load the characterization in the DataProducer to generate a synthetic dataset similar to the original one.
5.- Finally, we compare the compression ratio and time of these datasets for various compression engines chunk-by-chunk.

As a startign point, you can run this test with well-known datasets, such as the Canterbury/Calgary corpus (http://corpus.canterbury.ac.nz) or the Silesia corpus (http://www.data-compression.info/Corpora/SilesiaCorpus/index.htm).

Finally, we provide various dataset characterizations that had been build from scanning real datasets (/dataset_characterizations). Of course, you can scan, build and share new dataset characterizations from your own data or other datasets. That's the point of the framework.

# Integration with Benchmarks
At this moment, we provide modules for a basic integration of SDGen as a data generation layer with Impressions 
(http://research.cs.wisc.edu/adsl/Software/Impressions) and LinkBench (https://github.com/facebook/linkbench).

# Issue Tracking
We use the GitHub issue tracking.

# Licensing
SDGen is licensed under the GPLv3. Check [LICENSE](LICENSE) for the latest
licensing information.

# Contact
Visit http://ast-deim.urv.cat or http://iostack.eu for contact information.
