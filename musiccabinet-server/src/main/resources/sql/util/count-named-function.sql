create function util.count_functions(process_name varchar) returns int as $$
declare
	nr_of_functions int;
begin

	select count(*) into nr_of_functions from pg_proc where proname = process_name;
	return nr_of_functions;

end;
$$ language plpgsql;
