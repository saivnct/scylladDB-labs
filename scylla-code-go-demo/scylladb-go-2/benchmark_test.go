package main

import (
	"fmt"
	"giangbb.studio/scylladb/dao"
	"giangbb.studio/scylladb/entity"
	"giangbb.studio/scylladb/entity/udt"
	"github.com/gocql/gocql"
	"github.com/gookit/color"
	"github.com/joho/godotenv"
	"github.com/saivnct/gocqlx-orm/connection"
	cqlxoEntity "github.com/saivnct/gocqlx-orm/entity"
	"github.com/scylladb/gocqlx/v2"
	"github.com/stretchr/testify/assert"
	"log"
	"os"
	"strconv"
	"strings"
	"testing"
	"time"
)

var (
	session   gocqlx.Session
	clientDAO *dao.ClientDAO
)

func TestMain(m *testing.M) {
	loadEnv()

	code := m.Run()
	session.Close()
	os.Exit(code)
}

func loadEnv() {
	err := godotenv.Load(".env")
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Failed to get load env: %v", err))
	}

	hoststr := os.Getenv("SCYLLA_HOSTS")
	hosts := strings.Split(hoststr, ",")
	keyspace := os.Getenv("SCYLLA_KEYSPACE")

	log.Printf("working keyspace: %s\n", keyspace)

	clusterTimeout, err := strconv.Atoi(os.Getenv("SCYLLA_TIMEOUT"))
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Failed to get load env: %v", err))
	}

	numRetries, err := strconv.Atoi(os.Getenv("SCYLLA_NUM_RETRIES"))
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Failed to get load env: %v", err))
	}

	localDC := os.Getenv("SCYLLA_LOCAL_DC")
	cl := os.Getenv("SCYLLA_CONSISTENCY_LV")

	_, sessionP, err := cqlxo_connection.CreateCluster(hosts, keyspace, gocql.ParseConsistency(cl), localDC, clusterTimeout, numRetries)

	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to connect to scylla: %v", err))
	}
	log.Println(color.Green.Sprint("✅	Scylla cluster created!"))
	session = *sessionP
	dao.InitDAOs(session)

	clientDAO = dao.GetClientDAO()
}

func getDummyClientList(n int, m int) []cqlxoEntity.BaseModelInterface {
	var clientArr []cqlxoEntity.BaseModelInterface

	for i := 0; i < n; i++ {
		for j := 0; j < m; j++ {
			role := 1
			if i == 0 {
				role = 0
			}

			client := entity.Client{
				Id: gocql.TimeUUID(),
				ClientName: udt.ClientName{
					FirstName: fmt.Sprintf("first_%d", i),
					LastName:  fmt.Sprintf("last_%d", i),
				},
				ClientInfo: udt.ClientInfo{
					ZipCode: i + j,
					Age:     20 + j,
					Phones: []string{
						fmt.Sprintf("+%d111111%d", i, j),
						fmt.Sprintf("+%d222222%d", i, j),
						fmt.Sprintf("+%d333333%d", i, j),
					},
				},
				Role: role,
				Zones: []string{
					fmt.Sprintf("lzone-%d-%d", i, i),
					fmt.Sprintf("szone-%d-%d", i, j),
				},
			}

			clientArr = append(clientArr, client)
		}
	}

	return clientArr
}

func TestCount(t *testing.T) {
	assert.NotNil(t, clientDAO)

	count, err := clientDAO.CountAll()
	assert.Nil(t, err)

	log.Printf("count: %d\n", count)
}

func TestDelete(t *testing.T) {
	assert.NotNil(t, clientDAO)

	start := time.Now()
	err := clientDAO.DeleteAll()
	duration := time.Since(start)

	assert.Nil(t, err)
	log.Printf("Execution time: %dms\n", duration.Milliseconds())

	count, err := clientDAO.CountAll()
	assert.Nil(t, err)
	assert.Equal(t, int64(0), count, "table nothing left")
}

func TestInsert(t *testing.T) {
	assert.NotNil(t, clientDAO)

	startCount, err := clientDAO.CountAll()
	assert.Nil(t, err)

	clientList := getDummyClientList(10, 100)

	log.Println("Wait for insertion completed...")
	start := time.Now()
	err = clientDAO.SaveMany(clientList)
	duration := time.Since(start)

	assert.Nil(t, err)
	log.Printf("Execution time: %dms\n", duration.Milliseconds())

	count, err := clientDAO.CountAll()
	assert.Nil(t, err)

	assert.Equal(t, startCount+int64(len(clientList)), count, "Invalid count")
}

func TestUpdate(t *testing.T) {
	assert.NotNil(t, clientDAO)

	startCount, err := clientDAO.CountAll()
	assert.Nil(t, err)

	var clientArr []entity.Client
	err = clientDAO.FindAll(&clientArr)
	assert.Nil(t, err)
	assert.Equal(t, int64(len(clientArr)), startCount, "Invalid count")

	var clientUpdateArr []cqlxoEntity.BaseModelInterface
	for _, client := range clientArr {
		age := client.ClientInfo.Age
		client.ClientInfo.Age = age + 1
		clientUpdateArr = append(clientUpdateArr, client)
	}

	log.Println("Wait for update completed...")

	start := time.Now()
	err = clientDAO.SaveMany(clientUpdateArr)
	duration := time.Since(start)

	assert.Nil(t, err)
	log.Printf("Execution time: %dms\n", duration.Milliseconds())

	count, err := clientDAO.CountAll()
	assert.Nil(t, err)

	assert.Equal(t, startCount, count, "Invalid count")
}

func TestQuery(t *testing.T) {
	assert.NotNil(t, clientDAO)

	count, err := clientDAO.CountAll()
	assert.Nil(t, err)

	start := time.Now()
	var clientArr []entity.Client
	err = clientDAO.FindAll(&clientArr)
	duration := time.Since(start)

	assert.Nil(t, err)
	log.Printf("Execution time: %dms\n", duration.Milliseconds())

	assert.Equal(t, int64(len(clientArr)), count, "Invalid count")
}

func TestFindByPrimKey(t *testing.T) {
	assert.NotNil(t, clientDAO)

	uuid, err := gocql.ParseUUID("957ab028-ecad-11ee-afd9-02b8e97fa0d5")
	assert.Nil(t, err)

	start := time.Now()
	var clientArr []entity.Client
	err = clientDAO.FindByPrimaryKey(entity.Client{
		Id: uuid,
	}, &clientArr)
	duration := time.Since(start)

	assert.Nil(t, err)
	log.Printf("Execution time: %dms\n", duration.Milliseconds())
	if len(clientArr) > 0 {
		assert.Equal(t, clientArr[0].Id, uuid, "Invalid client")
	}

}

func TestFindByIndex_High_Selectivity(t *testing.T) {
	assert.NotNil(t, clientDAO)

	clientName := udt.ClientName{
		FirstName: "first_5",
		LastName:  "last_5",
	}

	start := time.Now()
	var clientArr []entity.Client
	err := clientDAO.Find(entity.Client{
		ClientName: clientName,
	}, false, &clientArr)
	duration := time.Since(start)

	assert.Nil(t, err)
	assert.Truef(t, len(clientArr) > 0, "No client found")
	log.Printf("Execution time: %dms\n", duration.Milliseconds())

	log.Printf("Found: %d\n", len(clientArr))

	for _, client := range clientArr {
		assert.Equal(t, client.ClientName, clientName, "Invalid clientName")
	}

}

func TestFindByIndex_Low_Selectivity(t *testing.T) {
	assert.NotNil(t, clientDAO)

	role := 1

	start := time.Now()
	var clientArr []entity.Client
	err := clientDAO.Find(entity.Client{
		Role: role,
	}, false, &clientArr)
	duration := time.Since(start)

	assert.Nil(t, err)
	assert.Truef(t, len(clientArr) > 0, "No client found")
	log.Printf("Execution time: %dms\n", duration.Milliseconds())

	log.Printf("Found: %d\n", len(clientArr))

	for _, client := range clientArr {
		assert.Equal(t, client.Role, role, "Invalid role")
	}

}
