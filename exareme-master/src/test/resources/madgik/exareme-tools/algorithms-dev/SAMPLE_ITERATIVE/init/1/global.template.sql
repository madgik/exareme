requirevars 'defaultDB';
attach database '%{defaultDB}' as defaultDB;

-- "Algorithmic" part
-- counter_sum will contain: Σ_i (#workers * step_i)
-- Goal of algorithm: calculate the above dummy sum ^ .
drop table if exists defaultDB.sum_tbl;
create table defaultDB.sum_tbl AS
  select 0 as sum_val from range(1);

-- Iterations control part
-- This table serves for counting iterations number.
-- As we discussed with Eleni, this should be generated from iterations module (thus
-- the algorithms-developer doesn't need to worry about it).
select "ok";
