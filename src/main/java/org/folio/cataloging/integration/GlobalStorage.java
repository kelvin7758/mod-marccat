package org.folio.cataloging.integration;

import org.folio.cataloging.dao.*;
import org.folio.cataloging.dao.persistence.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Global constants of storage service.
 *
 * @author natasciab
 * @since 1.0
 */
public class GlobalStorage {
    public final static String HEADER_TYPE_LABEL = "HEADER_TYPE";
    public final static String FORM_OF_MATERIAL_LABEL = "FORM_OF_MATERIAL";
    public final static String MATERIAL_TYPE_CODE_LABEL = "MATERIAL_TYPE_CODE";
    public final static String MATERIAL_TAG_CODE = "008";

    public final static Map <String, Class> MAP_CODE_LISTS = new HashMap <String, Class>() {
        {
            //008-006
            put("DATE_TYPE", T_ITM_DTE_TYP.class);
            put("MODIFIED_RECORD_TYPE", T_REC_MDFTN.class);
            put("CATALOGUING_SOURCE", T_REC_CTLGG_SRC.class);
            put("BOOK_ILLUSTRATION", T_BOOK_ILSTN.class);
            put("TARGET_AUDIENCE", T_TRGT_AUDNC.class);
            put("FORM_OF_ITEM", T_FORM_OF_ITM.class);
            put("NATURE_OF_CONTENT", T_NTR_OF_CNTNT.class);
            put("GOV_PUBLICATION", T_GOVT_PBLTN.class);
            put("CONF_PUBLICATION", T_CONF_PBLTN.class);
            put("BOOK_FESTSCHRIFT", T_BOOK_FTSCT.class);
            put("BOOK_INDEX", T_BOOK_IDX_AVBTY.class);
            put("BOOK_LITERARY_FORM", T_BOOK_LTRY_FORM_TYP.class);
            put("BOOK_BIOGRAPHY", T_BOOK_BGPHY.class);
            put("MSC_FORM_OF_COMPOSITION", T_MSC_FORM_OR_TYP.class);
            put("MSC_FORMAT", T_MSC_FRMT.class);
            put("MSC_PARTS", T_MSC_PRT.class);
            put("MSC_TEXTUAL_MAT_CODE", T_MSC_TXTL_MTR.class);
            put("MSC_LITERARY_TEXT", T_MSC_LTRY_TXT.class);
            put("MSC_TRANSPOSITION_CODE", T_MSC_TRNSPSN_ARRNGMNT.class);
            put("SRL_FREQUENCY", T_SRL_FREQ.class);
            put("SRL_REGULARITY", T_SRL_REGTY.class);
            put("SRL_TYPE_CONTINUING_RESOURCE", T_SRL_TYP.class);
            put("SRL_FORM_ORGNL_ITEM", T_SRL_FORM_ORGNL_ITM.class);
            put("SRL_NATURE_OF_WORK", T_NTR_OF_CNTNT.class);
            put("SRL_ORIGIN_ALPHABET", T_SRL_TTL_ALPBT.class);
            put("SRL_ENTRY_CONVENTION", T_SRL_SCSV_LTST.class);
            put("MAP_RELIEF", T_CRTGC_RLF.class);
            put("MAP_PROJECTION", T_CRTGC_PRJTN.class);
            put("MAP_TYPE_MATERIAL", T_CRTGC_MTRL.class);
            put("MAP_INDEX", T_CRTGC_IDX_AVBTY.class);
            put("MAP_SPECIAL_FORMAT_CHARACTERISTIC", T_CRTGC_FRMT.class);
            put("VSL_TARGET_AUDIENCE", T_VSL_TRGT_AUDNC.class);
            put("VSL_TYPE_MATERIAL", T_VSL_MTRL_TYP.class);
            put("VSL_TECHNIQUE", T_VSL_TECH.class);
            put("COMPUTER_TARGET_AUDIENCE", T_CMPTR_TRGT_AUDNC.class);
            put("COMPUTER_FORM_OF_ITEM", T_CF_FORM_OF_ITM.class);
            put("COMPUTER_TYPE_MATERIAL", T_CMPTR_FIL_TYP.class);
            //007
            put("CATEGORY_MATERIAL", GeneralMaterialDesignation.class);
            put("SOUND_MEDIUM_OR_SEP", T_SND_MDM_OR_SEPRT.class);
            put("MEDIUM_FOR_SOUND", T_MDM_FOR_SND.class);
            put("MAP_SPEC_DESIGN", T_MAP_SMD.class);
            put("MAP_COLOR", T_MAP_CLR.class);
            put("MAP_PHYSICAL_MEDIUM", T_MAP_PHSCL_MDM.class);
            put("MAP_TYPE_OF_REPRODUCTION", T_MAP_RPRDT_TYP.class);
            put("MAP_PRODUCTION_DETAILS", T_MAP_PRDTN_DTL.class);
            put("MAP_POLARITY", T_MAP_PLRTY.class);
            put("CF_SPEC_DESIGN", T_CF_SMD.class);
            put("CF_COLOR", T_CF_CLR.class);
            put("CF_DIMENSIONS", T_CF_DMNSN.class);
            put("CF_FILE_FORMAT", T_CF_FF.class);
            put("CF_QUALITY_ASS", T_CF_QAT.class);
            put("CF_ANTECEDENT_SRC", T_CF_ANTSRC.class);
            put("CF_COMPRESSION_LVL", T_CF_LOC.class);
            put("CF_REFORMATTING_QUALITY", T_CF_RQ.class);
            put("GLB_SPEC_DESIGN", T_GLB_SMD.class);
            put("GLB_COLOR", T_GLB_CLR.class);
            put("GLB_PHYSICAL_MEDIUM", T_GLB_PHSCL_MDM.class);
            put("GLB_TYPE_OF_REPRODUCTION", T_GLB_RPDTN_TYP.class);
            put("TCT_SPEC_DESIGN", T_TM_SMD.class);
            put("TCT_CLASS_BRAILLE_WRITING", T_TM_CBW.class);
            put("TCT_CONTRACTION_LVL", T_TM_LC.class);
            put("TCT_BRAILLE_MUSIC_FORMAT", T_TM_BMF.class);
            put("TCT_SPECIAL_PHYSICAL_CHAR", T_TM_SPC.class);
            put("PG_SPEC_DESIGN", T_PG_SMD.class);
            put("PG_COLOR", T_PG_CLR.class);
            put("PG_EMUL_BASE", T_PG_BSE_OF_EMLSN_MTRL.class);
            put("PG_DIMENSIONS", T_PG_DMNSN.class);
            put("PG_SECONDARY_SUPPORT", T_PG_SCDRY_SPRT_MTRL.class);
            put("NPG_SPEC_DESIGN", T_NPG_SMD.class);
            put("NPG_COLOR", T_NPG_CLR.class);
            put("NPG_PRIMARY_SUPPORT", T_NPG_PRMRY_SPRT_MTRL.class);
            put("NPG_SECONDARY_SUPPORT", T_NPG_SCDRY_SPRT_MTRL.class);
            put("MP_SPEC_DESIGN", T_MP_SMD.class);
            put("MP_COLOR", T_MP_CLR.class);
            put("MP_PRESENT_FORMAT", T_MP_PRSTN_FRMT.class);
            put("MP_DIMENSIONS", T_MP_DMNSN.class);
            put("MP_CONF_PLAYBACK", T_MP_CONFIG.class);
            put("MP_PROD_ELEM", T_MP_PROD_ELEM.class);
            put("MP_POLARITY", T_MP_POS_NEG.class);
            put("MP_GENERATION", T_MP_GNRTN.class);
            put("MP_BASE_FILM", T_MP_BSE_FLM.class);
            put("MP_REFINE_CAT_COLOR", T_MP_RF_CLR.class);
            put("MP_KIND_COLORS", T_MP_CLR_STCK.class);
            put("MP_DETERIORATION_STAGE", T_MP_DTRTN_STGE.class);
            put("MP_COMPLETENESS", T_MP_CMPLT.class);
            put("KIT_SPEC_DESIGN", T_KIT_SMD.class);
            put("NMU_SPEC_DESIGN", T_NM_SMD.class);
            put("TXT_SPEC_DESIGN", T_TXT_SMD.class);
            put("UNS_SPEC_DESIGN", T_USP_SMD_CDE.class); //errata
            put("RSI_SPEC_DESIGN", T_RSI_SMD.class);
            put("RSI_ALTITUDE", T_RSI_ALT_SENS.class);
            put("RSI_ATTITUDE", T_RSI_ATT_SENS.class);
            put("RSI_CLOUD_COVER", T_RSI_CLD_CVR.class);
            put("RSI_PLAT_CONSTRUCTION", T_RSI_PLTFRM_CNSTRCT.class);
            put("RSI_PLAT_USE", T_RSI_PLTFRM_USE.class);
            put("RSI_SENSOR_TYPE", T_RSI_SNSR_TPE.class);
            put("RSI_DATA_TYPE", T_RSI_DATA_TPE.class);
            put("SND_SPEC_DESIGN", T_SND_SMD.class);
            put("SND_SPEED", T_SND_SPD.class);
            put("SND_CONF_PLAYBACK", T_SND_PLYBC_CHNL_CFGTN.class);
            put("SND_GROOVE_WIDTH", T_SND_DISC_GRV_WDTH.class);
            put("SND_DIMENSIONS", T_SND_DMNSN.class);
            put("SND_TAPE_WIDTH", T_SND_TAPE_WDTH.class);
            put("SND_TAPE_CONF", T_SND_TAPE_CFGTN.class);
            put("SND_DISC_TYPE", T_SND_DISC_CYLND_TYP.class);
            put("SND_MATERIAL_TYPE", T_SND_MTRL_TYP.class);
            put("SND_CUTTING", T_SND_DISC_CTG.class);
            put("SND_SPEC_PLAYBACK", T_SND_SPCL_PLYBC_CHAR.class);
            put("SND_STORAGE_TECNIQUE", T_SND_STRG_TECH.class);
            put("VR_SPEC_DESIGN", T_VR_SMD.class);
            put("VR_COLOR", T_VR_CLR.class);
            put("VR_FORMAT", T_VR_FRMT.class);
            put("VR_DIMENSIONS", T_VR_DMNSN.class);
            put("VR_CONF_PLAYBACK", T_VR_PLYBC_CHNL_CFGTN.class);
            put("MIC_COLOR", T_MIC_CLR.class);
            put("MIC_DIMENSIONS", T_MIC_DMNSN.class);
            put("MIC_BASE_FILM", T_MIC_FLM_BSE.class);
            put("MIC_EMUL_FILM", T_MIC_FLM_EMLSN.class);
            put("MIC_GENERATION", T_MIC_GNRTN.class);
            put("MIC_POLARITY", T_MIC_PLRTY.class);
            put("MIC_REDUCT_RATIO_RANGE", T_MIC_RDCTN_RATIO_RNG.class);
            put("MIC_SPEC_DESIGN", T_MIC_SMD.class);
        }
    };


    public final static Map <String, Class> DAO_CLASS_MAP = new HashMap <String, Class>() {
        {
            put("2P0",   NameDescriptorDAO.class);
            put("3P10",  NameDescriptorDAO.class);
            put("2P0",   NameDescriptorDAO.class);
            put("3P10",  NameDescriptorDAO.class);
            put("4P10",  NameDescriptorDAO.class);
            put("5P10",  NameDescriptorDAO.class);
            put("7P0",   TitleDescriptorDAO.class);
            put("9P0",   SubjectDescriptorDAO.class);
            put("230P",  PublisherNameDescriptorDAO.class);
            put("243P",  PublisherPlaceDescriptorDAO.class);
            put("250S",  NameTitleNameDescriptorDAO.class);
            put("251S",  NameTitleTitleDescriptorDAO.class);
            put("16P30", ControlNumberDescriptorDAO.class);
            put("18P2",  ControlNumberDescriptorDAO.class);
            put("19P2",  ControlNumberDescriptorDAO.class);
            put("20P3",  ControlNumberDescriptorDAO.class);
            put("21P2",  ControlNumberDescriptorDAO.class);
            put("22P10", ControlNumberDescriptorDAO.class);
            put("29P20", ControlNumberDescriptorDAO.class);
            put("30P4",  ControlNumberDescriptorDAO.class);
            put("31P3",  ControlNumberDescriptorDAO.class);
            put("32P3",  ControlNumberDescriptorDAO.class);
            put("33P3",  ControlNumberDescriptorDAO.class);
            put("34P20", ControlNumberDescriptorDAO.class);
            put("35P20", ControlNumberDescriptorDAO.class);
            put("36P20", ControlNumberDescriptorDAO.class);
            put("51P3",  ControlNumberDescriptorDAO.class);
            put("52P3",  ControlNumberDescriptorDAO.class);
            put("53P3",  ControlNumberDescriptorDAO.class);
            put("54P3",  ControlNumberDescriptorDAO.class);
            put("55P3",  ControlNumberDescriptorDAO.class);
            put("47P40", ClassificationDescriptorDAO.class);
            put("24P5",  ClassificationDescriptorDAO.class);
            put("25P5",  ClassificationDescriptorDAO.class);
            put("27P5",  ClassificationDescriptorDAO.class);
            put("23P5",  ClassificationDescriptorDAO.class);
            put("48P3",  ClassificationDescriptorDAO.class);
            put("46P40", ClassificationDescriptorDAO.class);
            put("50P3",  ClassificationDescriptorDAO.class);
            put("49P3",  ClassificationDescriptorDAO.class);
            put("326P1", ClassificationDescriptorDAO.class);
            put("353P1", ClassificationDescriptorDAO.class);
            put("303P3", ClassificationDescriptorDAO.class);
            put("28P30", ShelfListDAO.class);
            put("244P30",ShelfListDAO.class);
            put("47P30", ShelfListDAO.class);
            put("37P30", ShelfListDAO.class);
            put("38P30", ShelfListDAO.class);
            put("39P30", ShelfListDAO.class);
            put("41P30", ShelfListDAO.class);
            put("42P30", ShelfListDAO.class);
            put("43P30", ShelfListDAO.class);
            put("44P30", ShelfListDAO.class);
            put("45P30", ShelfListDAO.class);
            put("46P30", ShelfListDAO.class);
            put("373P0", SubjectDescriptorDAO.class);
       }
    };

    public final static Map <String, String> FILTER_MAP = new HashMap <String, String>() {
        {
            put("2P0", "");
            put("3P10", " and hdg.typeCode = 2 ");
            put("4P10", " and hdg.typeCode = 3 ");
            put("5P10", " and hdg.typeCode = 4 ");
            put("7P0", "");
            put("9P0", "");
            put("230P", "");
            put("243P", "");
            put("250S", "");
            put("251S", "");
            put("16P30", "");
            put("18P2", " and hdg.typeCode = 9 ");
            put("19P2", " and hdg.typeCode = 10 ");
            put("20P3", " and hdg.typeCode = 93 ");
            put("21P2", " and hdg.typeCode = 2 ");
            put("22P10", " and hdg.typeCode = 93 ");
            put("29P20", " and hdg.typeCode = 71 ");
            put("30P4", "");
            put("31P3", " and hdg.typeCode = 84 ");
            put("32P3", " and hdg.typeCode = 88 ");
            put("33P3", " and hdg.typeCode = 90 ");
            put("34P20", "");
            put("35P20", "");
            put("36P20", " and hdg.typeCode = 52 ");
            put("51P3", " and hdg.typeCode = 89 ");
            put("52P3", " and hdg.typeCode = 83 ");
            put("53P3", " and hdg.typeCode = 91 ");
            put("54P3", " and hdg.typeCode = 97 ");
            put("55P3", " and hdg.typeCode = 98 ");
            put("47P40", " and hdg.typeCode = 21");
            put("24P5", " and hdg.typeCode = 12");
            put("25P5", " and hdg.typeCode = 1");
            put("27P5", " and hdg.typeCode = 6");
            put("23P5", " and hdg.typeCode not in (1,6,10,11,12,14,15,29) ");
            put("48P3", " and hdg.typeCode = 10");
            put("46P40", " and hdg.typeCode = 11");
            put("50P3", " and hdg.typeCode = 14");
            put("49P3", " and hdg.typeCode = 15");
            put("326P1", " and hdg.typeCode = 29");
            put("28P30", " and hdg.typeCode = '@'");
            put("244P30", " and hdg.typeCode = 'N'");
            put("47P30", " and hdg.typeCode = 'M'");
            put("37P30", " and hdg.typeCode = '2'");
            put("38P30", " and hdg.typeCode = '3'");
            put("39P30", " and hdg.typeCode = '4'");
            put("41P30", " and hdg.typeCode = '6'");
            put("42P30", " and hdg.typeCode = 'A'");
            put("43P30", " and hdg.typeCode = 'C'");
            put("44P30", " and hdg.typeCode = 'E'");
            put("45P30", " and hdg.typeCode = 'F'");
            put("46P30", " and hdg.typeCode = 'G'");
            put("303P3", " and hdg.typeCode = 13");
            put("354P0", "");
            put("353P1", " and hdg.typeCode = 80");
            put("373P0", " and hdg.sourceCode = 4 ");
        }
    };
}
