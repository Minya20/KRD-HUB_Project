package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import kr.krd.dao.SY_RESIDAO;
import kr.krd.vo.SY_AnnouncementVO;

public class SY_KRDRESIUI{
	private BufferedReader br;
	private SY_RESIDAO dao;
	
	public SY_KRDRESIUI() {
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
			System.out.println("1.공고 조회");
			System.out.println("2.과제 신청");
			System.out.println("3. 내 신청 조회");
			System.out.println("4. 선정 결과 확인");
			System.out.println("5. 보고서 제출");
			System.out.println("6. 내 정보 수정");
			System.out.println("7. 로그아웃");
			System.out.print("입력 > ");
			
			try {
				int no = Integer.parseInt(br.readLine());
				
				if(no == 1) {
					showAnnouncementMenu();
				}else if(no == 2) {
					
				}else if(no == 3) {
					
				}else if(no == 4) {
					
				}else if(no == 4) {
					
				}else if(no == 5) {

				}else if(no == 6){
					
				}else if(no == 7) {
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
				System.out.println("상세 조회할 공고 번호 입력 > ");
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
		
	}
	public static void main(String[] args) {
		new SY_KRDRESIUserMain();
	}
}



