# Bloom Filters

## What Is It?
A space-efficient probabilistic data structure that tells you:
- "Definitely NOT in the set" (100% accurate)
- "Probably in the set" (may have false positives)

## How It Works
1. Create a bit array of size `m`, all set to 0
2. Use `k` hash functions
3. To add: hash element with each function, set those bits to 1
4. To check: hash element, check if ALL bits are 1

## False Positive Rate
$$P(fp) = \left(1 - e^{-kn/m}\right)^k$$
Where: m = bit array size, k = hash functions, n = inserted elements

## Use Cases
- **CDN**: Check if content is cached before hitting origin
- **Database**: Check if a key exists before disk lookup (LSM trees)
- **Spam filters**: Quick check if email is spam
- **Web crawlers**: Track already-visited URLs

## Parameters for 1% FPR
| Elements | Bits Needed | Hash Functions |
|----------|-------------|----------------|
| 1M       | ~9.6M bits  | 7              |
| 10M      | ~96M bits   | 7              |
| 100M     | ~960M bits  | 7              |

## Limitations
- Cannot delete elements (use Counting Bloom Filter instead)
- Cannot list elements
- Size must be determined upfront
