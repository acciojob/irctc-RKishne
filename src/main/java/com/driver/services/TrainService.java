package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import io.swagger.models.auth.In;
import jdk.internal.icu.impl.Trie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.*;
import static java.time.LocalTime.*;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        String trainRoute="";

        List<Station> stationList=trainEntryDto.getStationRoute();

        for(Station station:stationList){
            trainRoute+=station;
            if(stationList.size()>0){
                trainRoute+=",";
            }
        }

        Train train=new Train();
        train.setRoute(trainRoute);
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        train=trainRepository.save(train);

        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        Train train=trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        Integer totalNoOfSeats=train.getNoOfSeats();

        List<Ticket> ticketList=train.getBookedTickets();

        int count=0;
        for(Ticket ticket:ticketList) {
            if (ticket.getFromStation().equals(seatAvailabilityEntryDto.getFromStation()) && ticket.getToStation().equals(seatAvailabilityEntryDto.getToStation())) {
                count++;
            }
        }
        int availableSeats=totalNoOfSeats-count;

        return availableSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train=trainRepository.findById(trainId).get();
        String trainRoute=train.getRoute();
        String []stations=trainRoute.split(",");

        for(int i=0;i<stations.length;i++){
            String station1=stations[i];
            if(!station1.equals(station)){
                throw new Exception("Train is not passing from this station");
            }
        }
        int count=0;
        List<Ticket> ticketList=train.getBookedTickets();
        for(Ticket ticket:ticketList){
            if(ticket.getToStation().equals(station)){
                count+=ticket.getPassengersList().size();
            }
        }

        return count;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Train train=trainRepository.findById(trainId).get();

        String []stations=train.getRoute().split(",");
        String fromStation=stations[0];
        String toStation=stations[stations.length-1];

        List<Ticket> ticketList=train.getBookedTickets();
        int oldestPassenger= 0;
        for(Ticket ticket:ticketList){
            if(ticket.getFromStation().equals(fromStation) && ticket.getToStation().equals(toStation)){
                List<Passenger> passengerList=ticket.getPassengersList();
                for(Passenger passenger:passengerList){
                    if(passenger.getAge()>oldestPassenger){
                        oldestPassenger=passenger.getAge();
                    }
                }
            }
        }
        return oldestPassenger;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Train> trainList=trainRepository.findAll();
        List<Integer> ans=new ArrayList<>();
        int trains=0;
        for(Train train:trainList){
            String []stations=train.getRoute().split(",");
            for(String station1:stations){
                if(station1.equals(station)){
                    LocalTime strTime=train.getDepartureTime();
                    LocalTime edTime=train.getDepartureTime().plusHours(1);
                    if((strTime.isAfter(startTime) || strTime.equals(startTime)) && (edTime.isBefore(endTime) || edTime.equals(endTime))){
                        ans.add(trains++);
                    }
                }
            }
        }
        return ans;
    }

}
