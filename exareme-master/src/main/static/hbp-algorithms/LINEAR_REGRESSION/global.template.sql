requirevars 'input_global_tbl';

drop table if exists slope;
create table slope as
select FARITH('/','-','*',Sn,Sxy,'*',Sx1,Sy1,'-','*',Sn,Sx2,'*',Sx1,Sx1)  as b  --(n*xy-x1*y1)/(n*x2-x1*x1)
from ( select FSUM(x1) as Sx1, FSUM(x2) as Sx2, FSUM(y1) as Sy1, FSUM(xy) as Sxy, FSUM(n) as Sn
       from %{input_global_tbl});

drop table if exists intercept;
create table intercept as
select  FARITH ('/','-',Sy1,'*',b,Sx1,Sn) as a  --(y1-b*x1)/n
from slope,
      ( select FSUM(x1) as Sx1, FSUM(x2) as Sx2, FSUM(y1) as Sy1, FSUM(xy) as Sxy, FSUM(n) as Sn
        from %{input_global_tbl});

drop table if exists point;
create table point as
select FARITH('/', Sx1,Sn) as xx
from ( select FSUM(x1) as Sx1, FSUM(n) as Sn
             from %{input_global_tbl});

select a,b, xx from slope,intercept,point;

--hidden var 'xx' 64;
--create table regressionEquation as
--select a+b*var('xx')
--from slope, intercept;


