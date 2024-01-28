package scylla

import (
	"github.com/gookit/color"
	"github.com/scylladb/gocqlx/v2"
	"log"
	"time"

	"github.com/gocql/gocql"
)

func CreateCluster(hosts []string, keyspace string, localDC string, clusterTimeout int, numRetries int) (*gocql.ClusterConfig, *gocqlx.Session, error) {
	retryPolicy := &gocql.ExponentialBackoffRetryPolicy{
		Min:        time.Second,
		Max:        10 * time.Second,
		NumRetries: numRetries,
	}
	cluster := gocql.NewCluster(hosts...)

	cluster.Keyspace = keyspace
	cluster.Timeout = time.Duration(clusterTimeout) * time.Second
	cluster.RetryPolicy = retryPolicy

	if localDC != "" {
		log.Println(color.Green.Sprintf("✅	[scylla] - Using local DC: %s", localDC))
		cluster.Consistency = gocql.LocalQuorum
		cluster.PoolConfig.HostSelectionPolicy = gocql.TokenAwareHostPolicy(gocql.DCAwareRoundRobinPolicy(localDC))
	} else {
		cluster.Consistency = gocql.Quorum
		cluster.PoolConfig.HostSelectionPolicy = gocql.TokenAwareHostPolicy(gocql.RoundRobinHostPolicy())
	}

	session, err := gocqlx.WrapSession(cluster.CreateSession())
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to connect to scylla: %v", err))
	}

	return cluster, &session, nil
}
