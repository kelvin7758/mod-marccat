DROP TRIGGER IF EXISTS publ_acs_pnt_plus_refs_d on AMICUS.publ_acs_pnt_plus_refs;

CREATE TRIGGER publ_acs_pnt_plus_refs_d
AFTER DELETE ON amicus.publ_acs_pnt_plus_refs FOR EACH ROW
EXECUTE PROCEDURE amicus.trigger_fct_publ_acs_pnt_plus_refs_d();