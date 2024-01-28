package entity

import (
	"giangbb.studio/scylladb/entity/udt"
	"time"
)

type Person struct {
	LastName      string            `db:"lastName" pk:"2"`
	FirstName     string            `db:"firstName" pk:"1"`
	FavoritePlace udt.FavoritePlace `db:"favorite_place"`
	Email         string            `db:"email"`
	CreatedAt     time.Time         `db:"createdAt" ck:"1"`
}

func (p Person) TableName() string {
	return "person"
}
