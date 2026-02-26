package kr.krd.main;

import java.util.List;
import java.util.Scanner;

import kr.krd.dao.RR_AnnouncementDAO;
import kr.krd.vo.RR_AnnouncementVO;
import kr.krd.dao.RR_ApplicationDAO;
import kr.krd.vo.RR_ApplicationVO;

public class RR_KRDAdminMain {

    private Scanner sc = new Scanner(System.in);
    private RR_AnnouncementDAO announcementDAO = new RR_AnnouncementDAO();
    private RR_ApplicationDAO applicationDAO = new RR_ApplicationDAO();

    // 상태값 통일 (DB에서 OPEN/CLOSED로 쓰고 싶으면 "OPEN"으로 바꾸기)
    private static final String STATUS_OPEN = "공고중";

    // 로그인 후 실제 값으로 세팅되어야 함 (일단 테스트용)
    // 나중에 로그인 성공 시 이 값들에 실제 로그인 사용자 정보 넣으면 됨
    private int loginAgyId = 1;              // 기관ID
    private String loginUserId = "agy01";  // 사용자ID

    public RR_KRDAdminMain() {
        callMenu();
    }

    // ===== 기관 담당자 메뉴 =====
    private void callMenu() {
        while (true) {
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
        vo.setStatus(STATUS_OPEN);
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

        // 공고중일 때만 수정 가능
        if (!STATUS_OPEN.equals(vo.getStatus())) {
            System.out.println("⚠ 해당 공고는 이미 " + vo.getStatus() + "입니다.");
            System.out.println("공고중일 때만 수정 가능합니다.");
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
            case 5:
                System.out.print("새 마감일(yyyy-MM-dd) 입력 : ");
                newValue = sc.nextLine().trim();
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

        // 공고중 상태만 삭제 가능
        if (!STATUS_OPEN.equals(vo.getStatus())) {
            System.out.println("⚠ 공고중 상태에서만 삭제 가능합니다.");
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
            // 이미 네가 만든 공고 목록 출력 함수가 있으면 그걸 써도 됨
            // 여기선 2번 메뉴에서 쓰던 목록 출력 그대로 재사용 추천:
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