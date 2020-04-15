package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;

import java.util.List;

public interface ShipService {

    Ship addShip(Ship ship);

    void deleteShip(String id);

    Ship updateShip(Ship ship, String id);

    Ship getShip(String id);

    Integer getShipsCount(String name, String planet, ShipType shipType, Long after, Long before,
                          Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize,
                          Integer maxCrewSize, Double minRating, Double maxRating);

    List<Ship> getShipsList(String name, String planet, ShipType shipType, Long after, Long before,
                            Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize,
                            Integer maxCrewSize, Double minRating, Double maxRating,
                            ShipOrder order, Integer pageNumber, Integer pageSize);
}
