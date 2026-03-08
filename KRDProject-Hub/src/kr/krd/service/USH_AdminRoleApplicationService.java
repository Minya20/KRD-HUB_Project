package kr.krd.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import kr.krd.dao.USH_RoleApplicationDAO;
import kr.krd.vo.USH_RoleAppVO;
import kr.util.USH_ConsoleUtil;

public class USH_AdminRoleApplicationService {
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
	private final USH_RoleApplicationDAO dao;
	private final String adminId;
	
	public USH_AdminRoleApplicationService(BufferedReader br, String adminId) {
		this.br = br;
		this.io = new USH_ConsoleUtil(br);
		this.dao = new USH_RoleApplicationDAO();
		this.adminId = adminId;
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

	private void printSuccess(String msg) {
		System.out.println("\n" + INDENT + CLR_SUC + BOLD + "✔ Success: " + UNBOLD + RESET + msg);
	}

	private String getInputPrompt(String tag) {
		return "\n" + INDENT + CLR_PRIMARY + BOLD + "▶ " + tag + RESET + " : ";
	}

	// ------------------------------------------------
	// Business Logic
	// ------------------------------------------------
	
	// 대기 목록 조회 + ID로 승인/거부 처리
	public void pendingListFlow() throws IOException {
		while(true) {
			printSubTitle("권한 신청 관리 (Role Application Approval)");
			
			List<USH_RoleAppVO> list = dao.selectPending();
			
			if(list.isEmpty()) {
				System.out.println(INDENT + CLR_GRY + "  현재 승인 대기 중인 권한 신청 내역이 없습니다." + RESET);
				return;
			}
			
			printPending(list);
			
			int roleAppId = io.readIntInRange(getInputPrompt("처리할 신청 ID 입력 (0: 이전 메뉴로 돌아가기)"), 0, Integer.MAX_VALUE);
			if(roleAppId == 0) return;
			
			USH_RoleAppVO target = null;
			for(USH_RoleAppVO v : list) {
				if(v.roleAppId == roleAppId) {
					target = v;
					break;
				}
			}
			
			if(target == null) {
				printError("방금 출력된 대기 목록에 존재하지 않는 ID입니다.");
				continue;
			}
			
			// 선택한 대상의 정보를 미니 카드로 출력하여 관리자의 실수 방지
			System.out.println("\n" + INDENT + CLR_PRIMARY + "┌─ [ 선택한 신청 내역 ] ────────────────────────┐" + RESET);
			System.out.println(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "신청자 ID : " + CLR_WHT + target.roleAppUserId + RESET);
			System.out.println(INDENT + CLR_PRIMARY + "│  " + CLR_GRY + "신청 권한 : " + CLR_WHT + target.roleAppRoleCd + RESET);
			System.out.println(INDENT + CLR_PRIMARY + "└───────────────────────────────────────────────┘" + RESET);
			
			System.out.println();
			printMenuOption("1", "권한 승인 처리 (Approve)");
			printMenuOption("2", "권한 거부 처리 (Reject)");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("0", "작업 취소 (Cancel)");
			
			int no = io.readIntInRange(getInputPrompt("처리 방식 선택"), 0, 2);
			if(no == 0) continue;
			
			if(no == 1) {
				int result = dao.approve(roleAppId, adminId); // 트랜잭션
				if(result == 1) printSuccess("해당 권한 신청이 정상적으로 승인 처리되었습니다.");
				else printError("이미 처리되었거나 오류가 발생했습니다.");
				
			} else if(no == 2) {
				System.out.print(getInputPrompt("거부 사유 입력"));
				String reason = br.readLine();
				
				int result = dao.reject(roleAppId, adminId, reason);
				if(result == 1) printSuccess("해당 권한 신청이 거부(반려) 처리되었습니다.");
				else printError("이미 처리되었거나 오류가 발생했습니다.");
			}
		}
	}
	
	// 대기 목록 그리드 출력
	private void printPending(List<USH_RoleAppVO> list) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-6s | %-12s | %-8s | %-12s | %-30s", 
				"ID", "신청자", "신청권한", "신청일", "신청사유") + RESET);
		System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------------------------" + RESET);
		
		for(USH_RoleAppVO v : list) {
			String dt = (v.roleAppAppliedAt == null) ? "-" : sdf.format(v.roleAppAppliedAt);
			String reason = (v.roleAppApplyReason == null) ? "사유 없음" : v.roleAppApplyReason;
			
			// 사유가 너무 길면 잘라주기
			if(reason.length() > 24) reason = reason.substring(0, 22) + "..";
			
			System.out.println(INDENT + String.format("%-6d | %-12s | " + CLR_PRIMARY + "%-8s" + RESET + " | %-12s | %-30s", 
					v.roleAppId, v.roleAppUserId, v.roleAppRoleCd, dt, reason));
		}
		System.out.println(INDENT + CLR_GRY + "--------------------------------------------------------------------------------" + RESET);
	}
}