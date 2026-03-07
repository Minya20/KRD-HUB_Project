package kr.krd.constant;

public class RR_ReportConst {
    private RR_ReportConst() {}

    // 보고서 종류 (DB 값이 다르면 여기만 바꾸면 됨)
    public static final String TYPE_MID = "MID";     // 중간보고
    public static final String TYPE_FINAL = "FINAL"; // 최종보고

    // 보고서 상태 (DB 값)
    public static final String ST_SUBMITTED = "SUBMITTED";
    public static final String ST_APPROVED  = "APPROVED";
    public static final String ST_REJECTED  = "REJECTED";

    // 콘솔 표시용
    public static String toKor(String statusCd) {
        if (statusCd == null) return "미제출";
        switch (statusCd) {
            case ST_SUBMITTED: return "제출";
            case ST_APPROVED:  return "승인";
            case ST_REJECTED:  return "반려";
            default: return statusCd;
        }
    }
}