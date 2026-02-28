package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import kr.krd.dao.SY_ApplicationDAO;
import kr.krd.dao.SY_RESIDAO;
import kr.krd.vo.SY_AnnouncementVO;

public class SY_KRDRESIUI{
	private BufferedReader br;
	private SY_RESIDAO dao;
	private String custId; //로그인한 사용자 ID
	private SY_ApplicationDAO appDao;
	
	public SY_KRDRESIUI(String custId) {
		this.custId = custId;
		this.appDao = new SY_ApplicationDAO();
		
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			dao = new SY_RESIDAO();
			callMenu();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			//자원정리
			if(br!=null)try {br.close();}catch(IOException e) {}
		}
	}
	
	//개인 연구자 로그인 후 메뉴
	private void callMenu()throws IOException{
		while(true) {
			System.out.println("==========개인 연구자 메뉴==========");
			System.out.println("1.공고 조회");;
			System.out.println("2. 내 신청 조회");
			System.out.println("3. 선정 결과 확인");
			System.out.println("4. 보고서 제출");
			System.out.println("5. 내 정보 수정");
			System.out.println("6. 로그아웃");
			System.out.print("입력 > ");
			
			try {
				int no = Integer.parseInt(br.readLine());
				
				if(no == 1) {
					showAnnouncementMenu();
				}else if(no == 2) {
					
				}else if(no == 3) {
					
				}else if(no == 4) {
	
				}else if(no == 5) {

				}else if(no == 6) {
					System.out.println("현재 계정에서 로그아웃합니다.");
					return;
				}else {
					System.out.println("잘못 입력했습니다.");
				}
			}catch(NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		}
	}
	
	//공고 조회(목록 -> 상세 -> 뒤로)
	private void showAnnouncementMenu() throws IOException {
		while(true) {
			ArrayList<SY_AnnouncementVO> list = dao.selectAnnList();
			
			System.out.println("==============공고 목록==============");
			if(list.isEmpty()) {
				System.out.println("등록된 공고가 없습니다.");
				return;
			}
			
			System.out.println("번호\t공고명\t예산\t시작일\t종료일");
			for (SY_AnnouncementVO a : list) {
				System.out.print(a.getAnnId() + "\t");
				System.out.print(a.getTitle() + "\t");
				System.out.printf("%,d\t",a.getTotalBudget());
				System.out.print(a.getStartDt() + "\t");
				System.out.println(a.getEndDt());
			}
			System.out.println("----------------------------------");
			System.out.println("0.이전메뉴(뒤로가기)");
			System.out.println("1.공고 상세 조회");
			System.out.print("입력 > ");
			
			int no;
			try {
				no = Integer.parseInt(br.readLine());
			}catch (NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
				continue;
			}
			
			if(no == 0) return;
			if(no == 1) {
				System.out.print("상세 조회할 공고 번호 입력 > ");
				int annId;
				try {
					annId = Integer.parseInt(br.readLine());
				}catch (NumberFormatException e) {
					System.out.println("[숫자만 입력 가능]");
					continue;
				}
				showAnnouncementDetail(annId);
			}else {
				System.out.println("잘못 입력하셨습니다.");
			}
		}
	}
	
	private void showAnnouncementDetail(int annId) throws IOException {
		while(true) {
			SY_AnnouncementVO a = dao.selectAnnDetail(annId);

			if(a == null) {
				System.out.println("해당 공고가 없습니다.");
				return;
			}

			System.out.println("==========공고 상세==========");
			System.out.println("공고번호 : " + a.getAnnId());
			System.out.println("기관번호 : " + a.getAgyId());
			System.out.println("공고명 : " + a.getTitle());
			System.out.println("재공고여부 : " + a.getReannYn());
			System.out.println("담당자연락처 : " + a.getPmContact());
			System.out.println("모집인원 : " + a.getRecruitCap());
			System.out.println("접수시작일 : " + a.getStartDt());
			System.out.println("접수종료일 : " + a.getEndDt());
			System.out.println("공고상태 : " + a.getStatus());
			System.out.println("모집분야 : " + a.getField());
			System.out.println("공고담당자 : " + a.getCreatedBy());
			System.out.printf("총예산 : %,d\n", a.getTotalBudget());
			System.out.println("공고설명 : " + a.getAnnDesc());
			System.out.println("===========================");

			System.out.println("0.이전메뉴(뒤로가기)");
			System.out.println("1.신청하기");
			System.out.print("입력 > ");

			int no;
			try {
				no = Integer.parseInt(br.readLine());
			}catch (NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
				continue;
			}
			if(no == 0) return;
			if(no == 1) {
				applyToAnnouncement(annId);
			}else {
				System.out.println("잘못 입력하셨습니다.");
			}

		}
	}
	
	//신청서 작성
	private void applyToAnnouncement(int annId)throws IOException {
		System.out.println("\n=====신청서 작성=====");
		System.out.println("신청자 ID: " + custId);
		System.out.print("첨부파일 경로 입력 > ");
		String attachPath = br.readLine();
		
		long budgetAmt;
		while(true) {
			System.out.print("신청 예산 입력 > ");
			try {
				budgetAmt = Long.parseLong(br.readLine());
				break;
			}catch (NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		}
		
		String statusCd = "APPLIED";
		
		int count = appDao.insertApplication(annId, custId, attachPath, statusCd, budgetAmt);
		
		if(count > 0) {
			System.out.println("신청이 완료되었습니다.");
		}else {
			System.out.println("신청 실패(값이 반영되지 않았습니다.)");
		}
		
		System.out.println("Enter을 누르면 상세로 돌아갑니다.");
		br.readLine();
		
	}

}



