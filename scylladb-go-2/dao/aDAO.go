package dao

import (
	"giangbb.studio/scylladb/entity"
	"giangbb.studio/scylladb/scylla"
	"github.com/scylladb/gocqlx/v2"
)

type DAO struct {
	EntityInfo scylla.EntityInfo
}

func (d *DAO) InitDAO(session gocqlx.Session, keyspace string, m entity.BaseModelInterface) error {
	entityInfo, err := scylla.ParseTableMetaData(m)
	if err != nil {
		return err
	}

	d.EntityInfo = entityInfo

	//err = d.CheckAndCreateTable(session, keyspace)
	//if err != nil {
	//	return err
	//}

	//log.Printf("DAO %s created!", m.TableName())
	return nil
}

//func (d *DAO) CheckAndCreateTable(session gocqlx.Session, keyspace string) error {
//	err := session.ExecStmt(fmt.Sprintf(`CREATE TABLE IF NOT EXISTS %s.%s (
//		id uuid PRIMARY KEY,
//		title text,
//		album text,
//		artist text,
//		tags set<text>,
//		data blob)`, keyspace, d.TableMetaData.Name))
//	return err
//}
//
//func (d *DAO) FindAll(session gocqlx.Session) ([]entity.BaseModelInterface, error) {
//	var rs []entity.BaseModelInterface
//	q := qb.Select(d.TableMetaData.Name).Columns(d.TableMetaData.Columns...).Query(session)
//	err := q.Select(&rs)
//	return rs, err
//}
