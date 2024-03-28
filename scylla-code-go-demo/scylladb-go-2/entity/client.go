package entity

import (
	"giangbb.studio/scylladb/entity/udt"
	"github.com/gocql/gocql"
)

type Client struct {
	Id gocql.UUID `db:"id" pk:"1"`

	ClientName udt.ClientName `db:"client_name" index:"true"`
	ClientInfo udt.ClientInfo `db:"client_info"`
	Role       int            `db:"role" index:"true"`
	Zones      []string       `db:"zones" dbType:"list<text>"`
}

func (p Client) TableName() string {
	return "client"
}
