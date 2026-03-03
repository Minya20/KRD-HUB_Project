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
	
	//1.예산 변경 이력 조회
	public void budgetHistFlow() throws IOException {
		System.out.println("===== 예산 변경 이력 조회 =====");
		System.out.println("[안내] 엔터 = 전체/생략");
		System.out.println();
		
		Long projectId = readOptionalLong("프로젝트 ID(엔터=전체) > ");
		String start = emptyToNull(io.readDateOrEmpty("변경일 시작(YYYY-MM-DD, 엔터=생략) > "));
		String end = emptyToNull(io.readDateOrEmpty("변경일 끝(YYYY-MM-DD) > "));
		int limit = readOptionalIntDefault("조회 건수(엔터=100) > ", 100);
		
		List<USH_BudgetHistRow> list = budgetDao.findBudgetHist(projectId, start, end, limit);
		printBudgetHistList(list, limit);
		
		io.readOptional("엔터를 누르면 이전 메뉴로 돌아갑니다. > ");
	}
	
	
	private void printBudgetHistList(List<USH_BudgetHistRow> list, int limit) {
		System.out.println();
		System.out.println("========== 예산 변경 이력 (최대 " + limit + "건) ==========");
		
		if(list == null || list.isEmpty()) {
			System.out.println("조회 결과가 없습니다.");
			System.out.println("-".repeat(120));
			return;
		}
		
		System.out.printf("%-10s %-10s %15s %15s %15s %-12s %-12s %s%n",
                "HIST_ID", "PROJECT", "BEFORE", "AFTER", "DIFF", "CHANGED_BY", "CHANGED_AT", "REASON");
        System.out.println("-".repeat(120));
        
        for(USH_BudgetHistRow r : list) {
        	BigDecimal before = nvlAmt(r.beforeAmt);
        	BigDecimal after = nvlAmt(r.afterAmt);
        	BigDecimal diff = after.subtract(before);
        	
        	System.out.printf("%-10d %-10d %15s %15s %15s %-12s %-12s %s%n",
        			r.histId,
        			r.projectId,
        			fmtAmt(before),
        			fmtAmt(after),
        			fmtAmt(diff),
        			nvl(r.changedBy),
        			fmtDate(r.changedAt),
        			nvl(r.reason));
        }
        
        System.out.println("-".repeat(120));

	}
	
	//예산 사용 현황 조회
	public void budgetUsageFlow() throws IOException {
		while(true) {
			List<USH_BudgetUsageSummary> list = budgetDao.findBudgetUsageAll();
			printUsageList(list);
			
			System.out.println("0.이전 메뉴(뒤로가기)");
			System.out.println("1.프로젝트 상세 조회(집행 내역 포함)");
			System.out.println();
			
			int no = io.readIntInRange("입력 > ", 0, 1);
			if(no == 0) return;
			
			int projectId = io.readIntInRange("조회할 PROJECT_ID 입력 > ", 1, Integer.MAX_VALUE);
			
			USH_BudgetUsageSummary summary = budgetDao.findBudgetUsageByProjectId(projectId);
			if(summary == null) {
				System.out.println("해당 PROJECT_ID가 존재하지 않거나 공고 연결이 없습니다." + projectId);
				continue;
			}
			
			printUsageDetail(summary);
			
			List<USH_FundingLine> lines = budgetDao.findFundingLinesByProjectId(projectId);
			printFundingLines(lines);
			
		}
	}
	
	private void printUsageList(List<USH_BudgetUsageSummary> list) {
		System.out.println("========== 예산 사용 현황(전체) ==========");
		if(list == null || list.isEmpty()) {
			System.out.println("조회 결과가 없습니다.");
			System.out.println("-".repeat(120));
			return;
		}
		
		System.out.printf("%-10s %-30s %-15s %-15s %-15s %-8s\n", "PROJECT", "공고명", "총액", "집행", "잔액", "집행률");
		System.out.println("-".repeat(120));
		
		for(USH_BudgetUsageSummary u : list) {
			System.out.printf("%-10d %-30s %-15d %-15d %-15d %-8.2fs\n",
							  u.projectId, cut(u.announcementTitle, 28), u.totalBudget, u.usedBudget, u.remainingBudget, u.usedPct);	
		}
		System.out.println("-".repeat(120));
	}
	
	private void printUsageDetail(USH_BudgetUsageSummary u) {
		System.out.println();
		System.out.println("========== 예산 사용 현황(상세) ==========");
		System.out.println("PROJECT_ID : " + u.projectId);
		System.out.println("공고명 : " + (u.announcementTitle == null ? "-" : u.announcementTitle));
		System.out.println("총액 : " + u.totalBudget);
		System.out.println("집행(승인) : " + u.usedBudget);
		System.out.println("잔액 : " + u.remainingBudget);
		System.out.println("집행률(%) : " + u.usedPct);
		System.out.println("-".repeat(60));
	}
	
	private void printFundingLines(List<USH_FundingLine> lines) {
		System.out.println();
		System.out.println("========== 집행(지급) 내역 ==========");
		if(lines == null || lines.isEmpty()) {
			System.out.println("지급 내역이 없습니다.");
			System.out.println("-".repeat(100));
			return;
		}
		
		System.out.printf("%-8s %-12s %-12s %-12s %-10s %-12s %-12s\n", "회차", "금액", "상태", "요청일", "승인(Y/N)", "승인자", "승인일");
		System.out.println("-".repeat(100));
		
		for(USH_FundingLine f : lines) {
			System.out.printf("%-8d %-12s %-12s %-12s %-10d %-12s %-12s\n",
							  f.payRound, f.amount, nvl(f.stautsCd), nvlDate(f.requestedDt), f.approvedYn, nvl(f.approvedBy), 
							  nvlDate(f.approvedDt));
		}
		System.out.println("-".repeat(100));
		
	}
	
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
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
		return nf.format(b);
	}
	private String fmtDate(Timestamp ts) {
		if (ts == null) return "-";
		return ts.toLocalDateTime().toLocalDate().toString();
	}
	
	private String cut(String s, int max) {
		if (s == null) return "-";
		return (s.length() <= max) ? s : s.substring(0, max - 1) + "…";
	}
	
	private String emptyToNull(String s) {
		if(s == null) return null;
		s = s.trim();
		return s.isEmpty() ? null : s;
	}
	
	private Long readOptionalLong(String prompt) throws IOException {
		while(true) {
			String s = io.readOptional(prompt);
			if(s == null) return null; //엔터=생략
			try {
				return Long.parseLong(s.trim());
			}catch (NumberFormatException e) {
				System.out.println("[숫자만 입력 가능] 예: 1001 (또는 엔터)");
			}
		}
	}
	
	private int readOptionalIntDefault(String prompt, int def) throws IOException {
		while(true) {
			String s =io.readOptional(prompt);
			if(s == null) return def;
			try {
				return Integer.parseInt(s.trim());
			}catch (NumberFormatException e) {
				System.out.println("[숫자만 입력 가능] 예 : 100 (또는 엔터)");
			}
		}
	}
}
