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

func mCarDAO(session gocqlx.Session) *CarDAO {
	d := &CarDAO{}
	err := d.InitDAO(session, entity.Car{})
	if err != nil {
		log.Fatal(color.Red.Sprintf("❌ Failed to create DAO: %v", err))
	}
	return d
}
