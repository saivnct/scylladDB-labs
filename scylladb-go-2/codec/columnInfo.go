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
	//TODO: implement
	if cqlUDT, ok := c.Type.(gocql.UDTTypeInfo); ok {
		return fmt.Sprintf("CREATE TYPE IF NOT EXISTS %s ( %s )", c.Name, cqlUDT.String()), nil
	}

	return "", fmt.Errorf("%s is not UDT type", c.Name)
}

func (c ColumnInfo) GetCqlTypeDeclareStatement() string {
	return fmt.Sprintf("%s %s", c.Name, GetCqlTypeDeclareStatement(c.Type))
}
