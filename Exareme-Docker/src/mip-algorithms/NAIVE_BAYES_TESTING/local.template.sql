requirevars  'defaultDB' 'model' 'x' 'y' 'iterationNumber' ;
attach database '%{defaultDB}' as defaultDB;

drop table if exists local_probabilities;
create temp table local_probabilities as  select colname,val,classval,average,sigma,probability from (select jsontotable('%{model}'));

drop table if exists testingset;
create temp table testingset as
select rowid as rid ,%{x},%{y} from defaultdb.localinputtblflat where idofset in (select %{iterationNumber});

drop table if exists tempprobabilities;
create temp table tempprobabilities (rid,colname,val,classval,probability) ;
--Compute probability density function for the normal distribution (For the non categorical values)

var 'computeprobabilities' from select create_complex_query("","
  insert into tempprobabilities
  select rid, '?' as colname, ? as val, classval, normpdf(?,average,sigma) as probability
  from testingset ,local_probabilities
  where colname in (select code from defaultDB.localmetadatatbl where categorical = 0) and colname ='?'; ", "" , "" , '%{x}');
%{computeprobabilities};
--
--For the categorical values
var 'computeprobabilities' from select create_complex_query("","
insert into tempprobabilities
select rid, colname, ? as val ,classval, probability from
(select rid, ?, %{y} from testingset),
(select colname,val,classval, probability from local_probabilities where colname ='?')
where colname in (select code from defaultDB.localmetadatatbl where categorical = 1) and colname='?' and val = ? order by rid;", "" , "" , '%{x}');
%{computeprobabilities};

--For the classname
insert into tempprobabilities
select rid,colname,val,classval,probability from
(select rid from testingset),
(select * from local_probabilities as probabilities_table where colname = '%{y}');

drop table if exists tempposteriorprobability; -- Mono ton arithmith ypologizw. Arkei gia na kanw thn sugkrish
create temp table tempposteriorprobability as
select rid,classval as classval,FMULT(probability) as probability
from tempprobabilities
group by rid,classval;

drop table if exists posteriorprobability; --Normalize results
create temp table posteriorprobability as
select rid,classval,probability/totalprobability as probability
from tempposteriorprobability,
     (select rid as rid1,sum(probability) as totalprobability from tempposteriorprobability group by rid)
where rid=rid1;

--select * from defaultDB.posteriorprobability;

--------------------------------------------

drop table if exists local_predictions;
create temp table local_predictions as
select %{iterationNumber}, testingtable.val as actualclass, bayesnaiveresults.classval as predictedclass --, bayesnaiveresults.rid as rid
from (select rid,classval,max(probability) from posteriorprobability group by rid) as bayesnaiveresults,
	 (select rid, %{y} as val from testingset ) as testingtable
where bayesnaiveresults.rid=testingtable.rid;

drop table if exists local_tempconfusionmatrix;
create temp table local_tempconfusionmatrix as
select %{iterationNumber} as iterationNumber , val1 as actualclass,val2 as predictedclass, 0 as val from
(select distinct val as val1 from local_probabilities where colname ='%{y}'),
(select distinct val as val2 from local_probabilities where colname = '%{y}');

insert into local_tempconfusionmatrix
select %{iterationNumber} as iterationNumber,actualclass,predictedclass,count(*) as val
from ( select bayesnaiveresults.rid,testingtable.val as actualclass, bayesnaiveresults.classval as predictedclass, 1 as val
       from (select rid,classval,max(probability) from posteriorprobability group by rid) as bayesnaiveresults,
		    (select rid, %{y} as val from testingset ) as testingtable
       where bayesnaiveresults.rid=testingtable.rid)
group by actualclass,predictedclass;

drop table if exists local_confusionmatrix;
create temp table local_confusionmatrix as
select iterationNumber,actualclass,predictedclass,max(val) as val
from local_tempconfusionmatrix
group by iterationNumber,actualclass,predictedclass;

select * from local_confusionmatrix;
