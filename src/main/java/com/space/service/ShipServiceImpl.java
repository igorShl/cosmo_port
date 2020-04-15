package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import com.space.exceptions.BadParamsException;
import com.space.exceptions.ShipNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Service
@Transactional
public class ShipServiceImpl implements ShipService{

    @Autowired
    private ShipRepository shipRepository;

    public Ship addShip(Ship ship) {
        if (ship.getUsed() == null) {
            ship.setUsed(false);
        }
        checkNullParams(ship);
        checkShipParams(ship);
        double rating = getShipRating(ship);
        ship.setRating(rating);
        ship.setSpeed((double) Math.round(ship.getSpeed()*100) / 100);
        return shipRepository.saveAndFlush(ship);
    }

    public void deleteShip(String id) {
        checkIdForValid(id);
        if (!shipRepository.existsById(Long.parseLong(id))) {
            throw new ShipNotFoundException();
        }
        shipRepository.deleteById(Long.parseLong(id));
    }

    public Ship updateShip(Ship ship, String id) {
        Long shipId = checkIdForValid(id);
        if (!shipRepository.existsById(shipId)) {
            throw new ShipNotFoundException();
        }
        checkShipParams(ship);
        Ship updatedShip = shipRepository.getOne(shipId);
        if (ship.getName() != null) {
            updatedShip.setName(ship.getName());
        }
        if (ship.getPlanet() != null) {
            updatedShip.setPlanet(ship.getPlanet());
        }
        if (ship.getShipType() != null) {
            updatedShip.setShipType(ship.getShipType());
        }
        if (ship.getProdDate() != null) {
            updatedShip.setProdDate(ship.getProdDate());
        }
        if (ship.getUsed() != null) {
            updatedShip.setUsed(ship.getUsed());
        }
        if (ship.getSpeed() != null) {
            updatedShip.setSpeed(ship.getSpeed());
        }
        if (ship.getCrewSize() != null) {
            updatedShip.setCrewSize(ship.getCrewSize());
        }
        updatedShip.setRating(getShipRating(updatedShip));
        return shipRepository.saveAndFlush(updatedShip);
    }

    public Ship getShip(String id) {
        Long shipId = checkIdForValid(id);
        if (!shipRepository.existsById(shipId)) {
            throw new ShipNotFoundException();
        }
        return shipRepository.findById(shipId).get();
    }

    public Integer getShipsCount(String name, String planet, ShipType shipType, Long after, Long before,
                                 Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize,
                                 Integer maxCrewSize, Double minRating, Double maxRating) {
        return getFilterShipsList(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating).size();
    }


    public List<Ship> getShipsList(String name, String planet, ShipType shipType, Long after, Long before,
                                   Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize,
                                   Integer maxCrewSize, Double minRating, Double maxRating,
                                   ShipOrder order, Integer pageNumber, Integer pageSize) {

        List<Ship> filterShipsList = getFilterShipsList(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);
        switch (order) {
            case ID:
                Collections.sort(filterShipsList, (ship1, ship2) -> {
                    if (ship1.getId() > ship2.getId()) return 1;
                    else if (ship1.getId() < ship2.getId()) return -1;
                    return 0;
                });
                break;
            case SPEED:
                Collections.sort(filterShipsList, (ship1, ship2) -> {
                    if (ship1.getSpeed() > ship2.getSpeed()) return 1;
                    else if (ship1.getSpeed() < ship2.getSpeed()) return -1;
                    return 0;
                });
                break;
            case RATING:
                Collections.sort(filterShipsList, (ship1, ship2) -> {
                    if (ship1.getRating() > ship2.getRating()) return 1;
                    else if (ship1.getRating() < ship2.getRating()) return -1;
                    return 0;
                });
                break;
            case DATE:
                Collections.sort(filterShipsList, (ship1, ship2) -> {
                    if (ship1.getProdDate().getTime() > ship2.getProdDate().getTime()) return 1;
                    else if (ship1.getProdDate().getTime() < ship2.getProdDate().getTime()) return -1;
                    return 0;
                });
                break;
        }
        List<Ship> resultShipsList = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            try {
                resultShipsList.add(filterShipsList.get(i + pageNumber * pageSize));
            } catch (IndexOutOfBoundsException e) {
                return resultShipsList;
            }
        }
        return resultShipsList;
    }

    private List<Ship> getFilterShipsList(String name, String planet, ShipType shipType, Long after,
                                          Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                                          Integer minCrewSize, Integer maxCrewSize, Double minRating,
                                          Double maxRating) {
        List<Ship> filtShipsList = shipRepository.findAll();
        for (int i = 0; i < filtShipsList.size(); ) {
            boolean isOkForRequest = true;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(filtShipsList.get(i).getProdDate());
            int shipYear = calendar.get(Calendar.YEAR);
            int beforeYear = -1;
            int afterYear = -1;
            if (before != null) {
                calendar.setTime(new Date(before));
                beforeYear = calendar.get(Calendar.YEAR);
            }
            if (after != null) {
                calendar.setTime(new Date(after));
                afterYear = calendar.get(Calendar.YEAR);
            }

            if (name != null && !filtShipsList.get(i).getName().contains(name)) isOkForRequest = false;
            if (planet != null && !filtShipsList.get(i).getPlanet().contains(planet)) isOkForRequest = false;
            if (shipType != null && filtShipsList.get(i).getShipType() != shipType) isOkForRequest = false;
            if (after != null && shipYear < afterYear) isOkForRequest = false;
            if (before != null && shipYear > beforeYear) isOkForRequest = false;
            if (isUsed != null && !filtShipsList.get(i).getUsed().equals(isUsed)) isOkForRequest = false;
            if (minSpeed != null && filtShipsList.get(i).getSpeed() < minSpeed) isOkForRequest = false;
            if (maxSpeed != null && filtShipsList.get(i).getSpeed() > maxSpeed) isOkForRequest = false;
            if (minCrewSize != null && filtShipsList.get(i).getCrewSize() < minCrewSize) isOkForRequest = false;
            if (maxCrewSize != null && filtShipsList.get(i).getCrewSize() > maxCrewSize) isOkForRequest = false;
            if (minRating != null && filtShipsList.get(i).getRating() < minRating) isOkForRequest = false;
            if (maxRating != null && filtShipsList.get(i).getRating() > maxRating) isOkForRequest = false;

            if (isOkForRequest) {
                i++;
            }
            else {
                filtShipsList.remove(i);
            }
        }
        return filtShipsList;
    }


    private double getShipRating(Ship ship) {
        double k = (ship.getUsed()) ? 0.5 : 1;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ship.getProdDate());
        int prodYear = calendar.get(Calendar.YEAR);
        double rating = (80 * ship.getSpeed() * k) / (3019 - prodYear +1);
        rating = (double) Math.round(rating * 100) / 100;
        return rating;
    }

    private Long checkIdForValid(String id) {
        Long shipId = null;
        try {
            shipId = Long.parseLong(id);
            if (shipId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new BadParamsException();
        }
        return shipId;
    }

    private void checkNullParams(Ship ship) {
        if (ship.getName() == null || ship.getPlanet() == null || ship.getShipType() == null
                || ship.getProdDate() == null || ship.getSpeed() == null || ship.getCrewSize() == null) {
            throw new BadParamsException();
        }
    }

    private void checkShipParams(Ship ship) {
        boolean paramsIsOk = true;
        if (ship.getName() != null && (ship.getName().length() > 50 || ship.getName().isEmpty())) paramsIsOk = false;
        if (ship.getPlanet() != null && (ship.getPlanet().length() > 50 || ship.getPlanet().isEmpty())) paramsIsOk = false;
        if (ship.getSpeed() != null && (ship.getSpeed() < 0.01 || ship.getSpeed() > 0.99)) paramsIsOk = false;
        if (ship.getCrewSize() != null && (ship.getCrewSize() < 1 || ship.getCrewSize() > 9999)) paramsIsOk = false;
        if (ship.getProdDate() != null && ship.getProdDate().getTime() < 0) paramsIsOk = false;
        if (ship.getProdDate() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(ship.getProdDate());
            int prodYear = calendar.get(Calendar.YEAR);
            if (prodYear < 2800 || prodYear > 3019) paramsIsOk = false;
        }
        if (!paramsIsOk) throw new BadParamsException();
    }
}
