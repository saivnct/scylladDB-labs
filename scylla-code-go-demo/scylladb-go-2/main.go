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
	cqlxoDAO "github.com/saivnct/gocqlx-orm/dao"
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
	employeeDAO := dao.GetEmployeeDAO()

	var employeeEntities []cqlxoEntity.BaseModelInterface
	for i := 0; i < 10; i++ {
		employee := entity.Employee{
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

		employeeEntities = append(employeeEntities, employee)
	}

	err = employeeDAO.SaveMany(employeeEntities)
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to save employee -> %v", err))
	}

	findAll()
	findWithPrimKey(employeeEntities[0].(entity.Employee).FirstName, employeeEntities[0].(entity.Employee).LastName, employeeEntities[0].(entity.Employee).CreatedAt)
	findWithPartKey(employeeEntities[0].(entity.Employee).FirstName, employeeEntities[0].(entity.Employee).LastName)
	findWithIndex(employeeEntities[0].(entity.Employee).FirstName)
	findWithIndexWithPagination(employeeEntities[0].(entity.Employee).FirstName)
}

func findAll() {
	mfindAll := func(employeeDAO *dao.EmployeeDAO) ([]entity.Employee, error) {
		var employeeArr []entity.Employee
		err := employeeDAO.FindAll(&employeeArr)
		return employeeArr, err
	}

	employees, err := mfindAll(dao.GetEmployeeDAO())
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to get employees -> %v", err))
	}

	log.Println("✅ findAll - employees", employees)
}

func findWithPrimKey(firstName string, lastName string, createdAt time.Time) {
	mfindWithPrimKey := func(employeeDAO *dao.EmployeeDAO, firstName string, lastName string, createdAt time.Time) (*entity.Employee, error) {
		var employeeArr []entity.Employee
		err := employeeDAO.FindByPrimaryKey(entity.Employee{
			FirstName: firstName,
			LastName:  lastName,
			CreatedAt: createdAt,
		}, &employeeArr)

		if err != nil {
			return nil, err
		}
		if len(employeeArr) == 0 {
			return nil, nil
		}
		return &employeeArr[0], nil
	}

	employee, err := mfindWithPrimKey(dao.GetEmployeeDAO(), firstName, lastName, createdAt)
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to get employee -> %v", err))
		return
	}
	log.Println("✅ findWithPrimKey - employee", employee)
}

func findWithPartKey(firstName string, lastName string) {
	mfindWithPartKey := func(employeeDAO *dao.EmployeeDAO, firstName string, lastName string) ([]entity.Employee, error) {
		var employeeArr []entity.Employee
		err := employeeDAO.FindByPartitionKey(entity.Employee{
			FirstName: firstName,
			LastName:  lastName,
		}, &employeeArr)

		return employeeArr, err
	}

	employees, err := mfindWithPartKey(dao.GetEmployeeDAO(), firstName, lastName)
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to get employees -> %v", err))
		return
	}
	log.Println("✅ findWithPartKey - employees", employees)
}

func findWithIndex(firstName string) {
	mfindWithIndex := func(employeeDAO *dao.EmployeeDAO, firstName string) ([]entity.Employee, error) {
		var employeeArr []entity.Employee
		err := employeeDAO.Find(entity.Employee{
			FirstName: firstName,
		}, false, &employeeArr)
		return employeeArr, err
	}

	employees, err := mfindWithIndex(dao.GetEmployeeDAO(), firstName)
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to get employees -> %v", err))
		return
	}
	log.Println("✅ findWithIndex - employees", employees)
}

func findWithIndexWithPagination(firstName string) {
	findWithPagination := func(employeeDAO *dao.EmployeeDAO, firstName string, itemsPerPage int) ([]entity.Employee, error) {
		var (
			employeeArr []entity.Employee
			page        []byte
		)
		for i := 0; ; i++ {
			var mEmployees []entity.Employee

			nextPage, err := employeeDAO.FindWithOption(entity.Employee{
				FirstName: firstName,
			}, cqlxoDAO.QueryOption{
				Page:         page,
				ItemsPerPage: itemsPerPage,
			}, &mEmployees)

			if err != nil {
				return nil, err
			}

			employeeArr = append(employeeArr, mEmployees...)

			log.Printf("Page: %d -  items: %d \n", i, len(mEmployees))

			page = nextPage
			if len(nextPage) == 0 {
				break
			}
		}

		return employeeArr, nil
	}

	employees, err := findWithPagination(dao.GetEmployeeDAO(), firstName, 5)
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to get employees -> %v", err))
		return
	}
	log.Println("✅ findWithIndexWithPagination - employees", employees)
}
