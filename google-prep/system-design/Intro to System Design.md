#Framework  for System Design
- Requirements
- Core Entities
- API or Interface
- Data Flow
- High-Level Design
- Deep Dives

#Evaluation criteria
- Problem Solving: Identify and prioritize core challenges(Functional requirements)
    ex: when its a ticket booking system.. priotorize on booking flow not authentication.
- Solution Design: Create scalabale architectures with balanced trade-offs.
    the overall design.. to make sure you arrived at the proper solution
- Technical Excellence: Demonstrate deep knowledge and expertise
    not in all... but some areas where you are proficient in.. talk abt technologies.. where things could break.
- Communication: Clearly explain complex concepts to stakeholders.
    Make sure you are competent in explaining things.

#Fundamentals
- Storage
    * ACID vs BASE model
    * Key-Value (cache)
    * Relational Data
    * Document Based
- Scalability
    * Scale Compute - Vertical scaling vs Horizontal scaling
    * Scale storage - Partitioning & Sharding  & consitent hashing 
- Networking
    * Application Layer - HTTP, HTTPS, REST, GRaphql, grpc, DNS resolution, websockets vs SSE
    * Transport Layer - TCP, UDP, request response lifecycle
    * Network Layer - Load Balancing, Firewalls, Acess Control Lists
- Latency, Throughput & Performance
    * RAM - 100ns
    * SSD - .1-.2ms
    * HDD - 1-2ms
    * Same Region - 1-10ms
    * Corss Region - !50ms
- Fault Tolerance & Redundancy
    * Failure Detection
    * Replication strategies
- CAP Theorem
    * choose bw Consistency(Stop serving data) OR Availability(risk wrong data) when network fails