requirevars 'iterationsDB';
attach database '%{iterationsDB}' as iterationsDB;
requirevars 'defaultDB' 'input_global_tbl';
attach database '%{defaultDB}' as defaultDB;

create temp table tmp_sum_tbl as
select sum(step) as tmp_sum_val from %{input_global_tbl};

update defaultDB.sum_tbl set sum_val = (
  select sum_tbl.sum_val + tmp_sum_tbl.tmp_sum_val
  from
    defaultDB.sum_tbl as sum_tbl, tmp_sum_tbl
);


select "ok";