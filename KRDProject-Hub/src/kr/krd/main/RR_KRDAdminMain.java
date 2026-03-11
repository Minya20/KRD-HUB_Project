package kr.krd.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import kr.krd.dao.RR_AnnouncementDAO;
import kr.krd.dao.RR_ApplicationDAO;
import kr.krd.dao.RR_SelectionDAO;
import kr.krd.dao.RR_ProjectDAO;
import kr.krd.dao.RR_FundingDAO;
import kr.krd.dao.RR_ReportDAO;
import kr.krd.dao.RR_TaskProgressDAO;

import kr.krd.vo.RR_AnnouncementVO;
import kr.krd.vo.RR_ApplicationVO;
import kr.krd.vo.RR_SelectionVO;
import kr.krd.vo.RR_ProjectVO;
import kr.krd.vo.RR_TaskVO;
import kr.krd.vo.RR_ReportVO;
import kr.krd.vo.RR_TaskProgressVO;
import kr.krd.vo.RR_TeamProgressVO;

import kr.krd.constant.RR_AnnouncementStatus;
import kr.krd.constant.RR_FundingConst;
import kr.krd.constant.RR_ReportConst;

import kr.util.DBUtil;

public class RR_KRDAdminMain {

	// ------------------------------------------------
	// ANSI Escape Codes & UI Elements
	// ------------------------------------------------
	public static final String RESET = "\u001B[0m";
	public static final String BOLD = "\u001B[1m";
	public static final String UNBOLD = "\u001B[22m";

	public static final String CLR_PRIMARY = "\u001B[36m"; // Cyan
	public static final String CLR_WHT = "\u001B[37m";    // White
	public static final String CLR_GRY = "\u001B[90m";    // Gray
	public static final String CLR_ERR = "\u001B[31m";    // Red
	public static final String CLR_SUC = "\u001B[32m";    // Green

	private static final String INDENT = "      ";

	// ------------------------------------------------
	// Fields
	// ------------------------------------------------
	private Scanner sc = new Scanner(System.in);
	private RR_AnnouncementDAO announcementDAO = new RR_AnnouncementDAO();
	private RR_ApplicationDAO applicationDAO = new RR_ApplicationDAO();
	private RR_SelectionDAO selectionDAO = new RR_SelectionDAO();
	private static final int REQUIRED_REVIEWERS = 5; // 신청서 1건당 평가 제출 개수
	private RR_ProjectDAO projectDAO = new RR_ProjectDAO();
	private RR_FundingDAO fundingDAO = new RR_FundingDAO();
	private RR_ReportDAO reportDAO = new RR_ReportDAO();
	private RR_TaskProgressDAO progressDAO = new RR_TaskProgressDAO();

	private int loginAgyId;
	private String loginUserId;

	public RR_KRDAdminMain(int loginAgyId, String loginUserId) {
		this.loginAgyId = loginAgyId;
		this.loginUserId = loginUserId;
	}

	// ------------------------------------------------
	// UI Helper Methods
	// ------------------------------------------------
	private void printSubTitle(String title) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ " + title + RESET);
		System.out.println(INDENT + CLR_GRY + "──────────────────────────────────────────────────────────────" + RESET);
	}

	private void printMenuOption(String key, String description) {
		System.out.print(INDENT + "  " + CLR_PRIMARY + BOLD + "[" + key + "]" + RESET + " ");
		System.out.println(CLR_WHT + description + RESET);
	}

	private void printInputTag(String tag) {
		System.out.print("\n" + INDENT + CLR_PRIMARY + BOLD + "▶ " + tag + RESET + " : ");
	}

	private void printError(String msg) {
		System.out.println("\n" + INDENT + CLR_ERR + BOLD + "✘ Error: " + UNBOLD + RESET + msg);
	}

	private void printSuccess(String msg) {
		System.out.println("\n" + INDENT + CLR_SUC + BOLD + "✔ Success: " + UNBOLD + RESET + msg);
	}

	private String getInputPrompt(String tag) {
		return "\n" + INDENT + CLR_PRIMARY + BOLD + "▶ " + tag + RESET + " : ";
	}

	private String cut(String str, int len) {
		if (str == null) return "";
		if (str.length() <= len) return str;
		return str.substring(0, len - 2) + "..";
	}

	// 상태 텍스트에 따른 색상 자동 할당 유틸리티
	private String getStatusColor(String status) {
		if (status == null) return CLR_WHT;
		String s = status.toUpperCase();
		if (s.contains("OPEN") || s.contains("완료") || s.contains("승인") || s.contains("체결") || s.contains("SELECTED") || s.contains("선정")) return CLR_SUC;
		if (s.contains("CLOSED") || s.contains("탈락") || s.contains("반려") || s.contains("중단") || s.contains("REJECTED") || s.contains("CANCELLED") || s.contains("실패") || s.contains("초과")) return CLR_ERR;
		if (s.contains("SCHEDULED") || s.contains("대기") || s.contains("진행") || s.contains("PENDING") || s.contains("APPLIED") || s.contains("심사") || s.contains("ONGOING") || s.contains("후보")) return CLR_PRIMARY;
		return CLR_WHT;
	}

	// ===== 기관 담당자 메뉴 =====
	public boolean callMenu() {
		while (true) {
			syncAnnouncementStatusByDate();

			System.out.println("\n\n" + INDENT + CLR_PRIMARY + BOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
			System.out.println(INDENT + BOLD + "   KRD Hubs | " + RESET + CLR_WHT + "기관 담당자 대시보드 (Agency Admin)" + RESET);
			System.out.println(INDENT + CLR_PRIMARY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
			System.out.println(INDENT + CLR_GRY + "  접속 계정: " + RESET + loginUserId + CLR_GRY + " | 소속 기관ID: " + RESET + loginAgyId);
			System.out.println();
			
			printMenuOption("1", "공고 등록 (Register Announcement)");
			printMenuOption("2", "공고 조회 및 관리 (Manage Announcements)");
			printMenuOption("3", "신청자 목록 조회 (View Applicants)");
			printMenuOption("4", "선정 관리 (Selection Management)");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("5", "연구 진행 관리 (Research Progress)");
			printMenuOption("6", "과제 진행 현황 조회 (Task Status)");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("7", "시스템 로그아웃 (Logout)");
			printMenuOption("8", "프로그램 종료 (Exit)");

			System.out.print(getInputPrompt("번호 선택"));
			int menu = readInt();

			switch (menu) {
				case 1: registerAnnouncement(); break;
				case 2: manageAnnouncement(); break;
				case 3: applicantListMenu(); break;
				case 4: selectionMenu(); break;
				case 5: researchProgressMenu(); break;
				case 6: taskProgressMenu(); break;
				case 7:
					printInputTag("로그아웃 하시겠습니까? (Y/N)");
					if ("Y".equalsIgnoreCase(sc.nextLine().trim())) {
						printSuccess("안전하게 로그아웃 되었습니다.");
						return true;
					}
					break;
				case 8:
					System.out.println("\n" + INDENT + "프로그램을 종료합니다.");
					System.exit(0);
					break;
				default: printError("잘못된 입력입니다.");
			}
		}
	}

	// ===== 날짜 기준 상태 자동 갱신 =====
	private void syncAnnouncementStatusByDate() {
		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = DBUtil.getConnection();
			String sql =
				"UPDATE ANNOUNCEMENT "
			  + "SET ANNOUNCEMENT_STATUS = CASE "
			  + "  WHEN TO_DATE(ANNOUNCEMENT_START_DT, 'YYYY-MM-DD') > TRUNC(SYSDATE) THEN ? "
			  + "  WHEN TO_DATE(ANNOUNCEMENT_END_DT,   'YYYY-MM-DD') < TRUNC(SYSDATE) THEN ? "
			  + "  ELSE ? "
			  + "END "
			  + "WHERE ANNOUNCEMENT_HIDDEN_YN = 0 "
			  + "  AND ANNOUNCEMENT_STATUS IN (?, ?, ?) "
			  + "  AND REGEXP_LIKE(ANNOUNCEMENT_START_DT, '^\\d{4}-\\d{2}-\\d{2}$') "
			  + "  AND REGEXP_LIKE(ANNOUNCEMENT_END_DT,   '^\\d{4}-\\d{2}-\\d{2}$')";

			pstmt = conn.prepareStatement(sql);
			int idx = 1;
			pstmt.setString(idx++, RR_AnnouncementStatus.SCHEDULED);
			pstmt.setString(idx++, RR_AnnouncementStatus.CLOSED);
			pstmt.setString(idx++, RR_AnnouncementStatus.OPEN);
			pstmt.setString(idx++, RR_AnnouncementStatus.SCHEDULED);
			pstmt.setString(idx++, RR_AnnouncementStatus.OPEN);
			pstmt.setString(idx++, RR_AnnouncementStatus.CLOSED);
			pstmt.executeUpdate();
		} catch (Exception e) { e.printStackTrace(); } 
		finally { DBUtil.executeClose(null, pstmt, conn); }
	}

	// ===== 1. 공고 등록 =====
	private void registerAnnouncement() {
		printSubTitle("신규 공고 등록 (Register Announcement)");
		RR_AnnouncementVO vo = new RR_AnnouncementVO();

		printInputTag("과제명 입력");
		String title = sc.nextLine().trim();

		printInputTag("과제 설명(공고문/양식명 포함 가능)");
		String desc = sc.nextLine().trim();

		printInputTag("모집 분야 입력");
		String field = sc.nextLine().trim();

		printInputTag("총 배정 예산(숫자)");
		long totalBudget = readLong();

		printInputTag("선정 팀 수(숫자)");
		int recruitCap = readInt();

		printInputTag("신청 시작일 (예: 2026-03-27)");
		String startDt = sc.nextLine().trim();

		printInputTag("신청 마감일 (예: 2026-04-27)");
		String endDt = sc.nextLine().trim();

		LocalDate start; LocalDate end;
		try {
			start = LocalDate.parse(startDt);
			end = LocalDate.parse(endDt);
		} catch (DateTimeParseException e) {
			printError("날짜 형식이 올바르지 않습니다.");
			return;
		}

		if (start.isAfter(end)) { printError("시작일이 마감일보다 늦을 수 없습니다."); return; }
		if (end.isBefore(LocalDate.now())) { printError("마감일이 이미 지난 공고는 등록할 수 없습니다."); return; }

		printInputTag("담당자 연락처");
		String pmContact = sc.nextLine().trim();

		System.out.println("\n" + INDENT + "  [1] 공고 최종 등록   [2] 취소 및 이전화면");
		System.out.print(getInputPrompt("선택"));
		int select = readInt();

		if (select != 1) { printError("공고 등록을 취소했습니다."); return; }

		vo.setAgyId(loginAgyId);
		vo.setTitle(title);
		vo.setDesc(desc);
		vo.setField(field);
		vo.setTotalBudget(totalBudget);
		vo.setRecruitCap(recruitCap);
		vo.setStartDt(startDt);
		vo.setEndDt(endDt);
		vo.setPmContact(pmContact);
		vo.setReannYn(0);

		if (start.isAfter(LocalDate.now())) vo.setStatus(RR_AnnouncementStatus.SCHEDULED);
		else vo.setStatus(RR_AnnouncementStatus.OPEN);

		vo.setCreatedBy(loginUserId);
		vo.setHiddenYn(0);

		int result = announcementDAO.insertAnnouncement(vo);
		if (result > 0) printSuccess("공고가 정상적으로 등록되었습니다.");
		else printError("공고 등록에 실패했습니다.");
	}

	// ===== 2. 공고 수정 / 삭제 =====
	private void manageAnnouncement() {
		while (true) {
			printSubTitle("공고 조회 및 관리 (Manage Announcements)");
			printAnnouncementList();

			System.out.println();
			printMenuOption("1", "특정 공고 내용 수정");
			printMenuOption("2", "특정 공고 삭제 처리");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("3", "이전 화면으로 돌아가기");

			System.out.print(getInputPrompt("메뉴 선택"));
			int menu = readInt();

			switch (menu) {
				case 1: updateAnnouncementFlow(); break;
				case 2: deleteAnnouncementFlow(); break;
				case 3: return;
				default: printError("잘못된 입력입니다.");
			}
		}
	}

	private void printAnnouncementList() {
		syncAnnouncementStatusByDate();
		List<RR_AnnouncementVO> list = announcementDAO.getAnnouncementListByAgency(loginAgyId);

		if (list == null || list.isEmpty()) {
			System.out.println(INDENT + CLR_GRY + "  등록된 공고가 없습니다." + RESET);
			return;
		}

		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-8s | %-25s | %-12s | %-8s | %-12s", "과제번호", "과제명", "상태", "신청팀수", "마감일") + RESET);
		System.out.println(INDENT + CLR_GRY + "-----------------------------------------------------------------------------" + RESET);

		for (RR_AnnouncementVO vo : list) {
			String stat = vo.getStatus();
			System.out.println(INDENT + String.format("%-10d | %-25s | " + getStatusColor(stat) + "%-12s" + RESET + " | %-8d | %-12s",
					vo.getAnnId(), cut(vo.getTitle(), 23), stat, vo.getApplicantCount(), vo.getEndDt()));
		}
		System.out.println(INDENT + CLR_GRY + "-----------------------------------------------------------------------------" + RESET);
	}

	private void printClosedAnnouncementListOnly() {
		syncAnnouncementStatusByDate();
		List<RR_AnnouncementVO> list = announcementDAO.getAnnouncementListByAgency(loginAgyId);
		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-8s | %-25s | %-12s | %-8s | %-12s", "과제번호", "과제명", "상태", "신청팀수", "마감일") + RESET);
		System.out.println(INDENT + CLR_GRY + "-----------------------------------------------------------------------------" + RESET);

		boolean hasAny = false;
		if (list != null) {
			for (RR_AnnouncementVO vo : list) {
				if (!RR_AnnouncementStatus.CLOSED.equals(vo.getStatus())) continue;
				hasAny = true;
				System.out.println(INDENT + String.format("%-10d | %-25s | " + getStatusColor(vo.getStatus()) + "%-12s" + RESET + " | %-8d | %-12s",
					vo.getAnnId(), cut(vo.getTitle(), 23), vo.getStatus(), vo.getApplicantCount(), vo.getEndDt()));
			}
		}
		if (!hasAny) System.out.println(INDENT + CLR_GRY + "  마감된 공고가 없습니다." + RESET);
		System.out.println(INDENT + CLR_GRY + "-----------------------------------------------------------------------------" + RESET);
	}

	private boolean printSelectPendingAnnouncementListOnly() {
		syncAnnouncementStatusByDate();
		announcementDAO.promoteClosedToSelectPending(loginAgyId);
		announcementDAO.syncSelectedDoneAnnouncements(loginAgyId);

		List<RR_AnnouncementVO> list = announcementDAO.getAnnouncementListByAgency(loginAgyId);
		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-8s | %-25s | %-12s | %-8s | %-12s", "과제번호", "과제명", "상태", "신청팀수", "마감일") + RESET);
		System.out.println(INDENT + CLR_GRY + "-----------------------------------------------------------------------------" + RESET);

		boolean hasAny = false;
		if (list != null) {
			for (RR_AnnouncementVO vo : list) {
				if (!RR_AnnouncementStatus.SELECT_PENDING.equals(vo.getStatus())) continue;
				hasAny = true;
				System.out.println(INDENT + String.format("%-10d | %-25s | " + getStatusColor(vo.getStatus()) + "%-12s" + RESET + " | %-8d | %-12s",
					vo.getAnnId(), cut(vo.getTitle(), 23), vo.getStatus(), vo.getApplicantCount(), vo.getEndDt()));
			}
		}
		if (!hasAny) System.out.println(INDENT + CLR_GRY + "  선정 대기 중인 공고가 없습니다." + RESET);
		System.out.println(INDENT + CLR_GRY + "-----------------------------------------------------------------------------" + RESET);
		return hasAny;
	}

	private boolean printSelectDoneAnnouncementListOnly() {
		syncAnnouncementStatusByDate();
		announcementDAO.promoteClosedToSelectPending(loginAgyId);
		announcementDAO.syncSelectedDoneAnnouncements(loginAgyId);

		List<RR_AnnouncementVO> list = announcementDAO.getAnnouncementListByAgency(loginAgyId);
		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-8s | %-25s | %-12s | %-8s | %-12s", "과제번호", "과제명", "상태", "신청팀수", "마감일") + RESET);
		System.out.println(INDENT + CLR_GRY + "-----------------------------------------------------------------------------" + RESET);

		boolean hasAny = false;
		if (list != null) {
			for (RR_AnnouncementVO vo : list) {
				if (!RR_AnnouncementStatus.SELECT_DONE.equals(vo.getStatus())) continue;
				hasAny = true;
				System.out.println(INDENT + String.format("%-10d | %-25s | " + getStatusColor(vo.getStatus()) + "%-12s" + RESET + " | %-8d | %-12s",
					vo.getAnnId(), cut(vo.getTitle(), 23), vo.getStatus(), vo.getApplicantCount(), vo.getEndDt()));
			}
		}
		if (!hasAny) System.out.println(INDENT + CLR_GRY + "  선정 완료된 공고가 없습니다." + RESET);
		System.out.println(INDENT + CLR_GRY + "-----------------------------------------------------------------------------" + RESET);
		return hasAny;
	}

	private void printOpenAnnouncementListOnly() {
		syncAnnouncementStatusByDate();
		List<RR_AnnouncementVO> list = announcementDAO.getAnnouncementListByAgency(loginAgyId);
		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-8s | %-25s | %-12s | %-8s | %-12s", "과제번호", "과제명", "상태", "신청팀수", "마감일") + RESET);
		System.out.println(INDENT + CLR_GRY + "-----------------------------------------------------------------------------" + RESET);

		boolean hasAny = false;
		if (list != null) {
			for (RR_AnnouncementVO vo : list) {
				if (!RR_AnnouncementStatus.OPEN.equals(vo.getStatus())) continue;
				hasAny = true;
				System.out.println(INDENT + String.format("%-10d | %-25s | " + getStatusColor(vo.getStatus()) + "%-12s" + RESET + " | %-8d | %-12s",
					vo.getAnnId(), cut(vo.getTitle(), 23), vo.getStatus(), vo.getApplicantCount(), vo.getEndDt()));
			}
		}
		if (!hasAny) System.out.println(INDENT + CLR_GRY + "  현재 진행 중인 공고가 없습니다." + RESET);
		System.out.println(INDENT + CLR_GRY + "-----------------------------------------------------------------------------" + RESET);
	}

	private void printOpenOrClosedAnnouncementListOnly() {
		syncAnnouncementStatusByDate();
		List<RR_AnnouncementVO> list = announcementDAO.getAnnouncementListByAgency(loginAgyId);
		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-8s | %-25s | %-12s | %-8s | %-12s", "과제번호", "과제명", "상태", "신청팀수", "마감일") + RESET);
		System.out.println(INDENT + CLR_GRY + "-----------------------------------------------------------------------------" + RESET);

		boolean hasAny = false;
		if (list != null) {
			for (RR_AnnouncementVO vo : list) {
				if (RR_AnnouncementStatus.SCHEDULED.equals(vo.getStatus())) continue;
				hasAny = true;
				System.out.println(INDENT + String.format("%-10d | %-25s | " + getStatusColor(vo.getStatus()) + "%-12s" + RESET + " | %-8d | %-12s",
					vo.getAnnId(), cut(vo.getTitle(), 23), vo.getStatus(), vo.getApplicantCount(), vo.getEndDt()));
			}
		}
		if (!hasAny) System.out.println(INDENT + CLR_GRY + "  조회 가능한 공고가 없습니다." + RESET);
		System.out.println(INDENT + CLR_GRY + "-----------------------------------------------------------------------------" + RESET);
	}

	private void updateAnnouncementFlow() {
		printInputTag("수정할 과제 ID 입력");
		int annId = readInt();
		RR_AnnouncementVO vo = announcementDAO.getAnnouncementDetail(annId, loginAgyId);

		if (vo == null) { printError("해당 공고를 찾을 수 없습니다."); return; }

		if (!(RR_AnnouncementStatus.OPEN.equals(vo.getStatus()) || RR_AnnouncementStatus.SCHEDULED.equals(vo.getStatus()))) {
			printError("해당 공고는 이미 [" + vo.getStatus() + "] 상태입니다. 공고예정/공고중일 때만 수정 가능합니다.");
			return;
		}

		System.out.println("\n" + INDENT + CLR_PRIMARY + "┌──────────────────────────────────────────────────────────────┐" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "과제명", vo.getTitle());
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "과제 설명", (vo.getDesc() == null ? "-" : vo.getDesc()));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%,d 원\n" + RESET, "총 예산", vo.getTotalBudget());
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%d 팀\n" + RESET, "선정 팀수", vo.getRecruitCap());
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "신청 마감일", vo.getEndDt());
		System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────────────────────┘" + RESET);

		System.out.println();
		printMenuOption("1", "과제명 수정"); printMenuOption("2", "과제 설명 수정"); printMenuOption("3", "예산 수정");
		printMenuOption("4", "선정 팀수 수정"); printMenuOption("5", "마감일 수정"); printMenuOption("6", "취소 및 뒤로가기");
		System.out.print(getInputPrompt("수정할 항목 번호 선택"));

		int fieldNo = readInt();
		if (fieldNo == 6) { printError("수정을 취소했습니다."); return; }

		String newValue;
		switch (fieldNo) {
			case 1: printInputTag("새 과제명 입력"); newValue = sc.nextLine().trim(); break;
			case 2: printInputTag("새 과제 설명 입력"); newValue = sc.nextLine().trim(); break;
			case 3: printInputTag("새 예산 입력(숫자)"); newValue = String.valueOf(readLong()); break;
			case 4: printInputTag("새 선정 팀수 입력(숫자)"); newValue = String.valueOf(readInt()); break;
			case 5:
				printInputTag("새 마감일(yyyy-MM-dd) 입력");
				newValue = sc.nextLine().trim();
				try {
					LocalDate today = LocalDate.now();
					LocalDate start = LocalDate.parse(vo.getStartDt());
					LocalDate newEnd = LocalDate.parse(newValue);
					if (newEnd.isBefore(start)) { printError("마감일은 시작일보다 빠를 수 없습니다."); return; }
					if (newEnd.isBefore(today)) { printError("이미 지난 날짜로 마감일을 설정할 수 없습니다."); return; }
				} catch (DateTimeParseException e) { printError("날짜 형식이 올바르지 않습니다."); return; }
				break;
			default: printError("잘못된 번호입니다."); return;
		}

		int result = announcementDAO.updateAnnouncementField(annId, loginAgyId, fieldNo, newValue);
		if (result > 0) printSuccess("공고 정보가 정상적으로 수정되었습니다.");
		else printError("공고 수정에 실패했습니다.");
	}

	private void deleteAnnouncementFlow() {
		printInputTag("삭제할 과제 ID 입력");
		int annId = readInt();
		RR_AnnouncementVO vo = announcementDAO.getAnnouncementDetail(annId, loginAgyId);

		if (vo == null) { printError("해당 공고를 찾을 수 없습니다."); return; }

		if (!(RR_AnnouncementStatus.OPEN.equals(vo.getStatus()) || RR_AnnouncementStatus.SCHEDULED.equals(vo.getStatus()))) {
			printError("공고예정/공고중 상태에서만 삭제 가능합니다. (현재 상태 : " + vo.getStatus() + ")"); return;
		}

		if (vo.getApplicantCount() > 0) {
			printError("신청자가 존재하여 삭제할 수 없습니다. (" + vo.getApplicantCount() + "팀)"); return;
		}

		printInputTag("정말 삭제하시겠습니까? (1: 예 / 2: 아니오)");
		int confirm = readInt();
		if (confirm != 1) { printError("삭제 처리를 취소했습니다."); return; }

		int result = announcementDAO.softDeleteAnnouncement(annId, loginAgyId);
		if (result > 0) printSuccess("공고가 정상적으로 삭제되었습니다. (논리삭제 적용)");
		else printError("공고 삭제에 실패했습니다.");
	}

	// ===== 3. 신청자 목록 조회 =====
	private void applicantListMenu() {
		while (true) {
			printSubTitle("신청자 목록 조회 (View Applicants)");
			printOpenOrClosedAnnouncementListOnly();

			printInputTag("조회할 과제 ID 입력 (0: 이전 화면)");
			int annId = readInt();
			if (annId == 0) return;

			RR_AnnouncementVO ann = announcementDAO.getAnnouncementDetail(annId, loginAgyId);
			if (ann == null) { printError("해당 과제를 찾을 수 없습니다."); continue; }

			List<RR_ApplicationVO> apps = applicationDAO.getApplicationsByAnnouncement(annId);

			while (true) {
				System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ [과제ID " + annId + "] " + ann.getTitle() + " - 신청자 목록" + RESET);
				System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-8s | %-12s | %-10s | %-12s | %-10s", "신청ID", "신청자ID", "이름", "신청일", "상태") + RESET);
				System.out.println(INDENT + CLR_GRY + "-------------------------------------------------------------------" + RESET);

				if (apps.isEmpty()) {
					System.out.println(INDENT + CLR_GRY + "  신청 내역이 없습니다." + RESET);
				} else {
					for (RR_ApplicationVO a : apps) {
						String statKor = toApplicationStatusKor(a.getStatusCd());
						System.out.println(INDENT + String.format("%-10d | %-12s | %-10s | %-12s | " + getStatusColor(statKor) + "%-10s" + RESET,
								a.getApplicationId(), a.getUserId(), cut(a.getUserName(), 9), a.getAppliedAt(), statKor));
					}
				}
				System.out.println(INDENT + CLR_GRY + "-------------------------------------------------------------------" + RESET);

				System.out.println();
				printMenuOption("1", "특정 신청건 상세 조회");
				printMenuOption("0", "과제 목록으로 돌아가기");
				printInputTag("선택");
				int sel = readInt();

				if (sel == 0) break;

				if (sel == 1) {
					while (true) {
						printInputTag("상세 조회할 신청ID 입력 (0: 신청 목록으로)");
						int appId = readInt();
						if (appId == 0) break;

						RR_ApplicationVO detail = applicationDAO.getApplicationDetail(appId);
						if (detail == null) { printError("해당 신청ID를 찾을 수 없습니다."); continue; }

						System.out.println("\n" + INDENT + CLR_PRIMARY + "┌──────────────────────────────────────────────────────────────┐" + RESET);
						System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "신청ID", detail.getApplicationId());
						System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s (%s)\n" + RESET, "신청자", detail.getUserId(), detail.getUserName());
						System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "제출서류명", (detail.getAttachPath() == null ? "-" : detail.getAttachPath()));
						System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "신청일", detail.getAppliedAt());
						System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────────┤" + RESET);
						System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%,d 원\n" + RESET, "희망 예산", detail.getBudgetAmt());
						System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_PRIMARY + "%s 점\n" + RESET, "평균 평가점수", (detail.getAvgScore() == null ? "-" : detail.getAvgScore()));
						System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + getStatusColor(toApplicationStatusKor(detail.getStatusCd())) + "%s\n" + RESET, "현재 상태", toApplicationStatusKor(detail.getStatusCd()));
						System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────────────────────┘" + RESET);
					}
					break;
				}
			}
		}
	}

	// ===== 4. 선정 관리 =====
	private void selectionMenu() {
		while (true) {
			printSubTitle("과제 선정 관리 (Selection Management)");
			printMenuOption("1", "자동 선정 점수 계산 및 승인");
			printMenuOption("2", "기존 선정 결과 조회");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("0", "이전 화면으로 돌아가기");
			
			printInputTag("메뉴 선택");
			int sel = readInt();
			
			if (sel == 0) return;
			else if (sel == 1) autoSelectFlow();
			else if (sel == 2) viewSelectionResultFlow();
			else printError("잘못된 입력입니다.");
		}
	}

	private void autoSelectFlow() {
		printSubTitle("자동 선정 계산 (Auto Selection)");
		boolean hasPending = printSelectPendingAnnouncementListOnly();

		if (!hasPending) { printError("현재 자동 선정 계산이 가능한 대기 공고가 없습니다."); return; }

		printInputTag("선정 처리할 과제 ID 입력 (0: 이전 화면)");
		int annId = readInt();
		if (annId == 0) return;

		RR_AnnouncementVO ann = announcementDAO.getAnnouncementDetail(annId, loginAgyId);
		if (ann == null) { printError("해당 공고를 찾을 수 없습니다."); return; }
		if (!RR_AnnouncementStatus.SELECT_PENDING.equals(ann.getStatus())) { printError("선정 계산/승인은 '선정대기' 공고에서만 가능합니다."); return; }
		if (selectionDAO.hasSelectionResult(annId)) { printError("이미 선정 승인이 완료된 공고입니다. (중복 처리 불가)"); return; }

		List<RR_SelectionVO> ranked = selectionDAO.getRankedCandidates(annId);
		if (ranked.isEmpty()) { printError("해당 공고에 대한 신청 내역이 없습니다."); return; }

		boolean allDone = selectionDAO.isAllEvaluationsSubmitted(annId, REQUIRED_REVIEWERS);
		int cap = ann.getRecruitCap();
		long totalBudget = ann.getTotalBudget();
		int limit = Math.min(cap, ranked.size());

		long sum = 0; int selectedCount = 0;

		System.out.println("\n" + INDENT + CLR_PRIMARY + "┌─ [ " + cut(ann.getTitle(), 20) + " - 선정 시뮬레이션 ] ──────────────┐" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%,d 원\n" + RESET, "총 배정 예산", totalBudget);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%d 팀\n" + RESET, "최대 선정 팀수", cap);
		System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────────────────────┘" + RESET);

		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-4s | %-8s | %-12s | %-8s | %-15s | %-8s | %s", "순위", "신청ID", "신청자", "평균점수", "희망예산(원)", "평가완료", "비고") + RESET);
		System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------------------------------------" + RESET);

		for (int i = 0; i < limit; i++) {
			RR_SelectionVO c = ranked.get(i);
			String evalTxt = c.getSubmittedCnt() + "/" + REQUIRED_REVIEWERS;
			String scoreTxt = String.format("%.2f", c.getAvgScore());
			long next = sum + c.getBudgetAmt();

			if (next > totalBudget) {
				System.out.println(INDENT + String.format("%-6d | %-10d | %-12s | %-12s | %-15s | %-12s | " + CLR_ERR + "⚠예산 초과 중단" + RESET,
					(i + 1), c.getApplicationId(), cut(c.getUserName(), 10), scoreTxt, String.format("%,d", c.getBudgetAmt()), evalTxt));
				break;
			}
			sum = next; selectedCount++;
			System.out.println(INDENT + String.format("%-6d | %-10d | %-12s | %-12s | %-15s | %-12s | " + CLR_SUC + "[선정 후보]" + RESET,
					(i + 1), c.getApplicationId(), cut(c.getUserName(), 10), scoreTxt, String.format("%,d", c.getBudgetAmt()), evalTxt));
		}
		System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------------------------------------" + RESET);
		System.out.println(INDENT + CLR_PRIMARY + "※ 집계 결과 : 최종 후보 " + selectedCount + "팀 / 누적 사용 예산 " + String.format("%,d", sum) + "원" + RESET);

		if (!allDone) {
			printError("아직 평가가 완료되지 않은 신청서가 있습니다. 모든 평가 제출 후 승인 가능합니다."); return;
		}

		System.out.println(INDENT + CLR_GRY + "※ 후보 외 나머지 팀은 자동 탈락 처리됩니다. (총 신청 " + ranked.size() + "팀)" + RESET);
		
		printInputTag("선정 결과 최종 승인 진행 (1: 승인 / 0: 이전 메뉴)");
		int choice = readInt();
		if (choice != 1) return;

		if (selectedCount == 0) { printError("예산 초과로 선정 가능한 팀이 0명입니다. 승인을 중단합니다."); return; }

		int result = selectionDAO.approveSelection(annId, loginAgyId, loginUserId, ranked, selectedCount);
		if (result == -2) printError("이미 승인 처리가 완료된 공고입니다.");
		else if (result > 0) printSuccess("선정 승인 완료! DB에 총 " + result + "건이 저장되었습니다.");
		else printError("선정 승인 중 오류가 발생했습니다.");
	}

	private void viewSelectionResultFlow() {
		while (true) {
			printSubTitle("선정 결과 조회 (View Selection Results)");
			boolean hasDone = printSelectDoneAnnouncementListOnly();

			if (!hasDone) {
				printError("최종 선정 승인이 완료된 공고가 없습니다.");
				printInputTag("1: 자동 선정 메뉴로 이동 / 0: 이전 화면");
				if (readInt() == 1) autoSelectFlow();
				return;
			}

			printInputTag("조회할 과제 ID 입력 (0: 이전 화면)");
			int annId = readInt();
			if (annId == 0) return;

			List<RR_SelectionVO> results = selectionDAO.getSelectionResults(annId);

			if (results.isEmpty()) { printError("해당 과제의 선정 결과를 찾을 수 없습니다."); continue; }

			System.out.println("\n" + INDENT + BOLD + CLR_WHT + String.format("%-8s | %-12s | %-10s | %-8s | %-8s | %-12s", "신청ID", "신청자ID", "이름", "점수", "최종결과", "승인일자") + RESET);
			System.out.println(INDENT + CLR_GRY + "------------------------------------------------------------------------" + RESET);

			for (RR_SelectionVO r : results) {
				String resultKor = "SELECTED".equals(r.getResultCd()) ? "선정" : "탈락";
				System.out.println(INDENT + String.format("%-10d | %-14s | %-10s | %-10.2f | " + getStatusColor(resultKor) + "%-12s" + RESET + " | %-12s",
						r.getApplicationId(), r.getUserId(), cut(r.getUserName(), 9), r.getAvgScore(), resultKor, r.getApprovedAt()));
			}
			System.out.println(INDENT + CLR_GRY + "------------------------------------------------------------------------" + RESET);
			
			printInputTag("엔터(Enter)를 누르면 메뉴로 돌아갑니다");
			sc.nextLine();
			return;
		}
	}

	// ===== 5. 연구 진행 관리 (과제 -> 팀) =====
	private void researchProgressMenu() {
		while (true) {
			printSubTitle("연구 진행 상태 관리 (Research Progress Management)");

			List<RR_TaskVO> tasks = projectDAO.getTaskListByAgency(loginAgyId);

			if (tasks.isEmpty()) {
				System.out.println(INDENT + CLR_GRY + "  현재 관리할 과제가 없습니다. (선정 후 PROJECTS 생성이 선행되어야 함)" + RESET);
				printInputTag("엔터(Enter)로 이전 메뉴 복귀"); sc.nextLine(); return;
			}

			System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-8s | %-25s | %-10s | %-8s", "과제ID", "과제명", "과제상태", "선정팀수") + RESET);
			System.out.println(INDENT + CLR_GRY + "----------------------------------------------------------------" + RESET);
			for (RR_TaskVO t : tasks) {
				System.out.println(INDENT + String.format("%-10d | %-25s | " + getStatusColor(t.getTaskStatus()) + "%-12s" + RESET + " | %-8d",
						t.getAnnId(), cut(t.getTitle(), 23), t.getTaskStatus(), t.getTeamCount()));
			}
			System.out.println(INDENT + CLR_GRY + "----------------------------------------------------------------" + RESET);

			printInputTag("관리할 과제 ID 입력 (0: 이전 화면)");
			int annId = readInt();
			if (annId == 0) return;

			taskDetailMenu(annId);
		}
	}

	private void taskDetailMenu(int annId) {
		RR_TaskVO task = projectDAO.getTaskByAnnId(loginAgyId, annId);
		if (task == null) { printError("해당 과제를 찾을 수 없습니다."); return; }

		boolean locked = "완료".equals(task.getTaskStatus()) || "중단".equals(task.getTaskStatus());

		while (true) {
			printSubTitle("과제 세부 관리 항목 [ID: " + annId + "]");
			
			if (locked) {
				System.out.println(INDENT + CLR_ERR + "※ 본 과제는 이미 [" + task.getTaskStatus() + "] 처리되어 추가적인 상태 변경이 불가능합니다." + RESET);
			}

			printMenuOption("1", "협약 체결 상태 변경");
			printMenuOption("2", "연구비 차수별 지급 승인");
			printMenuOption("3", "중간 보고서 승인/반려");
			printMenuOption("4", "최종 보고서 승인/반려");
			printMenuOption("5", "연구 과제 중단(페널티) 처리");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("0", "과제 목록으로 돌아가기");
			
			printInputTag("메뉴 선택");
			int sel = readInt();
			if (sel == 0) return;

			if (locked) { printError("완료 또는 중단된 과제는 세부 관리 기능을 수행할 수 없습니다."); continue; }

			switch (sel) {
				case 1: agreementMenuByTask(annId); break;
				case 2: fundingMenuByTask(annId); break;
				case 3: midReportMenuByTask(annId); break;
				case 4: finalReportMenuByTask(annId); break;
				case 5: stopTeamMenuByTask(annId); break;
				default: printError("잘못된 입력입니다.");
			}
		}
	}

	private void agreementMenuByTask(int annId) {
		while (true) {
			printSubTitle("협약 상태 변경 (Agreement Sign)");
			List<RR_ProjectVO> teams = projectDAO.getTeamsByTask(loginAgyId, annId);
			
			if (teams.isEmpty()) { printError("해당 과제에 배정된 연구팀이 없습니다."); return; }

			System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-4s | %-10s | %-12s | %-8s | %-8s", "번호", "프로젝트ID", "연구자", "협약여부", "프로젝트상태") + RESET);
			System.out.println(INDENT + CLR_GRY + "------------------------------------------------------------------" + RESET);
			for (int i = 0; i < teams.size(); i++) {
				RR_ProjectVO p = teams.get(i);
				String agStat = mapAgreementStatus(p.getAgreementStatusCd());
				String pStat = mapProjectStatus(p.getProjectStatusCd());
				System.out.println(INDENT + String.format("%-6d | %-12d | %-12s | " + getStatusColor(agStat) + "%-12s" + RESET + " | " + getStatusColor(pStat) + "%-10s" + RESET,
						(i + 1), p.getProjectId(), cut(p.getUserName(), 10), agStat, pStat));
			}
			System.out.println(INDENT + CLR_GRY + "------------------------------------------------------------------" + RESET);

			printInputTag("협약을 체결할 팀 번호 선택 (0: 취소)");
			int pick = readInt();
			if (pick == 0) return;
			if (pick < 1 || pick > teams.size()) { printError("잘못된 팀 번호입니다."); continue; }

			RR_ProjectVO target = teams.get(pick - 1);
			int r = projectDAO.signAgreement(target.getProjectId(), loginUserId);

			if (r == 0) printError("이미 협약이 완료된 팀입니다.");
			else if (r > 0) printSuccess(target.getUserName() + " 연구팀과 협약 체결 처리가 완료되었습니다.");
			else printError("DB 오류로 인해 협약 처리에 실패했습니다.");
		}
	}

	private void fundingMenuByTask(int annId) {
		List<RR_ProjectVO> teams = projectDAO.getTeamsByTask(loginAgyId, annId);
		if (teams == null || teams.isEmpty()) { printError("배정된 프로젝트 팀이 존재하지 않습니다."); return; }

		while (true) {
			printSubTitle("연구비 지급 승인 (Funding Approval)");
			System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-4s | %-10s | %-12s | %-8s | %-8s", "번호", "프로젝트ID", "연구자", "협약상태", "지급현황") + RESET);
			System.out.println(INDENT + CLR_GRY + "------------------------------------------------------------------" + RESET);
			for (int i = 0; i < teams.size(); i++) {
				RR_ProjectVO t = teams.get(i);
				int pid = t.getProjectId();
				boolean p1 = fundingDAO.isRoundPaid(pid, RR_FundingConst.ROUND1);
				boolean p2 = fundingDAO.isRoundPaid(pid, RR_FundingConst.ROUND2);
				boolean p3 = fundingDAO.isRoundPaid(pid, RR_FundingConst.ROUND3);

				String paidTxt = (p3 ? "100%" : p2 ? "80%" : p1 ? "40%" : "0%");
				String agTxt = ("SIGNED".equalsIgnoreCase(t.getAgreementStatusCd()) ? "체결" : "대기");

				System.out.println(INDENT + String.format("%-6d | %-12d | %-12s | " + getStatusColor(agTxt) + "%-12s" + RESET + " | %-8s",
						(i + 1), pid, cut(t.getUserName(), 10), agTxt, paidTxt));
			}
			System.out.println(INDENT + CLR_GRY + "------------------------------------------------------------------" + RESET);
			
			printInputTag("지급을 승인할 팀 번호 선택 (0: 취소)");
			int no = readInt();
			if (no == 0) return;
			if (no < 1 || no > teams.size()) { printError("잘못된 번호입니다."); continue; }

			RR_ProjectVO sel = teams.get(no - 1);
			fundingDetailMenu(sel.getProjectId(), sel.getUserId(), sel.getUserName());
		}
	}

	private void fundingDetailMenu(int projectId, String userId, String userName) {
		long total = fundingDAO.getRequestedBudgetAmt(projectId);
		if (total <= 0) { printError("희망 연구비가 설정되지 않았거나 0원입니다."); return; }

		long amt1 = total * RR_FundingConst.PCT1 / 100;
		long amt2 = total * RR_FundingConst.PCT2 / 100;
		long amt3 = total - amt1 - amt2;

		while (true) {
			boolean paid1 = fundingDAO.isRoundPaid(projectId, RR_FundingConst.ROUND1);
			boolean paid2 = fundingDAO.isRoundPaid(projectId, RR_FundingConst.ROUND2);
			boolean paid3 = fundingDAO.isRoundPaid(projectId, RR_FundingConst.ROUND3);
			boolean agreementSigned = fundingDAO.isAgreementSigned(projectId);

			RR_ReportVO mid = reportDAO.getLatestReport(projectId, RR_ReportConst.TYPE_MID);
			RR_ReportVO fin = reportDAO.getLatestReport(projectId, RR_ReportConst.TYPE_FINAL);
			boolean midApproved = (mid != null && RR_ReportConst.ST_APPROVED.equals(mid.getStatusCd()));
			boolean finApproved = (fin != null && RR_ReportConst.ST_APPROVED.equals(fin.getStatusCd()));

			boolean can1 = agreementSigned && !paid1;
			boolean can2 = paid1 && midApproved && !paid2;
			boolean can3 = paid2 && finApproved && !paid3;

			String msg1 = paid1 ? "지급완료" : (!agreementSigned ? "협약 미체결" : "지급대기");
			String msg2 = paid2 ? "지급완료" : (!paid1 ? "이전 미지급" : (!midApproved ? "중간 미승인" : "지급대기"));
			String msg3 = paid3 ? "지급완료" : (!paid2 ? "이전 미지급" : (!finApproved ? "최종 미승인" : "지급대기"));

			printSubTitle("연구팀 지급 상세 [Proj: " + projectId + " | " + userName + "]");
			System.out.println(INDENT + CLR_PRIMARY + "┌──────────────────────────────────────────────────────────────┐" + RESET);
			System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%,d 원\n" + RESET, "총 배정 예산", total);
			System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────────┤" + RESET);
			System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%,d 원 " + CLR_GRY + " [ " + getStatusColor(msg1) + "%-12s" + CLR_GRY + " ]\n" + RESET, "1차금(40%)", amt1, msg1);
			System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%,d 원 " + CLR_GRY + " [ " + getStatusColor(msg2) + "%-12s" + CLR_GRY + " ]\n" + RESET, "2차금(40%)", amt2, msg2);
			System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%,d 원 " + CLR_GRY + " [ " + getStatusColor(msg3) + "%-12s" + CLR_GRY + " ]\n" + RESET, "3차금(20%)", amt3, msg3);
			System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────────────────────┘" + RESET);

			System.out.println();
			printMenuOption("1", "1차 연구비금 지급 승인");
			printMenuOption("2", "2차 연구비금 지급 승인");
			printMenuOption("3", "3차(최종) 연구비 지급 승인");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("0", "이전 화면으로");
			
			printInputTag("선택");
			int choice = readInt();
			if (choice == 0) return;

			int roundNo; long amount; boolean can; boolean already;
			if (choice == 1) { roundNo = RR_FundingConst.ROUND1; amount = amt1; can = can1; already = paid1; } 
			else if (choice == 2) { roundNo = RR_FundingConst.ROUND2; amount = amt2; can = can2; already = paid2; } 
			else if (choice == 3) { roundNo = RR_FundingConst.ROUND3; amount = amt3; can = can3; already = paid3; } 
			else { printError("잘못된 입력입니다."); continue; }

			if (already) { printError("이미 지급이 완료된 차수입니다."); continue; }
			if (!can) { printError("선행 조건(협약, 이전 차수, 보고서 등)이 충족되지 않아 지급할 수 없습니다."); continue; }

			int r = fundingDAO.insertPaidFunding(projectId, roundNo, amount, loginUserId);
			if (r > 0) {
				printSuccess(roundNo + "차 연구비 지급(" + String.format("%,d원", amount) + ") 승인이 완료되었습니다.");
				if (roundNo == RR_FundingConst.ROUND3) projectDAO.completeProject(projectId);
			} else {
				printError("지급 승인 처리 중 DB 오류가 발생했습니다.");
			}
		}
	}

	private void midReportMenuByTask(int annId) {
		while (true) {
			printSubTitle("중간 보고서 승인 관리 (Mid-term Report)");
			List<RR_ProjectVO> teams = projectDAO.getTeamsByTask(loginAgyId, annId);
			if (teams == null || teams.isEmpty()) { printError("해당 과제에 배정된 팀이 존재하지 않습니다."); return; }

			System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-4s | %-10s | %-12s | %-8s | %-10s", "번호", "프로젝트ID", "연구자", "협약상태", "보고서상태") + RESET);
			System.out.println(INDENT + CLR_GRY + "------------------------------------------------------------------" + RESET);
			for (int i = 0; i < teams.size(); i++) {
				RR_ProjectVO t = teams.get(i);
				RR_ReportVO mid = reportDAO.getLatestReport(t.getProjectId(), RR_ReportConst.TYPE_MID);
				String midStatus = (mid == null) ? "미제출" : RR_ReportConst.toKor(mid.getStatusCd());
				String agStatus = ("SIGNED".equalsIgnoreCase(t.getAgreementStatusCd())) ? "체결" : "대기";

				System.out.println(INDENT + String.format("%-6d | %-12d | %-12s | " + getStatusColor(agStatus) + "%-12s" + RESET + " | " + getStatusColor(midStatus) + "%-10s" + RESET,
						(i + 1), t.getProjectId(), cut(t.getUserName(), 10), agStatus, midStatus));
			}
			System.out.println(INDENT + CLR_GRY + "------------------------------------------------------------------" + RESET);
			
			printInputTag("검토할 팀 번호 선택 (0: 취소)");
			int pick = readInt();
			if (pick == 0) return;
			if (pick < 1 || pick > teams.size()) { printError("잘못된 번호입니다."); continue; }

			midReportDetailMenu(teams.get(pick - 1));
		}
	}

	private void midReportDetailMenu(RR_ProjectVO team) {
		int projectId = team.getProjectId();
		if (!"SIGNED".equalsIgnoreCase(team.getAgreementStatusCd())) {
			printError("해당 팀과 아직 협약이 체결되지 않았습니다."); return;
		}

		RR_ReportVO mid = reportDAO.getLatestReport(projectId, RR_ReportConst.TYPE_MID);
		printSubTitle("중간 보고서 상세 내용 [Proj: " + projectId + " | " + team.getUserName() + "]");

		if (mid == null) { printError("해당 연구팀의 중간 보고서가 접수되지 않았습니다."); return; }

		System.out.println(INDENT + CLR_PRIMARY + "┌──────────────────────────────────────────────────────────────┐" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "보고서ID", mid.getReportRptId());
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "제출 일자", (mid.getSubmittedAt() == null ? "-" : mid.getSubmittedAt()));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s %%\n" + RESET, "진행률", (mid.getProgressRate() == null ? "0" : mid.getProgressRate()));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + getStatusColor(RR_ReportConst.toKor(mid.getStatusCd())) + "%s\n" + RESET, "처리 상태", RR_ReportConst.toKor(mid.getStatusCd()));
		System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────────┤" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "키워드", (mid.getKeywords() == null ? "-" : mid.getKeywords()));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "본문 내용", (mid.getContent() == null ? "-" : mid.getContent()));
		if (RR_ReportConst.ST_REJECTED.equals(mid.getStatusCd())) {
			System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_ERR + "%-10s : %s\n" + RESET, "반려 사유", (mid.getRejectReason() == null ? "-" : mid.getRejectReason()));
		}
		System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────────────────────┘" + RESET);

		if (!RR_ReportConst.ST_SUBMITTED.equals(mid.getStatusCd())) {
			printError("현재 '제출(SUBMITTED)' 상태가 아니므로 검토할 수 없습니다."); return;
		}

		while (true) {
			System.out.println();
			printMenuOption("1", "보고서 내용 정상 확인 및 승인 처리");
			printMenuOption("2", "보고서 반려(Reject) 처리");
			printMenuOption("0", "이전 화면으로");
			printInputTag("선택");
			int sel = readInt();

			if (sel == 0) return;
			if (sel == 1) {
				if (reportDAO.approveReport(mid.getReportRptId(), loginUserId) > 0) printSuccess("해당 보고서가 성공적으로 승인되었습니다.");
				else printError("승인 처리에 실패했습니다.");
				return;
			}
			if (sel == 2) {
				printInputTag("반려 사유 입력");
				String reason = sc.nextLine().trim();
				if (reason.isEmpty()) { printError("반려 사유 작성은 필수입니다."); continue; }
				if (reportDAO.rejectReport(mid.getReportRptId(), loginUserId, reason) > 0) printSuccess("보고서를 반려 처리했습니다.");
				else printError("반려 처리에 실패했습니다.");
				return;
			}
			printError("잘못된 입력입니다.");
		}
	}

	private void finalReportMenuByTask(int annId) {
		while (true) {
			printSubTitle("최종 보고서 승인 관리 (Final Report)");
			List<RR_ProjectVO> teams = projectDAO.getTeamsByTask(loginAgyId, annId);
			if (teams == null || teams.isEmpty()) { printError("해당 과제에 배정된 팀이 존재하지 않습니다."); return; }

			System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-4s | %-10s | %-12s | %-8s | %-10s", "번호", "프로젝트ID", "연구자", "협약상태", "보고서상태") + RESET);
			System.out.println(INDENT + CLR_GRY + "------------------------------------------------------------------" + RESET);
			for (int i = 0; i < teams.size(); i++) {
				RR_ProjectVO t = teams.get(i);
				RR_ReportVO fin = reportDAO.getLatestReport(t.getProjectId(), RR_ReportConst.TYPE_FINAL);
				String finStatus = (fin == null) ? "미제출" : RR_ReportConst.toKor(fin.getStatusCd());
				String agStatus = ("SIGNED".equalsIgnoreCase(t.getAgreementStatusCd())) ? "체결" : "대기";

				System.out.println(INDENT + String.format("%-6d | %-12d | %-12s | " + getStatusColor(agStatus) + "%-12s" + RESET + " | " + getStatusColor(finStatus) + "%-10s" + RESET,
						(i + 1), t.getProjectId(), cut(t.getUserName(), 10), agStatus, finStatus));
			}
			System.out.println(INDENT + CLR_GRY + "------------------------------------------------------------------" + RESET);
			
			printInputTag("검토할 팀 번호 선택 (0: 취소)");
			int pick = readInt();
			if (pick == 0) return;
			if (pick < 1 || pick > teams.size()) { printError("잘못된 번호입니다."); continue; }

			finalReportDetailMenu(teams.get(pick - 1));
		}
	}

	private void finalReportDetailMenu(RR_ProjectVO team) {
		int projectId = team.getProjectId();
		if (!"SIGNED".equalsIgnoreCase(team.getAgreementStatusCd())) {
			printError("해당 팀과 아직 협약이 체결되지 않았습니다."); return;
		}

		if (!fundingDAO.isRoundPaid(projectId, RR_FundingConst.ROUND2)) {
			printError("2차 연구비 지급이 완료되지 않아 최종보고 승인이 불가능합니다."); return;
		}

		RR_ReportVO fin = reportDAO.getLatestReport(projectId, RR_ReportConst.TYPE_FINAL);
		printSubTitle("최종 보고서 상세 내용 [Proj: " + projectId + " | " + team.getUserName() + "]");

		if (fin == null) { printError("해당 연구팀의 최종 보고서가 접수되지 않았습니다."); return; }

		System.out.println(INDENT + CLR_PRIMARY + "┌──────────────────────────────────────────────────────────────┐" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "보고서ID", fin.getReportRptId());
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "제출 일자", (fin.getSubmittedAt() == null ? "-" : fin.getSubmittedAt()));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s %%\n" + RESET, "진행률", (fin.getProgressRate() == null ? "0" : fin.getProgressRate()));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + getStatusColor(RR_ReportConst.toKor(fin.getStatusCd())) + "%s\n" + RESET, "처리 상태", RR_ReportConst.toKor(fin.getStatusCd()));
		System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────────────────┤" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "키워드", (fin.getKeywords() == null ? "-" : fin.getKeywords()));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-10s : " + CLR_WHT + "%s\n" + RESET, "본문 내용", (fin.getContent() == null ? "-" : fin.getContent()));
		if (RR_ReportConst.ST_REJECTED.equals(fin.getStatusCd())) {
			System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_ERR + "%-10s : %s\n" + RESET, "반려 사유", (fin.getRejectReason() == null ? "-" : fin.getRejectReason()));
		}
		System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────────────────────┘" + RESET);

		if (!RR_ReportConst.ST_SUBMITTED.equals(fin.getStatusCd())) {
			printError("현재 '제출(SUBMITTED)' 상태가 아니므로 검토할 수 없습니다."); return;
		}

		while (true) {
			System.out.println();
			printMenuOption("1", "최종 승인 확정");
			printMenuOption("2", "반려 처리");
			printMenuOption("0", "이전 화면으로");
			printInputTag("선택");
			int sel = readInt();

			if (sel == 0) return;
			if (sel == 1) {
				if (reportDAO.approveReport(fin.getReportRptId(), loginUserId) > 0) printSuccess("최종 보고서 승인이 완료되었습니다.");
				else printError("승인 처리에 실패했습니다.");
				return;
			}
			if (sel == 2) {
				printInputTag("반려 사유 입력");
				String reason = sc.nextLine().trim();
				if (reason.isEmpty()) { printError("반려 사유 작성은 필수입니다."); continue; }
				if (reportDAO.rejectReport(fin.getReportRptId(), loginUserId, reason) > 0) printSuccess("최종 보고서를 반려했습니다.");
				else printError("반려 처리에 실패했습니다.");
				return;
			}
			printError("잘못된 입력입니다.");
		}
	}

	private void stopTeamMenuByTask(int annId) {
		while (true) {
			printSubTitle("연구 중단 (페널티) 처리");
			List<RR_ProjectVO> teams = projectDAO.getTeamsByTask(loginAgyId, annId);
			if (teams.isEmpty()) { printError("선정된 팀 데이터가 없습니다."); return; }

			System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-4s | %-10s | %-12s | %-8s", "번호", "프로젝트ID", "연구자", "현재 상태") + RESET);
			System.out.println(INDENT + CLR_GRY + "------------------------------------------------------" + RESET);
			for (int i = 0; i < teams.size(); i++) {
				RR_ProjectVO p = teams.get(i);
				String pStat = mapProjectStatus(p.getProjectStatusCd());
				System.out.println(INDENT + String.format("%-6d | %-12d | %-12s | " + getStatusColor(pStat) + "%-10s" + RESET,
						(i + 1), p.getProjectId(), cut(p.getUserName(), 10), pStat));
			}
			System.out.println(INDENT + CLR_GRY + "------------------------------------------------------" + RESET);

			printInputTag("중단시킬 팀 번호 선택 (0: 취소)");
			int pick = readInt();
			if (pick == 0) return;
			if (pick < 1 || pick > teams.size()) { printError("잘못된 번호입니다."); continue; }

			RR_ProjectVO target = teams.get(pick - 1);

			printInputTag("해당 연구팀의 과제를 정말로 중단 처리하시겠습니까? (1: 예 / 2: 아니오)");
			if (readInt() != 1) { printError("중단 처리를 취소했습니다."); continue; }

			int r = projectDAO.stopProject(target.getProjectId());
			if (r > 0) printSuccess(target.getUserName() + " 연구팀 프로젝트를 즉시 중단(STOPPED) 처리했습니다.");
			else printError("DB 오류로 인해 중단 처리에 실패했습니다.");
		}
	}

	// ===== 6. 과제 진행 현황 조회 =====
	private void taskProgressMenu() {
		while (true) {
			printSubTitle("전체 과제 진행 현황 (Overall Task Status)");
			List<RR_TaskProgressVO> tasks = progressDAO.getTaskProgressList(loginAgyId);

			if (tasks == null || tasks.isEmpty()) {
				System.out.println(INDENT + CLR_GRY + "  현재 진행 중인 과제가 없습니다." + RESET);
				printInputTag("엔터(Enter)를 누르면 메뉴로 복귀합니다"); sc.nextLine(); return;
			}

			System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-8s | %-22s | %-14s | %-8s | %-8s", "과제ID", "과제명", "총예산(원)", "선정팀", "진행상태") + RESET);
			System.out.println(INDENT + CLR_GRY + "----------------------------------------------------------------------------" + RESET);
			for (RR_TaskProgressVO t : tasks) {
				System.out.println(INDENT + String.format("%-10d | %-22s | %-14s | %-9s | " + getStatusColor(t.getTaskStatus()) + "%-10s" + RESET,
						t.getAnnId(), cut(t.getTitle(), 20), String.format("%,d", t.getTotalBudget()), t.getTeamCount() + "팀", t.getTaskStatus()));
			}
			System.out.println(INDENT + CLR_GRY + "----------------------------------------------------------------------------" + RESET);

			printInputTag("상세 현황을 조회할 과제 ID 입력 (0: 이전 메뉴)");
			int annId = readInt();
			if (annId == 0) return;

			taskProgressDetail(annId);
		}
	}

	private void taskProgressDetail(int annId) {
		while (true) {
			List<RR_TeamProgressVO> teams = progressDAO.getTeamProgressList(loginAgyId, annId);

			if (teams == null || teams.isEmpty()) {
				printError("해당 과제의 선정팀 내역을 찾을 수 없습니다."); return;
			}

			printSubTitle("과제 소속 연구팀별 현황 [ID: " + annId + "]");
			System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-4s | %-10s | %-10s | %-6s | %-14s | %-8s | %-8s | %-8s", 
					"번호", "Proj_ID", "연구자", "협약", "지급현황", "중간보고", "최종보고", "최종상태") + RESET);
			System.out.println(INDENT + CLR_GRY + "-------------------------------------------------------------------------------------------" + RESET);

			for (int i = 0; i < teams.size(); i++) {
				RR_TeamProgressVO t = teams.get(i);

				String ag = toAgreementKor(t.getAgreementStatusCd());
				String mid = toReportKor(t.getMidStatusCd());
				String fin = toReportKor(t.getFinalStatusCd());
				boolean midApp = "APPROVED".equalsIgnoreCase(t.getMidStatusCd());
				boolean finApp = "APPROVED".equalsIgnoreCase(t.getFinalStatusCd());
				String stage = toPayStageKor(t.getPaidRound(), midApp, finApp);
				String pstat = toProjectKor(t.getProjectStatusCd());

				System.out.println(INDENT + String.format("%-6d | %-10d | %-10s | " + getStatusColor(ag) + "%-8s" + RESET + " | %-14s | " + getStatusColor(mid) + "%-8s" + RESET + " | " + getStatusColor(fin) + "%-8s" + RESET + " | " + getStatusColor(pstat) + "%-8s" + RESET,
						(i + 1), t.getProjectId(), cut(t.getUserName(), 8), ag, stage, mid, fin, pstat));
			}
			System.out.println(INDENT + CLR_GRY + "-------------------------------------------------------------------------------------------" + RESET);

			printInputTag("팀 번호를 입력하여 개별 상세정보 보기 (0: 이전 메뉴)");
			int teamNo = readInt();
			if (teamNo == 0) return;

			if (teamNo < 1 || teamNo > teams.size()) { printError("잘못된 팀 번호입니다."); continue; }

			int projectId = teams.get(teamNo - 1).getProjectId();
			RR_TeamProgressVO picked = progressDAO.getTeamProgressDetail(projectId);

			if (picked == null) { printError("정보를 로드할 수 없습니다."); continue; }
			printTeamDetail(picked);
		}
	}

	private void printTeamDetail(RR_TeamProgressVO t) {
		printSubTitle("연구팀 운영 상세 리포트 [Proj: " + t.getProjectId() + " | " + t.getUserName() + "]");
		
		System.out.println(INDENT + CLR_PRIMARY + "┌─ [ 평가 및 협약 정보 ] ────────────────────────┐" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "선정 일자", (t.getSelectionApprovedAt() == null ? "-" : t.getSelectionApprovedAt()));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s 점\n" + RESET, "심사 총점", (t.getSelectionScore() == null ? "-" : t.getSelectionScore()));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + getStatusColor(toAgreementKor(t.getAgreementStatusCd())) + "%s\n" + RESET, "협약 상태", toAgreementKor(t.getAgreementStatusCd()));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "협약 체결일", (t.getAgreementSignedAt() == null ? "-" : t.getAgreementSignedAt()));
		
		System.out.println(INDENT + CLR_PRIMARY + "├─ [ 연구비 지급 현황 ] ─────────────────────────┤" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%d 차 지급 완료\n" + RESET, "지급 단계", t.getPaidRound());
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_PRIMARY + "%,d 원\n" + RESET, "누적 지급액", t.getPaidTotalAmt());
		
		System.out.println(INDENT + CLR_PRIMARY + "├─ [ 보고서 및 프로젝트 상태 ] ──────────────────┤" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + getStatusColor(toReportKor(t.getMidStatusCd())) + "%s\n" + RESET, "중간 보고서", toReportKor(t.getMidStatusCd()));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + getStatusColor(toReportKor(t.getFinalStatusCd())) + "%s\n" + RESET, "최종 보고서", toReportKor(t.getFinalStatusCd()));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + getStatusColor(toProjectKor(t.getProjectStatusCd())) + BOLD + "%s\n" + RESET, "최종 과제 상태", toProjectKor(t.getProjectStatusCd()));
		System.out.println(INDENT + CLR_PRIMARY + "└────────────────────────────────────────────────┘" + RESET);

		printInputTag("엔터(Enter) 키를 누르면 팀 목록으로 돌아갑니다");
		sc.nextLine();
	}

	// ===== 표시 변환 유틸 =====
	private String toAgreementKor(String cd) {
		if (cd == null) return "대기";
		if ("SIGNED".equalsIgnoreCase(cd) || "체결".equals(cd)) return "체결";
		return "대기";
	}

	private String toReportKor(String cd) {
		if (cd == null) return "미제출";
		if ("SUBMITTED".equalsIgnoreCase(cd)) return "대기";
		if ("APPROVED".equalsIgnoreCase(cd)) return "승인";
		if ("REJECTED".equalsIgnoreCase(cd)) return "반려";
		return cd;
	}

	private String toProjectKor(String cd) {
		if (cd == null) return "진행중";
		if ("STOPPED".equalsIgnoreCase(cd) || "중단".equals(cd)) return "중단";
		if ("COMPLETED".equalsIgnoreCase(cd) || "완료".equals(cd)) return "완료";
		return "진행중";
	}

	private String toPayStageKor(int paidRound, boolean midApproved, boolean finalApproved) {
		if (paidRound >= 3) return "최종 완료";
		if (paidRound == 2) return finalApproved ? "최종 대기" : "2차 완료";
		if (paidRound == 1) return midApproved ? "2차 대기" : "1차 완료";
		return "미지급";
	}

	private String formatMoney(long v) {
		return String.format("%,d원", v);
	}

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
	
	private String toApplicationStatusKor(String cd) {
		if (cd == null) return "-";
		switch (cd.toUpperCase()) {
			case "APPLIED": return "심사대기";
			case "UNDER_REVIEW": return "심사중";
			case "SELECTED": return "선정";
			case "REJECTED": return "탈락";
			case "CANCELLED": return "취소";
			default: return cd;
		}
	}

	// ===== 공통 입력 유틸 =====
	private int readInt() {
		while (true) {
			try { return Integer.parseInt(sc.nextLine().trim()); } 
			catch (Exception e) { printError("숫자만 입력 가능합니다."); printInputTag("재입력"); }
		}
	}

	private long readLong() {
		while (true) {
			try { return Long.parseLong(sc.nextLine().trim()); } 
			catch (Exception e) { printError("숫자만 입력 가능합니다."); printInputTag("재입력"); }
		}
	}

	public static void main(String[] args) {
		// new RR_KRDAdminMain(1, "adminId");
	}
}