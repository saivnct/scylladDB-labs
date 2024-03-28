package udt

import (
	"github.com/scylladb/gocqlx/v2"
	"reflect"
)

type ClientInfo struct {
	gocqlx.UDT
	ZipCode int      `db:"zip_code"`
	Age     int      `db:"age"`
	Phones  []string `db:"phones" dbType:"set<text>"`
}

func (c ClientInfo) Equals(other ClientInfo) bool {
	return c.ZipCode == other.ZipCode && c.Age == other.Age && reflect.DeepEqual(c.Phones, other.Phones)
}
