package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;

import kr.krd.service.USH_AdminMemberService;
import kr.util.USH_ConsoleUtil;
import kr.krd.service.USH_AdminBudgetService;
import kr.krd.service.USH_AdminStatsService;
import kr.krd.service.USH_AdminAnnouncementService;
import kr.krd.service.USH_AdminRoleApplicationService;

public class USH_KRDAdminMain {
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

	private static final String LN_SINGLE = "─";
	private static final String INDENT = "      ";

	// ------------------------------------------------
	// Fields
	// ------------------------------------------------
	private final BufferedReader br;
	private final USH_AdminMemberService service;
	private final USH_ConsoleUtil io;
	private final USH_AdminBudgetService budgetService;
	private final USH_AdminStatsService statsService;
	private final USH_AdminAnnouncementService annService;
	private final USH_AdminRoleApplicationService roleAppService;

	public USH_KRDAdminMain(BufferedReader br, String adminId) {
		this.br = br;
		this.service = new USH_AdminMemberService(br, adminId);
		this.budgetService = new USH_AdminBudgetService(br, adminId);
		this.statsService = new USH_AdminStatsService(br, adminId);
		this.annService = new USH_AdminAnnouncementService(br, adminId);
		this.io = new USH_ConsoleUtil(br);
		this.roleAppService = new USH_AdminRoleApplicationService(br, adminId);
	}

	// ------------------------------------------------
	// UI Helper Methods
	// ------------------------------------------------
	private void printSubTitle(String title) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ " + title + RESET);
		System.out.println(INDENT + CLR_GRY + "────────────────────────────────────────────────" + RESET);
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

	// ------------------------------------------------
	// Logic Screens
	// ------------------------------------------------

	// 최상위(시스템 관리자) 메뉴
	public boolean callMenu() throws IOException {
		while(true) {
			System.out.println("\n\n" + INDENT + CLR_PRIMARY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
			System.out.println(INDENT + BOLD + "   KRD Hubs | " + RESET + CLR_WHT + "시스템 관리자 대시보드 (Admin)" + RESET);
			System.out.println(INDENT + CLR_PRIMARY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
			System.out.println();

			printMenuOption("1", "전체 회원 관리 (User Management)");
			printMenuOption("2", "예산 관리 메뉴 (Budget Control)");
			printMenuOption("3", "선정 통계 조회 (Statistics & Reports)");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("4", "게시물 관리 (Announcement Control)");
			printMenuOption("5", "권한 신청 관리 (Role Application)");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("6", "시스템 로그아웃 (Logout)");
			printMenuOption("7", "프로그램 종료 (Exit)");
			System.out.println();

			int no = io.readIntInRange(getInputPrompt("메뉴 선택"), 1, 7);

			if(no == 1) {
				callUserMenu();
			} else if(no == 2) {
				callBudgetMenu();
			} else if(no == 3) {
				callStatsMenu();
			} else if(no == 4) {
				callAnnouncementMenu();
			} else if(no == 5) {
				callRoleMenu();
			} else if(no == 6) {
				printInputTag("로그아웃 하시겠습니까? (Y/N)");
				String input = br.readLine();

				if("Y".equalsIgnoreCase(input)) {
					printSuccess("시스템 관리자 계정에서 안전하게 로그아웃 되었습니다.");
					return true;
				} else if("N".equalsIgnoreCase(input)) {
					continue;
				} else {
					printError("Y 또는 N만 입력해주세요.");
				}
			}else if(no == 7) {
				System.out.println("\n" + INDENT + "프로그램을 종료합니다.");
				System.exit(0);
			} else {
				printError("잘못된 입력입니다.");
			}
		}
	}

	// 전체 회원 관리 메뉴(서브 메뉴)
	private void callUserMenu() throws IOException {
		int restored = service.restoreExpiredSuspendedUsers();
		if(restored > 0) {
			printSuccess("만료된 정지 계정 " + restored + "건을 ACTIVE로 복구했습니다.");
		}

		while(true) {
			printSubTitle("전체 회원 관리 (User Management)");
			
			printMenuOption("1", "회원 목록 전체 조회");
			printMenuOption("2", "회원 조건부 검색");
			printMenuOption("3", "회원 계정 상태 변경 (정지/활성)");
			printMenuOption("4", "회원 계정 영구 삭제");
			printMenuOption("5", "회원 권한(역할) 강제 변경");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("0", "이전 메뉴로 돌아가기 (Back)");
			System.out.println();

			int no = io.readIntInRange(getInputPrompt("메뉴 선택"), 0, 5);

			if(no == 0) {
				return;
			} else if(no == 1) {
				System.out.println();
				service.userListFlow();
			} else if(no == 2) {
				service.userSearchFlow();
			} else if(no == 3) {
				service.changeUserStatusFlow();
			} else if(no == 4) {
				service.deleteUserFlow();
			} else if(no == 5) {
				service.roleChangeFlow();
			} else {
				printError("잘못된 입력입니다.");
			}
		}
	}
	
	// 예산 관리 메뉴(서브 메뉴)
	private void callBudgetMenu() throws IOException {
		while(true) {
			printSubTitle("예산 관리 메뉴 (Budget Control)");
			
			printMenuOption("1", "예산 변경 이력(History) 조회");
			printMenuOption("2", "예산 사용 현황(Usage) 실시간 조회");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("0", "이전 메뉴로 돌아가기 (Back)");
			System.out.println();
			
			int no = io.readIntInRange(getInputPrompt("메뉴 선택"), 0, 2);
			
			if(no == 0) return;
			else if(no == 1) budgetService.budgetHistFlow();
			else if(no == 2) budgetService.budgetUsageFlow();
			else printError("잘못된 입력입니다.");
		}
	}
	
	// 선정 통계 조회(서브 메뉴)
	private void callStatsMenu() throws IOException {
		while(true) {
			printSubTitle("선정 통계 조회 (Statistics & Reports)");
			
			printMenuOption("1", "연도별 선정 건수 현황");
			printMenuOption("2", "기관별 선정 건수 현황");
			printMenuOption("3", "연구과제 평균 경쟁률 분석");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("0", "이전 메뉴로 돌아가기 (Back)");
			System.out.println();
			
			int no = io.readIntInRange(getInputPrompt("메뉴 선택"), 0, 3);
			
			if(no == 0) return;
			else if(no == 1) statsService.selectedByYearFlow();
			else if(no == 2) statsService.selectedByAgencyFlow();
			else if(no == 3) statsService.avgCompetitionRateFlow();
			else printError("잘못된 입력입니다.");
		}
	}

	// 공고(게시물) 관리 메뉴(서브 메뉴)
	private void callAnnouncementMenu() throws IOException {
		while(true) {
			printSubTitle("시스템 설정 : 게시물 관리 (Announcement Control)");
			
			printMenuOption("1", "공고 상태(Status) 강제 변경");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("0", "이전 메뉴로 돌아가기 (Back)");
			System.out.println();
			
			int no = io.readIntInRange(getInputPrompt("메뉴 선택"), 0, 1);
			
			if(no == 0) return;
			if(no == 1) annService.forceChangeAnnStatusFlow();
		}
	}
	
	// 권한 신청 관리 메뉴(서브 메뉴)
	private void callRoleMenu() throws IOException {
		while(true) {
			printSubTitle("권한 신청 관리 (Role Application)");
			
			printMenuOption("1", "권한 신청(Pending) 목록 전체 조회");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("0", "이전 메뉴로 돌아가기 (Back)");
			System.out.println();
			
			int no = io.readIntInRange(getInputPrompt("메뉴 선택"), 0, 1);
			
			if(no == 0) return;
			if(no == 1) roleAppService.pendingListFlow();
		}
	}
}