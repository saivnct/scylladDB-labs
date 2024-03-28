package dao

import (
	"giangbb.studio/scylladb/entity"
	"github.com/scylladb/gocqlx/v2"
)

var (
	employeeDAO *EmployeeDAO
	clientDAO   *ClientDAO
)

func InitDAOs(session gocqlx.Session) error {
	employeeDAO = &EmployeeDAO{}
	err := employeeDAO.InitDAO(session, entity.Employee{})
	if err != nil {
		return err
	}

	clientDAO = &ClientDAO{}
	err = clientDAO.InitDAO(session, entity.Client{})
	return err
}

func GetEmployeeDAO() *EmployeeDAO {
	return employeeDAO
}

func GetClientDAO() *ClientDAO {
	return clientDAO
}
