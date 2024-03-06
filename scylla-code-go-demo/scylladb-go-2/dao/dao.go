package dao

import (
	"giangbb.studio/scylladb/entity"
	"github.com/scylladb/gocqlx/v2"
)

var (
	personDAO *PersonDAO
)

func InitDAOs(session gocqlx.Session) error {
	personDAO = &PersonDAO{}
	err := personDAO.InitDAO(session, entity.Person{})
	return err
}

func GetPersonDAO() *PersonDAO {
	return personDAO
}
