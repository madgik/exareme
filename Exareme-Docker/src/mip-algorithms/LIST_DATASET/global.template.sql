requirevars 'input_global_tbl';

drop table if exists finalresult;
create table finalresult as
select * from %{input_global_tbl};

select jdict("result",res) from (select jgroup(c1) as res from (select jdict("NodeName",who,"Patients",sum1,"Datasets",inform) as c1 from (select who,sum1,jgroup(val) as inform from finalresult group by who)));
