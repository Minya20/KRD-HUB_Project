package kr.krd.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import kr.krd.dao.USH_RoleApplicationDAO;
import kr.krd.vo.USH_RoleAppVO;
import kr.util.USH_ConsoleUtil;

public class USH_AdminRoleApplicationService {
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
	
	//대기 목록 조회 + ID로 승인/거부 처리
	public void pendingListFlow() throws IOException {
		while(true) {
			List<USH_RoleAppVO> list = dao.selectPending();
			
			if(list.isEmpty()) {
				System.out.println("[알림] 대기 중인 권한 신청이 없습니다.");
				return;
			}
			
			printPending(list);
			
			int roleAppId = io.readIntInRange("처리할 신청 ID (0:뒤로가기) > ", 0, Integer.MAX_VALUE);
			if(roleAppId == 0) return;
			
			USH_RoleAppVO target = null;
			for(USH_RoleAppVO v : list) {
				if(v.roleAppId == roleAppId) {
					target = v;
					break;
				}
			}
			
			if(target == null) {
				System.out.println("[경고] 방금 출력된 대기 목록에 없는 ID입니다.");
				continue;
			}
			
			System.out.println("신청자=" + target.roleAppUserId + ", 신청권한=" + target.roleAppRoleCd);
			
			int no = io.readIntInRange("1.승인  2.거부  0.취소 > ", 0, 2);
			if(no == 0) continue;
			
			if(no == 1) {
				int result = dao.approve(roleAppId,adminId); //트랜잭션
				System.out.println(result == 1 ? "[완료] 승인 처리되었습니다." : "[실패] 이미 처리되었거나 오류가 발생했습니다.");
			}else if(no == 2) {
				System.out.print("거부 사유 > ");
				String reason = br.readLine();
				
				int result = dao.reject(roleAppId, adminId, reason);
				System.out.println(result == 1 ? "[완료] 거부 처리되었습니다." : "[실패] 이미 처리되었거나 오류가 발생했습니다.");
			}
		}
	}
	
	private void printPending(List<USH_RoleAppVO> list) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println("--------------------------------------------------------------------");
		System.out.printf("%-6s %-12s %-8s %-12s %-30s%n", "ID", "신청자", "권한", "신청일", "사유");
		System.out.println("--------------------------------------------------------------------");
		
		for(USH_RoleAppVO v : list) {
			String dt = (v.roleAppAppliedAt == null)? "-" : sdf.format(v.roleAppAppliedAt);
			String reason = (v.roleAppApplyReason == null) ? "" : v.roleAppApplyReason;
			if(reason.length() > 28) reason = reason.substring(0, 28) + "...";
			
			System.out.printf("%-6d %-12s %-8s %-12s %-30s%n", v.roleAppId, v.roleAppUserId, v.roleAppRoleCd, dt, reason);
		}
		System.out.println("--------------------------------------------------------------------");
	}
}
