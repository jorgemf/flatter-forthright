package com.livae.ff.app;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

public class Constants {

	public static final String MARKET_PACKAGE_ID = "com.android.vending";

	public static final String ACCOUNT_TYPE = "com.livae.pensamientos";

	public static final String ACCOUNT_SUFFIX = (BuildConfig.DEV ? "-dev" : "");

	public enum COUNTRY {
		AC(R.string.country_ac, "+247"/*, R.drawable.flag_ac*/),
		AD(R.string.country_ad, "+376", R.drawable.flag_ad),
		AE(R.string.country_ae, "+971", R.drawable.flag_ae),
		AF(R.string.country_af, "+93", R.drawable.flag_af),
		AG(R.string.country_ag, "+1-268", R.drawable.flag_ag),
		AI(R.string.country_ai, "+1-264", R.drawable.flag_ai),
		AL(R.string.country_al, "+355", R.drawable.flag_al),
		AM(R.string.country_am, "+374", R.drawable.flag_am),
		AN(R.string.country_an, "+599"/*, R.drawable.flag_an*/),
		AO(R.string.country_ao, "+244", R.drawable.flag_ao),
		//		AQ(R.string.country_aq, "+", R.drawable.flag_aq),
		AR(R.string.country_ar, "+54", R.drawable.flag_ar),
		AS(R.string.country_as, "+1-684", R.drawable.flag_as),
		AT(R.string.country_at, "+43", R.drawable.flag_at),
		AU(R.string.country_au, "+61", R.drawable.flag_au),
		AW(R.string.country_aw, "+297", R.drawable.flag_aw),
		AX(R.string.country_ax, "+358-18", R.drawable.flag_ax),
		AZ(R.string.country_az, "+374-97", R.drawable.flag_az),
		AZ2(R.string.country_az, "+994", R.drawable.flag_az),
		BA(R.string.country_ba, "+387", R.drawable.flag_ba),
		BB(R.string.country_bb, "+1-246", R.drawable.flag_bb),
		BD(R.string.country_bd, "+880", R.drawable.flag_bd),
		BE(R.string.country_be, "+32", R.drawable.flag_be),
		BF(R.string.country_bf, "+226", R.drawable.flag_bf),
		BG(R.string.country_bg, "+359", R.drawable.flag_bg),
		BH(R.string.country_bh, "+973", R.drawable.flag_bh),
		BI(R.string.country_bi, "+257", R.drawable.flag_bi),
		BJ(R.string.country_bj, "+229", R.drawable.flag_bj),
		//		BL(R.string.country_bl, "+", R.drawable.flag_bl),
		BM(R.string.country_bm, "+1-441", R.drawable.flag_bm),
		BN(R.string.country_bn, "+673", R.drawable.flag_bn),
		BO(R.string.country_bo, "+591", R.drawable.flag_bo),
		//		BQ(R.string.country_bq, "+"/*, R.drawable.flag_bq*/),
		BR(R.string.country_br, "+55", R.drawable.flag_br),
		BS(R.string.country_bs, "+1-242", R.drawable.flag_bs),
		BT(R.string.country_bt, "+975", R.drawable.flag_bt),
		//		BV(R.string.country_bv, "+"/*, R.drawable.flag_bv*/),
		BW(R.string.country_bw, "+267", R.drawable.flag_bw),
		BY(R.string.country_by, "+375", R.drawable.flag_by),
		BZ(R.string.country_bz, "+501", R.drawable.flag_bz),
		CA(R.string.country_ca, "+1", R.drawable.flag_ca),
		CC(R.string.country_cc, "+61", R.drawable.flag_cc),
		CD(R.string.country_cd, "+243", R.drawable.flag_cd),
		CF(R.string.country_cf, "+236", R.drawable.flag_cf),
		CG(R.string.country_cg, "+242", R.drawable.flag_cg),
		CH(R.string.country_ch, "+41", R.drawable.flag_ch),
		CI(R.string.country_ci, "+225", R.drawable.flag_ci),
		CK(R.string.country_ck, "+682", R.drawable.flag_ck),
		CL(R.string.country_cl, "+56", R.drawable.flag_cl),
		CM(R.string.country_cm, "+237", R.drawable.flag_cm),
		CN(R.string.country_cn, "+86", R.drawable.flag_cn),
		CO(R.string.country_co, "+57", R.drawable.flag_co),
		CR(R.string.country_cr, "+506", R.drawable.flag_cr),
		//		CS(R.string.country_cs, "+381"/*, R.drawable.flag_cs*/),
		CU(R.string.country_cu, "+53", R.drawable.flag_cu),
		CV(R.string.country_cv, "+238", R.drawable.flag_cv),
		//		CW(R.string.country_cw, "+", R.drawable.flag_cw),
		CX(R.string.country_cx, "+61", R.drawable.flag_cx),
		CY(R.string.country_cy, "+90-392", R.drawable.flag_cy),
		CY2(R.string.country_cy, "+357", R.drawable.flag_cy),
		CZ(R.string.country_cz, "+420", R.drawable.flag_cz),
		DE(R.string.country_de, "+49", R.drawable.flag_de),
		DJ(R.string.country_dj, "+253", R.drawable.flag_dj),
		DK(R.string.country_dk, "+45", R.drawable.flag_dk),
		DM(R.string.country_dm, "+1-767", R.drawable.flag_dm),
		DO(R.string.country_do, "+1-809", R.drawable.flag_do),
		DO2(R.string.country_do, "+1-829", R.drawable.flag_do),
		DZ(R.string.country_dz, "+213", R.drawable.flag_dz),
		EC(R.string.country_ec, "+593", R.drawable.flag_ec),
		EE(R.string.country_ee, "+372", R.drawable.flag_ee),
		EG(R.string.country_eg, "+20", R.drawable.flag_eg),
		EH(R.string.country_eh, "+212", R.drawable.flag_eh),
		ER(R.string.country_er, "+291", R.drawable.flag_er),
		ES(R.string.country_es, "+34", R.drawable.flag_es),
		ET(R.string.country_et, "+251", R.drawable.flag_et),
		FI(R.string.country_fi, "+358", R.drawable.flag_fi),
		FJ(R.string.country_fj, "+679", R.drawable.flag_fj),
		FK(R.string.country_fk, "+500", R.drawable.flag_fk),
		FM(R.string.country_fm, "+691", R.drawable.flag_fm),
		FO(R.string.country_fo, "+298", R.drawable.flag_fo),
		FR(R.string.country_fr, "+33", R.drawable.flag_fr),
		GA(R.string.country_ga, "+241", R.drawable.flag_ga),
		GB(R.string.country_gb, "+44", R.drawable.flag_gb),
		GD(R.string.country_gd, "+1-473", R.drawable.flag_gd),
		GE(R.string.country_ge, "+995", R.drawable.flag_ge),
		GF(R.string.country_gf, "+594"/*, R.drawable.flag_gf*/),
		GG(R.string.country_gg, "+44", R.drawable.flag_gg),
		GH(R.string.country_gh, "+233", R.drawable.flag_gh),
		GI(R.string.country_gi, "+350", R.drawable.flag_gi),
		GL(R.string.country_gl, "+299", R.drawable.flag_gl),
		GM(R.string.country_gm, "+220", R.drawable.flag_gm),
		GN(R.string.country_gn, "+224", R.drawable.flag_gn),
		GP(R.string.country_gp, "+590"/*, R.drawable.flag_gp*/),
		GQ(R.string.country_gq, "+240", R.drawable.flag_gq),
		GR(R.string.country_gr, "+30", R.drawable.flag_gr),
		//		GS(R.string.country_gs, "+", R.drawable.flag_gs),
		GT(R.string.country_gt, "+502", R.drawable.flag_gt),
		GU(R.string.country_gu, "+1-671", R.drawable.flag_gu),
		GW(R.string.country_gw, "+245", R.drawable.flag_gw),
		GY(R.string.country_gy, "+592", R.drawable.flag_gy),
		HK(R.string.country_hk, "+852", R.drawable.flag_hk),
		//		HM(R.string.country_hm, "+"/*, R.drawable.flag_hm*/),
		HN(R.string.country_hn, "+504", R.drawable.flag_hn),
		HR(R.string.country_hr, "+385", R.drawable.flag_hr),
		HT(R.string.country_ht, "+509", R.drawable.flag_ht),
		HU(R.string.country_hu, "+36", R.drawable.flag_hu),
		ID(R.string.country_id, "+62", R.drawable.flag_id),
		IE(R.string.country_ie, "+353", R.drawable.flag_ie),
		IL(R.string.country_il, "+972", R.drawable.flag_il),
		IM(R.string.country_im, "+44", R.drawable.flag_im),
		IN(R.string.country_in, "+91", R.drawable.flag_in),
		IO(R.string.country_io, "+246"/*, R.drawable.flag_io*/),
		IQ(R.string.country_iq, "+964", R.drawable.flag_iq),
		IR(R.string.country_ir, "+98", R.drawable.flag_ir),
		IS(R.string.country_is, "+354", R.drawable.flag_is),
		IT(R.string.country_it, "+39", R.drawable.flag_it),
		JE(R.string.country_je, "+44", R.drawable.flag_je),
		JM(R.string.country_jm, "+1-876", R.drawable.flag_jm),
		JO(R.string.country_jo, "+962", R.drawable.flag_jo),
		JP(R.string.country_jp, "+81", R.drawable.flag_jp),
		KE(R.string.country_ke, "+254", R.drawable.flag_ke),
		KG(R.string.country_kg, "+996", R.drawable.flag_kg),
		KH(R.string.country_kh, "+855", R.drawable.flag_kh),
		KI(R.string.country_ki, "+686", R.drawable.flag_ki),
		KM(R.string.country_km, "+269", R.drawable.flag_km),
		KN(R.string.country_kn, "+1-869", R.drawable.flag_kn),
		KP(R.string.country_kp, "+850"/*, R.drawable.flag_kp*/),
		KR(R.string.country_kr, "+82", R.drawable.flag_kr),
		KW(R.string.country_kw, "+965", R.drawable.flag_kw),
		KY(R.string.country_ky, "+1-345", R.drawable.flag_ky),
		KZ(R.string.country_kz, "+7", R.drawable.flag_kz),
		LA(R.string.country_la, "+856", R.drawable.flag_la),
		LB(R.string.country_lb, "+961", R.drawable.flag_lb),
		LC(R.string.country_lc, "+1-758", R.drawable.flag_lc),
		LI(R.string.country_li, "+423", R.drawable.flag_li),
		LK(R.string.country_lk, "+94", R.drawable.flag_lk),
		LR(R.string.country_lr, "+231", R.drawable.flag_lr),
		LS(R.string.country_ls, "+266", R.drawable.flag_ls),
		LT(R.string.country_lt, "+370", R.drawable.flag_lt),
		LU(R.string.country_lu, "+352", R.drawable.flag_lu),
		LV(R.string.country_lv, "+371", R.drawable.flag_lv),
		LY(R.string.country_ly, "+218", R.drawable.flag_ly),
		MA(R.string.country_ma, "+212", R.drawable.flag_ma),
		MC(R.string.country_mc, "+377", R.drawable.flag_mc),
		MD(R.string.country_md, "+373", R.drawable.flag_md),
		ME(R.string.country_me, "+382", R.drawable.flag_me),
		//		MF(R.string.country_mf, "+", R.drawable.flag_mf),
		MG(R.string.country_mg, "+261", R.drawable.flag_mg),
		MH(R.string.country_mh, "+692", R.drawable.flag_mh),
		MK(R.string.country_mk, "+389", R.drawable.flag_mk),
		ML(R.string.country_ml, "+223", R.drawable.flag_ml),
		MM(R.string.country_mm, "+95", R.drawable.flag_mm),
		MN(R.string.country_mn, "+976", R.drawable.flag_mn),
		MO(R.string.country_mo, "+853", R.drawable.flag_mo),
		MP(R.string.country_mp, "+1-670", R.drawable.flag_mp),
		MQ(R.string.country_mq, "+596", R.drawable.flag_mq),
		MR(R.string.country_mr, "+222", R.drawable.flag_mr),
		MS(R.string.country_ms, "+1-664", R.drawable.flag_ms),
		MT(R.string.country_mt, "+356", R.drawable.flag_mt),
		MU(R.string.country_mu, "+230", R.drawable.flag_mu),
		MV(R.string.country_mv, "+960", R.drawable.flag_mv),
		MW(R.string.country_mw, "+265", R.drawable.flag_mw),
		MX(R.string.country_mx, "+52", R.drawable.flag_mx),
		MY(R.string.country_my, "+60", R.drawable.flag_my),
		MZ(R.string.country_mz, "+258", R.drawable.flag_mz),
		NA(R.string.country_na, "+264", R.drawable.flag_na),
		NC(R.string.country_nc, "+687", R.drawable.flag_nc),
		NE(R.string.country_ne, "+227", R.drawable.flag_ne),
		NF(R.string.country_nf, "+672", R.drawable.flag_nf),
		NG(R.string.country_ng, "+234", R.drawable.flag_ng),
		NI(R.string.country_ni, "+505", R.drawable.flag_ni),
		NL(R.string.country_nl, "+31", R.drawable.flag_nl),
		NO(R.string.country_no, "+47", R.drawable.flag_no),
		NP(R.string.country_np, "+977", R.drawable.flag_np),
		NR(R.string.country_nr, "+674", R.drawable.flag_nr),
		NU(R.string.country_nu, "+683", R.drawable.flag_nu),
		NZ(R.string.country_nz, "+64", R.drawable.flag_nz),
		OM(R.string.country_om, "+968", R.drawable.flag_om),
		PA(R.string.country_pa, "+507", R.drawable.flag_pa),
		PE(R.string.country_pe, "+51", R.drawable.flag_pe),
		PF(R.string.country_pf, "+689", R.drawable.flag_pf),
		PG(R.string.country_pg, "+675", R.drawable.flag_pg),
		PH(R.string.country_ph, "+63", R.drawable.flag_ph),
		PK(R.string.country_pk, "+92", R.drawable.flag_pk),
		PL(R.string.country_pl, "+48", R.drawable.flag_pl),
		PM(R.string.country_pm, "+508"/*, R.drawable.flag_pm*/),
		//		PN(R.string.country_pn, "+", R.drawable.flag_pn),
		PR(R.string.country_pr, "+1-787", R.drawable.flag_pr),
		PR2(R.string.country_pr, "+1-939", R.drawable.flag_pr),
		PS(R.string.country_ps, "+970", R.drawable.flag_ps),
		PT(R.string.country_pt, "+351", R.drawable.flag_pt),
		PW(R.string.country_pw, "+680", R.drawable.flag_pw),
		PY(R.string.country_py, "+595", R.drawable.flag_py),
		QA(R.string.country_qa, "+974", R.drawable.flag_qa),
		RE(R.string.country_re, "+262"/*, R.drawable.flag_re*/),
		RO(R.string.country_ro, "+40", R.drawable.flag_ro),
		RS(R.string.country_rs, "+381", R.drawable.flag_rs),
		RU(R.string.country_ru, "+7", R.drawable.flag_ru),
		RW(R.string.country_rw, "+250", R.drawable.flag_rw),
		SA(R.string.country_sa, "+966", R.drawable.flag_sa),
		SB(R.string.country_sb, "+677", R.drawable.flag_sb),
		SC(R.string.country_sc, "+248", R.drawable.flag_sc),
		SD(R.string.country_sd, "+249", R.drawable.flag_sd),
		SE(R.string.country_se, "+46", R.drawable.flag_se),
		SG(R.string.country_sg, "+65", R.drawable.flag_sg),
		SH(R.string.country_sh, "+290", R.drawable.flag_sh),
		SI(R.string.country_si, "+386", R.drawable.flag_si),
		SJ(R.string.country_sj, "+47"/*, R.drawable.flag_sj*/),
		SK(R.string.country_sk, "+421", R.drawable.flag_sk),
		SL(R.string.country_sl, "+232", R.drawable.flag_sl),
		SM(R.string.country_sm, "+378", R.drawable.flag_sm),
		SN(R.string.country_sn, "+221", R.drawable.flag_sn),
		SO(R.string.country_so, "+252", R.drawable.flag_so),
		SR(R.string.country_sr, "+597", R.drawable.flag_sr),
		//		SS(R.string.country_ss, "+", R.drawable.flag_ss),
		ST(R.string.country_st, "+239", R.drawable.flag_st),
		SV(R.string.country_sv, "+503", R.drawable.flag_sv),
		//		SX(R.string.country_sx, "+"/*, R.drawable.flag_sx*/),
		SY(R.string.country_sy, "+963", R.drawable.flag_sy),
		SZ(R.string.country_sz, "+268", R.drawable.flag_sz),
		TA(R.string.country_ta, "+290"/*, R.drawable.flag_ta*/),
		TC(R.string.country_tc, "+1-649", R.drawable.flag_tc),
		TD(R.string.country_td, "+235", R.drawable.flag_td),
		//		TF(R.string.country_tf, "+", R.drawable.flag_tf),
		TG(R.string.country_tg, "+228", R.drawable.flag_tg),
		TH(R.string.country_th, "+66", R.drawable.flag_th),
		TJ(R.string.country_tj, "+992", R.drawable.flag_tj),
		TK(R.string.country_tk, "+690", R.drawable.flag_tk),
		TL(R.string.country_tl, "+670"/*, R.drawable.flag_tl*/),
		TM(R.string.country_tm, "+993", R.drawable.flag_tm),
		TN(R.string.country_tn, "+216", R.drawable.flag_tn),
		TO(R.string.country_to, "+676", R.drawable.flag_to),
		TR(R.string.country_tr, "+90", R.drawable.flag_tr),
		TT(R.string.country_tt, "+1-868", R.drawable.flag_tt),
		TV(R.string.country_tv, "+688", R.drawable.flag_tv),
		TW(R.string.country_tw, "+886", R.drawable.flag_tw),
		TZ(R.string.country_tz, "+255", R.drawable.flag_tz),
		UA(R.string.country_ua, "+380", R.drawable.flag_ua),
		UG(R.string.country_ug, "+256", R.drawable.flag_ug),
		//		UM(R.string.country_um, "+"/*, R.drawable.flag_um*/),
		US(R.string.country_us, "+1", R.drawable.flag_us),
		UY(R.string.country_uy, "+598", R.drawable.flag_uy),
		UZ(R.string.country_uz, "+998", R.drawable.flag_uz),
		VA(R.string.country_va, "+379", R.drawable.flag_va),
		VC(R.string.country_vc, "+1-784", R.drawable.flag_vc),
		VE(R.string.country_ve, "+58", R.drawable.flag_ve),
		VG(R.string.country_vg, "+1-284", R.drawable.flag_vg),
		VI(R.string.country_vi, "+1-340", R.drawable.flag_vi),
		VN(R.string.country_vn, "+84", R.drawable.flag_vn),
		VU(R.string.country_vu, "+678", R.drawable.flag_vu),
		WF(R.string.country_wf, "+681", R.drawable.flag_wf),
		WS(R.string.country_ws, "+685", R.drawable.flag_ws),
		YE(R.string.country_ye, "+967", R.drawable.flag_ye),
		YT(R.string.country_yt, "+262", R.drawable.flag_yt),
		ZA(R.string.country_za, "+27", R.drawable.flag_za),
		ZM(R.string.country_zm, "+260", R.drawable.flag_zm),
		ZW(R.string.country_zw, "+263", R.drawable.flag_zw);

		private final int countryStringResId;

		private final int countryFlagResId;

		private final String phonePrefix;

		private COUNTRY(@StringRes int countryStringResId, String phonePrefix) {
			this(countryStringResId, phonePrefix, 0);
		}

		private COUNTRY(@StringRes int countryStringResId, String phonePrefix,
						@DrawableRes int countryFlagResId) {
			this.countryStringResId = countryStringResId;
			this.countryFlagResId = countryFlagResId;
			this.phonePrefix = phonePrefix;
		}

		public int getCountryStringResId() {
			return countryStringResId;
		}

		public int getCountryFlagResId() {
			return countryFlagResId;
		}

		public String getPhonePrefix() {
			return phonePrefix;
		}

	}

}
