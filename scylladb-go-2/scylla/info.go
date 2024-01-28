package scylla

import (
	"bytes"
	"fmt"
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

func (c ColumnInfo) String() string {
	return fmt.Sprintf("[%s] - %s", c.Name, GetCqlTypeInfo(c.Type))
}

func GetCqlTypeInfo(cqlType gocql.TypeInfo) string {
	buf := &bytes.Buffer{}

	if cqlCollection, ok := cqlType.(gocql.CollectionType); ok {
		fmt.Fprintf(buf, "%s{", cqlCollection.Type().String())

		if cqlCollection.Key != nil {
			if _, ok := cqlCollection.Key.(gocql.CollectionType); ok {
				fmt.Fprintf(buf, " key:%s ", GetCqlTypeInfo(cqlCollection.Key))
			} else if cqlUDT, ok := cqlCollection.Key.(gocql.UDTTypeInfo); ok {
				fmt.Fprintf(buf, " key:UDT%s ", cqlUDT.String())
			} else {
				fmt.Fprintf(buf, " key:%s ", cqlCollection.Key.Type().String())
			}
		}

		if _, ok := cqlCollection.Elem.(gocql.CollectionType); ok {
			fmt.Fprintf(buf, " elem:%s ", GetCqlTypeInfo(cqlCollection.Elem))
		} else if cqlUDT, ok := cqlCollection.Elem.(gocql.UDTTypeInfo); ok {
			fmt.Fprintf(buf, " elem:UDT%s ", cqlUDT.String())
		} else {
			fmt.Fprintf(buf, " elem:%s ", cqlCollection.Elem.Type().String())
		}

		fmt.Fprint(buf, "}")

	} else if cqlUDT, ok := cqlType.(gocql.UDTTypeInfo); ok {
		fmt.Fprintf(buf, "UDT%s", cqlUDT.String())

	} else {
		fmt.Fprintf(buf, "%s", cqlType.Type().String())
	}

	return buf.String()

}
