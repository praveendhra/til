# Firestore vs Bigtable

## Firestore (Document Database)
Serverless, auto-scaling document DB (evolved from Datastore).

- **Data model**: Collections → Documents → Subcollections
- **Queries**: Rich queries, composite indexes, real-time listeners
- **Transactions**: Multi-document ACID transactions
- **Scaling**: Automatic, handles millions of concurrent clients
- **Pricing**: Per-read/write/delete + storage
- **Offline support**: Mobile SDKs with offline cache

```python
doc = db.collection('users').document('user123')
doc.set({'name': 'Pravi', 'email': 'pravi@example.com'})

# Real-time listener
doc.on_snapshot(lambda doc_snapshot, changes, read_time: print(doc_snapshot))
```

## Bigtable (Wide-Column)
Managed HBase. For massive scale analytics.

- **Data model**: Row key → Column families → Columns → Cells (versioned)
- **Queries**: Row key prefix scan only (no secondary indexes)
- **Transactions**: Single-row only
- **Scaling**: Manual (add/remove nodes), handles billions of rows
- **Pricing**: Per-node ($0.65/hr) + storage
- **Latency**: Single-digit ms at any scale

**Use for**: IoT time-series, financial tick data, ad-tech, genomics

## Decision Guide
| Need | Choose |
|------|--------|
| App data with complex queries | Firestore |
| Mobile/web real-time sync | Firestore |
| Time-series at massive scale | Bigtable |
| Analytics pipeline (Dataflow → storage) | Bigtable |
| < 1TB data | Firestore |
| > 10TB data, simple access | Bigtable |
