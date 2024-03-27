package main

import (
	"fmt"
	"giangbb.studio/scylladb/dao"
	"giangbb.studio/scylladb/entity"
	"giangbb.studio/scylladb/entity/udt"
	"github.com/gocql/gocql"
	"github.com/gookit/color"
	"github.com/joho/godotenv"
	cqlxo_connection "github.com/saivnct/gocqlx-orm/connection"
	"github.com/saivnct/gocqlx-orm/dao"
	"github.com/saivnct/gocqlx-orm/entity"
	"log"
	"os"
	"strconv"
	"strings"
	"time"
)

func main() {
	log.SetFlags(log.LstdFlags | log.Lshortfile)

	err := godotenv.Load(".env")
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Failed to get load env: %v", err))
	}

	hoststr := os.Getenv("SCYLLA_HOSTS")
	hosts := strings.Split(hoststr, ",")
	keyspace := os.Getenv("SCYLLA_KEYSPACE")

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

	session := *sessionP

	defer session.Close()

	dao.InitDAOs(session)

	////// Working with entity
	personDAO := dao.GetPersonDAO()

	var personEntities []cqlxoEntity.BaseModelInterface
	for i := 0; i < 10; i++ {
		person := entity.Person{
			LastName:  fmt.Sprintf("first%d", i),
			FirstName: fmt.Sprintf("last%d", i),
			FavoritePlace: udt.FavoritePlace{
				City:       "HCM",
				Country:    "VN",
				Population: 0,
				CheckPoint: []string{"1", "2", "3"},
				Rating:     3,
			},
			Email:          "test@test.com",
			StaticIP:       fmt.Sprintf("192.168.2.%d", i),
			Nicknames:      []string{"test", "test2", "test3"},
			WorkingHistory: map[int]string{1: "test", 2: "test2", 3: "test3"},
			CreatedAt:      time.Now(),
		}

		personEntities = append(personEntities, person)
	}

	err = personDAO.SaveMany(personEntities)
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to save person -> %v", err))
	}

	findAll()
	findWithPrimKey(personEntities[0].(entity.Person).FirstName, personEntities[0].(entity.Person).LastName, personEntities[0].(entity.Person).CreatedAt)
	findWithPartKey(personEntities[0].(entity.Person).FirstName, personEntities[0].(entity.Person).LastName)
	findWithIndex(personEntities[0].(entity.Person).FirstName)
	findWithIndexWithPagination(personEntities[0].(entity.Person).FirstName)
}

func findAll() {
	mfindAll := func(personDao *dao.PersonDAO) ([]entity.Person, error) {
		var personArr []entity.Person
		err := personDao.FindAll(&personArr)
		return personArr, err
	}

	persons, err := mfindAll(dao.GetPersonDAO())
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to get persons -> %v", err))
	}

	log.Println("findAll - persons", persons)
}

func findWithPrimKey(firstName string, lastName string, createdAt time.Time) {
	mfindWithPrimKey := func(personDAO *dao.PersonDAO, firstName string, lastName string, createdAt time.Time) (*entity.Person, error) {
		var personArr []entity.Person
		err := personDAO.FindByPrimaryKey(entity.Person{
			FirstName: firstName,
			LastName:  lastName,
			CreatedAt: createdAt,
		}, &personArr)

		if err != nil {
			return nil, err
		}
		if len(personArr) == 0 {
			return nil, nil
		}
		return &personArr[0], nil
	}

	person, err := mfindWithPrimKey(dao.GetPersonDAO(), firstName, lastName, createdAt)
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to get person -> %v", err))
		return
	}
	log.Println("findWithPrimKey - person", person)
}

func findWithPartKey(firstName string, lastName string) {
	mfindWithPartKey := func(personDAO *dao.PersonDAO, firstName string, lastName string) ([]entity.Person, error) {
		var personArr []entity.Person
		err := personDAO.FindByPartitionKey(entity.Person{
			FirstName: firstName,
			LastName:  lastName,
		}, &personArr)

		return personArr, err
	}

	//persons, err = mfindWithPartKey(personDAO, personEntities[0].(entity.Person).FirstName, personEntities[0].(entity.Person).LastName)
	persons, err := mfindWithPartKey(dao.GetPersonDAO(), firstName, lastName)
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to get persons -> %v", err))
		return
	}
	log.Println("findWithPartKey - persons", persons)
}

func findWithIndex(firstName string) {
	mfindWithIndex := func(personDAO *dao.PersonDAO, firstName string) ([]entity.Person, error) {
		var personArr []entity.Person
		err := personDAO.Find(entity.Person{
			FirstName: firstName,
		}, false, &personArr)
		return personArr, err
	}

	persons, err := mfindWithIndex(dao.GetPersonDAO(), firstName)
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to get persons -> %v", err))
		return
	}
	log.Println("findWithIndex - persons", persons)
}

func findWithIndexWithPagination(firstName string) {
	findWithPagination := func(personDAO *dao.PersonDAO, firstName string, itemsPerPage int) ([]entity.Person, error) {
		var (
			personArr []entity.Person
			page      []byte
		)
		for i := 0; ; i++ {
			var mPersons []entity.Person

			nextPage, err := personDAO.FindWithOption(entity.Person{
				FirstName: firstName,
			}, cqlxoDAO.QueryOption{
				Page:         page,
				ItemsPerPage: itemsPerPage,
			}, &mPersons)

			if err != nil {
				return nil, err
			}

			personArr = append(personArr, mPersons...)

			log.Printf("Page: %d -  items: %d \n", i, len(mPersons))

			page = nextPage
			if len(nextPage) == 0 {
				break
			}
		}

		return personArr, nil
	}

	persons, err := findWithPagination(dao.GetPersonDAO(), firstName, 5)
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to get persons -> %v", err))
		return
	}
	log.Println("findWithIndexWithPagination - persons", persons)
}
