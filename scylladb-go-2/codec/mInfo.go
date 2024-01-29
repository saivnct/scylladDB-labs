package codec

import (
	"fmt"
	"github.com/gocql/gocql"
	"github.com/scylladb/gocqlx/v2/table"
)

type EntityInfo struct {
	TableMetaData table.Metadata
	Columns       []ColumnInfo
}

type ColumnInfo struct {
	Name string
	Type gocql.TypeInfo
}

func (c ColumnInfo) String() string {
	return fmt.Sprintf("[%s] - %s", c.Name, GetCqlTypeInfo(c.Type))
}
