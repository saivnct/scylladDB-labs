package codec

import "github.com/scylladb/gocqlx/v2/table"

type EntityInfo struct {
	TableMetaData table.Metadata
	Columns       []ColumnInfo
}

func (e EntityInfo) GetGreateTableStatement() string {
	//TODO: implement
	return ""
}
