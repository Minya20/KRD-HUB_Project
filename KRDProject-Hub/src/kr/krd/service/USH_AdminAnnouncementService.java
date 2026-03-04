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
	
	private final BufferedReader br;
	private final USH_ConsoleUtil io;
	private final USH_AnnouncementDAO dao;
	private final String adminId;
	private static final Set<String> ALLOWED_STATUS = Set.of("공고중","공고예정","마감");
	
	public USH_AdminAnnouncementService(BufferedReader br, String adminId) {
		this.br = br;
		this.io = new USH_ConsoleUtil(br);
		this.dao = new USH_AnnouncementDAO();
		this.adminId = adminId;
	}
	
	//공고 상태 강제 변경
	public void forceChangeAnnStatusFlow() throws IOException {
		while(true) {
			System.out.println();
			System.out.println("===== 공고 상태 강제 변경 =====");
			List<USH_AnnSummaryVO> list = dao.findAnnSummaryList();
			printAnnList(list);
			int annId = io.readIntInRange("상태 변경할 공고 ID 입력(0 : 이전 메뉴(뒤로가기)) > ", 0, Integer.MAX_VALUE);
			if(annId == 0) return; 
			//현재 상태/마감일 조회(DELETED 상관없이 1건 조회)
			USH_AnnStatusInfo info = dao.findAnnStatusInfo(annId);
			if(info == null) {
				System.out.println("해당 공고 ID가 없습니다.: " + annId);
				continue;
			}
			
			System.out.println("현재 상태: " + info.statusCd + ", 마감일: " + info.endDt);
			String newStatus = io.readRequired("변경할 상태(공고중/공고예정/마감) > ").trim();

			// 1) 허용값 체크
			if(!ALLOWED_STATUS.contains(newStatus)) {
			    System.out.println("잘못된 상태입니다. (공고중/공고예정/마감 중 하나로 입력)");
			    continue;
			}

			// 2) 정책 체크: 마감 -> 공고중 은 마감일이 오늘 이후일 때만 가능
			if("공고중".equals(newStatus) && "마감".equals(info.statusCd)) {
			    if(info.endDt == null) {
			        System.out.println("마감일이 없어 공고중으로 변경할 수 없습니다.");
			        continue;
			    }
			    if(!info.endDt.isAfter(LocalDate.now())) {
			        System.out.println("공고 기간이 남아있지 않아 공고중으로 변경할 수 없습니다. (마감일: " + info.endDt + ")");
			        continue;
			    }
			}
			
			if(newStatus.equals(info.statusCd)) {
				System.out.println("이미 해당 상태입니다.");
				continue;
			}
			
			if(!io.confirmYN("정말 변경하시겠습니까? (Y/N) > ")) {
				System.out.println("변경을 취소했습니다.");
				continue;
			}
			
			int updated = dao.updateAnnStatus(annId, newStatus);
			if(updated == 1) System.out.println("변경 완료되었습니다.");
			else System.out.println("변경 실패(처리 중 오류/조건 변경).");
		}
	}
	
	private void printAnnList(List<USH_AnnSummaryVO> list) {
		if(list == null || list.isEmpty()) {
			System.out.println("공고가 없습니다.");
			return;
		}
		System.out.printf("%-6s %-8s %-12s %-12s %-16s %s%n", "ID", "상태", "시작일", "마감일", "기관", "제목");
		System.out.println("-".repeat(90));
		for(USH_AnnSummaryVO a : list) {
			System.out.printf("%-6d %-8s %-12s %-12s %-16s %s%n", a.annId, safe(a.statusCd), safe(a.startDt), 
					           safe(a.endDt), safe(a.agencyName), safe(a.title));
		}
		System.out.println("-".repeat(90));
		
	}
	private String safe(String s) {
		return(s == null || s.isBlank()) ? "-": s;
	}
}
