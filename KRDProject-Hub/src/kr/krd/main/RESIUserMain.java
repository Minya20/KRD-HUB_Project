package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;

import kr.krd.dao.HYJ_KRDRESOUserDAO;
import kr.krd.dao.HYJ_MyInfoDAO;
import kr.krd.dao.HYJ_RESISearch;
import kr.krd.dao.HYJ_ReportDAO;
import kr.krd.dao.CMY_MemberDAO;
import kr.krd.dao.HYJ_APPLICATIONCheakDAO;

public class RESIUserMain {
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
	private BufferedReader br;
	private String role;
	private String cust_id;
	private String field;

	private HYJ_KRDRESOUserDAO dao1;
	private CMY_MemberDAO dao2;
	private HYJ_MyInfoDAO dao3;
	private HYJ_APPLICATIONCheakDAO dao4;
	private HYJ_ReportDAO dao5;
	private HYJ_RESISearch dao6;

	public RESIUserMain(BufferedReader br, String cust_id, String role, String field) {
		this.br = br;
		this.cust_id = cust_id;
		this.role = role;
		this.field = field;

		this.dao1 = new HYJ_KRDRESOUserDAO();
		this.dao2 = new CMY_MemberDAO();
		this.dao3 = new HYJ_MyInfoDAO();
		this.dao4 = new HYJ_APPLICATIONCheakDAO();
		this.dao5 = new HYJ_ReportDAO(br); // 여기 수정
		this.dao6 = new HYJ_RESISearch();
	}

	// ------------------------------------------------
	// UI Helper Methods
	// ------------------------------------------------
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

	// ------------------------------------------------
	// Main Dashboard Logic
	// ------------------------------------------------
	
	// 메뉴
	public boolean callMenu() throws IOException {
		while(true) {
			System.out.println("\n\n" + INDENT + CLR_PRIMARY + BOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
			System.out.println(INDENT + BOLD + "   KRD Hubs | " + RESET + CLR_WHT + "개인 연구자 대시보드 (Researcher)" + RESET);
			System.out.println(INDENT + CLR_PRIMARY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
			System.out.println(INDENT + CLR_GRY + "  접속 계정: " + RESET + cust_id + CLR_GRY + " | 연구 분야: " + RESET + (field == null ? "미지정" : field));
			System.out.println();
			
			printMenuOption("1", "공고 목록 조회 및 신청 (Announcements)");
			printMenuOption("2", "내 신청 내역 확인 (My Applications)");
			printMenuOption("3", "연구 보고서 제출 (Submit Report)");
			printMenuOption("4", "내 정보 관리 (My Profile)");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("5", "시스템 로그아웃 (Logout)");
			printMenuOption("6", "프로그램 종료 (Exit)");

			printInputTag("메뉴 선택");
			try {
				int no = Integer.parseInt(br.readLine());

				if(no == 1) {
					dao1.selectAnn(cust_id);
				} else if(no == 2) {
					dao4.CheckMyApp(cust_id);
				} else if(no == 3) {
					dao5.InsertReport(cust_id);
				} else if(no == 4) {
					dao3.SelectInfo(cust_id);
				} else if(no == 5) {
					printInputTag("로그아웃 하시겠습니까? (Y/N)");
					String input = br.readLine();

					if("Y".equalsIgnoreCase(input)) {
						printSuccess("개인 연구자 계정에서 안전하게 로그아웃 되었습니다.");
						return true;
					} else if("N".equalsIgnoreCase(input)) {
						continue;
					} else {
						printError("Y 또는 N만 입력해주세요.");
					}
				} else if(no == 6) {
					System.out.println("\n" + INDENT + "프로그램을 종료합니다.");
					System.exit(0);
				} else {
					printError("잘못된 선택입니다. 1~6 사이의 메뉴를 선택해주세요.");
				}
			} catch(NumberFormatException e) {
				printError("숫자만 입력 가능합니다.");
			}
		}
	}
}