package main

import (
	"context"
	"giangbb.studio/scylladb/scylla"
	"github.com/gocql/gocql"
	"github.com/gookit/color"
	"github.com/joho/godotenv"
	"log"
)

func main() {
	log.SetFlags(log.LstdFlags | log.Lshortfile)

	err := godotenv.Load("../.env")
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Failed to get load env: %v", err))
	}

	cluster := scylla.CreateCluster()
	session, err := cluster.CreateSession()
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to connect to scylla: %v", err))
	}
	defer session.Close()

	//selectQuery(session)
	//insertQuery(session)
	//selectQuery2(session)
	//deleteQuery(session)
	demoPagination(session)
}

func insertQuery(session *gocql.Session) {
	log.Println("Inserting Mike")
	//err := session.Query("INSERT INTO mutant_data (first_name,last_name,address,picture_location) VALUES ('Mike','Tyson','1515 Main St', 'http://www.facebook.com/mtyson')").Exec()
	err := session.Query("INSERT INTO mutant_data (first_name,last_name,address,picture_location) VALUES (?,?,?,?)", "Mike", "Tyson", "1515 Main St", "http://www.facebook.com/mtyson").Exec()
	if err != nil {
		log.Printf("insert catalog.mutant_data err %v", err)
	}
}

func deleteQuery(session *gocql.Session) {
	log.Println("Deleting Mike")
	//err := session.Query("DELETE FROM mutant_data WHERE first_name = 'Mike' and last_name = 'Tyson'").Exec()
	err := session.Query("DELETE FROM mutant_data WHERE first_name = ? and last_name = ?", "Mike", "Tyson").Exec()
	if err != nil {
		log.Printf("delete catalog.mutant_data err %v", err)
	}
}

func selectQuery(session *gocql.Session) {
	log.Println("selectQuery - Displaying Results:")
	scanner := session.Query("SELECT first_name,last_name,address,picture_location FROM mutant_data").
		WithContext(context.Background()).
		Iter().
		Scanner()

	for scanner.Next() {
		var firstName, lastName, address, pictureLocation string
		err := scanner.Scan(&firstName, &lastName, &address, &pictureLocation)
		if err != nil {
			log.Printf("select catalog.mutant_data err %v", err)
		}
		log.Println("\t" + firstName + " " + lastName + ", " + address + ", " + pictureLocation)
	}
	// scanner.Err() closes the iterator, so scanner nor iter should be used afterwards.
	if err := scanner.Err(); err != nil {
		log.Printf("select catalog.mutant_data err %v", err)
	}
}

func selectQuery2(session *gocql.Session) {
	log.Println("selectQuery2 - Displaying Results:")
	//q := session.Query("SELECT first_name,last_name,address,picture_location FROM mutant_data where first_name = 'Mike' and last_name = 'Tyson'")
	scanner := session.Query("SELECT first_name,last_name,address,picture_location FROM mutant_data where first_name = ? and last_name = ?", "Mike", "Tyson").
		WithContext(context.Background()).
		Iter().
		Scanner()

	for scanner.Next() {
		var firstName, lastName, address, pictureLocation string
		err := scanner.Scan(&firstName, &lastName, &address, &pictureLocation)
		if err != nil {
			log.Printf("select catalog.mutant_data err %v", err)
		}
		log.Println("\t" + firstName + " " + lastName + ", " + address + ", " + pictureLocation)
	}
	// scanner.Err() closes the iterator, so scanner nor iter should be used afterwards.
	if err := scanner.Err(); err != nil {
		log.Printf("select catalog.mutant_data err %v", err)
	}
}

func demoPagination(session *gocql.Session) {
	/* The example assumes the following CQL was used to setup the keyspace:
	create table itoa(id int, description text, PRIMARY KEY(id));
	insert into itoa (id, description) values (1, 'one');
	insert into itoa (id, description) values (2, 'two');
	insert into itoa (id, description) values (3, 'three');
	insert into itoa (id, description) values (4, 'four');
	insert into itoa (id, description) values (5, 'five');
	insert into itoa (id, description) values (6, 'six');
	*/

	var pageState []byte
	for {
		// We use PageSize(2) for the sake of example, use larger values in production (default is 5000) for performance
		// reasons.
		iter := session.Query(`SELECT id, description FROM itoa`).PageSize(2).PageState(pageState).Iter()
		nextPageState := iter.PageState()
		scanner := iter.Scanner()
		for scanner.Next() {
			var (
				id          string
				description int
			)
			err := scanner.Scan(&id, &description)
			if err != nil {
				log.Printf("err %v", err)
			}
			log.Println(id, description)
		}

		err := scanner.Err()
		if err != nil {
			log.Fatal(err)
		}

		log.Println("next page -->")
		//fmt.Printf("next page state: %v\n", nextPageState)
		if len(nextPageState) == 0 {
			break
		}
		pageState = nextPageState
	}
}
