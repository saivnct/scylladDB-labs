package scylla

import (
	"github.com/gookit/color"
	"log"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/gocql/gocql"
)

func CreateCluster() *gocql.ClusterConfig {
	hoststr := os.Getenv("SCYLLA_HOSTS")
	hosts := strings.Split(hoststr, ",")

	keyspace := os.Getenv("SCYLLA_KEYSPACE")

	//rf, err := strconv.Atoi(os.Getenv("SCYLLA_RF"))
	//if err != nil {
	//	log.Fatal(color.Red.Sprintf("❌ [scylla] - Failed to get load env: %v", err))
	//}

	clusterTimeout, err := strconv.Atoi(os.Getenv("SCYLLA_TIMEOUT"))
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ [scylla] - Failed to get load env: %v", err))
	}

	numRetries, err := strconv.Atoi(os.Getenv("SCYLLA_NUM_RETRIES"))
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ [scylla] - Failed to get load env: %v", err))
	}

	retryPolicy := &gocql.ExponentialBackoffRetryPolicy{
		Min:        time.Second,
		Max:        10 * time.Second,
		NumRetries: numRetries,
	}
	cluster := gocql.NewCluster(hosts...)
	cluster.Keyspace = keyspace
	cluster.Timeout = time.Duration(clusterTimeout) * time.Second
	cluster.RetryPolicy = retryPolicy

	localDC := os.Getenv("SCYLLA_LOCAL_DC")
	if localDC != "" {
		log.Println(color.Green.Sprintf("✅	[scylla] - Using local DC: %s", localDC))
		cluster.Consistency = gocql.LocalQuorum
		cluster.PoolConfig.HostSelectionPolicy = gocql.TokenAwareHostPolicy(gocql.DCAwareRoundRobinPolicy(localDC))
	} else {
		cluster.Consistency = gocql.Quorum
		cluster.PoolConfig.HostSelectionPolicy = gocql.TokenAwareHostPolicy(gocql.RoundRobinHostPolicy())
	}

	return cluster
}
