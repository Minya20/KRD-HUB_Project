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
import kr.krd.dao.RR_SelectionDAO;
import kr.krd.vo.RR_SelectionVO;
//import kr.krd.dao.RR_ProjectDAO;
import kr.krd.vo.RR_ProjectVO;

import kr.krd.dao.RR_ProjectDAO;
import kr.krd.vo.RR_ProjectVO;
import kr.krd.vo.RR_TaskVO;

import kr.krd.dao.RR_FundingDAO;
import kr.krd.constant.RR_FundingConst;
import kr.krd.dao.RR_ReportDAO;
import kr.krd.vo.RR_ReportVO;
import kr.krd.constant.RR_ReportConst;

import kr.krd.dao.RR_TaskProgressDAO;
import kr.krd.vo.RR_TaskProgressVO;
import kr.krd.vo.RR_TeamProgressVO;

public class RR_KRDAdminMain {

    private Scanner sc = new Scanner(System.in);
    private RR_AnnouncementDAO announcementDAO = new RR_AnnouncementDAO();
    private RR_ApplicationDAO applicationDAO = new RR_ApplicationDAO();
    private RR_SelectionDAO selectionDAO = new RR_SelectionDAO();
    private static final int REQUIRED_REVIEWERS = 5; // 신청서 1건당 평가 제출 개수
    private RR_ProjectDAO projectDAO = new RR_ProjectDAO();
    private RR_FundingDAO fundingDAO = new RR_FundingDAO();
    private RR_ReportDAO reportDAO = new RR_ReportDAO();
    private RR_TaskProgressDAO progressDAO = new RR_TaskProgressDAO();
    
    
    // 로그인 후 실제 값으로 세팅되어야 함 (일단 테스트용)
    private int loginAgyId = 1;
    private String loginUserId = "agy01";

    public RR_KRDAdminMain() {
        //callMenu();
        //코드 통합과정에서 생성자로 바로 콜메뉴를 해버리면 기관관리자가 먼저 호출 되서
    	//주석처리 진행함.
    }

    // ===== 기관 담당자 메뉴 =====
    public void callMenu() {
        while (true) {
            // 날짜 기준 상태 자동 갱신(공고예정/공고중/마감)
            syncAnnouncementStatusByDate();

            System.out.println("\n===== 기관 담당자 메뉴 =====");
            System.out.println("1. 공고 등록");
            System.out.println("2. 공고 조회 및 관리");
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
                	selectionMenu();
                    break;
                case 5:
                	researchProgressMenu();
                    break;
                case 6:
                	taskProgressMenu();
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
              + "  WHEN TO_DATE(ANNOUNCEMENT_START_DT, 'YYYY-MM-DD') > TRUNC(SYSDATE) THEN ? " // 공고예정
              + "  WHEN TO_DATE(ANNOUNCEMENT_END_DT,   'YYYY-MM-DD') < TRUNC(SYSDATE) THEN ? " // 마감
              + "  ELSE ? "                                                                     // 공고중
              + "END "
              + "WHERE ANNOUNCEMENT_HIDDEN_YN = 0 "
              + "  AND ANNOUNCEMENT_STATUS IN (?, ?, ?) "  // ✅ 이 3개 상태만 자동 갱신 대상!
              + "  AND REGEXP_LIKE(ANNOUNCEMENT_START_DT, '^\\d{4}-\\d{2}-\\d{2}$') "
              + "  AND REGEXP_LIKE(ANNOUNCEMENT_END_DT,   '^\\d{4}-\\d{2}-\\d{2}$')";

            pstmt = conn.prepareStatement(sql);

            int idx = 1;
            pstmt.setString(idx++, RR_AnnouncementStatus.SCHEDULED);
            pstmt.setString(idx++, RR_AnnouncementStatus.CLOSED);
            pstmt.setString(idx++, RR_AnnouncementStatus.OPEN);

            // ✅ 자동으로 바꿔도 되는 상태 3개만!
            pstmt.setString(idx++, RR_AnnouncementStatus.SCHEDULED);
            pstmt.setString(idx++, RR_AnnouncementStatus.OPEN);
            pstmt.setString(idx++, RR_AnnouncementStatus.CLOSED);

            pstmt.executeUpdate();

        } catch (Exception e) {
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

        //  마감일 지난 공고는 등록 자체 막기 + 시작일/마감일 검증
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

        // 시작일이 미래면 공고예정, 아니면 공고중 (마감은 등록에서 막음)
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
                    return;  // 이전 메뉴
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
    
    
 // 메뉴4(선정관리) 전용: 마감(CLOSED)만 출력
    private void printClosedAnnouncementListOnly() {
        syncAnnouncementStatusByDate();

        List<RR_AnnouncementVO> list = announcementDAO.getAnnouncementListByAgency(loginAgyId);

        System.out.println("[공고 목록 - 마감만]");
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-8s %-20s %-10s %-10s %-12s%n", "과제번호", "과제명", "상태", "신청자수", "마감일");
        System.out.println("--------------------------------------------------------------------------");

        boolean hasAny = false;

        if (list != null) {
            for (RR_AnnouncementVO vo : list) {
                if (!RR_AnnouncementStatus.CLOSED.equals(vo.getStatus())) continue; // ✅ 마감만
                hasAny = true;
                System.out.printf("%-8d %-20s %-10s %-10d %-12s%n",
                        vo.getAnnId(),
                        cut(vo.getTitle(), 18),
                        vo.getStatus(),
                        vo.getApplicantCount(),
                        vo.getEndDt());
            }
        }

        if (!hasAny) System.out.println("마감된 공고가 없습니다.");
        System.out.println("--------------------------------------------------------------------------");
    }
    
    
    
    private void printSelectPendingAnnouncementListOnly() {
        syncAnnouncementStatusByDate();
        announcementDAO.promoteClosedToSelectPending(loginAgyId);

        List<RR_AnnouncementVO> list = announcementDAO.getAnnouncementListByAgency(loginAgyId);

        System.out.println("[공고 목록 - 선정대기만]");
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-8s %-20s %-10s %-10s %-12s%n", "과제번호", "과제명", "상태", "신청자수", "마감일");
        System.out.println("--------------------------------------------------------------------------");

        boolean hasAny = false;
        if (list != null) {
            for (RR_AnnouncementVO vo : list) {
                if (!RR_AnnouncementStatus.SELECT_PENDING.equals(vo.getStatus())) continue;
                hasAny = true;
                System.out.printf("%-8d %-20s %-10s %-10d %-12s%n",
                        vo.getAnnId(), cut(vo.getTitle(), 18), vo.getStatus(), vo.getApplicantCount(), vo.getEndDt());
            }
        }
        if (!hasAny) System.out.println("선정대기 공고가 없습니다.");
        System.out.println("--------------------------------------------------------------------------");
    }
    
    
    
    
    private void printSelectDoneAnnouncementListOnly() {
        syncAnnouncementStatusByDate();
        // 선정완료는 날짜로 바꾸면 안 되므로 promote만 돌려도 SELECT_DONE은 건드리지 않음
        announcementDAO.promoteClosedToSelectPending(loginAgyId);

        List<RR_AnnouncementVO> list = announcementDAO.getAnnouncementListByAgency(loginAgyId);

        System.out.println("[공고 목록 - 선정완료만]");
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-8s %-20s %-10s %-10s %-12s%n", "과제번호", "과제명", "상태", "신청자수", "마감일");
        System.out.println("--------------------------------------------------------------------------");

        boolean hasAny = false;
        if (list != null) {
            for (RR_AnnouncementVO vo : list) {
                if (!RR_AnnouncementStatus.SELECT_DONE.equals(vo.getStatus())) continue;
                hasAny = true;
                System.out.printf("%-8d %-20s %-10s %-10d %-12s%n",
                        vo.getAnnId(), cut(vo.getTitle(), 18), vo.getStatus(), vo.getApplicantCount(), vo.getEndDt());
            }
        }
        if (!hasAny) System.out.println("선정완료 공고가 없습니다.");
        System.out.println("--------------------------------------------------------------------------");
    }
    
    
    
    
 // 메뉴4(선정관리) 전용: 공고중(OPEN)만 출력
    private void printOpenAnnouncementListOnly() {
        // 상태 자동 갱신(공고예정/공고중/마감 정리) 후 조회
        syncAnnouncementStatusByDate();

        List<RR_AnnouncementVO> list = announcementDAO.getAnnouncementListByAgency(loginAgyId);

        System.out.println("[공고 목록 - 공고중만]");
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-8s %-20s %-10s %-10s %-12s%n", "과제번호", "과제명", "상태", "신청자수", "마감일");
        System.out.println("--------------------------------------------------------------------------");

        boolean hasAny = false;

        if (list != null) {
            for (RR_AnnouncementVO vo : list) {
                if (!RR_AnnouncementStatus.OPEN.equals(vo.getStatus())) continue; // ✅ 공고중만

                hasAny = true;
                System.out.printf("%-8d %-20s %-10s %-10d %-12s%n",
                        vo.getAnnId(),
                        cut(vo.getTitle(), 18),
                        vo.getStatus(),
                        vo.getApplicantCount(),
                        vo.getEndDt());
            }
        }

        if (!hasAny) {
            System.out.println("공고중인 공고가 없습니다.");
        }

        System.out.println("--------------------------------------------------------------------------");
    }
    
    
    private void printOpenOrClosedAnnouncementListOnly() {
        syncAnnouncementStatusByDate();
        List<RR_AnnouncementVO> list = announcementDAO.getAnnouncementListByAgency(loginAgyId);

        System.out.println("[공고 목록 - 공고중/마감만]");
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-8s %-20s %-10s %-10s %-12s%n", "과제번호", "과제명", "상태", "신청자수", "마감일");
        System.out.println("--------------------------------------------------------------------------");

        boolean hasAny = false;
        if (list != null) {
            for (RR_AnnouncementVO vo : list) {
                if (RR_AnnouncementStatus.SCHEDULED.equals(vo.getStatus())) continue; // 공고예정 제외
                hasAny = true;
                System.out.printf("%-8d %-20s %-10s %-10d %-12s%n",
                        vo.getAnnId(),
                        cut(vo.getTitle(), 18),
                        vo.getStatus(),
                        vo.getApplicantCount(),
                        vo.getEndDt());
            }
        }
        if (!hasAny) System.out.println("조회 가능한 공고가 없습니다.");
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

        // 공고예정/공고중일 때만 수정 가능
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

            // 마감일 수정: (1) 오늘 이전 금지, (2) 시작일보다 빠르면 금지
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

        // 공고예정/공고중 상태만 삭제 가능
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
            printOpenOrClosedAnnouncementListOnly();

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
    
    
 // ===== 4. 선정 관리 =====
    private void selectionMenu() {
        while (true) {
            System.out.println("\n===== 선정 관리 =====");
            System.out.println("1. 자동 선정 계산");
            System.out.println("2. 선정 결과 조회");
            System.out.println("0. 이전 메뉴");
            System.out.print("번호 선택 : ");

            int sel = readInt();
            if (sel == 0) return;

            if (sel == 1) autoSelectFlow();
            else if (sel == 2) viewSelectionResultFlow();
            else System.out.println("잘못 입력했습니다.");
        }
    }

    // 정책: 점수순 상위 N만 보다가 예산 초과 순간 중단(뒤는 안 봄)
    private void autoSelectFlow() {
    	
    	syncAnnouncementStatusByDate();
        announcementDAO.promoteClosedToSelectPending(loginAgyId);
    	
        System.out.println("\n===== 자동 선정 계산 =====");
        System.out.println("[공고 목록]");
        printSelectPendingAnnouncementListOnly();

        System.out.print("과제 ID 입력 (0=이전) : ");
        int annId = readInt();
        if (annId == 0) return;

        RR_AnnouncementVO ann = announcementDAO.getAnnouncementDetail(annId, loginAgyId);
        
        if (ann == null) {
            System.out.println("해당 공고를 찾을 수 없습니다.");
            return;
        }
        
        
        if (!RR_AnnouncementStatus.SELECT_PENDING.equals(ann.getStatus())) {
            System.out.println("⚠ 선정 계산/승인은 '선정대기' 공고에서만 가능합니다.");
            System.out.println("현재 공고 상태 : " + ann.getStatus());
            return;
        }
        

        if (selectionDAO.hasSelectionResult(annId)) {
            System.out.println("⚠ 이미 선정 승인 완료된 공고입니다. (중복 승인 불가)");
            return;
        }

        List<RR_SelectionVO> ranked = selectionDAO.getRankedCandidates(annId);
        if (ranked.isEmpty()) {
            System.out.println("신청 내역이 없습니다.");
            return;
        }

        // “모든 신청서가 5명 제출 완료”인지 체크 (미완료면 승인 막음)
        boolean allDone = selectionDAO.isAllEvaluationsSubmitted(annId, REQUIRED_REVIEWERS);

        int cap = ann.getRecruitCap();              // 최대 선정팀수 N
        long totalBudget = ann.getTotalBudget();    // 공고 총예산
        int limit = Math.min(cap, ranked.size());

        long sum = 0;
        int selectedCount = 0;

        System.out.println("\n---------------------------------");
        System.out.println("과제명 : " + ann.getTitle());
        System.out.println("총 예산 : " + totalBudget);
        System.out.println("최대 선정 팀수 : " + cap + "팀");
        System.out.println("---------------------------------");

        // 상위 N만 출력/검토
        for (int i = 0; i < limit; i++) {
            RR_SelectionVO c = ranked.get(i);

            String evalTxt = c.getSubmittedCnt() + "/" + REQUIRED_REVIEWERS;
            String scoreTxt = String.format("%.2f", c.getAvgScore());

            long next = sum + c.getBudgetAmt();

            if (next > totalBudget) {
                System.out.printf("%d위  신청ID:%d  %s(%s)  점수:%s  희망:%d  평가:%s  -> ⚠ 예산 초과! 여기서 중단%n",
                        (i + 1),
                        c.getApplicationId(),
                        cut(c.getUserName(), 10),
                        c.getUserId(),
                        scoreTxt,
                        c.getBudgetAmt(),
                        evalTxt);
                break; // ✅ 정책: 초과 순간 중단, 뒤는 안 봄
            }

            sum = next;
            selectedCount++;

            System.out.printf("%d위  신청ID:%d  %s(%s)  점수:%s  희망:%d  평가:%s  [후보]%n",
                    (i + 1),
                    c.getApplicationId(),
                    cut(c.getUserName(), 10),
                    c.getUserId(),
                    scoreTxt,
                    c.getBudgetAmt(),
                    evalTxt);
        }

        System.out.println("---------------------------------");
        System.out.println("※ 최종 후보: " + selectedCount + "팀 / 누적 예산: " + sum);

        // 평가 미완료가 있으면 승인 막기
        if (!allDone) {
            System.out.println("⚠ 아직 평가가 완료되지 않은 신청서가 있습니다. (SUBMITTED 5/5 미완료)");
            System.out.println("평가가 모두 제출된 뒤에 승인할 수 있습니다.");
            return;
        }
        
        System.out.println("※ 후보 외 나머지 팀은 자동 탈락 예정입니다. (총 신청 "
                + ranked.size() + "팀 / 후보 " + selectedCount + "팀)");

        System.out.println("\n1. 최종 선정 승인");
        System.out.println("0. 이전 메뉴");
        System.out.print("선택 : ");
        int choice = readInt();
        if (choice != 1) return;

        if (selectedCount == 0) {
            System.out.println("⚠ 예산 초과로 선정 가능한 팀이 없습니다. 승인할 수 없습니다.");
            return;
        }

        System.out.println("\n최종 선정 승인하시겠습니까?");
        System.out.println("1. 승인");
        System.out.println("2. 취소");
        System.out.print("선택 : ");
        int confirm = readInt();
        if (confirm != 1) {
            System.out.println("승인을 취소했습니다.");
            return;
        }

        int result = selectionDAO.approveSelection(annId, loginAgyId, loginUserId, ranked, selectedCount);
        if (result == -2) System.out.println("⚠ 이미 승인된 공고입니다.");
        else if (result > 0) System.out.println("선정 승인 완료! (총 " + result + "건 저장)");
        else System.out.println("선정 승인 실패");
    }

    private void viewSelectionResultFlow() {
    	
    	syncAnnouncementStatusByDate();
        announcementDAO.promoteClosedToSelectPending(loginAgyId);
    	
        System.out.println("\n===== 선정 결과 조회 =====");
        System.out.println("[공고 목록]");
        printSelectDoneAnnouncementListOnly();

        System.out.print("과제 ID 입력 (0=이전) : ");
        int annId = readInt();
        if (annId == 0) return;

        List<RR_SelectionVO> results = selectionDAO.getSelectionResults(annId);

        if (results.isEmpty()) {
            System.out.println("아직 최종 선정 승인이 완료되지 않았습니다.");
            System.out.println("1. 자동 선정 계산으로 이동");
            System.out.println("0. 이전 메뉴");
            System.out.print("선택 : ");
            int go = readInt();
            if (go == 1) autoSelectFlow();
            return;
        }

        System.out.println("\n--------------------------------------------");
        System.out.printf("%-8s %-12s %-10s %-8s %-8s %-12s%n",
                "신청ID", "신청자ID", "이름", "점수", "결과", "승인일");
        System.out.println("--------------------------------------------");

        for (RR_SelectionVO r : results) {
            String resultKor = "SELECTED".equals(r.getResultCd()) ? "선정" : "탈락";
            System.out.printf("%-8d %-12s %-10s %-8.2f %-8s %-12s%n",
                    r.getApplicationId(),
                    r.getUserId(),
                    cut(r.getUserName(), 9),
                    r.getAvgScore(),
                    resultKor,
                    r.getApprovedAt());
        }
        System.out.println("--------------------------------------------");
    }
    
    
    
 // ===== 5. 연구 진행 관리 (과제 -> 팀) =====
    private void researchProgressMenu() {
        while (true) {
            System.out.println("\n===== 연구 진행 관리 =====");

            List<RR_TaskVO> tasks = projectDAO.getTaskListByAgency(loginAgyId);

            System.out.println("[관리할 과제 목록]");
            System.out.println("--------------------------------------------------------------------------");
            System.out.printf("%-8s %-24s %-10s %-8s%n", "과제ID", "과제명", "과제상태", "선정팀수");
            System.out.println("--------------------------------------------------------------------------");

            if (tasks.isEmpty()) {
                System.out.println("관리할 과제가 없습니다. (PROJECTS 데이터가 없을 수 있음)");
                System.out.println("0. 이전 메뉴");
                readInt();
                return;
            }

            for (RR_TaskVO t : tasks) {
                System.out.printf("%-8d %-24s %-10s %-8d%n",
                        t.getAnnId(),
                        cut(t.getTitle(), 22),
                        t.getTaskStatus(),
                        t.getTeamCount());
            }
            System.out.println("--------------------------------------------------------------------------");

            System.out.print("관리할 과제ID 입력 (0=이전) : ");
            int annId = readInt();
            if (annId == 0) return;

            taskDetailMenu(annId);
        }
    }

    private void taskDetailMenu(int annId) {
        while (true) {
            System.out.println("\n현재 과제 ID : " + annId);

            System.out.println("1. 협약 상태 변경");
            System.out.println("2. 연구비 지급 승인");
            System.out.println("3. 중간 보고 승인");
            System.out.println("4. 최종 보고 승인");
            System.out.println("5. 연구 중단 처리");
            System.out.println("0. 이전 메뉴");
            System.out.print("번호 선택 : ");

            int sel = readInt();
            if (sel == 0) return;

            switch (sel) {
                case 1:
                    agreementMenuByTask(annId);  // annId = 과제(공고) ID
                    break;
                case 2:
                	fundingMenuByTask(annId);   // annId = 과제(공고) ID
                    break;
                case 3:
                	midReportMenuByTask(annId); // annId = 과제(공고) ID
                    break;
                case 4:
                	finalReportMenuByTask(annId);  // annId = 과제(공고) ID
                    break;
                case 5:
                    stopTeamMenuByTask(annId);   // annId = 과제(공고) ID
                    break;
                default:
                    System.out.println("잘못 입력했습니다.");
            }
        }
    }

    // 5-1 협약: 과제 안의 선정팀 목록 보여주고 팀 선택해서 체결
    private void agreementMenuByTask(int annId) {
        while (true) {
            System.out.println("\n===== 협약 상태 변경 =====");

            List<RR_ProjectVO> teams = projectDAO.getTeamsByTask(loginAgyId, annId);
            if (teams.isEmpty()) {
                System.out.println("선정된 팀(프로젝트)이 없습니다.");
                return;
            }

            System.out.println("선정된 팀 목록");
            System.out.println("-------------------------------------------------------------");
            System.out.printf("%-4s %-10s %-10s %-8s %-8s%n", "번호", "프로젝트ID", "연구자", "협약", "상태");
            System.out.println("-------------------------------------------------------------");

            for (int i = 0; i < teams.size(); i++) {
                RR_ProjectVO p = teams.get(i);
                System.out.printf("%-4d %-10d %-10s %-8s %-8s%n",
                        (i + 1),
                        p.getProjectId(),
                        cut(p.getUserName(), 8),
                        mapAgreementStatus(p.getAgreementStatusCd()),
                        mapProjectStatus(p.getProjectStatusCd()));
            }
            System.out.println("-------------------------------------------------------------");

            System.out.print("협약 체결할 팀 번호 선택 (0=이전) : ");
            int pick = readInt();
            if (pick == 0) return;
            if (pick < 1 || pick > teams.size()) {
                System.out.println("잘못된 번호입니다.");
                continue;
            }

            RR_ProjectVO target = teams.get(pick - 1);
            int r = projectDAO.signAgreement(target.getProjectId(), loginUserId);

            if (r == 0) {
                System.out.println("이미 협약이 체결된 팀입니다.");
            } else if (r > 0) {
                System.out.println(target.getUserName() + " 팀 협약 체결 완료");
                System.out.println("현재 상태 : 체결");
            } else {
                System.out.println("협약 체결 실패");
            }

            System.out.println("\n1. 다른 팀 협약 체결");
            System.out.println("0. 이전 메뉴");
            System.out.print("선택 : ");
            int next = readInt();
            if (next == 0) return;
        }
    }
    
    // ===== 5-2 연구비 지급 승인
    private void fundingMenuByTask(int annId) {
        // 선정된 팀(프로젝트) 목록
        List<kr.krd.vo.RR_ProjectVO> teams = projectDAO.getTeamsByTask(loginAgyId, annId);

        if (teams == null || teams.isEmpty()) {
            System.out.println("선정된 팀(프로젝트)이 없습니다. (선정승인→PROJECTS 생성이 먼저 되어야 함)");
            return;
        }

        while (true) {
            System.out.println("\n===== 연구비 지급 승인 =====");
            System.out.println("선정된 팀 목록");
            System.out.println("-------------------------------------------------------------");
            System.out.printf("%-4s %-10s %-12s %-8s %-8s%n", "번호", "프로젝트ID", "연구자", "협약", "지급");
            System.out.println("-------------------------------------------------------------");

            for (int i = 0; i < teams.size(); i++) {
                kr.krd.vo.RR_ProjectVO t = teams.get(i);
                int pid = t.getProjectId();

                boolean p1 = fundingDAO.isRoundPaid(pid, RR_FundingConst.ROUND1);
                boolean p2 = fundingDAO.isRoundPaid(pid, RR_FundingConst.ROUND2);
                boolean p3 = fundingDAO.isRoundPaid(pid, RR_FundingConst.ROUND3);

                String paidTxt = (p3 ? "100%" : p2 ? "80%" : p1 ? "40%" : "0%");
                String agTxt = ("SIGNED".equalsIgnoreCase(t.getAgreementStatusCd()) ? "체결" : "대기");

                System.out.printf("%-4d %-10d %-12s %-8s %-8s%n",
                        (i + 1), pid, cut(t.getUserName(), 10), agTxt, paidTxt);
            }
            System.out.println("-------------------------------------------------------------");
            System.out.print("지급 처리할 팀 번호 선택 (0=이전) : ");
            int no = readInt();
            if (no == 0) return;
            if (no < 1 || no > teams.size()) {
                System.out.println("번호를 다시 선택하세요.");
                continue;
            }

            kr.krd.vo.RR_ProjectVO sel = teams.get(no - 1);
            fundingDetailMenu(sel.getProjectId(), sel.getUserId(), sel.getUserName());
        }
    }

    
    // ===== 5-2 연구비 지급 상세
    private void fundingDetailMenu(int projectId, String userId, String userName) {
        long total = fundingDAO.getRequestedBudgetAmt(projectId);
        if (total <= 0) {
            System.out.println("⚠ 희망 연구비(APPLICATION_BUDGET_AMT)가 0원입니다.");
            return;
        }

        long amt1 = total * RR_FundingConst.PCT1 / 100;
        long amt2 = total * RR_FundingConst.PCT2 / 100;
        long amt3 = total - amt1 - amt2; // 오차 방지(마지막에 몰아줌)

        while (true) {
            // 지급 여부 (FUNDING 테이블 기반)
            boolean paid1 = fundingDAO.isRoundPaid(projectId, RR_FundingConst.ROUND1);
            boolean paid2 = fundingDAO.isRoundPaid(projectId, RR_FundingConst.ROUND2);
            boolean paid3 = fundingDAO.isRoundPaid(projectId, RR_FundingConst.ROUND3);

            // 협약(1차 조건)
            boolean agreementSigned = fundingDAO.isAgreementSigned(projectId);

            // 보고서 승인 여부는 REPORTS에서 최신 보고서 조회해서 'APPROVED'인지로 판단
            RR_ReportVO mid = reportDAO.getLatestReport(projectId, RR_ReportConst.TYPE_MID);     // "MID"
            RR_ReportVO fin = reportDAO.getLatestReport(projectId, RR_ReportConst.TYPE_FINAL);  // "FINAL"

            boolean midApproved = (mid != null && RR_ReportConst.ST_APPROVED.equals(mid.getStatusCd()));   // "APPROVED"
            boolean finApproved = (fin != null && RR_ReportConst.ST_APPROVED.equals(fin.getStatusCd()));

            // 각 차수 지급 가능 조건
            boolean can1 = agreementSigned && !paid1;
            boolean can2 = paid1 && midApproved && !paid2;
            boolean can3 = paid2 && finApproved && !paid3;

            // 상태 문구 만들기(조건별로 정확히)
            String msg1;
            if (paid1) msg1 = "지급완료";
            else if (!agreementSigned) msg1 = "지급불가(협약 미체결)";
            else msg1 = "지급가능";

            String msg2;
            if (paid2) msg2 = "지급완료";
            else {
                if (!paid1) msg2 = "지급불가(1차 미지급)";
                else if (!midApproved) msg2 = "지급불가(중간보고 미승인)";
                else msg2 = "지급가능";
            }

            String msg3;
            if (paid3) msg3 = "지급완료";
            else {
                if (!paid2) msg3 = "지급불가(2차 미지급)";
                else if (!finApproved) msg3 = "지급불가(최종보고 미승인)";
                else msg3 = "지급가능";
            }

            System.out.println("\n===== 연구비 지급 상세 =====");
            System.out.println("프로젝트ID : " + projectId);
            System.out.println("연구자 : " + userId + " (" + userName + ")");
            System.out.println("총 희망 연구비 : " + total);
            System.out.println("-------------------------------------------------");
            System.out.println("1차(40%) : " + amt1 + " | " + msg1);
            System.out.println("2차(40%) : " + amt2 + " | " + msg2);
            System.out.println("3차(20%) : " + amt3 + " | " + msg3);
            System.out.println("-------------------------------------------------");

            System.out.println("1. 1차 지급 승인");
            System.out.println("2. 2차 지급 승인");
            System.out.println("3. 3차 지급 승인");
            System.out.println("0. 이전");
            System.out.print("선택 : ");
            int choice = readInt();
            if (choice == 0) return;

            int roundNo;
            long amount;
            boolean can;
            boolean already;

            if (choice == 1) {
                roundNo = RR_FundingConst.ROUND1;
                amount = amt1;
                can = can1;
                already = paid1;
            } else if (choice == 2) {
                roundNo = RR_FundingConst.ROUND2;
                amount = amt2;
                can = can2;
                already = paid2;
            } else if (choice == 3) {
                roundNo = RR_FundingConst.ROUND3;
                amount = amt3;
                can = can3;
                already = paid3;
            } else {
                System.out.println("잘못 입력");
                continue;
            }

            if (already) {
                System.out.println("⚠ 이미 지급 완료된 단계입니다.");
                continue;
            }
            if (!can) {
                System.out.println("⚠ 지급 조건이 충족되지 않았습니다.");
                continue;
            }

            int r = fundingDAO.insertPaidFunding(projectId, roundNo, amount, loginUserId);
            if (r > 0) {
                System.out.println("✅ " + roundNo + "차 지급 승인 완료 (" + amount + "원)");
            } else {
                System.out.println("지급 승인 실패(중복/DB 오류 가능)");
            }
        }
    }
    
    


    // ===== 5-3. 중간 보고 승인 =====
    private void midReportMenuByTask(int annId) {
        while (true) {
            System.out.println("\n===== 중간 보고 승인 =====");
            System.out.println("선정된 팀 목록");
            System.out.println("-------------------------------------------------------------");
            System.out.printf("%-4s %-12s %-14s %-8s %-10s%n", "번호", "프로젝트ID", "연구자", "협약", "중간보고");
            System.out.println("-------------------------------------------------------------");

            List<RR_ProjectVO> teams = projectDAO.getTeamsByTask(loginAgyId, annId);

            if (teams == null || teams.isEmpty()) {
                System.out.println("선정된 팀(프로젝트)이 없습니다. (선정완료 후 PROJECTS 생성 필요)");
                return;
            }

            // 팀 목록 출력 + 중간보고 상태 표시
            for (int i = 0; i < teams.size(); i++) {
                RR_ProjectVO t = teams.get(i);

                // 중간보고 최신 1건 조회
                RR_ReportVO mid = reportDAO.getLatestReport(t.getProjectId(), RR_ReportConst.TYPE_MID);

                String midStatus = (mid == null) ? "미제출" : RR_ReportConst.toKor(mid.getStatusCd());
                String agStatus = ("SIGNED".equalsIgnoreCase(t.getAgreementStatusCd())) ? "체결" : "대기";

                System.out.printf("%-4d %-12d %-14s %-8s %-10s%n",
                        (i + 1),
                        t.getProjectId(),
                        cut(t.getUserName(), 10),
                        agStatus,
                        midStatus);
            }
            System.out.println("-------------------------------------------------------------");
            System.out.print("확인할 팀 번호 선택 (0=이전) : ");
            int pick = readInt();
            if (pick == 0) return;

            if (pick < 1 || pick > teams.size()) {
                System.out.println("번호를 다시 선택하세요.");
                continue;
            }

            RR_ProjectVO selected = teams.get(pick - 1);
            midReportDetailMenu(selected); // 상세로 이동
        }
    }

 // 팀 1개(프로젝트) 중간보고 상세 + 승인/반려
    private void midReportDetailMenu(RR_ProjectVO team) {
        int projectId = team.getProjectId();

        // 협약 체결 전이면 승인 불가 (정책)
        if (!"SIGNED".equalsIgnoreCase(team.getAgreementStatusCd())) {
            System.out.println("⚠ 협약이 아직 체결되지 않았습니다. 협약 체결 후 승인 가능합니다.");
            return;
        }

        RR_ReportVO mid = reportDAO.getLatestReport(projectId, RR_ReportConst.TYPE_MID);

        System.out.println("\n===== 중간 보고 상세 =====");
        System.out.println("프로젝트ID : " + projectId);
        System.out.println("연구자 : " + team.getUserId() + " (" + team.getUserName() + ")");

        if (mid == null) {
            System.out.println("중간 보고서 : 미제출");
            return;
        }

        System.out.println("보고서ID : " + mid.getReportRptId());
        System.out.println("제출일 : " + (mid.getSubmittedAt() == null ? "-" : mid.getSubmittedAt()));
        System.out.println("상태 : " + RR_ReportConst.toKor(mid.getStatusCd()));
        System.out.println("진행률 : " + (mid.getProgressRate() == null ? "-" : (mid.getProgressRate() + "%")));
        System.out.println("키워드 : " + (mid.getKeywords() == null ? "-" : mid.getKeywords()));
        System.out.println("내용 : " + (mid.getContent() == null ? "-" : mid.getContent()));
        if (RR_ReportConst.ST_REJECTED.equals(mid.getStatusCd())) {
            System.out.println("반려사유 : " + (mid.getRejectReason() == null ? "-" : mid.getRejectReason()));
        }
        System.out.println("-------------------------------------------------");

        // 승인/반려는 SUBMITTED만 가능(정책)
        if (!RR_ReportConst.ST_SUBMITTED.equals(mid.getStatusCd())) {
            System.out.println("※ 제출(SUBMITTED) 상태에서만 승인/반려 가능합니다.");
            return;
        }

        while (true) {
            System.out.println("1. 승인");
            System.out.println("2. 반려");
            System.out.println("0. 이전");
            System.out.print("선택 : ");
            int sel = readInt();

            if (sel == 0) return;

            if (sel == 1) {
                int r = reportDAO.approveReport(mid.getReportRptId(), loginUserId);
                if (r > 0) System.out.println("✅ 중간 보고 승인 완료");
                else System.out.println("승인 실패(DB 오류)");
                return;
            }

            if (sel == 2) {
                System.out.print("반려 사유 입력 : ");
                String reason = sc.nextLine().trim();
                if (reason.isEmpty()) {
                    System.out.println("⚠ 반려 사유는 필수입니다.");
                    continue;
                }
                int r = reportDAO.rejectReport(mid.getReportRptId(), loginUserId, reason);
                if (r > 0) System.out.println("✅ 중간 보고 반려 완료");
                else System.out.println("반려 실패(DB 오류)");
                return;
            }

            System.out.println("잘못 입력했습니다.");
        }
    }
    
    
 // ===== 5-4. 최종 보고 승인 =====
    private void finalReportMenuByTask(int annId) {
        while (true) {
            System.out.println("\n===== 최종 보고 승인 =====");
            System.out.println("선정된 팀 목록");
            System.out.println("-------------------------------------------------------------");
            System.out.printf("%-4s %-12s %-14s %-8s %-10s%n", "번호", "프로젝트ID", "연구자", "협약", "최종보고");
            System.out.println("-------------------------------------------------------------");

            List<RR_ProjectVO> teams = projectDAO.getTeamsByTask(loginAgyId, annId);

            if (teams == null || teams.isEmpty()) {
                System.out.println("선정된 팀(프로젝트)이 없습니다.");
                return;
            }

            for (int i = 0; i < teams.size(); i++) {
                RR_ProjectVO t = teams.get(i);

                RR_ReportVO fin = reportDAO.getLatestReport(t.getProjectId(), RR_ReportConst.TYPE_FINAL); // "FINAL"
                String finStatus = (fin == null) ? "미제출" : RR_ReportConst.toKor(fin.getStatusCd());
                String agStatus = ("SIGNED".equalsIgnoreCase(t.getAgreementStatusCd())) ? "체결" : "대기";

                System.out.printf("%-4d %-12d %-14s %-8s %-10s%n",
                        (i + 1),
                        t.getProjectId(),
                        cut(t.getUserName(), 10),
                        agStatus,
                        finStatus);
            }
            System.out.println("-------------------------------------------------------------");

            System.out.print("확인할 팀 번호 선택 (0=이전) : ");
            int pick = readInt();
            if (pick == 0) return;

            if (pick < 1 || pick > teams.size()) {
                System.out.println("번호를 다시 선택하세요.");
                continue;
            }

            RR_ProjectVO selected = teams.get(pick - 1);
            finalReportDetailMenu(selected);
        }
    }

    private void finalReportDetailMenu(RR_ProjectVO team) {
        int projectId = team.getProjectId();

        // 협약 체결 전이면 승인 불가
        if (!"SIGNED".equalsIgnoreCase(team.getAgreementStatusCd())) {
            System.out.println("⚠ 협약이 아직 체결되지 않았습니다. 협약 체결 후 승인 가능합니다.");
            return;
        }

        // (정책) 최종보고 승인은 2차 지급 완료 후 가능하게 막기 (원하면 삭제 가능)
        boolean paid2 = fundingDAO.isRoundPaid(projectId, RR_FundingConst.ROUND2);
        if (!paid2) {
            System.out.println("⚠ 2차 연구비 지급 완료 후 최종보고 승인이 가능합니다.");
            return;
        }

        RR_ReportVO fin = reportDAO.getLatestReport(projectId, RR_ReportConst.TYPE_FINAL); // "FINAL"

        System.out.println("\n===== 최종 보고 상세 =====");
        System.out.println("프로젝트ID : " + projectId);
        System.out.println("연구자 : " + team.getUserId() + " (" + team.getUserName() + ")");

        if (fin == null) {
            System.out.println("최종 보고서 : 미제출");
            return;
        }

        System.out.println("보고서ID : " + fin.getReportRptId());
        System.out.println("제출일 : " + (fin.getSubmittedAt() == null ? "-" : fin.getSubmittedAt()));
        System.out.println("상태 : " + RR_ReportConst.toKor(fin.getStatusCd()));
        System.out.println("진행률 : " + (fin.getProgressRate() == null ? "-" : (fin.getProgressRate() + "%")));
        System.out.println("키워드 : " + (fin.getKeywords() == null ? "-" : fin.getKeywords()));
        System.out.println("내용 : " + (fin.getContent() == null ? "-" : fin.getContent()));
        if (RR_ReportConst.ST_REJECTED.equals(fin.getStatusCd())) {
            System.out.println("반려사유 : " + (fin.getRejectReason() == null ? "-" : fin.getRejectReason()));
        }
        System.out.println("-------------------------------------------------");

        // 제출된 것만 승인/반려 가능
        if (!RR_ReportConst.ST_SUBMITTED.equals(fin.getStatusCd())) {
            System.out.println("※ 제출(SUBMITTED) 상태에서만 승인/반려 가능합니다.");
            return;
        }

        while (true) {
            System.out.println("1. 승인");
            System.out.println("2. 반려");
            System.out.println("0. 이전");
            System.out.print("선택 : ");
            int sel = readInt();

            if (sel == 0) return;

            if (sel == 1) {
                // approveReport는 타입 체크 없이 승인하므로, FINAL 전용 메소드가 있으면 그걸 써도 됨
                int r = reportDAO.approveReport(fin.getReportRptId(), loginUserId);
                if (r > 0) System.out.println("✅ 최종 보고 승인 완료");
                else System.out.println("승인 실패(DB 오류)");
                return;
            }

            if (sel == 2) {
                System.out.print("반려 사유 입력 : ");
                String reason = sc.nextLine().trim();
                if (reason.isEmpty()) {
                    System.out.println("⚠ 반려 사유는 필수입니다.");
                    continue;
                }
                int r = reportDAO.rejectReport(fin.getReportRptId(), loginUserId, reason);
                if (r > 0) System.out.println("✅ 최종 보고 반려 완료");
                else System.out.println("반려 실패(DB 오류)");
                return;
            }

            System.out.println("잘못 입력했습니다.");
        }
    }
    
    
    
    // 5-5 중단: 과제 안의 팀 목록 보여주고 팀 선택해서 중단
    private void stopTeamMenuByTask(int annId) {
        while (true) {
            System.out.println("\n===== 연구 중단 처리 =====");

            List<RR_ProjectVO> teams = projectDAO.getTeamsByTask(loginAgyId, annId);
            if (teams.isEmpty()) {
                System.out.println("선정된 팀(프로젝트)이 없습니다.");
                return;
            }

            System.out.println("중단 가능 팀 목록");
            System.out.println("-------------------------------------------------------------");
            System.out.printf("%-4s %-10s %-10s %-8s%n", "번호", "프로젝트ID", "연구자", "상태");
            System.out.println("-------------------------------------------------------------");

            for (int i = 0; i < teams.size(); i++) {
                RR_ProjectVO p = teams.get(i);
                System.out.printf("%-4d %-10d %-10s %-8s%n",
                        (i + 1),
                        p.getProjectId(),
                        cut(p.getUserName(), 8),
                        mapProjectStatus(p.getProjectStatusCd()));
            }
            System.out.println("-------------------------------------------------------------");

            System.out.print("중단할 팀 번호 선택 (0=이전) : ");
            int pick = readInt();
            if (pick == 0) return;
            if (pick < 1 || pick > teams.size()) {
                System.out.println("잘못된 번호입니다.");
                continue;
            }

            RR_ProjectVO target = teams.get(pick - 1);

            System.out.println("정말 중단하시겠습니까?");
            System.out.println("1. 예");
            System.out.println("2. 아니오");
            System.out.print("선택 : ");
            int confirm = readInt();
            if (confirm != 1) continue;

            int r = projectDAO.stopProject(target.getProjectId());
            if (r > 0) {
                System.out.println(target.getUserName() + " 팀 과제 중단 처리 완료");
                System.out.println("현재 상태 : 중단");
            } else {
                System.out.println("중단 처리 실패");
            }

            System.out.println("\n1. 다른 팀 중단 처리");
            System.out.println("0. 이전 메뉴");
            System.out.print("선택 : ");
            int next = readInt();
            if (next == 0) return;
        }
    }
    
    
    
    
 // ===== 6. 과제 진행 현황 조회 =====
    private void taskProgressMenu() {
        while (true) {
            System.out.println("\n===== 과제 진행 현황 조회 =====");

            List<RR_TaskProgressVO> tasks = progressDAO.getTaskProgressList(loginAgyId);

            System.out.println("------------------------------------------------------------");
            System.out.printf("%-8s %-18s %-14s %-8s %-8s%n", "과제ID", "과제명", "총예산", "선정팀수", "과제상태");
            System.out.println("------------------------------------------------------------");

            if (tasks == null || tasks.isEmpty()) {
                System.out.println("진행 현황을 조회할 과제가 없습니다. (PROJECTS가 생성된 과제만 표시됨)");
                System.out.println("0. 이전 메뉴");
                readInt();
                return;
            }

            for (RR_TaskProgressVO t : tasks) {
                System.out.printf("%-8d %-18s %-14s %-8s %-8s%n",
                        t.getAnnId(),
                        cut(t.getTitle(), 16),
                        formatMoney(t.getTotalBudget()),
                        t.getTeamCount() + "팀",
                        t.getTaskStatus());
            }
            System.out.println("------------------------------------------------------------");

            System.out.print("조회할 과제 ID 입력 (0=이전) : ");
            int annId = readInt();
            if (annId == 0) return;

            taskProgressDetail(annId);
        }
    }

    private void taskProgressDetail(int annId) {
        while (true) {
            List<RR_TeamProgressVO> teams = progressDAO.getTeamProgressList(loginAgyId, annId);

            if (teams == null || teams.isEmpty()) {
                System.out.println("해당 과제ID를 찾을 수 없습니다. (선정된 프로젝트가 없을 수 있음)");
                return;
            }

            System.out.println("\n===== 과제 상세 현황 =====");
            System.out.println("과제ID : " + annId);
            System.out.println("선정 팀수 : " + teams.size() + "팀");
            System.out.println("--------------------------------------------------------------");
            System.out.printf("%-4s %-10s %-10s %-8s %-14s %-8s %-8s %-8s%n",
                    "번호", "프로젝트ID", "연구자", "협약", "지급단계", "중간", "최종", "팀상태");
            System.out.println("--------------------------------------------------------------");

            for (int i = 0; i < teams.size(); i++) {
                RR_TeamProgressVO t = teams.get(i);

                String ag = toAgreementKor(t.getAgreementStatusCd());
                String mid = toReportKor(t.getMidStatusCd());
                String fin = toReportKor(t.getFinalStatusCd());

                boolean midApproved = "APPROVED".equalsIgnoreCase(t.getMidStatusCd());
                boolean finApproved = "APPROVED".equalsIgnoreCase(t.getFinalStatusCd());

                String stage = toPayStageKor(t.getPaidRound(), midApproved, finApproved);
                String pstat = toProjectKor(t.getProjectStatusCd());

                System.out.printf("%-4d %-10d %-10s %-8s %-14s %-8s %-8s %-8s%n",
                        (i + 1),
                        t.getProjectId(),
                        cut(t.getUserName(), 8),
                        ag,
                        stage,
                        mid,
                        fin,
                        pstat);
            }
            System.out.println("--------------------------------------------------------------");

            System.out.println("1. 팀 상세 조회");
            System.out.println("0. 이전 메뉴");
            System.out.print("선택 : ");
            int sel = readInt();

            if (sel == 0) return;

            if (sel != 1) {
                System.out.println("잘못 입력");
                continue;
            }

            System.out.print("조회할 팀 번호 선택 : ");
            int teamNo = readInt();

            if (teamNo < 1 || teamNo > teams.size()) {
                System.out.println("잘못된 번호입니다.");
                continue;
            }

            int projectId = teams.get(teamNo - 1).getProjectId();
            RR_TeamProgressVO picked = progressDAO.getTeamProgressDetail(projectId);

            if (picked == null) {
                System.out.println("팀 상세 정보를 찾을 수 없습니다.");
                continue;
            }

            //  상세 화면 출력 후 엔터 누르면 자동으로 while 루프가 다시 '과제 상세 현황'을 출력
            printTeamDetail(picked);
        }
    }

    private void printTeamDetail(RR_TeamProgressVO t) {
        System.out.println("\n===== 팀 상세 정보 =====");
        System.out.println("연구자 : " + t.getUserId() + " (" + t.getUserName() + ")");

        System.out.println("\n[선정 정보]");
        System.out.println("선정일 : " + (t.getSelectionApprovedAt() == null ? "-" : t.getSelectionApprovedAt()));
        System.out.println("총점 : " + (t.getSelectionScore() == null ? "-" : t.getSelectionScore()));

        System.out.println("\n[협약 정보]");
        System.out.println("협약 상태 : " + toAgreementKor(t.getAgreementStatusCd()));
        System.out.println("협약 체결일 : " + (t.getAgreementSignedAt() == null ? "-" : t.getAgreementSignedAt()));

        System.out.println("\n[연구비 지급 정보]");
        System.out.println("현재 지급 단계 : " + t.getPaidRound() + "차 완료");
        System.out.println("총 지급 금액 : " + formatMoney(t.getPaidTotalAmt()));

        System.out.println("\n[보고 상태]");
        System.out.println("중간 보고 : " + toReportKor(t.getMidStatusCd()));
        System.out.println("최종 보고 : " + toReportKor(t.getFinalStatusCd()));

        System.out.println("\n현재 상태 : " + toProjectKor(t.getProjectStatusCd()));

        System.out.println("\n(엔터를 누르면 팀 목록으로 돌아갑니다)");
        sc.nextLine(); //  엔터 대기
    }

    // ===== 표시 변환 유틸 =====
    private String toAgreementKor(String cd) {
        if (cd == null) return "대기";
        if ("SIGNED".equalsIgnoreCase(cd) || "체결".equals(cd)) return "체결";
        return "대기";
    }

    private String toReportKor(String cd) {
        if (cd == null) return "미제출";
        if ("SUBMITTED".equalsIgnoreCase(cd)) return "제출";
        if ("APPROVED".equalsIgnoreCase(cd)) return "승인";
        if ("REJECTED".equalsIgnoreCase(cd)) return "반려";
        return cd;
    }

    private String toProjectKor(String cd) {
        if (cd == null) return "진행중";
        if ("STOPPED".equalsIgnoreCase(cd) || "중단".equals(cd)) return "중단";
        if ("COMPLETED".equalsIgnoreCase(cd) || "완료".equals(cd)) return "완료";
        return "진행중"; // ONGOING/IN_PROGRESS 등은 진행중 처리
    }

    // 지급단계 표시(단순 버전)
    // paidRound: 0~3, midApproved/finalApproved는 'APPROVED' 여부(리스트 기준)
    private String toPayStageKor(int paidRound, boolean midApproved, boolean finalApproved) {
        if (paidRound >= 3) return "최종 지급 완료";
        if (paidRound == 2) return finalApproved ? "최종 지급 대기" : "2차 지급 완료";
        if (paidRound == 1) return midApproved ? "2차 지급 대기" : "1차 지급 완료";
        return "미지급";
    }

    // 돈 표시
    private String formatMoney(long v) {
        return String.format("%,d원", v);
    }
    
    
    

    // 상태 표시용 매핑
    private String mapAgreementStatus(String cd) {
        if (cd == null) return "대기";
        if ("SIGNED".equalsIgnoreCase(cd)) return "체결";
        return "대기";
    }

    private String mapProjectStatus(String cd) {
        if (cd == null) return "대기";
        if ("ONGOING".equalsIgnoreCase(cd) || "IN_PROGRESS".equalsIgnoreCase(cd)) return "진행중";
        if ("STOPPED".equalsIgnoreCase(cd)) return "중단";
        if ("COMPLETED".equalsIgnoreCase(cd)) return "완료";
        return cd;
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
        //new RR_KRDAdminMain();
    }
}