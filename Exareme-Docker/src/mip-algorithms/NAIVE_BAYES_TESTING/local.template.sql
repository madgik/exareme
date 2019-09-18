requirevars  'defaultDB' 'model' 'x' 'y' 'iterationNumber' ;
attach database '%{defaultDB}' as defaultDB;

-- var 'model'  '';
-- var 'iterationNumber' 0;
-- var 'model' from select tabletojson(colname,val,classval,average,sigma,probability, "colname,val,classval,average,sigma,probability",0)  as model from defaultdb.global_probabilities;
-------------------

drop table if exists defaultDB.local_probabilities;
create table defaultDB.local_probabilities as  select colname,val,classval,average,sigma,probability from (select jsontotable('%{model}'));

drop table if exists defaultDB.testingset;
create table defaultDB.testingset as
select rowid as rid ,%{x},%{y} from defaultdb.localinputtblflat where idofset in (select %{iterationNumber});

--var 'file' from select  'Testingset'||%{iterationNumber}||'.csv';
--output '%{file}' header:t  select * from defaultDB.testingset;

drop table if exists defaultDB.tempprobabilities;
create table defaultDB.tempprobabilities (rid,colname,val,classval,probability) ;
--Compute probability density function for the normal distribution (For the non categorical values)

var 'computeprobabilities' from select create_complex_query("","
  insert into defaultDB.tempprobabilities
  select rid, '?' as colname, ? as val, classval, normpdf(?,average,sigma) as probability
  from defaultDB.testingset ,defaultDB.local_probabilities
  where colname in (select code from defaultDB.localmetadatatbl where categorical = 0) and colname ='?'; ", "" , "" , '%{x}');
%{computeprobabilities};
--
--For the categorical values
var 'computeprobabilities' from select create_complex_query("","
insert into defaultDB.tempprobabilities
select rid, colname, ? as val ,classval, probability from
(select rid, ?, %{y} from defaultDB.testingset),
(select colname,val,classval, probability from defaultDB.local_probabilities where colname ='?')
where colname in (select code from defaultDB.localmetadatatbl where categorical = 1) and colname='?' and val = ? order by rid;", "" , "" , '%{x}');
%{computeprobabilities};

--For the classname
insert into defaultDB.tempprobabilities
select rid,colname,val,classval,probability from
(select rid from defaultDB.testingset),
(select * from defaultDB.local_probabilities as probabilities_table where colname = '%{y}');

drop table if exists defaultDB.tempposteriorprobability; -- Mono ton arithmith ypologizw. Arkei gia na kanw thn sugkrish
create table defaultDB.tempposteriorprobability as
select rid,classval as classval,FMULT(probability) as probability
from defaultDB.tempprobabilities
group by rid,classval;

drop table if exists defaultDB.posteriorprobability; --Normalize results
create table defaultDB.posteriorprobability as
select rid,classval,probability/totalprobability as probability
from defaultDB.tempposteriorprobability,
     (select rid as rid1,sum(probability) as totalprobability from defaultDB.tempposteriorprobability group by rid)
where rid=rid1;

--select * from defaultDB.posteriorprobability;

--------------------------------------------

drop table if exists defaultDB.local_predictions;
create table defaultDB.local_predictions as
select %{iterationNumber}, testingtable.val as actualclass, bayesnaiveresults.classval as predictedclass --, bayesnaiveresults.rid as rid
from (select rid,classval,max(probability) from defaultDB.posteriorprobability group by rid) as bayesnaiveresults,
	 (select rid, %{y} as val from defaultDB.testingset ) as testingtable
where bayesnaiveresults.rid=testingtable.rid;


drop table if exists defaultDB.local_tempconfusionmatrix;
create table defaultDB.local_tempconfusionmatrix as
select %{iterationNumber} as iterationNumber , val1 as actualclass,val2 as predictedclass, 0 as val from
(select distinct val as val1 from defaultDB.local_probabilities where colname ='%{y}'),
(select distinct val as val2 from defaultDB.local_probabilities where colname = '%{y}');

insert into defaultDB.local_tempconfusionmatrix
select %{iterationNumber} as iterationNumber,actualclass,predictedclass,count(*) as val
from ( select bayesnaiveresults.rid,testingtable.val as actualclass, bayesnaiveresults.classval as predictedclass, 1 as val
       from (select rid,classval,max(probability) from defaultDB.posteriorprobability group by rid) as bayesnaiveresults,
		    (select rid, %{y} as val from defaultDB.testingset ) as testingtable
       where bayesnaiveresults.rid=testingtable.rid)
group by actualclass,predictedclass;

drop table if exists defaultDB.local_confusionmatrix;
create table defaultDB.local_confusionmatrix as
select iterationNumber,actualclass,predictedclass,max(val) as val
from defaultDB.local_tempconfusionmatrix
group by iterationNumber,actualclass,predictedclass;

select * from defaultDB.local_confusionmatrix;
