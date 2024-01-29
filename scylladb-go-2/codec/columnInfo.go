package codec

import (
	"fmt"
	"github.com/gocql/gocql"
)

type ColumnInfo struct {
	Name string
	Type gocql.TypeInfo
}

func (c ColumnInfo) String() string {
	return fmt.Sprintf("[%s] - %s", c.Name, GetCqlTypeInfo(c.Type))
}

func (c ColumnInfo) GetGreateUDTStatement() (string, error) {
	if cqlUDT, ok := c.Type.(gocql.UDTTypeInfo); ok {
		return fmt.Sprintf("CREATE TYPE IF NOT EXISTS %s ( %s )", c.Name, cqlUDT.String()), nil
	}

	return "", fmt.Errorf("%s is not UDT type", c.Name)
}

func (c ColumnInfo) GetTypeStatement() (string, error) {
	//TODO: implement

	//if cqlNativeType, ok := c.Type.(gocql.NativeType); ok {
	//
	//} else if cqlCollectionType, ok := c.Type.(gocql.CollectionType); ok {
	//
	//} else if cqlUDTTypeInfo, ok := c.Type.(gocql.UDTTypeInfo); ok {
	//
	//} else if cqlTupleTypeInfo, ok := c.Type.(gocql.TupleTypeInfo); ok {
	//
	//}

	return "", fmt.Errorf("%s: Unknown Type %s", c.Name, c.Type.Type().String())
}
