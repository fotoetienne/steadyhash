# Steady Hash
*Stable Hashing implementations in Clojure[Script]*

[![Clojars Project](https://img.shields.io/clojars/v/steadyhash.svg)](https://clojars.org/steadyhash)
[![Build Status](https://travis-ci.org/fotoetienne/steadyhash.svg?branch=master)](https://travis-ci.org/fotoetienne/steadyhash)

Hash tables allow deterministic mapping of keys to velues.
Stable hashing is a special kind of hashing such that when a hash table is resized, only K/n keys need to be remapped on average, where K is the number of keys, and n is the number of slots.
In contrast, in most traditional hash tables, a change in the number of array slots causes nearly all keys to be remapped because the mapping between the keys and the slots is defined by a modular operation.
This is useful for tasks such as load balancing work among servers, or coordinating distributed storage.

There are two algorithms implemented in the library:

 - Rendezvous Hashing
 - Maglev Hashing

## Applications

Stable hashing can be used to decide
  - Storage node in storage system or database
    - If buckets are “storage nodes”, we can use hashing so readers and writers select the same storage locations for the same names
  - Proxy server that has a cache
    - If buckets are “caching servers”, we can use hashing to maximize reuse of the same caching servers for the same URLs
  - Task assignment in distributed computing

## Rendezvous
Simple algorithm for stable hashing, also known as Highest Random Weight (HRW).

https://en.wikipedia.org/wiki/Rendezvous_hashing

  - Each nodes assignments remain constant when view is the same
  - When a node enters/leaves, most buckets remain with the same node. i.e.:
    - New nodes take a few assignments from each of the existing nodes.
    - Assignments belonging to dropped nodes are divied up among remaining nodes.
  - Only piece of information exchanged is peer view
  - Remains exactly as consistant as the peer view
  - Could easily be modified to assign N nodes to each bucket"

## Maglev Hash
Consistent hashing using the "Maglev Hash" method as described in Google's 2016 Maglev paper.

  - Designed for cases where a lookup table is necessary because the number of nodes is high and latency needs to be minimal.
  - Creates a lookup table where assignment differs among nodes by at most one.
  - We choose the size of the lookup table to be > N * 100 (where N is the number of nodes) to ensure at most a 1% difference in hash space assigned to nodes.
  - See Section 3.4: [Maglev: A Fast and Reliable Software Network Load Balancer](http://static.googleusercontent.com/media/research.google.com/en//pubs/archive/44824.pdf)

## Usage
### Rendezvous hashing

    (require '[stable-hash.rendezvous :as r])

Given a list of nodes,

    (def nodes [:a :b :c :d])

find the assigned node for a given item.

    (r/highest-random-weight nodes :foo)
    ;; => :c

Assignment will be distributed evenly amongst nodes.

    (->> (pmap (partial highest-random-weight nodes) (range 4e5))
      frequencies)
    ;; => {:a 99358, :b 99871, :c 100300, :d 100471}

You can create a lookup table for faster lookups.

    (def table (m/populate nodes))

Look up values in the table.

    (m/lookup table :foo)
    ;; => :a
    (m/lookup table :bar)
    ;; => :b

The downside to a lookup table is that assignment will be somewhat less equal amongst nodes.

    (frequencies table)
    ;; => {:a 109, :b 99, :c 109, :d 84}

    (->> (pmap (partial lookup table) (range 4e5))
      frequencies)
    ;; => {:a 108887, :b 98564, :c 109088, :d 83461}

### Maglev hashing

Maglev hashing is designed for cases where a lookup table is necessary because the number of nodes is high and latency needs to be minimal.

    (require '[stable-hash.maglev :as m])

Create a lookup table from a list of nodes.

    (def table (m/populate [:a :b :c :d]))

Look up values in the table.

    (m/lookup table :foo)
    ;; => :d
    (m/lookup table :bar)
    ;; => :c

Maglev is designed to create a lookup table where assignment differs among nodes by at most one.
We choose the size of the lookup table to be > N * 100 (where N is the number of nodes) to ensure at most a 1% difference in hash space assigned to nodes.

    (frequencies table)
    ;; => {:a 101, :b 100, :c 100, :d 100}

    (->> (pmap (partial lookup table) (range 4e5))
      frequencies)
    ;; => {:a 100924, :b 99901, :c 99628, :d 99547}

### Requirements

As steadyhash uses Clojure's reader conditionals, steadyhash is dependent on both Clojure 1.7 and Leiningen 2.5.2 or later.

## TODO

 - [x] Rendezvous implementation
 - [x] Maglev implementation
 - [ ] Churn test
 - [ ] Performance tests
 - [ ] Weighted Rendezvous implementation [Source](http://www.snia.org/sites/default/files/SDC15_presentations/dist_sys/Jason_Resch_New_Consistent_Hashings_Rev.pdf)

## References

 - Maglev - http://static.googleusercontent.com/media/research.google.com/en//pubs/archive/44824.pdf
 - Rendezvous - http://www.eecs.umich.edu/techreports/cse/96/CSE-TR-316-96.pdf

## License

Copyright © 2016 Stephen Spalding

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
