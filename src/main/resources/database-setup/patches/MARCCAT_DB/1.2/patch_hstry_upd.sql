\echo PATCH HISTORY UPDATE - Start -------------------------------------------------------
\echo
update olisuite.s_patch_history
set component = 'MARCCAT DB PLPGSQL'
where component_typ = 11;
\echo
update olisuite.s_patch_history
set component = 'MARCCAT DB'
where component_typ = 12;
\echo
INSERT INTO olisuite.s_patch_history(component, release_number, service_pack_number, component_typ)
VALUES('MARCCAT DB', 1, 2, 12);
\echo
\echo PATCH HISTORY UPDATE - End ---------------------------------------------------------
