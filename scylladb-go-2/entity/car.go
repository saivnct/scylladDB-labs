package entity

import (
	"giangbb.studio/scylladb/entity/tuple"
	"giangbb.studio/scylladb/entity/udt"
	"github.com/gocql/gocql"
)

type Car struct {
	Id                  gocql.UUID             `db:"id" pk:"1"`
	Brand               string                 `db:"brand"`
	Model               string                 `db:"model"`
	Year                int                    `db:"year" ck:"1"`
	Colors              []string               `db:"colors"`
	PriceLogs           []udt.CarPriceLog      `db:"price_logs"`
	Rewards             map[int]udt.CarReward  `db:"rewards"` //year - reward
	Matrix              [][]int                `db:"matrix"`
	Levels              []int                  `db:"levels"`
	Distributions       map[string]int         `db:"distributions"` //country - amount
	MatrixMap           map[string][][]float64 `db:"matrix_map"`    //country - [][]
	Machine             tuple.MachineTuple     //not declare db:"machine" -> default db:"machine"
	ThisIgnoreField     string                 `db:"-"`
	thisUnexportedField string
}

func (p Car) TableName() string {
	return "car"
}
