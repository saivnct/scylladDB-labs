package scylla

import (
	"errors"
	"fmt"
	"giangbb.studio/scylladb/entity"
	"giangbb.studio/scylladb/entity/udt"
	sliceUtils "giangbb.studio/scylladb/utils"
	"github.com/gocql/gocql"
	"github.com/scylladb/gocqlx/v2/table"
	"gopkg.in/inf.v0"
	"math/big"
	"reflect"
	"slices"
	"strconv"
	"strings"
	"time"
)

var (
	InvalidTableNameErr        = errors.New("invalid Table Name")
	NoColumnErr                = errors.New("no Column")
	ConflictColumnNameErr      = errors.New("conflict Column Name")
	InvalidPartitionKeyErr     = errors.New("invalid Partition Key")
	NoPartitionKeyErr          = errors.New("no Partition Key")
	InvalidClusterKeyErr       = errors.New("invalid Cluster Key")
	ConflictUDTFieldNameErr    = errors.New("conflict UDT Field Name")
	ConvertToDefaultCQLTypeErr = errors.New("failed convert to CQL Type")
)

func ParseTableMetaData(m entity.BaseModelInterface) (EntityInfo, error) {
	var entityInfo EntityInfo

	tableName := m.TableName()
	if len(m.TableName()) == 0 {
		return entityInfo, InvalidTableNameErr
	}

	t := reflect.TypeOf(m)

	//var columns []string
	var columns []ColumnInfo

	pKeyMap := map[int]string{}
	cKeyMap := map[int]string{}

	maxPkeyIndex := 0
	maxCkeyIndex := 0

	for i := 0; i < t.NumField(); i++ {
		field := t.Field(i)
		if field.PkgPath != "" { //ignore unexported field
			continue
		}

		cqlType, err := ConvertToDefaultCQLType(field.Type)
		if err != nil {
			return entityInfo, fmt.Errorf("%w -> field: %s -> %w", ConvertToDefaultCQLTypeErr, field.Name, err)
		}

		//log.Printf("Field Name: %s,\t Field Type: %s,\t Field Value: %s\n%s\n", field.Name, field.Type, v.Field(i).Interface(), GetCqlTypeInfo(cqlType))
		//log.Printf("(%s-%s): %s\n", field.Name, field.Type, GetCqlTypeInfo(cqlType))

		colName := strings.TrimSpace(field.Tag.Get("db"))
		if len(colName) > 0 && colName != "-" {
			idx := slices.IndexFunc(columns, func(c ColumnInfo) bool { return c.Name == colName })
			if idx >= 0 {
				return entityInfo, fmt.Errorf("%w -> column name: %s", ConflictColumnNameErr, colName)
			}

			columns = append(columns, ColumnInfo{
				Name: colName,
				Type: cqlType,
			})
		}

		pk := strings.TrimSpace(field.Tag.Get("pk"))
		if len(pk) > 0 {
			pkIndex, err := strconv.Atoi(pk)
			if err != nil {
				return entityInfo, fmt.Errorf("%w -> field: %s -> %w", InvalidPartitionKeyErr, field.Name, err)
			}
			if pkIndex <= 0 {
				return entityInfo, fmt.Errorf("%w -> field: %s -> wrong index format", InvalidPartitionKeyErr, field.Name)
			}
			pKeyMap[pkIndex] = colName
			if pkIndex > maxPkeyIndex {
				maxPkeyIndex = pkIndex
			}
		}

		ck := strings.TrimSpace(field.Tag.Get("ck"))
		if len(ck) > 0 {
			ckIndex, err := strconv.Atoi(ck)
			if err != nil {
				return entityInfo, fmt.Errorf("%w -> field: %s -> %w", InvalidClusterKeyErr, field.Name, err)
			}
			if ckIndex <= 0 {
				return entityInfo, fmt.Errorf("%w -> field: %s -> wrong index format", InvalidClusterKeyErr, field.Name)
			}
			cKeyMap[ckIndex] = colName
			if ckIndex > maxCkeyIndex {
				maxCkeyIndex = ckIndex
			}
		}
	}

	if len(columns) == 0 {
		return entityInfo, NoColumnErr
	}

	if len(pKeyMap) == 0 {
		return entityInfo, NoPartitionKeyErr
	}

	var pkeys []string
	for i := 1; i <= maxPkeyIndex; i++ {
		if pk, ok := pKeyMap[i]; ok {
			pkeys = append(pkeys, pk)
		} else {
			return entityInfo, fmt.Errorf("%w -> no column for index %d", InvalidPartitionKeyErr, i)
		}
	}

	var ckeys []string
	for i := 1; i <= maxCkeyIndex; i++ {
		if ck, ok := cKeyMap[i]; ok {
			ckeys = append(ckeys, ck)
		} else {
			return entityInfo, fmt.Errorf("%w -> no column for index %d", InvalidClusterKeyErr, i)
		}
	}

	entityInfo = EntityInfo{
		TableMetaData: table.Metadata{
			Name:    tableName,
			Columns: sliceUtils.Map(columns, func(c ColumnInfo) string { return c.Name }),
			PartKey: pkeys,
			SortKey: ckeys,
		},
		Columns: columns,
	}

	return entityInfo, nil
}

// ConvertToDefaultCQLType - based on gocql -> helpers.go -> goType()
func ConvertToDefaultCQLType(t reflect.Type) (gocql.TypeInfo, error) {

	switch t {
	case reflect.TypeOf(*new(string)):
		return gocql.NewNativeType(5, gocql.TypeText, ""), nil
	case reflect.TypeOf(*new(time.Duration)):
		return gocql.NewNativeType(5, gocql.TypeDuration, ""), nil
	case reflect.TypeOf(*new(time.Time)):
		return gocql.NewNativeType(5, gocql.TypeTimestamp, ""), nil
	case reflect.TypeOf(*new([]byte)):
		return gocql.NewNativeType(5, gocql.TypeBlob, ""), nil
	case reflect.TypeOf(*new(bool)):
		return gocql.NewNativeType(5, gocql.TypeBoolean, ""), nil
	case reflect.TypeOf(*new(float32)):
		return gocql.NewNativeType(5, gocql.TypeFloat, ""), nil
	case reflect.TypeOf(*new(float64)):
		return gocql.NewNativeType(5, gocql.TypeDouble, ""), nil
	case reflect.TypeOf(*new(int)):
		return gocql.NewNativeType(5, gocql.TypeInt, ""), nil
	case reflect.TypeOf(*new(int64)):
		return gocql.NewNativeType(5, gocql.TypeBigInt, ""), nil
	case reflect.TypeOf(*new(*big.Int)):
		return gocql.NewNativeType(5, gocql.TypeVarint, ""), nil
	case reflect.TypeOf(*new(int16)):
		return gocql.NewNativeType(5, gocql.TypeSmallInt, ""), nil
	case reflect.TypeOf(*new(int8)):
		return gocql.NewNativeType(5, gocql.TypeTinyInt, ""), nil
	case reflect.TypeOf(*new(*inf.Dec)):
		return gocql.NewNativeType(5, gocql.TypeDecimal, ""), nil
	case reflect.TypeOf(*new(gocql.UUID)):
		return gocql.NewNativeType(5, gocql.TypeUUID, ""), nil
	default:
		{
			if t.Kind() == reflect.Slice || t.Kind() == reflect.Array {
				elemTypeInfo, err := ConvertToDefaultCQLType(t.Elem())
				typeInfo := gocql.CollectionType{
					NativeType: gocql.NewNativeType(5, gocql.TypeList, ""),
					Elem:       elemTypeInfo,
				}
				return typeInfo, err
			} else if t.Kind() == reflect.Map {
				typeInfo := gocql.CollectionType{
					NativeType: gocql.NewNativeType(5, gocql.TypeMap, ""),
				}
				keyTypeInfo, err := ConvertToDefaultCQLType(t.Key())
				if err != nil {
					return typeInfo, err
				}
				elemTypeInfo, err := ConvertToDefaultCQLType(t.Elem())
				if err != nil {
					return typeInfo, err
				}

				typeInfo.Key = keyTypeInfo
				typeInfo.Elem = elemTypeInfo
				return typeInfo, nil
			} else if t.Implements(reflect.TypeOf((*udt.BaseUDTInterface)(nil)).Elem()) {

				var tVal reflect.Value = reflect.New(t)
				if baseUDT, ok := tVal.Elem().Interface().(udt.BaseUDTInterface); ok {
					typeInfo := gocql.UDTTypeInfo{
						NativeType: gocql.NewNativeType(5, gocql.TypeUDT, ""),
					}
					udtFields, err := ParseUDT(baseUDT)
					if err != nil {
						return typeInfo, err
					}
					typeInfo.Elements = udtFields
					return typeInfo, nil
				}
			}
			return gocql.NewNativeType(0, 0, ""), fmt.Errorf("cannot create Go type for unknown CQL type %s", t)
		}

	}
}

func ParseUDT(m udt.BaseUDTInterface) ([]gocql.UDTField, error) {
	t := reflect.TypeOf(m)

	var udtFields []gocql.UDTField
	var fieldNames []string

	for i := 0; i < t.NumField(); i++ {
		field := t.Field(i)
		if field.PkgPath != "" { //ignore unexported field
			continue
		}

		fieldName := strings.TrimSpace(field.Tag.Get("db"))
		if len(fieldName) > 0 && fieldName != "-" {
			idx := slices.IndexFunc(fieldNames, func(c string) bool { return c == fieldName })
			if idx >= 0 {
				return nil, fmt.Errorf("%w -> field name: %s", ConflictUDTFieldNameErr, fieldName)
			}
			fieldNames = append(fieldNames, fieldName)
		}

		cqlType, _ := ConvertToDefaultCQLType(field.Type)

		udtFields = append(udtFields, gocql.UDTField{
			Name: fieldName,
			Type: cqlType,
		})
	}

	return udtFields, nil
}
