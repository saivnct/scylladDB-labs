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

	err := godotenv.Load(".env")
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Failed to get load env: %v", err))
	}

	cluster := scylla.CreateCluster()
	session, err := gocql.NewSession(*cluster)
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to connect to scylla: %v", err))
	}
	defer session.Close()

	selectQuery(session)
	insertQuery(session)
	selectQuery2(session)
	deleteQuery(session)
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
