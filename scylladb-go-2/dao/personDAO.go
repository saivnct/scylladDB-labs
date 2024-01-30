package dao

import (
	"giangbb.studio/scylladb/entity"
	"github.com/gookit/color"
	"github.com/scylladb/gocqlx/v2"
	"log"
)

type PersonDAO struct {
	DAO
}

func mPersonDAO(session gocqlx.Session) *PersonDAO {
	d := &PersonDAO{}
	err := d.InitDAO(session, entity.Person{})
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Failed to create DAO: %v", err))
	}
	return d
}
