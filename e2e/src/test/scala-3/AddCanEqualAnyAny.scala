trait AddCanEqualAnyAny {
  given CanEqual[Any, Any] = CanEqual.derived
}
