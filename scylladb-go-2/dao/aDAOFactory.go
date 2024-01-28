package dao

import "github.com/scylladb/gocqlx/v2"

var (
	Car    *CarDAO
	Person *PersonDAO
)

func Init(session gocqlx.Session, keyspace string) {
	Car = mCarDAO(session, keyspace)
	//Person = mPersonDAO(session, keyspace)
}
