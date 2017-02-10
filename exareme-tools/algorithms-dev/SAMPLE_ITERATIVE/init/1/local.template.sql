requirevars 'defaultDB';
attach database '%{defaultDB}' as defaultDB;

-- Keeping iterations state into defaultDB
-- Initialize steps table with 0
drop table if exists defaultDB.step_tbl;
create table defaultDB.step_tbl AS
  select 0 as step from range(1);

-- No output here, simply a dummy "ok".
select "ok";