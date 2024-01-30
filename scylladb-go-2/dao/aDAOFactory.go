package dao

import (
	"github.com/scylladb/gocqlx/v2"
)

var (
	Car    *CarDAO
	Person *PersonDAO
)

func Init(session gocqlx.Session) {
	Car = mCarDAO(session)
	Person = mPersonDAO(session)
}
