package kr.krd.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import kr.krd.dao.USH_AnnouncementDAO;
import kr.krd.vo.USH_AnnStatusInfo;
import kr.krd.vo.USH_AnnSummaryVO;
import kr.util.USH_ConsoleUtil;

public class USH_AdminAnnouncementService {
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
	private final USH_AnnouncementDAO dao;
	private final String adminId;
	private static final Set<String> ALLOWED_STATUS = Set.of("공고중", "공고예정", "마감");
	
	public USH_AdminAnnouncementService(BufferedReader br, String adminId) {
		this.br = br;
		this.io = new USH_ConsoleUtil(br);
		this.dao = new USH_AnnouncementDAO();
		this.adminId = adminId;
	}

	// ------------------------------------------------
	// UI Helper Methods
	// ------------------------------------------------
	private void printSubTitle(String title) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ " + title + RESET);
		System.out.println(INDENT + CLR_GRY + "────────────────────────────────────────────────────────" + RESET);
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

	private String cut(String s, int max) {
		if (s == null) return "-";
		return (s.length() <= max) ? s : s.substring(0, max - 2) + "..";
	}
	
	// ------------------------------------------------
	// Business Logic
	// ------------------------------------------------

	// 공고 상태 강제 변경
	public void forceChangeAnnStatusFlow() throws IOException {
		while(true) {
			printSubTitle("공고 상태 강제 변경 (Force Status Change)");
			
			List<USH_AnnSummaryVO> list = dao.findAnnSummaryList();
			printAnnList(list);
			
			int annId = io.readIntInRange(getInputPrompt("상태 변경할 공고 ID 입력 (0: 이전 메뉴로 돌아가기)"), 0, Integer.MAX_VALUE);
			if(annId == 0) return; 
			
			// 현재 상태/마감일 조회 (DELETED 상관없이 1건 조회)
			USH_AnnStatusInfo info = dao.findAnnStatusInfo(annId);
			if(info == null) {
				printError("해당 공고 ID(" + annId + ")를 찾을 수 없습니다.");
				continue;
			}
			
			// 현재 상태 요약 출력 (직관성 강화)
			System.out.println("\n" + INDENT + CLR_PRIMARY + "┌─ [ 선택한 공고 정보 ] ────────────────────────┐" + RESET);
			System.out.println(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "현재 상태 : " + CLR_WHT + info.statusCd + RESET);
			System.out.println(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "마감 일자 : " + CLR_WHT + (info.endDt != null ? info.endDt : "미설정") + RESET);
			System.out.println(INDENT + CLR_PRIMARY + "└───────────────────────────────────────────────┘" + RESET);

			String newStatus = io.readRequired(getInputPrompt("변경할 상태 (공고중 / 공고예정 / 마감)")).trim();

			// 1) 허용값 체크
			if(!ALLOWED_STATUS.contains(newStatus)) {
				printError("잘못된 상태입니다. (공고중, 공고예정, 마감 중 하나만 입력 가능)");
				continue;
			}

			if(newStatus.equals(info.statusCd)) {
				printError("이미 선택하신 상태(" + newStatus + ")로 설정되어 있습니다.");
				continue;
			}

			// 2) 정책 체크: 마감 -> 공고중 은 마감일이 오늘 이후일 때만 가능
			if("공고중".equals(newStatus) && "마감".equals(info.statusCd)) {
				if(info.endDt == null) {
					printError("마감일이 설정되어 있지 않아 '공고중'으로 변경할 수 없습니다.");
					continue;
				}
				if(!info.endDt.isAfter(LocalDate.now())) {
					printError("공고 기간이 남아있지 않아 '공고중'으로 변경할 수 없습니다. (마감일: " + info.endDt + ")");
					continue;
				}
			}
			
			if(!io.confirmYN(getInputPrompt("상태를 [" + info.statusCd + " -> " + newStatus + "]로 강제 변경하시겠습니까? (Y/N)"))) {
				printError("상태 변경을 취소했습니다.");
				continue;
			}
			
			int updated = dao.updateAnnStatus(annId, newStatus);
			if(updated == 1) printSuccess("공고 상태가 성공적으로 변경되었습니다.");
			else printError("상태 변경 실패 (DB 처리 오류 또는 조건 불충족)");
		}
	}
	
	// 공고 리스트 그리드 출력
	private void printAnnList(List<USH_AnnSummaryVO> list) {
		if(list == null || list.isEmpty()) {
			System.out.println(INDENT + CLR_GRY + "  조회 가능한 공고가 없습니다." + RESET);
			return;
		}
		
		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-6s | %-8s | %-12s | %-12s | %-15s | %s", 
				"ID", "상태", "시작일", "마감일", "기관명", "제목") + RESET);
		System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------------------------------------------------" + RESET);
		
		for(USH_AnnSummaryVO a : list) {
			String status = safe(a.statusCd);
			// 상태에 따른 색상 부여
			String statColor = CLR_WHT;
			if("공고중".equals(status)) statColor = CLR_SUC; // 초록
			else if("마감".equals(status)) statColor = CLR_ERR;  // 빨강
			else if("공고예정".equals(status)) statColor = CLR_PRIMARY; // 하늘색

			System.out.println(INDENT + String.format("%-6d | " + statColor + "%-8s" + RESET + " | %-12s | %-12s | %-15s | %s", 
					a.annId, 
					status, 
					safe(a.startDt), 
					safe(a.endDt), 
					cut(safe(a.agencyName), 14), 
					cut(safe(a.title), 30)));
		}
		System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------------------------------------------------" + RESET);
	}

	// ------------------------------------------------
	// Utility
	// ------------------------------------------------
	private String safe(String s) {
		return (s == null || s.isBlank()) ? "-" : s;
	}
}