package main

import (
	"giangbb.studio/scylladb/internal/log"
	"giangbb.studio/scylladb/internal/scylla"
	"github.com/gocql/gocql"
	"github.com/scylladb/gocqlx/v2"
	"github.com/scylladb/gocqlx/v2/qb"
	"github.com/scylladb/gocqlx/v2/table"
	"go.uber.org/zap"
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
	logger := log.CreateLogger("info")

	cluster := scylla.CreateCluster(gocql.LocalQuorum, "catalog", "192.168.31.41")
	// Wrap session on creation, gocqlx session embeds gocql.Session pointer.
	session, err := gocqlx.WrapSession(cluster.CreateSession())
	if err != nil {
		logger.Fatal("unable to connect to scylla", zap.Error(err))
	}

	defer session.Close()

	selectQuery(session, logger)
	insertQuery(session, "Mike", "Tyson", "12345 Foo Lane", "http://www.facebook.com/mtyson", logger)
	insertQuery(session, "Alex", "Jones", "56789 Hickory St", "http://www.facebook.com/ajones", logger)
	selectQuery(session, logger)
	deleteQuery(session, "Mike", "Tyson", logger)
	selectQuery(session, logger)
	deleteQuery(session, "Alex", "Jones", logger)
	selectQuery(session, logger)
}

func selectQuery(session gocqlx.Session, logger *zap.Logger) {
	logger.Info("Displaying Results:")
	var rs []Record

	q := qb.Select(mutantMetadata.Name).Columns(mutantMetadata.Columns...).Query(session)
	if err := q.Select(&rs); err != nil {
		logger.Warn("select catalog.mutant", zap.Error(err))
		return
	}

	for _, r := range rs {
		logger.Info("\t" + r.FirstName + " " + r.LastName + ", " + r.Address + ", " + r.PictureLocation)
	}
}

func deleteQuery(session gocqlx.Session, firstName string, lastName string, logger *zap.Logger) {
	logger.Info("Deleting " + firstName + "......")
	r := Record{
		FirstName: firstName,
		LastName:  lastName,
	}

	//C1
	err := session.Query(mutantTable.Delete()).BindStruct(r).ExecRelease()
	if err != nil {
		logger.Error("delete catalog.mutant_data", zap.Error(err))
	}

	//C2
	//q := qb.Delete(mutantMetadata.Name).Where(qb.Eq("first_name"), qb.Eq("last_name")).Query(session).BindStruct(r)
	//if err := q.ExecRelease(); err != nil {
	//	logger.Error("delete catalog.mutant_data", zap.Error(err))
	//}
}

func insertQuery(session gocqlx.Session, firstName, lastName, address, pictureLocation string, logger *zap.Logger) {
	logger.Info("Inserting " + firstName + "......")
	r := Record{
		FirstName:       firstName,
		LastName:        lastName,
		Address:         address,
		PictureLocation: pictureLocation,
	}
	//C1
	q := session.Query(mutantTable.Insert()).BindStruct(r)
	if err := q.ExecRelease(); err != nil {
		logger.Error("insert catalog.mutant_data", zap.Error(err))
	}

	//C2
	//q := qb.Insert(mutantMetadata.Name).Columns(mutantMetadata.Columns...).Query(session).BindStruct(r)
	//if err := q.ExecRelease(); err != nil {
	//	logger.Error("insert catalog.mutant_data", zap.Error(err))
	//}
}
