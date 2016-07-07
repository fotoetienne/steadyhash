# Steady Hash

Stable Hashing implementations in Clojure[Script]

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

## Maglev Hash
Consistent Hashing using the "Maglev Hash" method as described in Google's 2016 Maglev paper.

See Section 3.4:
http://static.googleusercontent.com/media/research.google.com/en//pubs/archive/44824.pdf

## Rendezvous
Also know as Highest Random Weight (HRW)

https://en.wikipedia.org/wiki/Rendezvous_hashing

## Weighted Rendezvous Hashing
Coming soon...

## Usage

### Maglev hashing

  (require '[stable-hash.maglev :as m])

Create a lookup table from a list of nodes

  (def table (m/populate [:a :b :c :d]))

Look up values in the table

  (m/lookup table :foo)
  ;; => :d
  (m/lookup table :bar)
  ;; => :c

See that the table is evenly distributed

  (frequencies table)
  ;; => {:c 100, :b 100, :d 100, :a 101}


## TODO

 - [ ] Churn test
 - [ ] Rendezvous implementation
 - [ ] Performance tests

## References

Comparison of algorithms
http://www.snia.org/sites/default/files/SDC15_presentations/dist_sys/Jason_Resch_New_Consistent_Hashings_Rev.pdf


## License

Copyright © 2016 Stephen Spalding

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
