requirevars 'defaultDB';
attach database '%{defaultDB}' as defaultDB;

-- Exareme's iteration module will access this table's value
-- before submitting again the stepDFL script.
-- This will be generated if the 'iterations_condition_query_provided' property is set to false.
-- update iterationsDB.iterations_condition_check_result_tbl set iterations_condition_check_result = (
--   select iterations_counter < cast(%{iterations_max_number} as decimal)
--   from iterationsDB.iterations_counter_tbl
-- );

-- This is the query that will be generated and appended if the 'iterations_condition_query_provided'
-- property is set to true.
-- The algorithm-specific query will have been executed before this one, and thus, the actual algorithm
-- condition check result will be used in conjunction with the maximum iterations condition result
-- in a bitwise AND operation to determine whether iterations must continue.
-- update iterationsDB.iterations_condition_check_result_tbl set iterations_condition_check_result = (
--   select max_iterations_condition_result & iterations_condition_check_result
--   from
--   (
--     select iterations_counter < cast(%{iterations_max_number} as decimal) as max_iterations_condition_result
--     from iterationsDB.iterations_counter_tbl
--   ), iterations_condition_check_result_tbl
-- );

update iterationsDB.iterations_condition_check_result_tbl set iterations_condition_check_result = (
  select sum_tbl.sum_val < 5
  from defaultDB.sum_tbl
);

select "ok";
