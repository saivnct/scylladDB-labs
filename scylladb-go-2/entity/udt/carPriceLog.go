package udt

import "time"

type CarPriceLog struct {
	Price     float64   `db:"price"`
	CreatedAt time.Time `db:"created_at"`
}

func (f CarPriceLog) UDTName() string {
	return "car_price_log"
}
