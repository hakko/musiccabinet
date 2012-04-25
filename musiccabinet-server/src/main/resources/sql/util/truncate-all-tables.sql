create or replace function util.truncate_all_tables() returns void as $$
declare
    statements cursor for
        select schemaname, tablename from pg_tables
        where tableowner = 'postgres' and schemaname in ('library', 'music')
        	and tablename not in ('webservice_calltype'); -- move this a separate package?
begin
    for stmt in statements loop
        execute 'truncate table ' ||  quote_ident(stmt.schemaname) || '.' ||
        	quote_ident(stmt.tablename) || ' cascade;';
    end loop;
end;
$$ language plpgsql;
