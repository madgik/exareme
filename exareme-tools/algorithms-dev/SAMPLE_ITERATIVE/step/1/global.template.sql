requirevars 'defaultDB' 'input_global_tbl';
attach database '%{defaultDB}' as defaultDB;

-- "Algorithmic" part:
-- Sum workers' counters from input_global_tbl.
-- Exareme will gather the output of the local script into the
-- output_local_tbl in a merging phase into the input_global_tbl.
create temp table tmp_sum_tbl as
select sum(step) as tmp_sum_val from %{input_global_tbl};

update defaultDB.sum_tbl set sum_val = (
  select sum_tbl.sum_val + tmp_sum_tbl.tmp_sum_val
  from
    defaultDB.sum_tbl as sum_tbl, tmp_sum_tbl
);

select "ok";
-- Iterations control part: update iterations counter (this could be snippet generated
-- by iterations logic).
