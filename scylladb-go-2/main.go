package main

import (
	"giangbb.studio/scylladb/codec"
	"giangbb.studio/scylladb/connection"
	"giangbb.studio/scylladb/dao"
	sliceUtils "giangbb.studio/scylladb/utils"
	"github.com/gocql/gocql"
	"github.com/gookit/color"
	"github.com/joho/godotenv"
	"github.com/scylladb/gocqlx/v2/table"
	"log"
	"os"
	"strconv"
	"strings"
)

var mutantMetadata = table.Metadata{
	Name:    "mutant_data",
	Columns: []string{"first_name", "last_name", "address", "picture_location"},
	PartKey: []string{"first_name", "last_name"},
}
var mutantTable = table.New(mutantMetadata)

type Record struct {
	FirstName       string `db:"first_name"`
	LastName        string `db:"last_name"`
	Address         string `db:"address"`
	PictureLocation string `db:"picture_location"`
}

func main() {
	log.SetFlags(log.LstdFlags | log.Lshortfile)

	err := godotenv.Load(".env")
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Failed to get load env: %v", err))
	}

	hoststr := os.Getenv("SCYLLA_HOSTS")
	hosts := strings.Split(hoststr, ",")
	keyspace := os.Getenv("SCYLLA_KEYSPACE")

	//rf, err := strconv.Atoi(os.Getenv("SCYLLA_RF"))
	//if err != nil {
	//	log.Fatal(color.Red.Sprintf("❌ [scylla] - Failed to get load env: %v", err))
	//}

	clusterTimeout, err := strconv.Atoi(os.Getenv("SCYLLA_TIMEOUT"))
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Failed to get load env: %v", err))
	}

	numRetries, err := strconv.Atoi(os.Getenv("SCYLLA_NUM_RETRIES"))
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Failed to get load env: %v", err))
	}

	localDC := os.Getenv("SCYLLA_LOCAL_DC")

	_, sessionP, err := connection.CreateCluster(hosts, keyspace, localDC, clusterTimeout, numRetries)
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to connect to scylla: %v", err))
	}
	log.Println(color.Green.Sprint("✅	Scylla cluster created!"))

	session := *sessionP

	defer session.Close()

	dao.Init(session)

	log.Println("------------Car----------------")
	//for _, column := range dao.Car.EntityInfo.Columns {
	//	log.Println(column.String())
	//	log.Printf("%s\n\n", column.GetCqlTypeDeclareStatement())
	//}
	//log.Println("Car", dao.Car.EntityInfo.TableMetaData)

	udts := dao.Car.EntityInfo.ScanUDTs()
	//udtNames := sliceUtils.Map(udts, func(udt gocql.UDTTypeInfo) string { return udt.Name })
	//log.Printf("Car UDTs: %s\n\n", strings.Join(udtNames, ", "))
	udtStms := sliceUtils.Map(udts, func(udt gocql.UDTTypeInfo) string { return codec.GetCqlCreateUDTStatement(udt) })
	log.Printf("Car UDTs: \n%s\n", strings.Join(udtStms, "\n"))
	log.Printf("Car: %s\n\n", dao.Car.EntityInfo.GetGreateTableStatement())

	log.Println("------------Person----------------")
	//for _, column := range dao.Person.EntityInfo.Columns {
	//	log.Println(column.String())
	//	log.Printf("%s\n\n", column.GetCqlTypeDeclareStatement())
	//}
	//log.Println("Person", dao.Person.EntityInfo.TableMetaData)

	udts = dao.Person.EntityInfo.ScanUDTs()
	//udtNames = sliceUtils.Map(udts, func(udt gocql.UDTTypeInfo) string { return udt.Name })
	//log.Printf("Car UDTs: %s\n\n", strings.Join(udtNames, ", "))
	udtStms = sliceUtils.Map(udts, func(udt gocql.UDTTypeInfo) string { return codec.GetCqlCreateUDTStatement(udt) })
	log.Printf("Person UDTs: \n%s\n\n", strings.Join(udtStms, "\n"))
	log.Printf("Car: %s\n\n", dao.Person.EntityInfo.GetGreateTableStatement())
}
