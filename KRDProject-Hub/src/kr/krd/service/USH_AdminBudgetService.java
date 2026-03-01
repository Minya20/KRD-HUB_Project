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
		
		io.readRequired("엔터를 누르면 이전 메뉴로 돌아갑니다. > ");
	}
	
	//2.예산 사용 현황 조회
	public void budgetUsageFlow() throws IOException {
		System.out.println("[예산 사용 현황 조회] 아직 구현 전입니다.");
		io.readRequired("엔터를 누르면 이전 메뉴로 돌아갑니다. > ");		
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
	
	private String nvl(String s) {
		return (s == null || s.isBlank()) ? "-" : s;
	}
	
	private BigDecimal nvlAmt(BigDecimal b) {
		return (b == null) ? BigDecimal.ZERO : b;
	}
	
	private String fmtAmt(BigDecimal b) {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
		return nf.format(b);
	}
	private String fmtDate(Timestamp ts) {
		if (ts == null) return "-";
		return ts.toLocalDateTime().toLocalDate().toString();
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
