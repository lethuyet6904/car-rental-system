package com.carrental.repository;

import com.carrental.entity.Car;
import com.carrental.entity.CarSchedule;
import com.carrental.enums.CarStatus;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class CarSpecificationRepository {

    public static Specification<Car> hasStatus(CarStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Car> hasCity(String city) {
        return (root, query, cb) -> {
            if (city == null || city.trim().isEmpty())
                return cb.conjunction();
            return cb.like(
                    cb.lower(root.get("region").get("regionName")),
                    "%" + city.toLowerCase() + "%");
        };
    }

    public static Specification<Car> hasBrand(Long brandId) {
        return (root, query, cb) -> brandId == null ? cb.conjunction()
                : cb.equal(root.get("brand").get("brandId"), brandId);
    }

    public static Specification<Car> hasCarType(Long carTypeId) {
        return (root, query, cb) -> carTypeId == null ? cb.conjunction()
                : cb.equal(root.get("carType").get("carTypeId"), carTypeId);
    }

    public static Specification<Car> hasFuel(String fuelStr) {
        return (root, query, cb) -> {
            if (fuelStr == null || fuelStr.trim().isEmpty())
                return cb.conjunction();
            try {
                com.carrental.enums.FuelType fuel = com.carrental.enums.FuelType.valueOf(fuelStr);
                return cb.equal(root.get("fuel"), fuel);
            } catch (IllegalArgumentException e) {
                return cb.conjunction();
            }
        };
    }

    public static Specification<Car> hasTransmission(String transmissionStr) {
        return (root, query, cb) -> {
            if (transmissionStr == null || transmissionStr.trim().isEmpty())
                return cb.conjunction();
            try {
                com.carrental.enums.TransmissionType transmission = com.carrental.enums.TransmissionType
                        .valueOf(transmissionStr);
                return cb.equal(root.get("transmission"), transmission);
            } catch (IllegalArgumentException e) {
                return cb.conjunction();
            }
        };
    }

    // ⚠ Logic overlap đúng: loại xe nếu lịch bận giao nhau bất kỳ phần nào
    public static Specification<Car> hasSeats(Integer seats) {
        return (root, query, cb) -> seats == null ? cb.conjunction()
                : cb.equal(root.get("seats"), seats);
    }

    public static Specification<Car> availableBetween(LocalDate dateFrom, LocalDate dateTo) {
        return (root, query, cb) -> {
            if (dateFrom == null || dateTo == null)
                return cb.conjunction();
            Subquery<Long> sub = query.subquery(Long.class);
            Root<CarSchedule> sch = sub.from(CarSchedule.class);
            sub.select(cb.literal(1L))
                    .where(
                            cb.equal(sch.get("car"), root),
                            cb.lessThan(sch.get("startDate"), dateTo),
                            cb.greaterThan(sch.get("endDate"), dateFrom));
            return cb.not(cb.exists(sub));
        };
    }
}
