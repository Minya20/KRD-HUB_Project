package kr.krd.service;

import java.io.IOException;
import java.util.List;
import java.io.BufferedReader;

import kr.util.USH_ConsoleUtil;
import kr.krd.vo.USH_AgencyCountVO;
import kr.krd.vo.USH_YearCountVO;
import kr.krd.dao.USH_StatsDAO;

public class USH_AdminStatsService {
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
	private final BufferedReader br;
	private final USH_ConsoleUtil io;
	private final String adminId;
	private final USH_StatsDAO dao;
	
	public USH_AdminStatsService(BufferedReader br, String adminId) {
		this.br = br;
		this.io = new USH_ConsoleUtil(br);
		this.adminId = adminId;
		this.dao = new USH_StatsDAO();
	}

	// ------------------------------------------------
	// UI Helper Methods
	// ------------------------------------------------
	private void printSubTitle(String title) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ " + title + RESET);
		System.out.println(INDENT + CLR_GRY + "────────────────────────────────────────────────" + RESET);
	}

	private String getInputPrompt(String tag) {
		return "\n" + INDENT + CLR_PRIMARY + BOLD + "▶ " + tag + RESET + " : ";
	}

	private String cut(String s, int max) {
		if (s == null) return "-";
		return (s.length() <= max) ? s : s.substring(0, max - 2) + "..";
	}

	// ------------------------------------------------
	// Business Logic
	// ------------------------------------------------

	// 1. 연도별 선정 건수
	public void selectedByYearFlow() throws IOException {
		printSubTitle("연도별 선정 건수 현황 (Selected Projects by Year)");
		
		List<USH_YearCountVO> list = dao.findSelectedCountByYear();
		
		if(list == null || list.isEmpty()) {
			System.out.println(INDENT + CLR_GRY + "  조회 결과가 없습니다." + RESET);
			System.out.println(INDENT + CLR_GRY + "------------------------------------------------" + RESET);
			io.readOptional(getInputPrompt("엔터를 누르면 이전 메뉴로 돌아갑니다."));
			return;
		}

		// 표 헤더 출력
		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-10s | %-12s", "연도(Year)", "총 선정 건수") + RESET);
		System.out.println(INDENT + CLR_GRY + "------------------------------------------------" + RESET);
		
		for(USH_YearCountVO row : list) {
			System.out.println(INDENT + String.format("%-12d | " + CLR_PRIMARY + "%-12d" + RESET + " 건", 
					row.year, row.selectedCount));
		}
		
		System.out.println(INDENT + CLR_GRY + "------------------------------------------------" + RESET);
		io.readOptional(getInputPrompt("엔터를 누르면 이전 메뉴로 돌아갑니다."));
	}
	
	// 2. 기관별 선정 건수
	public void selectedByAgencyFlow() throws IOException {
		printSubTitle("기관별 선정 건수 현황 (Selected Projects by Agency)");
		
		List<USH_AgencyCountVO> list = dao.findSelectedCountByAgency();
		
		if(list == null || list.isEmpty()) {
			System.out.println(INDENT + CLR_GRY + "  조회된 결과가 없습니다." + RESET);
			System.out.println(INDENT + CLR_GRY + "------------------------------------------------" + RESET);
			io.readOptional(getInputPrompt("엔터를 누르면 이전 메뉴로 돌아갑니다."));
			return;
		}
		
		// 표 헤더 출력
		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-25s | %-12s", "기관명 (Agency)", "총 선정 건수") + RESET);
		System.out.println(INDENT + CLR_GRY + "------------------------------------------------" + RESET);
		
		for(USH_AgencyCountVO row : list) {
			System.out.println(INDENT + String.format("%-25s | " + CLR_PRIMARY + "%-12d" + RESET + " 건", 
					cut(row.agencyName, 24), row.selectedCount));
		}

		System.out.println(INDENT + CLR_GRY + "------------------------------------------------" + RESET);
		io.readOptional(getInputPrompt("엔터를 누르면 이전 메뉴로 돌아갑니다."));
	}
	
	// 3. 평균 경쟁률
	public void avgCompetitionRateFlow() throws IOException {
		printSubTitle("연구과제 평균 경쟁률 분석 (Average Competition Rate)");
		
		double avg = dao.findAvgCompetitionRate();
		
		// 단일 통계 데이터는 눈에 잘 띄도록 카드(Box) 형태로 감싸서 출력
		System.out.println(INDENT + CLR_PRIMARY + "┌──────────────────────────────────────────────┐" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-15s : " + CLR_WHT + BOLD + "%.2f : 1\n" + RESET, "현재 평균 경쟁률", avg);
		System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────┘" + RESET);
		
		io.readOptional(getInputPrompt("엔터를 누르면 이전 메뉴로 돌아갑니다."));
	}
}