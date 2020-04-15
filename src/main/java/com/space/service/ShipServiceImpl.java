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
import java.sql.Date;
import java.util.*;

@Service
@Transactional
public class ShipServiceImpl implements ShipService{

    private ShipRepository shipRepository;

    @Autowired
    public void setShipRepository(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    public Ship addShip(Ship ship) {
        if (ship.getUsed() == null) {
            ship.setUsed(false);
        }
        checkNullParams(ship);
        checkShipParams(ship);
        double rating = getShipRating(ship);
        ship.setRating(rating);
        double roundedShipSpeed = Double.parseDouble(String.format("%.2f", ship.getSpeed()).replaceAll(",", "."));
        ship.setSpeed(roundedShipSpeed);
        return shipRepository.saveAndFlush(ship);
    }

    public void deleteShip(String id) {
        Long shipId = checkIdForValidAndParseIt(id);
        if (!shipRepository.existsById(shipId)) {
            throw new ShipNotFoundException();
        }
        shipRepository.deleteById(shipId);
    }

    public Ship updateShip(Ship ship, String id) {
        Long shipId = checkIdForValidAndParseIt(id);
        if (!shipRepository.existsById(shipId)) {
            throw new ShipNotFoundException();
        }
        checkShipParams(ship);
        Ship needToBeUpdateShip = shipRepository.findById(shipId).get();
        if (ship.getName() != null) {
            needToBeUpdateShip.setName(ship.getName());
        }
        if (ship.getPlanet() != null) {
            needToBeUpdateShip.setPlanet(ship.getPlanet());
        }
        if (ship.getShipType() != null) {
            needToBeUpdateShip.setShipType(ship.getShipType());
        }
        if (ship.getProdDate() != null) {
            needToBeUpdateShip.setProdDate(ship.getProdDate());
        }
        if (ship.getUsed() != null) {
            needToBeUpdateShip.setUsed(ship.getUsed());
        }
        if (ship.getSpeed() != null) {
            needToBeUpdateShip.setSpeed(ship.getSpeed());
        }
        if (ship.getCrewSize() != null) {
            needToBeUpdateShip.setCrewSize(ship.getCrewSize());
        }
        needToBeUpdateShip.setRating(getShipRating(needToBeUpdateShip));
        return shipRepository.saveAndFlush(needToBeUpdateShip);
    }

    public Ship getShip(String id) {
        Long shipId = checkIdForValidAndParseIt(id);
        if (!shipRepository.existsById(shipId)) {
            throw new ShipNotFoundException();
        }
        return shipRepository.findById(shipId).get();
    }

    public Integer getShipsCount(String name, String planet, ShipType shipType, Long after, Long before,
                                 Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize,
                                 Integer maxCrewSize, Double minRating, Double maxRating) {
        return getFilteredShipsList(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating).size();
    }


    public List<Ship> getShipsList(String name, String planet, ShipType shipType, Long after, Long before,
                                   Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize,
                                   Integer maxCrewSize, Double minRating, Double maxRating,
                                   ShipOrder order, Integer pageNumber, Integer pageSize) {

        List<Ship> filteredShipsList = getFilteredShipsList(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);
        switch (order) {
            case ID:
                Collections.sort(filteredShipsList, (ship1, ship2) -> {
                    if (ship1.getId() > ship2.getId()) return 1;
                    else if (ship1.getId() < ship2.getId()) return -1;
                    return 0;
                });
                break;
            case SPEED:
                Collections.sort(filteredShipsList, (ship1, ship2) -> {
                    if (ship1.getSpeed() > ship2.getSpeed()) return 1;
                    else if (ship1.getSpeed() < ship2.getSpeed()) return -1;
                    return 0;
                });
                break;
            case RATING:
                Collections.sort(filteredShipsList, (ship1, ship2) -> {
                    if (ship1.getRating() > ship2.getRating()) return 1;
                    else if (ship1.getRating() < ship2.getRating()) return -1;
                    return 0;
                });
                break;
            case DATE:
                Collections.sort(filteredShipsList, (ship1, ship2) -> {
                    if (ship1.getProdDate().getTime() > ship2.getProdDate().getTime()) return 1;
                    else if (ship1.getProdDate().getTime() < ship2.getProdDate().getTime()) return -1;
                    return 0;
                });
                break;
        }
        List<Ship> resultShipsList = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            try {
                resultShipsList.add(filteredShipsList.get(i + pageNumber * pageSize));
            } catch (IndexOutOfBoundsException e) {
                return resultShipsList;
            }
        }
        return resultShipsList;
    }

    private List<Ship> getFilteredShipsList(String name, String planet, ShipType shipType, Long after,
                                            Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                                            Integer minCrewSize, Integer maxCrewSize, Double minRating,
                                            Double maxRating) {
        List<Ship> filteredShipsList = shipRepository.findAll();
        for (int i = 0; i < filteredShipsList.size(); ) {
            boolean isOkForRequest = true;

            long shipProdDateInMillis = filteredShipsList.get(i).getProdDate().getTime();
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(filteredShipsList.get(i).getProdDate());
//            int shipProdYear = calendar.get(Calendar.YEAR);
//            int beforeYearRequest = -1;
//            int afterYearRequest = -1;
//            if (before != null) {
//                calendar.setTime(new Date(before));
//                beforeYearRequest = calendar.get(Calendar.YEAR);
//            }
//            if (after != null) {
//                calendar.setTime(new Date(after));
//                afterYearRequest = calendar.get(Calendar.YEAR);
//            }

            if (name != null && !filteredShipsList.get(i).getName().contains(name)) isOkForRequest = false;
            if (planet != null && !filteredShipsList.get(i).getPlanet().contains(planet)) isOkForRequest = false;
            if (shipType != null && filteredShipsList.get(i).getShipType() != shipType) isOkForRequest = false;
//            if (after != null && shipProdYear < afterYearRequest) isOkForRequest = false;
//            if (before != null && shipProdYear > beforeYearRequest) isOkForRequest = false;
            if (after != null && shipProdDateInMillis < after) isOkForRequest = false;
            if (before != null && shipProdDateInMillis > before) isOkForRequest = false;
            if (isUsed != null && !filteredShipsList.get(i).getUsed().equals(isUsed)) isOkForRequest = false;
            if (minSpeed != null && filteredShipsList.get(i).getSpeed() < minSpeed) isOkForRequest = false;
            if (maxSpeed != null && filteredShipsList.get(i).getSpeed() > maxSpeed) isOkForRequest = false;
            if (minCrewSize != null && filteredShipsList.get(i).getCrewSize() < minCrewSize) isOkForRequest = false;
            if (maxCrewSize != null && filteredShipsList.get(i).getCrewSize() > maxCrewSize) isOkForRequest = false;
            if (minRating != null && filteredShipsList.get(i).getRating() < minRating) isOkForRequest = false;
            if (maxRating != null && filteredShipsList.get(i).getRating() > maxRating) isOkForRequest = false;

            if (isOkForRequest) {
                i++;
            }
            else {
                filteredShipsList.remove(i);
            }
        }
        return filteredShipsList;
    }

    private double getShipRating(Ship ship) {
        double usedCoefficient = (ship.getUsed()) ? 0.5 : 1;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ship.getProdDate());
        int prodYear = calendar.get(Calendar.YEAR);
        double rating = (80 * ship.getSpeed() * usedCoefficient) / (3019 - prodYear +1);
        rating = Double.parseDouble(String.format("%.2f", rating).replaceAll(",", "."));
        return rating;
    }

    private Long checkIdForValidAndParseIt(String id) {
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
