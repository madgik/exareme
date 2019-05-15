requirevars 'defaultDB' 'input_global_tbl' ;
attach database '%{defaultDB}' as defaultDB;

drop table if exists defaultDB.residuals;
create table defaultDB.residuals as

select min(min_e) as min_e, max(max_e) as max_e, sum(sum_e) as sum_e, sum(sum_ee) as sum_ee, sum(counte) as counte from %{input_global_tbl};

--select * from %{input_global_tbl};
select 'ok';
