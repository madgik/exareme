f <- function(levels) {
  l0_net <- 367.6820687999996
  l0_load <- 2725.8433333333405

  l1_net <- 13.506289920000007
  l1_load <- 9.873333333333331

  l2_net <- 0.4347581039999998
  l2_load <- 0.495

  l3_net <- 0.0
  l3_load <- 0.0

  time <- l0_load / levels[1] + l1_load / levels[2] + l1_net / (50.0 * levels[2]) + l2_load / levels[3] + l2_net / (50.0 * levels[3]) + l3_load / levels[4] + l3_net / (50.0 * levels[4])

  sla0 <- 0.0 * 20.0 * exp(-time/1000.0)
  sla1 <- 0.0 * 200.0 * exp(-time/20.0)
  sla2 <- 0.0 * 100.0 * exp(-time/40.0)
  sla3 <- 587.0 * 50.0 * exp(-time/80.0)

  sla0 + sla1 + sla2 + sla3 - (levels[1] + levels[2] + levels[3] + levels[4]) * 0.2
}
