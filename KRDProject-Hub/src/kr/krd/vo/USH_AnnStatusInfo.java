package kr.krd.vo;

import java.time.LocalDate;

public class USH_AnnStatusInfo {
	public final String statusCd; //OPEN/CLOSED
	public final LocalDate endDt; //마감일
	
	public USH_AnnStatusInfo(String statusCd, LocalDate endDt) {
		this.statusCd = statusCd;
		this.endDt = endDt;
	}
}
