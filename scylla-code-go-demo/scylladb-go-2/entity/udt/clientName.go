package udt

import "github.com/scylladb/gocqlx/v2"

type ClientName struct {
	gocqlx.UDT
	FirstName string `db:"first_name"`
	LastName  string `db:"last_name"`
}

func (c ClientName) Equals(other ClientName) bool {
	return c.FirstName == other.FirstName && c.LastName == other.LastName
}
