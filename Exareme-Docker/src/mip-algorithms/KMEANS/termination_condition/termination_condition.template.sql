requirevars 'defaultDB' 'x' 'e';
attach database '%{defaultDB}' as defaultDB;

--var 'e' 0.0001; --DELETE

var 'a' from select create_complex_query('','?_clval as ?_old',',','','%{x}') ;
var 'b' from select create_complex_query('','?_clval as ?_new',',','','%{x}') ;
var 'c' from select create_complex_query("max(","abs(?_old -?_new)",",",") as diff",'%{x}');
var 'maxdifference'  from  select max (diff) from (select %{c} from
(select clid as clid_old, %{a} from defaultDB.clustercenters_global),
(select clid as clid_new, %{b} from defaultDB.clustercentersnew_global)
where clid_old=clid_new);

update iterationsDB.iterations_condition_check_result_tbl set iterations_condition_check_result = ( select  %{maxdifference}> %{e});
select "ok";
