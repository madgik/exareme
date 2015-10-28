-- Find all employees with salary more than 20000
distributed create table %1$s to 1 as
select ename, salary
from %2$s
where salary > 20000;
