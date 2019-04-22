requirevars 'iterationsDB';
attach database '%{iterationsDB}' as iterationsDB;
requirevars 'defaultDB';
attach database '%{defaultDB}' as defaultDB;

-- "Algorithmic" part
-- counter_sum will contain: Σ_i (#workers * step_i)
-- Goal of algorithm: calculate the above dummy sum ^ .
drop table if exists defaultDB.sum_tbl;
create table defaultDB.sum_tbl AS
  select 0 as sum_val from range(1);

drop table if exists iterationsDB.iterations_counter_tbl;
create table iterationsDB.iterations_counter_tbl AS
  select 0 as iterations_counter from range(1);

drop table if exists iterationsDB.iterations_condition_check_result_tbl;
create table iterationsDB.iterations_condition_check_result_tbl AS
  select 1 as iterations_condition_check_result from range(1);

-- Iterations control part
-- This table serves for counting iterations number.
-- As we discussed with Eleni, this should be generated from iterations module (thus
-- the algorithms-developer doesn't need to worry about it).
select "ok";
