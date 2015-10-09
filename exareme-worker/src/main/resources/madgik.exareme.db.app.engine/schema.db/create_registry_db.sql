-- Registry DB

-- Schema

create table if not exists sql(
  table_name text,
  sql_definition text,
  primary key(table_name)
);

create table if not exists partition(
  table_name text,
  partition_number integer,
  location text,
  partition_column text,
  primary key(table_name, partition_number, location, partition_column)
  foreign key(table_name) references sql(table_name)
);

create table if not exists table_index(
  index_name text,
  table_name text,
  column_name text,
  partition_number integer,
  primary key(index_name, partition_number),
  foreign key(table_name) references sql(table_name)
);

-- History

-- QL, DFL, EP
create table if not exists history(
  h_query_name text,
  h_session_id text,

);

-- Concrete
