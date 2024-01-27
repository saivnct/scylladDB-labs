package main

import (
	"giangbb.studio/scylladb/scylla"
	"github.com/gookit/color"
	"github.com/joho/godotenv"
	"github.com/scylladb/gocqlx/v2"
	"github.com/scylladb/gocqlx/v2/qb"
	"github.com/scylladb/gocqlx/v2/table"
	"log"
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

	cluster := scylla.CreateCluster()
	log.Println(color.Green.Sprint("✅	Scylla cluster created!"))

	// Wrap session on creation, gocqlx session embeds gocql.Session pointer.
	session, err := gocqlx.WrapSession(cluster.CreateSession())
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Unable to connect to scylla: %v", err))
	}

	defer session.Close()

	selectAllQuery(session)
	insertQuery(session, "Mike", "Tyson", "12345 Foo Lane", "http://www.facebook.com/mtyson")
	insertQuery(session, "Alex", "Jones", "56789 Hickory St", "http://www.facebook.com/ajones")
	selectOneQuery(session, "Mike", "Tyson")
	selectAllQuery(session)
	deleteQuery(session, "Mike", "Tyson")
	selectAllQuery(session)
	deleteQuery(session, "Alex", "Jones")
	selectAllQuery(session)
}

func selectOneQuery(session gocqlx.Session, firstName string, lastName string) {
	log.Println("Displaying One Results:")
	r := Record{
		FirstName: firstName,
		LastName:  lastName,
	}
	//C1
	//err := mutantTable.GetQuery(session).BindStruct(r).GetRelease(&r)
	//if err != nil {
	//	logger.Error("select catalog.mutant_data", zap.Error(err))
	//}

	//C2
	q := session.Query(mutantTable.Get()).BindStruct(r)
	if err := q.GetRelease(&r); err != nil {
		log.Printf("select catalog.mutant_data err %v", err)
	}

	log.Println("\t" + r.FirstName + " " + r.LastName + ", " + r.Address + ", " + r.PictureLocation)
}

func selectAllQuery(session gocqlx.Session) {
	log.Println("Displaying Results:")
	var rs []Record

	q := qb.Select(mutantMetadata.Name).Columns(mutantMetadata.Columns...).Query(session)
	if err := q.Select(&rs); err != nil {
		log.Printf("select catalog.mutant_data err %v", err)
		return
	}

	for _, r := range rs {
		log.Println("\t" + r.FirstName + " " + r.LastName + ", " + r.Address + ", " + r.PictureLocation)
	}
}

func deleteQuery(session gocqlx.Session, firstName string, lastName string) {
	log.Println("Deleting " + firstName + "......")
	r := Record{
		FirstName: firstName,
		LastName:  lastName,
	}

	//C1
	err := session.Query(mutantTable.Delete()).BindStruct(r).ExecRelease()
	if err != nil {
		log.Printf("delete catalog.mutant_data err %v", err)
	}

	//C2
	//q := qb.Delete(mutantMetadata.Name).Where(qb.Eq("first_name"), qb.Eq("last_name")).Query(session).BindStruct(r)
	//if err := q.ExecRelease(); err != nil {
	//	log.Printf("delete catalog.mutant_data err %v", err)
	//}
}

func insertQuery(session gocqlx.Session, firstName, lastName, address, pictureLocation string) {
	log.Println("Inserting " + firstName + "......")
	r := Record{
		FirstName:       firstName,
		LastName:        lastName,
		Address:         address,
		PictureLocation: pictureLocation,
	}
	//C1
	q := session.Query(mutantTable.Insert()).BindStruct(r)
	if err := q.ExecRelease(); err != nil {
		log.Printf("insert catalog.mutant_data err %v", err)
	}

	//C2
	//q := qb.Insert(mutantMetadata.Name).Columns(mutantMetadata.Columns...).Query(session).BindStruct(r)
	//if err := q.ExecRelease(); err != nil {
	//	log.Printf("insert catalog.mutant_data err %v", err)
	//}
}
