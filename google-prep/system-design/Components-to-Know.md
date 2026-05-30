# Database
  - MySql, Postgres - tables, relational data
  - Mongo DB - document based storage
  - Redis - simple key value pairs
  ## Tips
    - Don't get caught in SQL vs NoSQL debate.. most  modern dbs can scale well and maintain data integrity
    - the real question is what kind  of read write the application needs and how to make it relatable to usecase.

# Cache
  - Redis
  - They store frequently accessed data so you dont have to fetch from db everytime thus making it faster.
  ## challenges
    - keep data fresh
    - data validity
  ## Note
    - Adding them is gonna keep the system fast for repeated requests but expect additional complexities in the overall design and maintenance.

# Message Queue
  - Asynchronous Communication buffer 
  - kafka, RabbitMQ 
  - one service can drop it in queue and other system can pick it up when ready instead of waiting for each other
  - helps handle traffic spikes and helps keep system running even if one service crashes
  ## Challenges
   - Making sure msgs actually get delivered in first place
    - also sometimes gets delivered once.. sometimes multiple times.. so duplication.. need effective strategies

# Load Balancers
  - Traffic Control
  - sends it to different servers so any server won't get overwhelmed
  - decides on which server or service the request is routed to

# Blob Storage - media and latge blobs
  - media, unstructured data..

# CDN 
  - Content Delivery Network - server blobs closer to client
  - store copies of blobs across regions closer to user so to minimize latency

