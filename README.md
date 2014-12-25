SDGen
=====

**Table of Contents**

- [Introduction](#introduction)
- [Architecture](#architecture)
  - [Scan Phase: Characterizations](#scan-phase) 
  - [Generation Phase](#generation-phase)
- [How to Extend SDGen](#extension)
- [A Data Mimicking Method](#mimicking)
- [Integration with Benchmarks](#integration)
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
  <img width="500" src="https://raw.github.com/iostackproject/SDGen/master/res/sdgen-architecture.png">
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

#How to Extend SDGen
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

# Integration with Benchmarks
At this moment, we integrated SDGen as a data generation layer with Impressions 
(http://research.cs.wisc.edu/adsl/Software/Impressions) and LinkBench (https://github.com/facebook/linkbench).


# Issue Tracking
We use the GitHub issue tracking.

# Licensing
SDGen is licensed under the GPLv3. Check [LICENSE](LICENSE) for the latest
licensing information.

# Contact
Visit www.iostack.eu for contact information.
