package dao

import (
	"giangbb.studio/scylladb/entity"
	"github.com/gookit/color"
	"github.com/scylladb/gocqlx/v2"
	"log"
)

type CarDAO struct {
	DAO
}

func mCarDAO(session gocqlx.Session, keyspace string) *CarDAO {
	d := &CarDAO{}
	err := d.InitDAO(session, keyspace, entity.Car{})
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Failed to create DAO: %v", err))
	}
	return d
}
