# Bloom Filters - Probabilistic Data Structures

## What Is It?
A space-efficient probabilistic set that tells you:
- **Definitely NOT in set** (100% accurate)
- **Probably in set** (small false positive rate)

## How It Works
1. Bit array of size m, initialized to all 0s
2. k hash functions
3. **Insert**: Hash item k times → set those k bits to 1
4. **Lookup**: Hash item k times → if ALL bits are 1, probably in set

## Properties
- No false negatives (if it says "not in set", it's true)
- Possible false positives (tunable with m and k)
- Cannot delete elements (use Counting Bloom Filter for that)
- Very space efficient: ~10 bits per element for 1% false positive rate

## Real-World Uses
- **Chrome**: Checks URLs against malicious URL list
- **Cassandra/HBase**: Skip disk reads for non-existent keys
- **Medium**: Avoid recommending already-read articles
- **CDNs**: One-hit-wonder filter (only cache after 2nd request)

## Cloud Example
```python
# Redis has built-in Bloom Filter support
# BF.ADD myfilter "hello"
# BF.EXISTS myfilter "hello"  → 1
# BF.EXISTS myfilter "world"  → 0 (definitely not in set)
```
