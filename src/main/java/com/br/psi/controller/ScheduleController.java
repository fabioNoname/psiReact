package com.br.psi.controller;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.br.psi.model.Const;
import com.br.psi.model.Patient;
import com.br.psi.model.PaymentPatient;
import com.br.psi.model.Professional;
import com.br.psi.model.Schedule;
import com.br.psi.model.Shifts;
import com.br.psi.model.User;
import com.br.psi.repository.PaymentPatientRepository;
import com.br.psi.repository.ProfessionalRepository;
import com.br.psi.repository.ProfessionalRepositoryService;
import com.br.psi.repository.ScheduleRepository;
import com.br.psi.repository.ShiftsRepository;

@RestController
@RequestMapping
public class ScheduleController {

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private PaymentPatientRepository paymentPatientRepository;
    @Autowired
    private ShiftsRepository shiftsRepository;
    @Autowired
    private ProfessionalRepositoryService professionalRepositoryService;
    @Autowired
    private ProfessionalRepository professionalRepository;
    
    @Secured({Const.ROLE_ADMIN,Const.ROLE_PRFESSIONAL,Const.ROLE_CLIENT})
    @RequestMapping(value = "/schedule/save", method = RequestMethod.POST)
    public ResponseEntity<Object> save(@RequestBody List<Schedule> listSchedule) {
    	for (Schedule schedule : listSchedule) {
    		if(schedule.getId() == null && schedule.getProfessional() != null && schedule.getKind() != null) {
    				List<Schedule> list = scheduleRepository.findByProfessionalAndDayOfWeekAndPatientAndDateStartAndKind(schedule.getProfessional(),schedule.getDayOfWeek(),schedule.getPatient(),new Date(),schedule.getKind());
    				try {
						proccessKindSchedule(schedule,list);
					} catch (Exception e) {
						return new ResponseEntity<Object>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);				
					}
    		}
		}
    	 
        return new ResponseEntity<Object>(listSchedule, HttpStatus.OK);
    }


	private void proccessKindSchedule(Schedule schedule, List<Schedule> listSch) throws Exception {
			switch (schedule.getKind().getAmountDay()) {
			case 0:
				createUnique(schedule,listSch);
				break;
			default:
				createDefault(schedule,listSch);
			}
		
	}

	private void createDefault(Schedule schedule, List<Schedule> list) throws Exception {
		creatList(schedule,list);
		this.scheduleRepository.saveAll(list);
		
	}


	private void creatList(Schedule schedule, List<Schedule> list) throws Exception {
		Professional professional = professionalRepository.findAllById(schedule.getProfessional().getId());
//		List<PaymentPatient> paymentPatients = paymentPatientRepository.findByPatientAndFormation(schedule.getPatient(),professional.getFormation());;
		int count = list.size()+1;
		int contol = 1;
		int j = 1;
		list.add(schedule);
//		for (PaymentPatient paymentPatient : paymentPatients) {
			Integer amountMultiple = schedule.getKind().getAmountMultiple();
				if(schedule.getPaymentPatient().getAmount() != null) {
					List<Schedule> findByPaymentPatient = scheduleRepository.findByPaymentPatient(schedule.getPaymentPatient());
					amountMultiple = (schedule.getPaymentPatient().getAmount() - findByPaymentPatient.size() - contol) ;
				}
			
				if(schedule.getPaymentPatient() == null && amountMultiple > 0) {
					schedule.setPaymentPatient(schedule.getPaymentPatient());
					contol = 0;
				}
			for (int i = 1; i <= amountMultiple ; i++) {
				Date dateStart = new Date(schedule.getDateStart().getTime());
				Date dateEnd = new Date(schedule.getDateEnd().getTime());
				Schedule model = new Schedule();
				model.setProfessional(schedule.getProfessional());
				model.setPatient(schedule.getPatient());
				model.setKind(schedule.getKind());
				dateStart.setDate(dateStart.getDate() + (schedule.getKind().getAmountDay() * j));
				dateEnd.setDate(dateEnd.getDate() + (schedule.getKind().getAmountDay() * j));
				model.setDateStart(dateStart);
				model.setDateEnd(dateEnd);
				model.setAmount(schedule.getAmount());
				model.setPlanCode(schedule.getPlanCode());
				model.setDayOfWeek(schedule.getDayOfWeek());
				model.setPaymentPatient(schedule.getPaymentPatient());
				model.setOfficeRoom(schedule.getOfficeRoom());
				list.add(model);
				j+=1;
			}
//			}

		if(list.size() <= count)
			throw new Exception("Paciente "+schedule.getPatient().getPerson().getName()+" não possui créditos disponível para especialidade "+ professional.getFormation().getName());
		
		
	}

	private void createUnique(Schedule schedule, List<Schedule> list) throws Exception {
		//alterar metodo para validar status de pagamentos validos.
		Professional professional = professionalRepository.findAllById(schedule.getProfessional().getId());
//		List<PaymentPatient> paymentPatients = paymentPatientRepository.findByPatientAndFormation(schedule.getPatient(),professional.getFormation());
//		for (PaymentPatient paymentPatient : paymentPatients) {
			List<Schedule> findByPaymentPatient = scheduleRepository.findByPaymentPatient(schedule.getPaymentPatient());
				if(schedule.getPaymentPatient() != null && (schedule.getPaymentPatient().getAmount() == null || findByPaymentPatient.size() < schedule.getPaymentPatient().getAmount())) {
//					schedule.setPaymentPatient(paymentPatient);
					this.scheduleRepository.save(schedule);
					return;
				}
//		}
		throw new Exception("Paciente "+schedule.getPatient().getPerson().getName()+" não possui créditos disponível para especialidade "+ professional.getFormation().getName());
		
	}


	private void deleteList(List<Schedule> list) {
		this.scheduleRepository.deleteAll(list);
		list.removeAll(list);
	}


	@Secured({Const.ROLE_ADMIN,Const.ROLE_PRFESSIONAL,Const.ROLE_CLIENT})
    @RequestMapping(value = "/schedule/edit", method = RequestMethod.PUT)
    public ResponseEntity<Schedule> edit(@RequestBody Schedule schedule){
        this.scheduleRepository.save(schedule);
        return new ResponseEntity<Schedule>(schedule, HttpStatus.OK);
    }
	
	@Secured({Const.ROLE_ADMIN,Const.ROLE_PRFESSIONAL,Const.ROLE_CLIENT})
    @RequestMapping(value = "/schedule/delete", method = RequestMethod.POST)
    public ResponseEntity delete(@RequestBody Schedule schedule){
        this.scheduleRepository.delete(schedule);
        return new ResponseEntity<>( HttpStatus.OK);
    }
	@Secured({Const.ROLE_ADMIN,Const.ROLE_PRFESSIONAL,Const.ROLE_CLIENT})
    @RequestMapping(value = "/schedule/releaseSchedule", method = RequestMethod.POST)
    public ResponseEntity releaseSchedule(@RequestBody Schedule schedule){
		schedule = scheduleRepository.findById(schedule.getId());
		List<Schedule> list = scheduleRepository.findByProfessionalAndDayOfWeekAndPatientAndDateStartAndKind(schedule.getProfessional(),schedule.getDayOfWeek(),schedule.getPatient(), new Date(),schedule.getKind());
        this.scheduleRepository.deleteAll(list);
        return new ResponseEntity<>( HttpStatus.OK);
    }
	

    @Secured({Const.ROLE_CLIENT, Const.ROLE_ADMIN})
    @RequestMapping(value = "/schedule/findByProfessionorOfficeRoom", method = RequestMethod.POST)
    public ResponseEntity<List<Schedule>> list(@RequestBody Schedule schedule){
    	List<Schedule> list = new ArrayList<Schedule>();
    	if(schedule.getProfessional().getId() != null) {
    		list = scheduleRepository.findByProfessionalAndDateStartAndDateEnd(schedule.getProfessional(),schedule.getDateStart(),schedule.getDateEnd());
    	}else {
    		list = scheduleRepository.findByDateStartAndDateEndAndOfficeRoom(schedule.getDateStart(),schedule.getDateEnd(),schedule.getOfficeRoom());
    	}
    	list = createListSchedule(list,schedule);
    	
        return new ResponseEntity<List<Schedule>>(list, HttpStatus.OK);
    }

	private List<Schedule> createListSchedule(List<Schedule> list, Schedule schedule) {
		List<Schedule> listSchedule = list;
		Date date = new Date();
		int timeSession = 30;
		long start = schedule.getDateStart().getTime();
		long end = schedule.getDateEnd().getTime();
		long interval = end - start;
//		long amoutPosibled = (interval /1000 / 60) / timeSession;
		Date dateStart = schedule.getDateStart();
		Boolean cotinua = Boolean.TRUE;
		while ( cotinua ) {
			Schedule model = new Schedule();
			model.setDateStart(dateStart);
			Date dateEnd = new Date(dateStart.getTime());
			dateEnd.setMinutes(dateEnd.getMinutes()+timeSession);
			model.setDateEnd(dateEnd);
			model.setProfessional(schedule.getProfessional());
			model.setDayOfWeek(dateStart.getDay());
			for (Schedule schedule2 : list) {
				if(schedule2.getDateStart().compareTo(model.getDateStart()) == 0 || (model.getDateStart().before(schedule2.getDateEnd()) && model.getDateStart().after(schedule2.getDateStart()))){
					model = null;
					break;
				}
			}
			
			dateStart = dateEnd;
			if(model != null && model.getDateStart().after(date) && !model.getDateEnd().after(new Date(end))) {
				model.setOfficeRoom(schedule.getOfficeRoom());
				listSchedule.add(model);
				cotinua = false;
			}
			if(model != null && model.getDateStart().after( new Date(end))) {
				cotinua = false;
			}
		}
		
		listSchedule.sort(new Comparator<Schedule>() {

			@Override
			public int compare(Schedule o1, Schedule o2) {
				if(o1.getId() != null && o2.getId() != null) {
					if(o1.getId() < o2.getId()) {
						return -1;
					}
					return 1;
				}
				
				if(o1.getId() == null && o2.getId() != null) {
					return -1;
				}
							
				return 1;
			}
    		
		});
		return listSchedule;
	}

	
	@Secured({Const.ROLE_ADMIN,Const.ROLE_PRFESSIONAL})
	@RequestMapping(value = "/schedule/findAllByprofessional", method = RequestMethod.GET)
	public ResponseEntity<List<Schedule>> findAllByprofessional() throws ParseException{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	    Professional professional = professionalRepositoryService.findByPerson(user.getPerson());
		 
		List<Schedule> list = scheduleRepository.findByProfessionalIdAndDateStart(professional.getId(), new Date());
			list = createListSchedule(list,professional);       
	    return new ResponseEntity<List<Schedule>>(list, HttpStatus.OK);
	}

	@Secured({Const.ROLE_ADMIN,Const.ROLE_PRFESSIONAL})
	@RequestMapping(value = "/schedule/findAllByPatient", method = RequestMethod.POST)
	public ResponseEntity<List<Schedule>> findAllByPatient(@RequestBody Patient patient){
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<Schedule> list = new ArrayList<Schedule>();
		if(patient.getId() != null) {
			list = scheduleRepository.findByPatientDateStart(patient, new Date());
		 }else {
			 list = scheduleRepository.findByProfessionalPersonClientAndDateStart(user.getPerson().getClient(), new Date()); 
		 }
		
	
		return new ResponseEntity<List<Schedule>>(list, HttpStatus.OK);
	}
	
	private List<Schedule> createListSchedule(List<Schedule> list, Professional professional) throws ParseException {
		List<Shifts> shifts = shiftsRepository.findByProfessionalId(professional.getId());
		
		List<Schedule> listSchedule = list;
		SimpleDateFormat format = new SimpleDateFormat();
		for (Shifts shift : shifts) {
			LocalDateTime now = LocalDateTime.now();
			for(int i = 0; i < 56; i++) {
				Date start = format.parse(shift.getTimeStart());
				Date ended = format.parse(shift.getTimeEnd());
				int timeSession = 30;
				long st = start.getTime();
				long end = ended.getTime();
				long interval = end - st;
				long amoutPosibled = (interval /1000 / 60) / timeSession;
				if(now.getDayOfWeek().getValue() == shift.getDayWeek().getDayOfWeek().intValue()) {
					for (int j = 0; j < amoutPosibled; j++) {
						LocalDateTime localStart = LocalDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), start.getHours(), start.getMinutes());
						LocalDateTime localEned = localStart.plusMinutes(timeSession);
						Schedule model = new Schedule();
						model.setDateStart(Date.from(localStart.atZone(ZoneId.systemDefault()).toInstant()));
						model.setDateEnd(Date.from(localEned.atZone(ZoneId.systemDefault()).toInstant()));
						model.setProfessional(shift.getProfessional());
						model.setDayOfWeek(shift.getDayWeek().getDayOfWeek());
						for (Schedule schedule : list) {
								if(schedule.getDayOfWeek().equals(model.getDayOfWeek()) && (schedule.getDateStart().compareTo(model.getDateStart()) == 0 || model.getDateEnd().before(ended) || (model.getDateStart().before(schedule.getDateEnd()) && model.getDateStart().after(schedule.getDateStart()))) ) {
									model = null;
									break;
								}
								
						}
						if(model != null)
							listSchedule.add(model);
						
						if(localEned.getHour() > ended.getHours()) {
							break;
						}
						
						start = Date.from(localEned.atZone(ZoneId.systemDefault()).toInstant());
					}
					
				}
					
				now = now.plusDays(1);
			}
		}
		
		return listSchedule;
	}

}
