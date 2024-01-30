package codec

import (
	"bytes"
	"fmt"
	"github.com/gocql/gocql"
	"strings"
)

func GetCqlTypeInfo(cqlType gocql.TypeInfo) string {
	buf := &bytes.Buffer{}

	if cqlCollection, ok := cqlType.(gocql.CollectionType); ok {
		fmt.Fprintf(buf, "%s{", cqlCollection.Type().String())

		if cqlCollection.Key != nil {
			fmt.Fprintf(buf, " key:%s ", GetCqlTypeInfo(cqlCollection.Key))
		}

		fmt.Fprintf(buf, " elem:%s }", GetCqlTypeInfo(cqlCollection.Elem))

	} else if cqlUDT, ok := cqlType.(gocql.UDTTypeInfo); ok {
		fmt.Fprintf(buf, "UDT%s", cqlUDT.String())
	} else if cqlTuple, ok := cqlType.(gocql.TupleTypeInfo); ok {
		fmt.Fprintf(buf, "%s", cqlTuple.String())
	} else {
		fmt.Fprintf(buf, "%s", cqlType.Type().String())
	}

	return buf.String()
}

func GetCqlTypeDeclareStatement(cqlType gocql.TypeInfo) string {
	buf := &bytes.Buffer{}

	if cqlCollection, ok := cqlType.(gocql.CollectionType); ok {
		fmt.Fprintf(buf, "%s<", cqlCollection.NativeType.Type().String())

		if cqlCollection.Key != nil {
			fmt.Fprintf(buf, "%s, ", GetCqlTypeDeclareStatement(cqlCollection.Key))
		}

		fmt.Fprintf(buf, "%s>", GetCqlTypeDeclareStatement(cqlCollection.Elem))

	} else if cqlUDT, ok := cqlType.(gocql.UDTTypeInfo); ok {
		fmt.Fprintf(buf, "%s", cqlUDT.Name)
	} else if cqlTuple, ok := cqlType.(gocql.TupleTypeInfo); ok {
		var elemTypStrs []string
		for _, elem := range cqlTuple.Elems {
			elemTypStrs = append(elemTypStrs, GetCqlTypeDeclareStatement(elem))
		}
		fmt.Fprintf(buf, "%s<%s>", cqlTuple.NativeType.Type().String(), strings.Join(elemTypStrs, ", "))
	} else {
		if cqlType.Type() == gocql.TypeCustom {
			fmt.Fprintf(buf, "%s", cqlType.Custom())
		} else {
			fmt.Fprintf(buf, "%s", cqlType.Type().String())
		}

	}

	return buf.String()
}

func ScanUDTs(cqlType gocql.TypeInfo) []gocql.UDTTypeInfo {
	var udtColInfo []gocql.UDTTypeInfo

	if cqlUDT, ok := cqlType.(gocql.UDTTypeInfo); ok {
		udtColInfo = append(udtColInfo, cqlUDT)
		for _, udtField := range cqlUDT.Elements {
			udtColInfo = append(udtColInfo, ScanUDTs(udtField.Type)...)
		}

	} else if cqlCollection, ok := cqlType.(gocql.CollectionType); ok {
		if cqlCollection.Key != nil {
			udtColInfo = append(udtColInfo, ScanUDTs(cqlCollection.Key)...)
		}
		udtColInfo = append(udtColInfo, ScanUDTs(cqlCollection.Elem)...)
	} else if cqlTuple, ok := cqlType.(gocql.TupleTypeInfo); ok {
		for _, e := range cqlTuple.Elems {
			udtColInfo = append(udtColInfo, ScanUDTs(e)...)
		}
	}

	return udtColInfo
}

func GetCqlCreateUDTStatement(cqlUDT gocql.UDTTypeInfo) string {
	var udtFieldsDeclareSts []string
	for _, udtField := range cqlUDT.Elements {
		udtFieldsDeclareSts = append(udtFieldsDeclareSts, fmt.Sprintf("%s %s", udtField.Name, GetCqlTypeDeclareStatement(udtField.Type)))
	}

	return fmt.Sprintf("CREATE TYPE IF NOT EXISTS %s (%s)", cqlUDT.Name, strings.Join(udtFieldsDeclareSts, ", "))

}
