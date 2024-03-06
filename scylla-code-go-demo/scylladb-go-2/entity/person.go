package entity

import (
	"giangbb.studio/scylladb/entity/udt"
	"time"
)

type Person struct {
	LastName       string            `db:"last_name" pk:"2" index:"true"`
	FirstName      string            `pk:"1" index:"true"` // not declare db:"first_name" -> default db:"first_name"
	FavoritePlace  udt.FavoritePlace `db:"favorite_place"`
	Email          string            `index:"true"` //not declare db:"email" -> default db:"email"
	StaticIP       string            `db:"static_ip" dbType:"inet"`
	Nicknames      []string          `db:"nick_names"`
	WorkingHistory map[int]string
	CreatedAt      time.Time `db:"created_at" ck:"1"`
}

func (p Person) TableName() string {
	return "person"
}
