requirevars 'defaultDB';
attach database '%{defaultDB}' as defaultDB;

-- "Algorithmic" part: Display sum and number of iterations
select jdict(
  'sum_value', sum_tbl.sum_val,
  'number_of_iterations', iter_tbl.iterations_counter
  )
from
  defaultDB.sum_tbl as sum_tbl,
  iterationsDB.iterations_counter_tbl as iter_tbl
;
