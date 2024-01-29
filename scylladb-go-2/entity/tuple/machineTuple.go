package tuple

type MachineTuple struct {
	Name  string
	Year  int
	Brand string
}

func (m MachineTuple) Tuple() string { return "" }
