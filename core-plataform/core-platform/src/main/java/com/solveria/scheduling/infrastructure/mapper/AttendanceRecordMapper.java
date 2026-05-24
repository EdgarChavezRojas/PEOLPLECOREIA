package com.solveria.scheduling.infrastructure.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.scheduling.domain.model.ar.AttendanceRecord;
import com.solveria.scheduling.domain.model.entity.TimeEntry;
import com.solveria.scheduling.domain.model.enums.AttendanceStatus;
import com.solveria.scheduling.domain.model.enums.PunchType;
import com.solveria.scheduling.domain.model.vo.GeoValidation;
import com.solveria.scheduling.domain.model.vo.TimeDeviation;
import com.solveria.scheduling.infrastructure.jpa.AttendanceRecordJpa;
import com.solveria.scheduling.infrastructure.jpa.TimeEntrySchedulingJpa;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper manual para convertir entre AttendanceRecord (dominio) y AttendanceRecordJpa
 * (infraestructura).
 */
@Component
public class AttendanceRecordMapper {

  private final ObjectMapper objectMapper;

  public AttendanceRecordMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public AttendanceRecordJpa toJpa(AttendanceRecord domain) {
    if (domain == null) return null;

    AttendanceRecordJpa jpa = new AttendanceRecordJpa();
    jpa.setRecordId(domain.getRecordId());
    jpa.setRelationshipId(domain.getRelationshipId());
    jpa.setWorkDate(domain.getWorkDate());
    jpa.setClosed(domain.isClosed());
    jpa.setStatus(domain.getStatus().name());
    jpa.setDeviations(toJson(domain.getDeviations()));

    List<TimeEntrySchedulingJpa> entryJpas =
        domain.getEntries().stream()
            .map(entry -> toTimeEntryJpa(entry, jpa))
            .collect(Collectors.toList());
    jpa.setEntries(entryJpas);

    return jpa;
  }

  public AttendanceRecord toDomain(AttendanceRecordJpa jpa) {
    if (jpa == null) return null;

    List<TimeEntry> entries =
        jpa.getEntries().stream().map(this::toTimeEntryDomain).collect(Collectors.toList());

    List<TimeDeviation> deviations = fromJson(jpa.getDeviations(), new TypeReference<>() {});

    return new AttendanceRecord(
        jpa.getRecordId(),
        jpa.getRelationshipId(),
        jpa.getWorkDate(),
        jpa.isClosed(),
        AttendanceStatus.valueOf(jpa.getStatus()),
        entries,
        deviations);
  }

  private TimeEntrySchedulingJpa toTimeEntryJpa(TimeEntry entry, AttendanceRecordJpa parentJpa) {
    TimeEntrySchedulingJpa jpa = new TimeEntrySchedulingJpa();
    jpa.setEntryId(entry.getEntryId());
    jpa.setAttendanceRecord(parentJpa);
    jpa.setPunchTime(entry.getPunchTime());
    jpa.setPunchType(entry.getPunchType().name());
    jpa.setDeviceId(entry.getDeviceId());
    jpa.setSource(entry.getSource());
    jpa.setGeoValidation(toJson(entry.getGeoValidation()));
    return jpa;
  }

  private TimeEntry toTimeEntryDomain(TimeEntrySchedulingJpa jpa) {
    GeoValidation geo = fromJson(jpa.getGeoValidation(), new TypeReference<>() {});
    return new TimeEntry(
        jpa.getEntryId(),
        jpa.getPunchTime(),
        PunchType.valueOf(jpa.getPunchType()),
        jpa.getDeviceId(),
        jpa.getSource(),
        geo);
  }

  private String toJson(Object value) {
    if (value == null) return null;
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Error serializando a JSON", e);
    }
  }

  private <T> T fromJson(String json, TypeReference<T> typeRef) {
    if (json == null || json.isBlank()) return null;
    try {
      return objectMapper.readValue(json, typeRef);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Error deserializando desde JSON", e);
    }
  }
}
