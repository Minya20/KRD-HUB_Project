package kr.krd.service;

import java.io.IOException;
import java.util.List;
import java.io.BufferedReader;

import kr.util.USH_ConsoleUtil;
import kr.krd.vo.USH_AgencyCountVO;
import kr.krd.vo.USH_YearCountVO;
import kr.krd.dao.USH_StatsDAO;

public class USH_AdminStatsService {
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

	//연도별 선정 건수
	public void selectedByYearFlow() throws IOException {
		System.out.println();
		System.out.println("===== 연도별 선정 건수 =====");
		
		List<USH_YearCountVO> list = dao.findSelectedCountByYear();
		
		if(list == null || list.isEmpty()) {
			System.out.println("조회 결과가 없습니다.");
			System.out.println("-".repeat(30));
			io.readOptional("엔터를 누르면 이전 메뉴로 돌아갑니다.");
			return;
		}
		System.out.printf("%-10s %-12s\n", "연도", "총 선정 건수");
		System.out.println("-".repeat(30));
		
		for(USH_YearCountVO row : list) {
			System.out.printf("%-10d %-12d\n", row.year, row.selectedCount);
		}
		
		System.out.println("-".repeat(30));
		io.readOptional("엔터를 누르면 이전 메뉴로 돌아갑니다.");

	}
	
	//기관별 선정 건수
	public void selectedByAgencyFlow() throws IOException {
		System.out.println();
		System.out.println("===== 기관별 선정 건수 =====");
		
		List<USH_AgencyCountVO> list = dao.findSelectedCountByAgency();
		
		if(list == null || list.isEmpty()) {
			System.out.println("조회된 결과가 없습니다.");
			System.out.println("-".repeat(30));
			io.readOptional("엔터를 누르면 이전 메뉴로 돌아갑니다.");
			return;
		}
		
		System.out.printf("%-25s %-12s\n", "기관명", "총 선정 건수");
		System.out.println("-".repeat(30));
		
		for(USH_AgencyCountVO row : list) {
			System.out.printf("%-25s %-12d\n", row.agencyName, row.selectedCount);
		}
		System.out.println("-".repeat(30));
		io.readOptional("엔터를 누르면 이전 메뉴로 돌아갑니다.");
	}
	
	//평균 경쟁률
	public void avgCompetitionRateFlow() throws IOException {
		System.out.println();
		System.out.println("===== 평균 경쟁률 조회 =====");
		
		double avg = dao.findAvgCompetitionRate();
		
		System.out.printf("현재 평균 경쟁률 : %.2f\n", avg);
		System.out.println("-".repeat(30));
		io.readOptional("엔터를 누르면 이전 메뉴로 돌아갑니다.");
	}
}
