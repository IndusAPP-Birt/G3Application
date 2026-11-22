package eus.birtlh.g3.incident.repository;

import eus.birtlh.g3.incident.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Integer> {
}
