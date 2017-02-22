requirevars 'defaultDB';
attach database '%{defaultDB}' as defaultDB;

-- Increment value by 1.
update defaultDB.step_tbl set step = 1 + step;

-- This is the output of the local step, and will be
-- passed to the global script as "input_local_tbl"
select * from defaultDB.step_tbl;