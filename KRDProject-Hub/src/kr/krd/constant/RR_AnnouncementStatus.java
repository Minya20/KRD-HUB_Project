package kr.krd.constant;

public class RR_AnnouncementStatus {
    private RR_AnnouncementStatus() {} // 객체 생성 방지

    // 공고 단계
    public static final String SCHEDULED = "공고예정";
    public static final String OPEN = "공고중";
    public static final String CLOSED = "마감";

    // 심사 단계
    public static final String REVIEWING = "심사중";
    public static final String REVIEW_DONE = "심사완료";

    // 선정 단계
    public static final String SELECT_PENDING = "선정대기";
    public static final String SELECT_DONE = "선정완료";

    // 수행 단계
    public static final String IN_PROGRESS = "진행중";
    public static final String STOPPED = "중단";
    public static final String COMPLETED = "완료";
}