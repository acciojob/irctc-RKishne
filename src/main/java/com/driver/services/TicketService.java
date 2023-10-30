package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db
        Optional<Passenger> optionalPassenger=passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());

        if (!optionalPassenger.isPresent()) {
            throw new Exception("Booking Person is not found in system. Please first sing up from the app.");
        }
        Passenger passenger=optionalPassenger.get();
        List<Integer> list=bookTicketEntryDto.getPassengerIds();

        List<Passenger> passengerList=new ArrayList<>();

        for(Integer id:list){
            Passenger passenger1=passengerRepository.findById(id).get();
            passengerList.add(passenger1);
        }

        Train train=trainRepository.findById(bookTicketEntryDto.getTrainId()).get();

        int totalNoOfSeats=train.getNoOfSeats();
        List<Ticket> ticketList=train.getBookedTickets();

        int AvailableSeats=totalNoOfSeats-ticketList.size();

        if(AvailableSeats-bookTicketEntryDto.getNoOfSeats()<0){
            throw new Exception("Less tickets are available");
        }
        String []stations=train.getRoute().split(",");
        int cnt=0;

        for(String station:stations){
            if(station.equals(bookTicketEntryDto.getFromStation()) || station.equals(bookTicketEntryDto.getToStation())){
                cnt++;
            }
        }
        int fare=300;
        int totalFare=(cnt-1)*fare;

        Ticket ticket=new Ticket();
        ticket.setPassengersList(passengerList);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(totalFare);
        ticket.setTrain(train);

        trainRepository.save(train);
        ticket=ticketRepository.save(ticket);

       return ticket.getTicketId();

    }
}
