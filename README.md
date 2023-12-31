# [Giangbb Studio] - ScyllaDB Labs

Our project leverages ScyllaDB, a highly scalable and distributed NoSQL database, to address our data storage and retrieval needs. ScyllaDB is built on Apache Cassandra's architecture but offers improved performance and scalability characteristics. It is designed to handle massive amounts of data while ensuring low-latency responses, making it an excellent choice for high-traffic and data-intensive applications.

In our project, we have developed a demo application using Spring Boot, a popular Java framework. The application showcases the integration of ScyllaDB as the backend data store. By utilizing ScyllaDB's flexible data model, fault tolerance, and horizontal scalability, we are able to efficiently store, manage, and retrieve large volumes of structured and semi-structured data.

# ScyllaDB Cluster with 2 DC

## Located at scylladb-cluster-docker-sample folder

    Run DC 1:
    	docker-compose up -d
    	-> After roughly 60 seconds, the first datacenter, DC1, will be created

    Run DC 2:
    	docker-compose -f docker-compose-dc2.yml up -d
    	-> After about 60 seconds, you should be able to see DC1 and DC2 when running the “nodetool status” command

    Check Status:
    	docker exec -it scylla-node1 nodetool status

# Springboot demo project folder

## Located at scylladb-springboot folder



# <a href="Guide-to-Scylla-no-memes.pdf">Click Here "Guide to ScyllaDB"</a>


# Basic concepts

1. Cluster:

   - A Cluster is a collection of nodes.
   - The nodes are logically distributed like a ring.
   - A minimum cluster typically consists of at least three nodes.
   - Data is automatically replicated across the cluster, depending on the Replication Factor.
   - Cluster is often referred to as a ring architecture, based on a hash ring — the way the cluster knows how to distribute data across the different nodes.
   - A cluster can change size over time, adding more nodes (to expand storage and processing power) or removing nodes (either through purposeful decommissioning or system failure) => When a topology change occurs, the ScyllaDB cluster is designed to reconfigure itself and rebalance the data held within it automatically.

   - Within the ScyllaDB cluster, all internode communication is peer-to-peer.
   - For communication outside of the cluster, such as a read or write, a ScyllaDB client will communicate with a single server node, called the coordinator => The selection of the coordinator is made with each client connection request to prevent bottlenecking requests through a single node

   - A "Partition Key" is one or more columns that are responsible for data distribution across the nodes. It determines in which nodes to store a given row. A consistent "hash function" is used to determine to which nodes data is written.

2. Node:

   - In ScyllaDB, all nodes are equal.
   - Each ScyllaDB node contains a portion of the entire database cluster’s content and consists of several independent shards.

3. Shard:

   - Each ScyllaDB node consists of several independent shards, which contain their share of the node’s total data. - ScyllaDB creates a one shard per core (technically, one shard per hyperthread, meaning some physical cores may have two or more virtual cores). - Each shard operates on a shared-nothing architecture basis. This means each shard is assigned its RAM and its storage, manages its schedulers for the CPU and I/O, performs its compactions (more about compaction later on), and maintains its multi-queue network connection. - Each shard runs as a single thread, and communicates asynchronously with its peers, without locking. - From the outside, nodes are viewed as a single object. Operations are performed at the node level. - To check the number of physical cores on the server, and how each map to a ScyllaDB shard, run the following from any server running ScyllaDB:
     docker exec -it Node_Z bash
     ./usr/lib/scylla/seastar-cpu-map.sh -n scylla

4. Keyspace:

   - Keyspace is a collection of tables with attributes that define how data is replicated across nodes (Replication Strategy).
   - It defines a number of options that apply to all the tables it contains
   - It is generally recommended to use one Keyspace per application, and thus a cluster may define only one Keyspace.

5. Multi DC (data center) cluster

   - ScyllaDB also offers multi-datacenter replication. This means that two (or more) datacenters can distribute and share data between each other.
   - Your replication strategy allows you to define how many datacenters you wish to replicate across, and what replication factors you will use in each datacenter.
   - Datacenters are generally configured as peer-to-peer, meaning there is no central authority, nor are there “primary/replication” hierarchical relationships between clusters.
   - Thereafter, data can be replicated between your clusters to support localized traffic for lowest latency and highest throughput, or for resiliency in case of an entire datacenter outage

6. Replication Strategy
   The Replication Strategy determines on which nodes replicas are placed. There are two available replication strategies:

   - SimpleStrategy – Places the first replica on the node selected by the partitioner. The partitioner, or partition hash function, is a hash function for computing which data is stored on which node in the cluster. The remaining replicas are placed in the clockwise direction on the node ring.

     NOTE: This replication strategy should not be used in production environments.

   - NetworkTopologyStrategy – Places replicas in a clockwise direction in the ring until it reaches the first node of a different rack. This is used for clusters deployed across multiple data centers. Using this strategy allows you to define the number of replicas for each DC.
     Ex: if we have two DCs, DC1 with a replication factor of 3 and DC2 with a replication factor of 2, the replication factor of the Keyspace will be 5

7. Token Ranges

   - Each node in a ring is assigned a range. The hash function computes a token for a given partition key => The hash function determines the placement of the data in the cluster.
   - Without using Vnodes or virtual nodes, each node could only support one token range. By using vnodes, each node can support multiple, non-contiguous token ranges. By doing this, we can think of each physical node as hosting many virtual nodes. By default, each node has 256 virtual nodes.

   - To see the tokens for each node on our cluster, we use the nodetool ring command:
     docker exec -it scylla-node1 nodetool ring
     -> This shows us the token range for each node. The value in the column Token is the end of the token range, up to (and including) the value listed

   - Another way to show the tokens present on a specific node is with the describing command using the keyspace as a parameter:
     docker exec -it scylla-node1 nodetool describering scyllau

8. Consistency Level (CL)

   - The number of nodes that must acknowledge a read or write operation before it is considered successful.

   - You can set consistency levels for all operations (on the client-side) but ScyllaDB also allows each operation (each read or write) to have its own consistency level

     - CL of 1: Wait for ACK from one replica node
     - CL of ALL: Wait for ACK from all replica nodes
     - CL LOCAL_QUORUM: Wait for floor((RF/2)+1) ACK from all replica nodes
     - CL EACH_QUORUM: For multi DC, each DC must have a LOCAL_QUORUM. This is unsupported for reads.
     - CL ALL: All replica nodes must respond => Provides the highest consistency but lowest availability.

9. Cluster Level Read/Write Interaction

   1. A client connects to a ScyllaDB node using the CQL shell and performs a CQL request
   2. The node the client is connected to is now designated as the Coordinator Node -> The Coordinator Node, based on hashing the data, using the partition key and on the Replication Strategy, sends the request to the applicable nodes. Internode messages are sent through a messaging queue asynchronously.
   3. The Consistency Level determines the number of nodes the coordinator needs to hear back from for the request to be successful.
   4. The client is notified if the request is successful.

10. Gossip

    - ScyllaDB, like Apache Cassandra, uses a type of internode communication protocol called Gossip, for nodes to exchange information with each other.
    - Gossip is decentralized, and there is no single point of failure. It’s used for peer node discovery and metadata propagation.
    - Gossip communication occurs periodically. Each node communicates with three other nodes.
    - Eventually (within a few seconds), the information is propagated throughout the cluster.
    - To see whether a node is communicating using Gossip, we use the statusgossip command:
      docker exec -it scylla-node1 nodetool statusgossip
    - To see what a node is communicating to the other nodes in the cluster, we use the gossipinfo command:
      docker exec -it scylla-node1 nodetool gossipinfo

11. Snitch

    - The Snitch is responsible for determining which racks and datacenters are to be written and read from.
    - ScyllaDB supports the following snitches:

      - SimpleSnitch: Default value should only be used in single DC clusters
      - RackInferringSnitch: Binds nodes to DCs and racks according to their broadcast IP addresses.
      - GossipingPropertyFileSnitch: Explicitly define for each node in which Rack and DC it belongs. It’s better to use this instead of Simplesnitch
      - Ec2Snitch: Learns the topology on its own from AWS API. Useful for single region clusters deployed on AWS. The region is treated as a DC.
      - Ec2MultiRegionSnitch: Same as Ec2Snitch but for multiple region deployments.
      - GoogleCloudSnitch: For deploying ScyllaDB on the Google Cloud Engine (GCE) platform across one or more regions. The region is treated as a datacenter, and the availability zones are treated as racks within the datacenter.

    - To see the snitch defined in our multi-dc cluster, we use the describecluster command:
      docker exec -it scylla-node1 nodetool describecluster

    - The snitch is configured within file located under /etc/scylla/cassandra-rackdc.properties

12. Scylla CDC (Change Data Capture )

    - Change Data Capture (CDC) is a feature that allows you to not only query the current state of a database’s table but also to query the history of all changes made to the table
    - In Scylla, CDC is optional and enabled on a per-table basis

      CREATE TABLE ks.t (pk int, ck int, v int, PRIMARY KEY (pk, ck, v)) WITH cdc = {'enabled':true};

13. Time-Window Compaction Strategy (TWCS)
    - Time-Window Compaction Strategy compacts SSTables within each time window using the Size-tiered Compaction Strategy (STCS).
    - It works as follows:
      - A time window is configured. The window is determined by the compaction window size compaction_window_size and the time unit (compaction_window_unit).
      - SSTables created within the time window are compacted using Size-tiered Compaction Strategy (STCS).
      - Once a time window ends, take all SSTables created during the time window and compact the data into one SSTable.
      - The final resulting SSTable is never compacted with other time-windows SSTables.
        => With this explanation, if the time window were for one day, at the end of the day, the SSTables accumulated for that day only would be compacted into one SSTable.
