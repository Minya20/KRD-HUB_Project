package kr.krd.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import kr.krd.dao.RR_AnnouncementDAO;
import kr.krd.dao.RR_ApplicationDAO;
import kr.krd.vo.RR_AnnouncementVO;
import kr.krd.vo.RR_ApplicationVO;
import kr.krd.constant.RR_AnnouncementStatus;
import kr.util.DBUtil;

public class RR_KRDAdminMain {

    private Scanner sc = new Scanner(System.in);
    private RR_AnnouncementDAO announcementDAO = new RR_AnnouncementDAO();
    private RR_ApplicationDAO applicationDAO = new RR_ApplicationDAO();

    // 로그인 후 실제 값으로 세팅되어야 함 (일단 테스트용)
    private int loginAgyId = 1;
    private String loginUserId = "agy01";

    public RR_KRDAdminMain() {
        callMenu();
    }

    // ===== 기관 담당자 메뉴 =====
    private void callMenu() {
        while (true) {
            // 날짜 기준 상태 자동 갱신(공고예정/공고중/마감)
            syncAnnouncementStatusByDate();

            System.out.println("\n===== 기관 담당자 메뉴 =====");
            System.out.println("1. 공고 등록");
            System.out.println("2. 공고 수정 / 삭제");
            System.out.println("3. 신청자 목록 조회");
            System.out.println("4. 선정 관리");
            System.out.println("5. 연구 진행 관리");
            System.out.println("6. 과제 진행 현황 조회");
            System.out.println("7. 로그아웃");
            System.out.print("번호 선택 : ");

            int menu = readInt();

            switch (menu) {
                case 1:
                    registerAnnouncement();
                    break;
                case 2:
                    manageAnnouncement();
                    break;
                case 3:
                    applicantListMenu();
                    break;
                case 4:
                case 5:
                case 6:
                    System.out.println("아직 구현 예정 메뉴입니다.");
                    break;
                case 7:
                    System.out.println("로그아웃합니다.");
                    return;
                default:
                    System.out.println("잘못 입력했습니다.");
            }
        }
    }

    // ===== 날짜 기준 상태 자동 갱신 =====
    // - 공고 3종 상태(공고예정/공고중/마감)만 갱신
    // - 심사중/선정완료/진행중 등 다른 단계는 건드리지 않음
    private void syncAnnouncementStatusByDate() {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "UPDATE ANNOUNCEMENT "
              + "SET ANNOUNCEMENT_STATUS = CASE "
              + "  WHEN TO_DATE(ANNOUNCEMENT_END_DT, 'YYYY-MM-DD') < TRUNC(SYSDATE) THEN ? "       // 마감
              + "  WHEN TO_DATE(ANNOUNCEMENT_START_DT, 'YYYY-MM-DD') > TRUNC(SYSDATE) THEN ? "    // 공고예정
              + "  ELSE ? "                                                                       // 공고중
              + "END "
              + "WHERE ANNOUNCEMENT_AGY_ID = ? "
              + "  AND ANNOUNCEMENT_HIDDEN_YN = 0 "
              + "  AND ANNOUNCEMENT_STATUS IN (?, ?, ?) "
              + "  AND REGEXP_LIKE(ANNOUNCEMENT_START_DT, '^\\d{4}-\\d{2}-\\d{2}$') "
              + "  AND REGEXP_LIKE(ANNOUNCEMENT_END_DT, '^\\d{4}-\\d{2}-\\d{2}$')";

            pstmt = conn.prepareStatement(sql);

            int idx = 1;
            pstmt.setString(idx++, RR_AnnouncementStatus.CLOSED);
            pstmt.setString(idx++, RR_AnnouncementStatus.SCHEDULED);
            pstmt.setString(idx++, RR_AnnouncementStatus.OPEN);
            pstmt.setInt(idx++, loginAgyId);

            pstmt.setString(idx++, RR_AnnouncementStatus.SCHEDULED);
            pstmt.setString(idx++, RR_AnnouncementStatus.OPEN);
            pstmt.setString(idx++, RR_AnnouncementStatus.CLOSED);

            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("⚠ 상태 자동 갱신 중 오류가 발생했습니다.");
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // ===== 1. 공고 등록 =====
    private void registerAnnouncement() {
        System.out.println("\n===== 공고 등록 =====");
        System.out.println("[공고 등록]");
        System.out.println("--------------------");

        RR_AnnouncementVO vo = new RR_AnnouncementVO();

        System.out.print("과제명 입력 : ");
        String title = sc.nextLine().trim();

        System.out.print("과제 설명(공고문/양식명 포함 가능) : ");
        String desc = sc.nextLine().trim();

        System.out.print("모집 분야 입력 : ");
        String field = sc.nextLine().trim();

        System.out.print("총 예산 : ");
        long totalBudget = readLong();

        System.out.print("선정 팀수 : ");
        int recruitCap = readInt();

        System.out.print("신청 시작일(yyyy-MM-dd) : ");
        String startDt = sc.nextLine().trim();

        System.out.print("신청 마감일(yyyy-MM-dd) : ");
        String endDt = sc.nextLine().trim();

        // ✅ 마감일 지난 공고는 등록 자체 막기 + 시작일/마감일 검증
        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(startDt);
            end = LocalDate.parse(endDt);
        } catch (DateTimeParseException e) {
            System.out.println("⚠ 날짜 형식이 올바르지 않습니다. 예) 2026-03-27");
            return;
        }

        if (start.isAfter(end)) {
            System.out.println("⚠ 시작일이 마감일보다 늦을 수 없습니다.");
            return;
        }

        // end == today 는 허용(오늘까지 접수)
        if (end.isBefore(LocalDate.now())) {
            System.out.println("⚠ 마감일이 이미 지난 공고는 등록할 수 없습니다.");
            return;
        }

        System.out.print("담당자 연락처 : ");
        String pmContact = sc.nextLine().trim();

        System.out.println("\n1. 등록");
        System.out.println("2. 취소(이전페이지)");
        System.out.println("--------------------");
        System.out.print("선택 : ");
        int select = readInt();

        if (select != 1) {
            System.out.println("공고 등록을 취소했습니다.");
            return;
        }

        // VO 세팅
        vo.setAgyId(loginAgyId);
        vo.setTitle(title);
        vo.setDesc(desc);
        vo.setField(field);
        vo.setTotalBudget(totalBudget);
        vo.setRecruitCap(recruitCap);
        vo.setStartDt(startDt);
        vo.setEndDt(endDt);
        vo.setPmContact(pmContact);

        // 기본값
        vo.setReannYn(0);

        // ✅ 시작일이 미래면 공고예정, 아니면 공고중 (마감은 등록에서 막음)
        if (start.isAfter(LocalDate.now())) {
            vo.setStatus(RR_AnnouncementStatus.SCHEDULED);
        } else {
            vo.setStatus(RR_AnnouncementStatus.OPEN);
        }

        vo.setCreatedBy(loginUserId);
        vo.setHiddenYn(0);

        int result = announcementDAO.insertAnnouncement(vo);

        if (result > 0) {
            System.out.println("공고가 정상적으로 등록되었습니다.");
        } else {
            System.out.println("공고 등록에 실패했습니다.");
        }
    }

    // ===== 2. 공고 수정 / 삭제 =====
    private void manageAnnouncement() {
        while (true) {
            System.out.println("\n===== 공고 수정 / 삭제 =====");
            printAnnouncementList();

            System.out.println("1. 공고 수정");
            System.out.println("2. 공고 삭제");
            System.out.println("3. 뒤로가기");
            System.out.print("메뉴 선택 : ");

            int menu = readInt();

            switch (menu) {
                case 1:
                    updateAnnouncementFlow();
                    break;
                case 2:
                    deleteAnnouncementFlow();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("잘못 입력했습니다.");
            }
        }
    }

    // 공고 목록 출력
    private void printAnnouncementList() {
        // 목록 보기 전에 상태 자동 갱신
        syncAnnouncementStatusByDate();

        List<RR_AnnouncementVO> list = announcementDAO.getAnnouncementListByAgency(loginAgyId);

        System.out.println("[현재 등록된 공고 목록]");
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-8s %-20s %-10s %-10s %-12s%n", "과제번호", "과제명", "상태", "신청자수", "마감일");
        System.out.println("--------------------------------------------------------------------------");

        if (list == null || list.isEmpty()) {
            System.out.println("등록된 공고가 없습니다.");
        } else {
            for (RR_AnnouncementVO vo : list) {
                System.out.printf("%-8d %-20s %-10s %-10d %-12s%n",
                        vo.getAnnId(),
                        cut(vo.getTitle(), 18),
                        vo.getStatus(),
                        vo.getApplicantCount(),
                        vo.getEndDt());
            }
        }
        System.out.println("--------------------------------------------------------------------------");
    }

    // 공고 수정 흐름
    private void updateAnnouncementFlow() {
        System.out.print("\n수정할 과제 ID 입력 : ");
        int annId = readInt();

        RR_AnnouncementVO vo = announcementDAO.getAnnouncementDetail(annId, loginAgyId);

        if (vo == null) {
            System.out.println("해당 공고를 찾을 수 없습니다.");
            return;
        }

        // ✅ 공고예정/공고중일 때만 수정 가능
        if (!(RR_AnnouncementStatus.OPEN.equals(vo.getStatus())
           || RR_AnnouncementStatus.SCHEDULED.equals(vo.getStatus()))) {
            System.out.println("⚠ 해당 공고는 이미 " + vo.getStatus() + "입니다.");
            System.out.println("공고예정/공고중일 때만 수정 가능합니다.");
            return;
        }

        System.out.println("\n[현재 정보]");
        System.out.println("과제명 : " + vo.getTitle());
        System.out.println("과제 설명 : " + (vo.getDesc() == null ? "-" : vo.getDesc()));
        System.out.println("총 예산 : " + vo.getTotalBudget());
        System.out.println("선정 팀수 : " + vo.getRecruitCap() + "팀");
        System.out.println("신청 마감일 : " + vo.getEndDt());

        System.out.println("\n수정할 항목 선택");
        System.out.println("1. 과제명");
        System.out.println("2. 과제 설명");
        System.out.println("3. 예산");
        System.out.println("4. 선정 팀수");
        System.out.println("5. 마감일");
        System.out.println("6. 취소");
        System.out.print("번호 선택 : ");

        int fieldNo = readInt();

        if (fieldNo == 6) {
            System.out.println("수정을 취소했습니다.");
            return;
        }

        String newValue;
        switch (fieldNo) {
            case 1:
                System.out.print("새 과제명 입력 : ");
                newValue = sc.nextLine().trim();
                break;
            case 2:
                System.out.print("새 과제 설명 입력 : ");
                newValue = sc.nextLine().trim();
                break;
            case 3:
                System.out.print("새 예산 입력 : ");
                newValue = String.valueOf(readLong());
                break;
            case 4:
                System.out.print("새 선정 팀수 입력 : ");
                newValue = String.valueOf(readInt());
                break;

            // ✅ 마감일 수정: (1) 오늘 이전 금지, (2) 시작일보다 빠르면 금지
            case 5:
                System.out.print("새 마감일(yyyy-MM-dd) 입력 : ");
                newValue = sc.nextLine().trim();

                try {
                    LocalDate today = LocalDate.now();
                    LocalDate start = LocalDate.parse(vo.getStartDt());  // 기존 시작일
                    LocalDate newEnd = LocalDate.parse(newValue);

                    if (newEnd.isBefore(start)) {
                        System.out.println("⚠ 마감일은 시작일보다 빠를 수 없습니다.");
                        return;
                    }
                    if (newEnd.isBefore(today)) {
                        System.out.println("⚠ 이미 지난 날짜로 마감일을 설정할 수 없습니다.");
                        return;
                    }

                } catch (DateTimeParseException e) {
                    System.out.println("⚠ 날짜 형식이 올바르지 않습니다. 예) 2026-03-27");
                    return;
                }
                break;

            default:
                System.out.println("잘못된 번호입니다.");
                return;
        }

        int result = announcementDAO.updateAnnouncementField(annId, loginAgyId, fieldNo, newValue);

        if (result > 0) {
            System.out.println("공고 정보가 수정되었습니다.");
        } else {
            System.out.println("공고 수정에 실패했습니다.");
        }
    }

    // 공고 논리 삭제 흐름
    private void deleteAnnouncementFlow() {
        System.out.print("\n삭제할 과제 ID 입력 : ");
        int annId = readInt();

        RR_AnnouncementVO vo = announcementDAO.getAnnouncementDetail(annId, loginAgyId);

        if (vo == null) {
            System.out.println("해당 공고를 찾을 수 없습니다.");
            return;
        }

        // ✅ 공고예정/공고중 상태만 삭제 가능
        if (!(RR_AnnouncementStatus.OPEN.equals(vo.getStatus())
           || RR_AnnouncementStatus.SCHEDULED.equals(vo.getStatus()))) {
            System.out.println("⚠ 공고예정/공고중 상태에서만 삭제 가능합니다.");
            System.out.println("현재 상태 : " + vo.getStatus());
            return;
        }

        // 신청팀 있으면 삭제 불가
        if (vo.getApplicantCount() > 0) {
            System.out.println("⚠ 신청자가 존재합니다. (" + vo.getApplicantCount() + "팀)");
            System.out.println("신청자가 0팀일 때만 삭제 가능합니다.");
            return;
        }

        System.out.println("\n정말 삭제하시겠습니까?");
        System.out.println("1. 예");
        System.out.println("2. 아니오");
        System.out.print("선택 : ");

        int confirm = readInt();

        if (confirm != 1) {
            System.out.println("삭제를 취소했습니다.");
            return;
        }

        int result = announcementDAO.softDeleteAnnouncement(annId, loginAgyId);

        if (result > 0) {
            System.out.println("공고가 정상적으로 삭제되었습니다. (논리삭제)");
        } else {
            System.out.println("공고 삭제에 실패했습니다.");
        }
    }

    // ===== 3. 신청자 목록 조회 =====
    private void applicantListMenu() {
        while (true) {
            System.out.println("\n===== 신청자 목록 조회 =====");
            System.out.println("[공고 목록]");
            printAnnouncementList();

            System.out.print("조회할 과제ID 입력 (0=이전) : ");
            int annId = readInt();
            if (annId == 0) return;

            List<RR_ApplicationVO> apps = applicationDAO.getApplicationsByAnnouncement(annId);

            System.out.println("\n===== 신청자 목록 =====");
            System.out.println("현재 과제 : [" + annId + "]");
            System.out.println("--------------------------------------------------------------");
            System.out.printf("%-8s %-12s %-10s %-12s %-10s%n", "신청ID", "신청자ID", "이름", "신청일", "상태");
            System.out.println("--------------------------------------------------------------");

            if (apps.isEmpty()) {
                System.out.println("신청 내역이 없습니다.");
            } else {
                for (RR_ApplicationVO a : apps) {
                    System.out.printf("%-8d %-12s %-10s %-12s %-10s%n",
                            a.getApplicationId(),
                            a.getUserId(),
                            cut(a.getUserName(), 9),
                            a.getAppliedAt(),
                            a.getStatusCd());
                }
            }
            System.out.println("--------------------------------------------------------------");

            System.out.println("1. 신청 상세 조회");
            System.out.println("0. 이전 메뉴");
            System.out.print("선택 : ");
            int sel = readInt();
            if (sel == 0) continue;

            if (sel == 1) {
                System.out.print("신청 상세 조회할 신청ID 입력 : ");
                int appId = readInt();

                RR_ApplicationVO detail = applicationDAO.getApplicationDetail(appId);
                if (detail == null) {
                    System.out.println("해당 신청ID를 찾을 수 없습니다.");
                    continue;
                }

                System.out.println("\n===== 신청 상세 =====");
                System.out.println("신청ID : " + detail.getApplicationId());
                System.out.println("신청자 : " + detail.getUserId() + " (" + detail.getUserName() + ")");
                System.out.println("제출서류명 : " + (detail.getAttachPath() == null ? "-" : detail.getAttachPath()));
                System.out.println("신청일 : " + detail.getAppliedAt());
                System.out.println("상태 : " + detail.getStatusCd());
                System.out.println("희망 예산 : " + detail.getBudgetAmt());

                if (detail.getAvgScore() == null) {
                    System.out.println("평균 점수 : -");
                } else {
                    System.out.println("평균 점수 : " + detail.getAvgScore());
                }

                System.out.println("\n0. 이전 메뉴");
                readInt();
            }
        }
    }

    // ===== 공통 입력 유틸 =====
    private int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.print("숫자로 다시 입력하세요 : ");
            }
        }
    }

    private long readLong() {
        while (true) {
            try {
                return Long.parseLong(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.print("숫자로 다시 입력하세요 : ");
            }
        }
    }

    private String cut(String str, int len) {
        if (str == null) return "";
        if (str.length() <= len) return str;
        return str.substring(0, len - 1) + "…";
    }

    public static void main(String[] args) {
        new RR_KRDAdminMain();
    }
}