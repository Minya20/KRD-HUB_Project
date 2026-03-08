package kr.krd.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import kr.util.USH_ConsoleUtil;
import kr.krd.dao.USH_BudgetDAO;
import kr.krd.vo.USH_BudgetHistRow;
import kr.krd.vo.USH_BudgetUsageSummary;
import kr.krd.vo.USH_FundingLine;

public class USH_AdminBudgetService {
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
	private final USH_BudgetDAO budgetDao;
	
	public USH_AdminBudgetService(BufferedReader br, String adminId) {
		this.br = br;
		this.io = new USH_ConsoleUtil(br);
		this.adminId = adminId;
		this.budgetDao = new USH_BudgetDAO(); 
	}
	
	// ------------------------------------------------
	// UI Helper Methods
	// ------------------------------------------------
	private void printSubTitle(String title) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ " + title + RESET);
		System.out.println(INDENT + CLR_GRY + "────────────────────────────────────────────────────────" + RESET);
	}

	private void printMenuOption(String key, String description) {
		System.out.print(INDENT + "  " + CLR_PRIMARY + BOLD + "[" + key + "]" + RESET + " ");
		System.out.println(CLR_WHT + description + RESET);
	}

	private void printError(String msg) {
		System.out.println("\n" + INDENT + CLR_ERR + BOLD + "✘ Error: " + UNBOLD + RESET + msg);
	}

	private String getInputPrompt(String tag) {
		return "\n" + INDENT + CLR_PRIMARY + BOLD + "▶ " + tag + RESET + " : ";
	}

	// ------------------------------------------------
	// Business Logic
	// ------------------------------------------------
	
	// 1. 예산 변경 이력 조회
	public void budgetHistFlow() throws IOException {
		printSubTitle("예산 변경 이력 조회 (Budget History)");
		System.out.println(INDENT + CLR_GRY + "  [안내] 값을 입력하지 않고 엔터(Enter)를 치면 해당 조건은 생략됩니다." + RESET);
		
		Long projectId = readOptionalLong(getInputPrompt("프로젝트 ID"));
		String start = emptyToNull(io.readDateOrEmpty(getInputPrompt("변경일 시작(YYYY-MM-DD)")));
		String end = emptyToNull(io.readDateOrEmpty(getInputPrompt("변경일 끝(YYYY-MM-DD)")));
		int limit = readOptionalIntDefault(getInputPrompt("조회 건수(기본값: 100)"), 100);
		
		List<USH_BudgetHistRow> list = budgetDao.findBudgetHist(projectId, start, end, limit);
		printBudgetHistList(list, limit);
		
		io.readOptional(getInputPrompt("엔터를 누르면 이전 메뉴로 돌아갑니다."));
	}
	
	private void printBudgetHistList(List<USH_BudgetHistRow> list, int limit) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ 예산 변경 이력 결과 (최대 " + limit + "건)" + RESET);
		
		if(list == null || list.isEmpty()) {
			System.out.println(INDENT + CLR_GRY + "  조회 결과가 없습니다." + RESET);
			return;
		}
		
		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-8s | %-8s | %15s | %15s | %15s | %-12s | %-12s | %s",
				"HIST_ID", "PROJECT", "BEFORE(원)", "AFTER(원)", "DIFF(원)", "CHANGED_BY", "CHANGED_AT", "REASON") + RESET);
		System.out.println(INDENT + CLR_GRY + "------------------------------------------------------------------------------------------------------------------------" + RESET);
		
		for(USH_BudgetHistRow r : list) {
			BigDecimal before = nvlAmt(r.beforeAmt);
			BigDecimal after = nvlAmt(r.afterAmt);
			BigDecimal diff = after.subtract(before);
			
			// 증감액에 따른 색상 변경 (양수: 초록, 음수: 빨강)
			String diffColor = diff.compareTo(BigDecimal.ZERO) > 0 ? CLR_SUC : (diff.compareTo(BigDecimal.ZERO) < 0 ? CLR_ERR : CLR_WHT);
			String diffSign = diff.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";

			System.out.println(INDENT + String.format("%-8d | %-8d | %15s | %15s | " + diffColor + "%15s" + RESET + " | %-12s | %-12s | %s",
					r.histId,
					r.projectId,
					fmtAmt(before),
					fmtAmt(after),
					diffSign + fmtAmt(diff),
					nvl(r.changedBy),
					fmtDate(r.changedAt),
					cut(nvl(r.reason), 20)));
		}
		System.out.println(INDENT + CLR_GRY + "------------------------------------------------------------------------------------------------------------------------" + RESET);
	}
	
	// 2. 예산 사용 현황 조회
	public void budgetUsageFlow() throws IOException {
		while(true) {
			List<USH_BudgetUsageSummary> list = budgetDao.findBudgetUsageAll();
			printUsageList(list);
			
			System.out.println();
			printMenuOption("1", "프로젝트 예산 상세 조회 (집행 내역 포함)");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("0", "이전 메뉴로 돌아가기 (Back)");
			System.out.println();
			
			int no = io.readIntInRange(getInputPrompt("메뉴 선택"), 0, 1);
			if(no == 0) return;
			
			int projectId = io.readIntInRange(getInputPrompt("상세 조회할 PROJECT_ID 입력"), 1, Integer.MAX_VALUE);
			
			USH_BudgetUsageSummary summary = budgetDao.findBudgetUsageByProjectId(projectId);
			if(summary == null) {
				printError("해당 PROJECT_ID(" + projectId + ")가 존재하지 않거나 연결된 공고가 없습니다.");
				continue;
			}
			
			printUsageDetail(summary);
			
			List<USH_FundingLine> lines = budgetDao.findFundingLinesByProjectId(projectId);
			printFundingLines(lines);
		}
	}
	
	private void printUsageList(List<USH_BudgetUsageSummary> list) {
		printSubTitle("예산 사용 현황 전체 조회 (Budget Usage Overview)");
		
		if(list == null || list.isEmpty()) {
			System.out.println(INDENT + CLR_GRY + "  조회 가능한 프로젝트 예산 정보가 없습니다." + RESET);
			return;
		}
		
		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-8s | %-28s | %15s | %15s | %15s | %-8s", 
				"PROJECT", "공고명", "총액(원)", "집행액(원)", "잔액(원)", "집행률(%)") + RESET);
		System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------------------------------------------------" + RESET);
		
		for(USH_BudgetUsageSummary u : list) {
			System.out.println(INDENT + String.format("%-8d | %-28s | %15s | %15s | %15s | %6.2f%%",
					u.projectId, 
					cut(u.announcementTitle, 26), 
					fmtAmt(new BigDecimal(u.totalBudget)), 
					fmtAmt(new BigDecimal(u.usedBudget)), 
					fmtAmt(new BigDecimal(u.remainingBudget)), 
					u.usedPct));	
		}
		System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------------------------------------------------" + RESET);
	}
	
	private void printUsageDetail(USH_BudgetUsageSummary u) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ 프로젝트 예산 상세 정보 (Project Detail)" + RESET);
		System.out.println(INDENT + CLR_PRIMARY + "┌────────────────────────────────────────────────────────────┐" + RESET);
		
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "PROJECT_ID", u.projectId);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%s\n" + RESET, "공고명", nvl(u.announcementTitle));
		System.out.println(INDENT + CLR_PRIMARY + "├────────────────────────────────────────────────────────────┤" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_PRIMARY + "%s 원\n" + RESET, "총 배정 예산", fmtAmt(new BigDecimal(u.totalBudget)));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_ERR + "%s 원\n" + RESET, "집행 승인액", fmtAmt(new BigDecimal(u.usedBudget)));
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_SUC + "%s 원\n" + RESET, "현재 잔액", fmtAmt(new BigDecimal(u.remainingBudget)));
		System.out.println(INDENT + CLR_PRIMARY + "├────────────────────────────────────────────────────────────┤" + RESET);
		System.out.printf(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "%-12s : " + CLR_WHT + "%.2f %%\n" + RESET, "집행률", u.usedPct);
		
		System.out.println(INDENT + CLR_PRIMARY + "└────────────────────────────────────────────────────────────┘" + RESET);
	}
	
	private void printFundingLines(List<USH_FundingLine> lines) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ 집행(지급) 내역 리스트" + RESET);
		if(lines == null || lines.isEmpty()) {
			System.out.println(INDENT + CLR_GRY + "  지급 내역이 존재하지 않습니다." + RESET);
			return;
		}
		
		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-6s | %15s | %-10s | %-12s | %-8s | %-10s | %-12s", 
				"회차", "금액(원)", "상태", "요청일", "승인(Y/N)", "승인자", "승인일") + RESET);
		System.out.println(INDENT + CLR_GRY + "----------------------------------------------------------------------------------------" + RESET);
		
		for(USH_FundingLine f : lines) {
			String appYnColor = f.approvedYn == 1 ? CLR_SUC : CLR_GRY; // 승인 완료면 녹색
			
			System.out.println(INDENT + String.format("%-6d | %15s | %-10s | %-12s | " + appYnColor + "%-8s" + RESET + " | %-10s | %-12s",
					f.payRound, 
					f.amount, // amount가 Number/String인 경우 BigDecimal로 변환
					nvl(f.stautsCd), 
					nvlDate(f.requestedDt), 
					(f.approvedYn == 1 ? "Y" : "N"), 
					nvl(f.approvedBy), 
					nvlDate(f.approvedDt)));
		}
		System.out.println(INDENT + CLR_GRY + "----------------------------------------------------------------------------------------" + RESET);
	}
	
	// ------------------------------------------------
	// Utility Format Methods
	// ------------------------------------------------
	private String nvl(String s) {
		return (s == null || s.isBlank()) ? "-" : s;
	}
	
	private BigDecimal nvlAmt(BigDecimal b) {
		return (b == null) ? BigDecimal.ZERO : b;
	}
	
	private String nvlDate(java.sql.Date d) {
		return (d == null) ? "-" : d.toString();
	}
	
	private String fmtAmt(BigDecimal b) {
		if(b == null) return "0";
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
		return nf.format(b);
	}
	
	private String fmtDate(Timestamp ts) {
		if (ts == null) return "-";
		return ts.toLocalDateTime().toLocalDate().toString();
	}
	
	private String cut(String s, int max) {
		if (s == null) return "-";
		return (s.length() <= max) ? s : s.substring(0, max - 2) + "..";
	}
	
	private String emptyToNull(String s) {
		if(s == null) return null;
		s = s.trim();
		return s.isEmpty() ? null : s;
	}
	
	// 입력 유틸리티 에러 UI 적용
	private Long readOptionalLong(String prompt) throws IOException {
		while(true) {
			String s = io.readOptional(prompt);
			if(s == null) return null; 
			try {
				return Long.parseLong(s.trim());
			} catch (NumberFormatException e) {
				printError("숫자만 입력 가능합니다. (예: 1001)");
			}
		}
	}
	
	private int readOptionalIntDefault(String prompt, int def) throws IOException {
		while(true) {
			String s = io.readOptional(prompt);
			if(s == null) return def;
			try {
				return Integer.parseInt(s.trim());
			} catch (NumberFormatException e) {
				printError("숫자만 입력 가능합니다. (예: 100)");
			}
		}
	}
}