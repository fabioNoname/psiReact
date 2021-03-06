package com.br.psi.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.br.psi.model.Client;
import com.br.psi.model.Kind;
import com.br.psi.model.OfficeRoom;
import com.br.psi.model.Patient;
import com.br.psi.model.PaymentPatient;
import com.br.psi.model.Professional;
import com.br.psi.model.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, String> {
	@Query("select s from Schedule s where s.professional = :professional and s.dateStart >= :start and s.dateEnd <= :end")
	List<Schedule> findByProfessionalAndDateStartAndDateEnd(Professional professional, Date start, Date end);
	@Query("select s from Schedule s where s.professional = :professional and s.dayOfWeek = :dayOfWeek and s.patient = :patient and s.dateStart > :dateStart and s.kind = :kind")
	List<Schedule> findByProfessionalAndDayOfWeekAndPatientAndDateStartAndKind(Professional professional,Integer dayOfWeek, Patient patient, Date dateStart, Kind kind);

	Schedule findById(Long id);

	List<Schedule> findByPaymentPatient(PaymentPatient paymentPatient);
	List<Schedule> findByProfessionalId(Long id);
	@Query("select s from Schedule s where s.professional.id = :id and s.dateStart >= :start")
	List<Schedule> findByProfessionalIdAndDateStart(Long id,Date start);
	@Query("select s from Schedule s where s.dateStart >= :dateStart and s.dateEnd <= :dateEnd")
	List<Schedule> findByDateStartAndDateEnd(Date dateStart, Date dateEnd);
	@Query("select s from Schedule s where s.dateStart >= :dateStart and s.dateEnd <= :dateEnd and s.officeRoom =:officeRoom")
	List<Schedule> findByDateStartAndDateEndAndOfficeRoom(Date dateStart, Date dateEnd, OfficeRoom officeRoom);
	@Query("select s from Schedule s where s.professional.person.client = :client and s.dateStart >= :start")
	List<Schedule> findByProfessionalPersonClientAndDateStart(Client client, Date start);
	@Query("select s from Schedule s where s.patient = :patient and s.dateStart >= :start")
	List<Schedule> findByPatientDateStart(Patient patient, Date start);
	
}