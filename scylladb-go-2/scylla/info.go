package scylla

import (
	"github.com/gocql/gocql"
	"github.com/scylladb/gocqlx/v2/table"
)

type ColumnInfo struct {
	Name string
	Type gocql.TypeInfo
}

type EntityInfo struct {
	TableMetaData table.Metadata
	Columns       []ColumnInfo
}
